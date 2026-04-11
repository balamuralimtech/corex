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

const demoRequestForm = document.getElementById("demo-request-form");
const demoRequestStatus = document.getElementById("demo-request-status");

if (demoRequestForm) {
  demoRequestForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const endpoint = demoRequestForm.getAttribute("data-endpoint");
    if (!endpoint) {
      return;
    }

    const submitButton = demoRequestForm.querySelector("button[type='submit']");
    if (submitButton) {
      submitButton.disabled = true;
      submitButton.textContent = "Submitting...";
    }
    if (demoRequestStatus) {
      demoRequestStatus.classList.remove("error");
      demoRequestStatus.textContent = "";
    }

    try {
      const formData = new FormData(demoRequestForm);
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

      if (!response.ok) {
        throw new Error("Request failed");
      }

      const contentType = response.headers.get("content-type") || "";
      if (!contentType.includes("application/json")) {
        throw new Error("Unexpected response");
      }

      const payload = await response.json();
      if (!payload || payload.status !== "ok") {
        throw new Error("Submission not accepted");
      }

      demoRequestForm.reset();
      if (demoRequestStatus) {
        demoRequestStatus.textContent = "Request received. Our team will contact you soon.";
      }
    } catch (error) {
      if (demoRequestStatus) {
        demoRequestStatus.classList.add("error");
        demoRequestStatus.textContent =
          "Unable to submit now. Please try again shortly.";
      }
    } finally {
      if (submitButton) {
        submitButton.disabled = false;
        submitButton.textContent = "Request Demo";
      }
    }
  });
}

const landingParams = new URLSearchParams(window.location.search);
if (demoRequestStatus) {
  if (landingParams.get("demo") === "success") {
    demoRequestStatus.classList.remove("error");
    demoRequestStatus.textContent = "Our business team will contact you soon.";
  } else if (landingParams.get("demo") === "error") {
    demoRequestStatus.classList.add("error");
    demoRequestStatus.textContent = "Unable to submit now. Please try again shortly.";
  }
}
