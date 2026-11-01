const $ = (s) => document.querySelector(s);
const money = (n) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(n);
const esc = (s) => String(s ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

let products = [];
let cart = loadCart();
let currentUser = null;

function loadCart() {
  try { return JSON.parse(localStorage.getItem("cart")) || []; } catch { return []; }
}
function saveCart() { localStorage.setItem("cart", JSON.stringify(cart)); renderCart(); }

function toast(msg) {
  const t = $("#toast");
  t.textContent = msg;
  t.classList.remove("hidden");
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => t.classList.add("hidden"), 3000);
}

async function api(url, options) {
  const res = await fetch(url, options);
  if (!res.ok) {
    let msg = "Request failed";
    try { msg = (await res.json()).message || msg; } catch {}
    const err = new Error(msg);
    err.status = res.status;
    throw err;
  }
  return res.status === 204 ? null : res.json();
}

async function loadFilters() {
  const [cats, brands] = await Promise.all([api("/api/products/categories"), api("/api/products/brands")]);
  cats.forEach((c) => $("#category").insertAdjacentHTML("beforeend", `<option>${esc(c)}</option>`));
  brands.forEach((b) => $("#brand").insertAdjacentHTML("beforeend", `<option>${esc(b)}</option>`));
}

async function loadProducts() {
  const params = new URLSearchParams();
  if ($("#search").value.trim()) params.set("q", $("#search").value.trim());
  if ($("#category").value) params.set("category", $("#category").value);
  if ($("#brand").value) params.set("brand", $("#brand").value);
  if ($("#sort").value) params.set("sort", $("#sort").value);
  try {
    products = await api("/api/products?" + params);
    renderProducts();
  } catch (e) {
    $("#status").textContent = "Could not load products: " + e.message;
  }
}

function renderProducts() {
  $("#status").textContent = products.length ? `${products.length} product(s)` : "No products found.";
  $("#grid").innerHTML = products.map((p) => {
    const sizes = (p.sizes || "").split(",").map((s) => s.trim()).filter(Boolean);
    const stockNote = p.stock === 0 ? `<span class="out">Out of stock</span>`
      : p.stock <= 10 ? `<span class="low">Only ${p.stock} left</span>` : "";
    return `
      <article class="card">
        <img src="${esc(p.imageUrl)}" alt="${esc(p.name)}" loading="lazy">
        <div class="card-body">
          <span class="brand">${esc(p.brand)}</span>
          <h3 class="name">${esc(p.name)}</h3>
          <p class="desc">${esc(p.description)}</p>
          <div class="meta"><span>${esc(p.category)} · ${esc(p.color)}</span><span>★ ${p.rating.toFixed(1)}</span></div>
          <div class="row"><span class="price">${money(p.price)}</span>${stockNote}</div>
          <div class="row">
            <select id="size-${p.id}" aria-label="Size">${sizes.map((s) => `<option>${esc(s)}</option>`).join("")}</select>
            <button class="add-btn" data-id="${p.id}" ${p.stock === 0 ? "disabled" : ""}>Add to cart</button>
          </div>
        </div>
      </article>`;
  }).join("");
}

function addToCart(id) {
  const p = products.find((x) => x.id === id);
  if (!p) return;
  const size = $(`#size-${id}`)?.value || "";
  const existing = cart.find((i) => i.productId === id && i.size === size);
  const currentQty = existing ? existing.quantity : 0;
  if (currentQty + 1 > p.stock) return toast("No more stock available");
  if (existing) existing.quantity++;
  else cart.push({ productId: id, name: p.name, brand: p.brand, imageUrl: p.imageUrl, price: p.price, size, quantity: 1 });
  saveCart();
  toast(`${p.name} added to cart`);
}

function renderCart() {
  const count = cart.reduce((n, i) => n + i.quantity, 0);
  const total = cart.reduce((n, i) => n + i.price * i.quantity, 0);
  $("#cartCount").textContent = count;
  $("#cartTotal").textContent = money(total);
  $("#checkoutBtn").disabled = cart.length === 0;
  $("#cartItems").innerHTML = cart.length ? cart.map((i, idx) => `
    <div class="cart-item">
      <img src="${esc(i.imageUrl)}" alt="${esc(i.name)}">
      <div class="info">
        <strong>${esc(i.name)}</strong>
        <small>${esc(i.brand)} · Size: ${esc(i.size)}</small>
        <div>${money(i.price)}</div>
        <div class="qty">
          <button data-act="dec" data-idx="${idx}">−</button><span>${i.quantity}</span><button data-act="inc" data-idx="${idx}">+</button>
          <button class="link" data-act="rm" data-idx="${idx}">Remove</button>
        </div>
      </div>
    </div>`).join("") : `<div class="empty">Your cart is empty.</div>`;
}

