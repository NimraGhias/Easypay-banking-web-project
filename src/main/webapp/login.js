// TEMPORARY: frontend-only login. Will be replaced by fetch("/api/auth/login") later.
const loginForm = document.querySelector("#loginForm");
const authMessage = document.querySelector("#authMessage");

loginForm.addEventListener("submit", (event) => {
  event.preventDefault();

  const email = loginForm.email.value.trim();
  const password = loginForm.password.value;

  if (!email || !password) {
    authMessage.textContent = "Please enter email and password.";
    return;
  }

  window.location.href = "index.html";
});