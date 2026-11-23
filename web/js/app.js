/**
 * RELOOP: Smart Recycling & Rewards Management Platform
 * Frontend Client Controller
 * Communicates with the Java backend HttpServer REST API endpoints.
 */

const API_BASE = "";

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
    alert("Unauthorized access. Redirecting to your assigned dashboard.");
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
    showToast("Signed in as " + data.name);
    setTimeout(() => {
      if (data.role === "CUSTOMER") window.location.href = "/customer.html";
      else if (data.role === "OPERATOR") window.location.href = "/operator.html";
      else if (data.role === "ADMIN") window.location.href = "/admin.html";
    }, 500);
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
    }, 500);
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
  // Load stations
  try {
    const stations = await apiCall("/api/stations");
    const select = document.getElementById("reg-station");
    if (select) {
      select.innerHTML = '<option value="">-- Choose Assigned Station --</option>';
      stations.forEach(s => {
        const opt = document.createElement("option");
        opt.value = s.id;
        opt.text = s.name + " (" + s.area + ")";
        select.appendChild(opt);
      });
    }

    const stationsContainer = document.getElementById("landing-stations-grid");
    if (stationsContainer) {
      stationsContainer.innerHTML = "";
      stations.forEach(s => {
        const card = document.createElement("div");
        card.className = "metric-card";
        card.style.flexDirection = "column";
        card.style.alignItems = "flex-start";
        card.innerHTML = `
          <div style="display: flex; justify-content: space-between; width: 100%; margin-bottom: 8px;">
            <h4 style="font-size: 15px; color: var(--text-main); font-weight: 700;">${s.name}</h4>
            <span class="status-pill status-verified">Open</span>
          </div>
          <p style="font-size: 12.5px; color: var(--text-secondary); margin-bottom: 4px;">📍 ${s.address}, ${s.area}</p>
          <p style="font-size: 12px; color: var(--primary-green); font-weight: 600;">⏰ Hours: ${s.openTime} - ${s.closeTime}</p>
        `;
        stationsContainer.appendChild(card);
      });
    }
  } catch (err) {
    console.error("Error loading stations for landing page:", err);
  }

  // Check URL query parameters to auto-open login modal
  const params = new URLSearchParams(window.location.search);
  if (params.get("openAuth")) {
    openAuthModal(params.get("openAuth"));
  }
}
