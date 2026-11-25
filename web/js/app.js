/**
 * RELOOP: Smart Recycling & Rewards Management Platform
 * Frontend Client Controller
 */

const API_BASE = "";

// Default fallback catalogs to ensure zero "undefined" values under any condition
const DEFAULT_MATERIALS = [
  { id: 1, name: "Plastic Bottles", unit: "PIECE", pointsPerUnit: 2, active: true },
  { id: 2, name: "Aluminium Cans", unit: "PIECE", pointsPerUnit: 3, active: true },
  { id: 3, name: "Paper", unit: "KG", pointsPerUnit: 10, active: true },
  { id: 4, name: "Cardboard", unit: "KG", pointsPerUnit: 8, active: true },
  { id: 5, name: "Glass Jars", unit: "PIECE", pointsPerUnit: 4, active: true }
];

const DEFAULT_STATIONS = [
  { id: 1, name: "GreenMart Central Station", address: "Ground Floor Parking, GreenMart", area: "City Centre", openTime: "08:00:00", closeTime: "20:00:00" },
  { id: 2, name: "FreshBasket Eastside Station", address: "Gate 2, FreshBasket Supermarket", area: "Model Town", openTime: "09:00:00", closeTime: "21:00:00" },
  { id: 3, name: "MetroMart Railway Station", address: "Beside MetroMart Entrance", area: "Railway Road", openTime: "07:30:00", closeTime: "19:30:00" }
];

// ---------------- Authentication & State ----------------
function getToken() {
  return localStorage.getItem("reloop_token");
}

function getUser() {
  const u = localStorage.getItem("reloop_user");
  return u ? JSON.parse(u) : null;
}

function setSession(token, user) {
  localStorage.setItem("reloop_token", token);
  localStorage.setItem("reloop_user", JSON.stringify(user));
}

function clearSession() {
  localStorage.removeItem("reloop_token");
  localStorage.removeItem("reloop_user");
}

function checkAuth(expectedRole = null) {
  const user = getUser();
  const token = getToken();
  if (!token || !user) {
    window.location.href = "/index.html?openAuth=login";
    return null;
  }
  if (expectedRole && user.role !== expectedRole && user.role !== "ADMIN") {
    alert("Unauthorized access. Redirecting to your dashboard.");
    if (user.role === "CUSTOMER") window.location.href = "/customer.html";
    else if (user.role === "OPERATOR") window.location.href = "/operator.html";
    else if (user.role === "ADMIN") window.location.href = "/admin.html";
    return null;
  }
  return user;
}

function logout() {
  const token = getToken();
  if (token) {
    fetch(API_BASE + "/api/auth/logout", {
      method: "POST",
      headers: { "Authorization": "Bearer " + token }
    }).catch(() => {});
  }
  clearSession();
  window.location.href = "/index.html";
}

// ---------------- Safe display helpers (zero undefined) ----------------
function safeText(val, fallback = "Not available") {
  if (val == null || val === undefined) return fallback;
  const s = String(val).trim();
  if (s === "" || s.toLowerCase() === "null" || s.toLowerCase() === "undefined" || s === "NaN") return fallback;
  return s;
}

function safeNumber(val, fallback = 0) {
  if (val == null || val === undefined) return fallback;
  const n = Number(val);
  return isNaN(n) ? fallback : n;
}

function getMaterialIcon(name) {
  const n = (name || "").toLowerCase();
  if (n.includes("plastic")) return "♻️";
  if (n.includes("aluminium") || n.includes("aluminum")) return "🥫";
  if (n.includes("paper")) return "📄";
  if (n.includes("cardboard")) return "📦";
  if (n.includes("glass")) return "🍾";
  if (n.includes("steel")) return "🔩";
  if (n.includes("e-waste") || n.includes("ewaste")) return "📱";
  return "🗑️";
}

function safeDate(val, fallback = "Not available") {
  if (!val) return fallback;
  try {
    const d = new Date(val);
    if (isNaN(d.getTime())) return fallback;
    return d.toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" });
  } catch (e) {
    return fallback;
  }
}

// ---------------- Email Dropdown Login (instead of quick buttons) ----------------
const DEMO_ACCOUNTS = [
  { label: "Emma Watson (Customer)", email: "emma@reloop.com", password: "customer123", role: "CUSTOMER" },
  { label: "John Doe (Customer)", email: "john.doe@reloop.com", password: "customer123", role: "CUSTOMER" },
  { label: "Sarah Lee (Customer)", email: "sarah.lee@reloop.com", password: "customer123", role: "CUSTOMER" },
  { label: "Mike Chen (Customer)", email: "mike.chen@reloop.com", password: "customer123", role: "CUSTOMER" },
  { label: "Lisa Wong (Customer)", email: "lisa.wong@reloop.com", password: "customer123", role: "CUSTOMER" },
  { label: "Vikram Singh (Operator)", email: "vikram@reloop.com", password: "operator123", role: "OPERATOR" },
  { label: "Priya Nair (Operator)", email: "priya@reloop.com", password: "operator123", role: "OPERATOR" },
  { label: "Ahmed Khan (Operator)", email: "ahmed@reloop.com", password: "operator123", role: "OPERATOR" },
  { label: "Carlos Ruiz (Operator)", email: "carlos@reloop.com", password: "operator123", role: "OPERATOR" },
  { label: "Fatima Ali (Operator)", email: "fatima@reloop.com", password: "operator123", role: "OPERATOR" },
  { label: "Platform Admin (Admin)", email: "admin@reloop.com", password: "admin123", role: "ADMIN" }
];

