/* ---------- HELPERS ---------- */
async function postJson(url, data) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });
  let body = {};
  try {
    body = await response.json();
  } catch (e) {
    // Jawab JSON nahi tha
  }
  return { ok: response.ok, body: body };
}

function showMessage(text, isError) {
  const box = document.querySelector("#authMessage");
  if (!box) return;
  box.textContent = text;
  box.style.color = isError ? "#c0392b" : "#1e8e3e";
}

function showPopup(title, text, buttonText, onClose) {
  const overlay = document.createElement("div");
  overlay.className = "popup-overlay";

  const box = document.createElement("div");
  box.className = "popup-box";
  box.setAttribute("role", "dialog");
  box.setAttribute("aria-modal", "true");

  const icon = document.createElement("div");
  icon.className = "popup-icon";
  icon.textContent = "✓";

  const heading = document.createElement("h2");
  heading.textContent = title;

  const message = document.createElement("p");
  message.textContent = text;

  const button = document.createElement("button");
  button.type = "button";
  button.textContent = buttonText;
  button.addEventListener("click", () => {
    overlay.remove();
    onClose();
  });

  box.append(icon, heading, message, button);
  overlay.append(box);
  document.body.append(overlay);
  button.focus();
}

// PIN mein sirf digits likhne do
function digitsOnly(input) {
  input.addEventListener("input", () => {
    input.value = input.value.replace(/\D/g, "");
  });
}

/* ---------- PIN RULES (server jaisi hi) ---------- */
function isWeakPin(pin) {
  const allSame = new Set(pin).size === 1;
  const ascending = "01234567890".includes(pin);
  const descending = "09876543210".includes(pin);
  return allSame || ascending || descending;
}

function evaluatePin(pin) {
  const rules = {
    digits: pin.length > 0 && /^\d+$/.test(pin),
    length: /^\d{5}$/.test(pin),
    strong: /^\d{5}$/.test(pin) && !isWeakPin(pin)
  };
  return {
    rules: rules,
    allMet: rules.digits && rules.length && rules.strong
  };
}

/* ---------- LOGIN ---------- */
const loginForm = document.querySelector("#loginForm");
if (loginForm) {
  digitsOnly(loginForm.pin);

  loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const button = loginForm.querySelector("button[type=submit]");
    button.disabled = true;
    showMessage("Logging in...", false);

    try {
      const result = await postJson("api/auth/login", {
        identifier: loginForm.identifier.value.trim(),
        pin: loginForm.pin.value
      });

      if (result.ok) {
        window.location.href = "index.html";
      } else {
        showMessage(result.body.message || "Login failed.", true);
        loginForm.pin.value = ""; // sirf PIN saaf, email/mobile rehta hai
        loginForm.pin.focus();
      }
    } catch (e) {
      showMessage("Cannot reach the server. Is Tomcat running?", true);
    } finally {
      button.disabled = false;
    }
  });
}

/* ---------- REGISTER ---------- */
const registerForm = document.querySelector("#registerForm");
if (registerForm) {
  const pinInput = registerForm.querySelector("#pin");
  const confirmInput = registerForm.querySelector("#confirmPin");

  digitsOnly(pinInput);
  digitsOnly(confirmInput);

  // Type karte waqt rules ✓ / ✗
  pinInput.addEventListener("input", () => {
    const result = evaluatePin(pinInput.value);
    document.querySelectorAll("#pinRules li").forEach((item) => {
      item.classList.toggle("met", result.rules[item.dataset.rule]);
    });
  });

  registerForm.addEventListener("submit", async (event) => {
    event.preventDefault(); // sab se pehle: page reload nahi hoga

    const pin = pinInput.value;

    // Ghalti par sirf error dikhate hain, fields khali nahi karte
    if (!evaluatePin(pin).allMet) {
      showMessage("PIN must be 5 digits and not an easy PIN.", true);
      pinInput.focus();
      return;
    }
    if (pin !== confirmInput.value) {
      showMessage("PINs do not match.", true);
      confirmInput.focus();
      return;
    }

    const button = registerForm.querySelector("button[type=submit]");
    button.disabled = true;
    showMessage("Creating account...", false);

    try {
      const result = await postJson("api/auth/register", {
        fullName: registerForm.fullName.value.trim(),
        email: registerForm.email.value.trim(),
        phone: registerForm.phone.value.trim(),
        pin: pin
      });

      if (result.ok) {
        showMessage("", false);
        registerForm.reset(); // sirf success par
        document.querySelectorAll("#pinRules li").forEach((item) => {
          item.classList.remove("met");
        });
        showPopup(
          "Account created!",
          "Your Easypay account is ready. Please log in.",
          "Go to login",
          () => { window.location.href = "login.html"; }
        );
      } else {
        showMessage(result.body.message || "Registration failed.", true);
      }
    } catch (e) {
      showMessage("Cannot reach the server. Is Tomcat running?", true);
    } finally {
      button.disabled = false;
    }
  });
}