const revealNodes = document.querySelectorAll(".reveal");
const counters = document.querySelectorAll("[data-count]");
const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

const formatValue = (finalValue, currentValue) => {
  if (finalValue >= 1000 && finalValue % 100 !== 0) {
    return `$${(currentValue / 1000).toFixed(1)}K`;
  }

  if (finalValue >= 1000) {
    return currentValue.toLocaleString();
  }

  return String(currentValue);
};

if (!("IntersectionObserver" in window) || prefersReducedMotion) {
  revealNodes.forEach((node) => node.classList.add("is-visible"));
  counters.forEach((counter) => {
    const finalValue = Number(counter.dataset.count);
    counter.textContent = formatValue(finalValue, finalValue);
  });
} else {
  const revealObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("is-visible");
          revealObserver.unobserve(entry.target);
        }
      });
    },
    {
      threshold: 0.18,
    }
  );

  revealNodes.forEach((node) => revealObserver.observe(node));

  const counterObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) {
          return;
        }

        const element = entry.target;
        const finalValue = Number(element.dataset.count);
        const duration = 1400;
        const start = performance.now();

        const step = (now) => {
          const progress = Math.min((now - start) / duration, 1);
          const eased = 1 - Math.pow(1 - progress, 3);
          const current = Math.round(finalValue * eased);
          element.textContent = formatValue(finalValue, current);

          if (progress < 1) {
            requestAnimationFrame(step);
          }
        };

        requestAnimationFrame(step);
        counterObserver.unobserve(element);
      });
    },
    { threshold: 0.4 }
  );

  counters.forEach((counter) => counterObserver.observe(counter));
}

const clinicRegistrationForm = document.getElementById("clinic-registration-form");
const clinicRegistrationStatus = document.getElementById("clinic-registration-status");
const demoRequestForm = document.getElementById("demo-request-form");
const demoRequestStatus = document.getElementById("demo-request-status");

const setFormBusyState = (form, label) => {
  const submitButton = form.querySelector("button[type='submit']");
  if (submitButton) {
    submitButton.disabled = true;
    submitButton.textContent = label;
  }
};

const clearFormBusyState = (form, label) => {
  const submitButton = form.querySelector("button[type='submit']");
  if (submitButton) {
    submitButton.disabled = false;
    submitButton.textContent = label;
  }
};

const setStatus = (node, message, isError = false) => {
  if (!node) {
    return;
  }
  node.classList.toggle("error", isError);
  node.textContent = message;
};

const postForm = async (endpoint, formData) => {
  const body = new URLSearchParams();
  formData.forEach((value, key) => body.append(key, value));

  const response = await fetch(endpoint, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
      "X-Requested-With": "XMLHttpRequest",
    },
    body: body.toString(),
  });

  const contentType = response.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) {
    throw new Error("Unexpected response");
  }

  const payload = await response.json();
  if (!response.ok || !payload || payload.status !== "ok") {
    throw new Error(payload && payload.message ? payload.message : "Unable to submit now.");
  }

  return payload;
};

const registerClinicDirectly = async (form) => {
  const endpoint = form.getAttribute("data-endpoint");
  if (!endpoint) {
    throw new Error("Registration endpoint is missing.");
  }

  const payload = await postForm(endpoint, new FormData(form));
  form.reset();
  return payload.message || "Clinic registration completed.";
};

const registerClinicAfterRazorpay = async (form) => {
  const formData = new FormData(form);
  const orderEndpoint = form.getAttribute("data-razorpay-order-endpoint");
  const verifyEndpoint = form.getAttribute("data-razorpay-verify-endpoint");
  if (!orderEndpoint || !verifyEndpoint) {
    throw new Error("Razorpay endpoints are not configured.");
  }

  const orderPayload = await postForm(orderEndpoint, formData);

  if (typeof window.Razorpay !== "function") {
    throw new Error("Razorpay checkout is not available.");
  }

  return await new Promise((resolve, reject) => {
    const razorpay = new window.Razorpay({
      key: orderPayload.keyId,
      amount: Number(orderPayload.amount),
      currency: orderPayload.currency,
      name: orderPayload.businessName,
      description: orderPayload.description,
      order_id: orderPayload.orderId,
      prefill: {
        email: formData.get("adminEmail") || "",
        contact: formData.get("adminPhone") || "",
      },
      theme: {
        color: "#0b4ca6",
      },
      handler: async (response) => {
        try {
          formData.append("razorpay_payment_id", response.razorpay_payment_id);
          formData.append("razorpay_order_id", response.razorpay_order_id);
          formData.append("razorpay_signature", response.razorpay_signature);
          formData.append("paymentAmount", orderPayload.amount);

          const verifyPayload = await postForm(verifyEndpoint, formData);
          form.reset();
          resolve(
            verifyPayload.message ||
              "Payment verified and clinic registration completed."
          );
        } catch (error) {
          reject(error);
        }
      },
      modal: {
        ondismiss: () => reject(new Error("Razorpay checkout was cancelled.")),
      },
    });

    razorpay.on("payment.failed", (event) => {
      const reason =
        event &&
        event.error &&
        (event.error.description || event.error.reason || event.error.code);
      reject(new Error(reason || "Razorpay payment failed."));
    });

    razorpay.open();
  });
};

if (clinicRegistrationForm) {
  clinicRegistrationForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    setStatus(clinicRegistrationStatus, "");
    setFormBusyState(clinicRegistrationForm, "Processing...");

    try {
      const gateway = (
        clinicRegistrationForm.querySelector("[name='paymentGatewayCode']")?.value || "manual"
      ).toLowerCase();

      const message =
        gateway === "razorpay"
          ? await registerClinicAfterRazorpay(clinicRegistrationForm)
          : await registerClinicDirectly(clinicRegistrationForm);

      setStatus(clinicRegistrationStatus, message, false);
    } catch (error) {
      setStatus(
        clinicRegistrationStatus,
        error.message || "Unable to complete registration.",
        true
      );
    } finally {
      clearFormBusyState(clinicRegistrationForm, "Register Clinic");
    }
  });
}

if (demoRequestForm) {
  demoRequestForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    setStatus(demoRequestStatus, "");
    setFormBusyState(demoRequestForm, "Submitting...");

    try {
      const endpoint = demoRequestForm.getAttribute("data-endpoint");
      if (!endpoint) {
        throw new Error("Demo request endpoint is missing.");
      }

      const payload = await postForm(endpoint, new FormData(demoRequestForm));
      demoRequestForm.reset();
      setStatus(
        demoRequestStatus,
        payload.message || "Request received. Our team will contact you soon.",
        false
      );
    } catch (error) {
      setStatus(
        demoRequestStatus,
        error.message || "Unable to submit now. Please try again shortly.",
        true
      );
    } finally {
      clearFormBusyState(demoRequestForm, "Request Demo");
    }
  });
}
