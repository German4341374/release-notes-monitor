document.querySelectorAll(".delete-form").forEach((form) => {
  form.addEventListener("submit", (event) => {
    if (!window.confirm("Delete this product and its check history?")) {
      event.preventDefault();
    }
  });
});