async function quickLoginFromDropdown() {
  const select = document.getElementById("demo-account-select");
  if (!select) return;
  const idx = select.selectedIndex;
  if (idx <= 0) {
    showToast("Please select an account from the dropdown", "error");
    return;
  }
  const acc = DEMO_ACCOUNTS[idx - 1];
  showToast(`Signing in as ${safeText(acc.label)}...`);
  try {
    const data = await apiCall("/api/auth/login", "POST", { email: acc.email, password: acc.password });
    setSession(data.token, {
      userId: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
      stationId: data.stationId
    });
    showToast("Signed in as " + safeText(data.name));
    setTimeout(() => {
      if (data.role === "CUSTOMER") window.location.href = "/customer.html";
      else if (data.role === "OPERATOR") window.location.href = "/operator.html";
      else if (data.role === "ADMIN") window.location.href = "/admin.html";
    }, 400);
  } catch (err) {
    showToast("Login error: " + err.message, "error");
  }
}

// ---------------- Generic API Fetcher ----------------
async function apiCall(endpoint, method = "GET", body = null) {
  const headers = { "Content-Type": "application/json" };
  const token = getToken();
  if (token) {
    headers["Authorization"] = "Bearer " + token;
  }

  const options = { method, headers };
  if (body) {
    options.body = JSON.stringify(body);
  }

  try {
    const res = await fetch(API_BASE + endpoint, options);
    const data = await res.json();
    if (!res.ok || !data.success) {
      throw new Error(data.error || "Request failed with status " + res.status);
    }
    return data.data;
  } catch (err) {
    console.error("API error at " + endpoint + ":", err);
    throw err;
  }
}

// ---------------- Toast Notifications ----------------
function showToast(message, type = "success") {
  let container = document.getElementById("toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "toast-container";
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.innerText = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 4000);
}

// ---------------- Success Popup Modal ----------------
function showSuccessModal(title, message) {
  let modal = document.getElementById("success-popup-modal");
  if (!modal) {
    modal = document.createElement("div");
    modal.id = "success-popup-modal";
    modal.className = "modal-overlay";
    modal.innerHTML = `
      <div class="modal-card" style="max-width: 440px;">
        <div class="modal-body success-alert-card">
          <div class="success-icon-large">✓</div>
          <h3 style="font-size: 20px; font-weight: 700; color: var(--text-main); margin-bottom: 8px;" id="success-popup-title">Success</h3>
          <p style="font-size: 14.5px; color: var(--text-secondary); margin-bottom: 24px;" id="success-popup-msg">Action completed successfully.</p>
          <button class="btn btn-primary" style="width: 100%;" onclick="document.getElementById('success-popup-modal').classList.remove('active')">Continue</button>
        </div>
      </div>
    `;
    document.body.appendChild(modal);
  }

  document.getElementById("success-popup-title").innerText = title;
  document.getElementById("success-popup-msg").innerText = message;
  modal.classList.add("active");
}

// ---------------- Modals ----------------
function openAuthModal(mode = "login") {
  const modal = document.getElementById("auth-modal");
  if (!modal) return;
  modal.classList.add("active");
  switchAuthTab(mode);
}

function closeAuthModal() {
  const modal = document.getElementById("auth-modal");
  if (modal) modal.classList.remove("active");
}

function switchAuthTab(tab) {
  const loginForm = document.getElementById("login-form");
  const regForm = document.getElementById("register-form");
  const tabLogin = document.getElementById("tab-login");
  const tabReg = document.getElementById("tab-register");

  if (tab === "login") {
    loginForm.style.display = "block";
    regForm.style.display = "none";
    tabLogin.classList.add("active");
    tabReg.classList.remove("active");
  } else {
    loginForm.style.display = "none";
    regForm.style.display = "block";
    tabLogin.classList.remove("active");
    tabReg.classList.add("active");
  }
}

