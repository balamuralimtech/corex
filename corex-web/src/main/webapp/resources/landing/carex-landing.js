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