function toggleCart(open) {
  $("#cart").classList.toggle("hidden", !open);
  $("#overlay").classList.toggle("hidden", !open);
}

// ---- authentication ----
async function loadUser() {
  try {
    const u = await api("/api/auth/me");
    currentUser = u.authenticated ? u : null;
  } catch { currentUser = null; }
  renderAuth();
}

function renderAuth() {
  $("#authArea").innerHTML = currentUser
    ? `<span class="hello">Hi, ${esc(currentUser.name.split(" ")[0])}</span>
       <a href="/orders.html">My orders</a>
       <button id="logoutBtn" class="linkbtn">Sign out</button>`
    : `<a href="/login.html">Sign in</a><a class="signup-link" href="/signup.html">Sign up</a>`;
}

$("#authArea").addEventListener("click", async (e) => {
  if (e.target.id !== "logoutBtn") return;
  await api("/api/auth/logout", { method: "POST" });
  currentUser = null;
  renderAuth();
  toast("You have been signed out");
});

// ---- events ----
$("#grid").addEventListener("click", (e) => {
  const btn = e.target.closest(".add-btn");
  if (btn) addToCart(Number(btn.dataset.id));
});
$("#cartItems").addEventListener("click", (e) => {
  const b = e.target.closest("button[data-act]");
  if (!b) return;
  const i = cart[Number(b.dataset.idx)];
  if (b.dataset.act === "inc") {
    const p = products.find((x) => x.id === i.productId);
    if (p && i.quantity + 1 > p.stock) return toast("No more stock available");
    i.quantity++;
  } else if (b.dataset.act === "dec") i.quantity = Math.max(1, i.quantity - 1);
  else cart.splice(Number(b.dataset.idx), 1);
  saveCart();
});
$("#cartBtn").onclick = () => toggleCart(true);
$("#closeCart").onclick = () => toggleCart(false);
$("#overlay").onclick = () => toggleCart(false);
$("#checkoutBtn").onclick = () => {
  toggleCart(false);
  if (!currentUser) {                       // cart is kept in the browser, so nothing is lost
    toast("Please sign in to place your order");
    setTimeout(() => { location.href = "/login.html?next=/"; }, 900);
    return;
  }
  const form = $("#checkoutForm");
  if (!form.customerName.value) form.customerName.value = currentUser.name;
  if (!form.email.value) form.email.value = currentUser.email;
  $("#checkoutError").textContent = "";
  $("#checkoutModal").classList.remove("hidden");
};
$("#cancelCheckout").onclick = () => $("#checkoutModal").classList.add("hidden");

let debounce;
$("#search").addEventListener("input", () => { clearTimeout(debounce); debounce = setTimeout(loadProducts, 250); });
["#category", "#brand", "#sort"].forEach((s) => $(s).addEventListener("change", loadProducts));

$("#checkoutForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = new FormData(e.target);
  const body = {
    customerName: f.get("customerName"), email: f.get("email"), phone: f.get("phone"), address: f.get("address"),
    items: cart.map((i) => ({ productId: i.productId, size: i.size, quantity: i.quantity })),
  };
  try {
    const order = await api("/api/orders", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
    cart = []; saveCart();
    e.target.reset();
    $("#checkoutModal").classList.add("hidden");
    toast(`Order #${order.id} placed! Total ${money(order.total)}`);
    loadProducts();
  } catch (err) {
    if (err.status === 401) { location.href = "/login.html?next=/"; return; }
    $("#checkoutError").textContent = err.message;
  }
});

(async function init() {
  renderCart();
  await loadUser();
  await loadFilters();
  await loadProducts();
})();

// If a product photo (.jpg) has not been downloaded yet, fall back to the bundled illustration (.svg)
document.addEventListener("error", (e) => {
  const t = e.target;
  if (t.tagName === "IMG" && /\.jpg$/.test(t.src) && !t.dataset.fallback) {
    t.dataset.fallback = "1";
    t.src = t.src.replace(/\.jpg$/, ".svg");
  }
}, true);