// ---------------- Auth Form Handlers ----------------
async function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById("login-email").value.trim();
  const password = document.getElementById("login-password").value;
  const errorEl = document.getElementById("login-error");
  errorEl.innerText = "";

  try {
    const data = await apiCall("/api/auth/login", "POST", { email, password });
    setSession(data.token, {
      userId: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
      stationId: data.stationId
    });
    showToast("Signed in as " + safeText(data.name, "User"));
    setTimeout(() => {
      if (data.role === "CUSTOMER") window.location.href = "/customer.html";
      else if (data.role === "OPERATOR") window.location.href = "/operator.html";
      else if (data.role === "ADMIN") window.location.href = "/admin.html";
    }, 400);
  } catch (err) {
    errorEl.innerText = err.message;
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const name = document.getElementById("reg-name").value.trim();
  const email = document.getElementById("reg-email").value.trim();
  const password = document.getElementById("reg-password").value;
  const role = document.getElementById("reg-role").value;
  const phone = document.getElementById("reg-phone").value.trim();
  const stationSelect = document.getElementById("reg-station");
  const stationId = stationSelect && stationSelect.value ? parseInt(stationSelect.value) : null;
  const errorEl = document.getElementById("reg-error");
  errorEl.innerText = "";

  try {
    const data = await apiCall("/api/auth/register", "POST", {
      name, email, password, role, phone, stationId
    });
    setSession(data.token, {
      userId: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
      stationId: data.stationId
    });
    showToast("Account created successfully!");
    setTimeout(() => {
      if (data.role === "CUSTOMER") window.location.href = "/customer.html";
      else if (data.role === "OPERATOR") window.location.href = "/operator.html";
      else if (data.role === "ADMIN") window.location.href = "/admin.html";
    }, 400);
  } catch (err) {
    errorEl.innerText = err.message;
  }
}

function toggleStationField() {
  const role = document.getElementById("reg-role").value;
  const stationGroup = document.getElementById("reg-station-group");
  if (stationGroup) {
    stationGroup.style.display = (role === "OPERATOR") ? "block" : "none";
  }
}

// ---------------- Public Landing Page Initialization ----------------
async function initLandingPage() {
  let stations = DEFAULT_STATIONS;
  let materials = DEFAULT_MATERIALS;

  try {
    const res = await apiCall("/api/stations");
    if (res && res.length > 0) stations = res;
  } catch (e) {}

  try {
    const mRes = await apiCall("/api/materials");
    if (mRes && mRes.length > 0) materials = mRes;
  } catch (e) {}

  // Populate stations dropdown for registration
  const select = document.getElementById("reg-station");
  if (select) {
    select.innerHTML = '<option value="">-- Choose Assigned Station --</option>';
    stations.forEach(s => {
      const opt = document.createElement("option");
      opt.value = s.id;
      opt.text = (s.name || "Station " + s.id) + " (" + (s.area || "Area") + ")";
      select.appendChild(opt);
    });
  }

  // Populate station cards
  const stationsContainer = document.getElementById("landing-stations-grid");
  if (stationsContainer) {
    stationsContainer.innerHTML = "";
    stations.forEach(s => {
      const card = document.createElement("div");
      card.className = "card";
      card.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
          <h4 style="font-size: 16px; color: var(--text-main); font-weight: 700;">${safeText(s.name, "Recycling Kiosk")}</h4>
          <span class="status-pill status-verified">Open</span>
        </div>
        <p style="font-size: 13.5px; color: var(--text-secondary); margin-bottom: 6px;">📍 ${safeText(s.address, "City Center")}, ${safeText(s.area, "Central")}</p>
        <p style="font-size: 13px; color: var(--primary-green); font-weight: 600;">⏰ Hours: ${safeText(s.openTime, "08:00")} - ${safeText(s.closeTime, "20:00")}</p>
      `;
      stationsContainer.appendChild(card);
    });
  }

  // Populate materials exchange list
  const materialsContainer = document.getElementById("landing-materials-grid");
  if (materialsContainer) {
    materialsContainer.innerHTML = "";
    materials.forEach(m => {
      const card = document.createElement("div");
      card.className = "card";
      const matIcon = getMaterialIcon(safeText(m.name, "Material"));
      card.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div style="display: flex; align-items: center; gap: 10px;">
            <span class="material-icon">${matIcon}</span>
            <div>
              <h4 style="font-size: 16px; font-weight: 700; color: var(--text-main);">${safeText(m.name, "Recyclable")}</h4>
              <p style="font-size: 13px; color: var(--text-secondary);">Unit: ${safeText(m.unit, "KG")}</p>
            </div>
          </div>
          <span style="font-size: 15px; font-weight: 700; color: var(--primary-green); background: var(--highlight-light); padding: 4px 12px; border-radius: 6px;">
            ${safeNumber(m.pointsPerUnit, 5)} pts
          </span>
        </div>
      `;
      materialsContainer.appendChild(card);
    });
  }

  const params = new URLSearchParams(window.location.search);
  if (params.get("openAuth")) {
    openAuthModal(params.get("openAuth"));
  }
}
