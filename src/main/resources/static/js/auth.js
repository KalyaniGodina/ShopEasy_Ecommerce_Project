// Shared script for login.html and signup.html
const params = new URLSearchParams(location.search);

// Only allow redirects to pages on this site (prevents open-redirect tricks)
function nextUrl() {
  const n = params.get("next");
  return n && n.startsWith("/") && !n.startsWith("//") ? n : "/";
}

const errorBox = document.getElementById("authError");
const showError = (m) => { errorBox.textContent = m; };

// keep ?next=... when switching between sign in / sign up
const other = document.getElementById("otherLink");
if (other && params.get("next")) other.href += "?next=" + encodeURIComponent(nextUrl());

// show / hide password
document.querySelectorAll("[data-toggle]").forEach((btn) => {
  btn.addEventListener("click", () => {
    const input = btn.parentElement.querySelector("input");
    const show = input.type === "password";
    input.type = show ? "text" : "password";
    btn.textContent = show ? "Hide" : "Show";
  });
});

async function send(url, body) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  let data = {};
  try { data = await res.json(); } catch {}
  if (!res.ok) throw new Error(data.message || "Something went wrong. Please try again.");
  return data;
}

function busy(form, on, label) {
  const b = form.querySelector("button[type=submit]");
  b.disabled = on;
  b.textContent = on ? "Please wait..." : label;
}

// already signed in? skip the form
fetch("/api/auth/me").then((r) => r.json()).then((u) => {
  if (u.authenticated) location.replace(nextUrl());
}).catch(() => {});

const loginForm = document.getElementById("loginForm");
if (loginForm) {
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    showError("");
    const f = new FormData(loginForm);
    const email = f.get("email").trim();
    const password = f.get("password");
    if (!email || !password) return showError("Please enter your email and password.");
    busy(loginForm, true);
    try {
      await send("/api/auth/login", { email, password });
      location.href = nextUrl();
    } catch (err) {
      showError(err.message);
      busy(loginForm, false, "Sign in");
    }
  });
}

const signupForm = document.getElementById("signupForm");
if (signupForm) {
  signupForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    showError("");
    const f = new FormData(signupForm);
    const name = f.get("name").trim();
    const email = f.get("email").trim();
    const password = f.get("password");
    if (!name || !email || !password) return showError("Please fill in all fields.");
    if (password.length < 8) return showError("Password must be at least 8 characters.");
    if (password !== f.get("confirm")) return showError("Passwords do not match.");
    busy(signupForm, true);
    try {
      await send("/api/auth/signup", { name, email, password });
      location.href = nextUrl();
    } catch (err) {
      showError(err.message);
      busy(signupForm, false, "Create account");
    }
  });
}
