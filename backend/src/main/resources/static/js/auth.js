const API_BASE = "http://localhost:8080/api";

function showAuthMessage(text, isError = false) {
    const messageEl = document.getElementById("message");
    if (!messageEl) return;
    messageEl.textContent = text;
    messageEl.className = `message ${isError ? "error" : "success"} visible`;
}

async function getErrorMessage(response) {
    const status = response.status;
    if (status === 401) return "Please sign in again.";
    if (status === 403) return "You don't have permission to perform this action.";
    if (status === 404) return "The requested information could not be found.";
    if (status === 409) return "This information already exists.";
    return "Something went wrong. Please try again.";
}

async function handleSignup(name, email, password) {
    try {
        const response = await fetch(`${API_BASE}/auth/signup`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, email, password }),
            credentials: "include"
        });

        if (!response.ok) {
            const message = await getErrorMessage(response);
            showAuthMessage(message, true);
            return;
        }

        showAuthMessage("Account created successfully! Redirecting…");
        setTimeout(() => {
            window.location.href = "dashboard.html";
        }, 800);
    } catch (error) {
        showAuthMessage("Could not reach the server. Please try again.", true);
        console.error("Signup error:", error);
    }
}

async function handleLogin(email, password) {
    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
            credentials: "include"
        });

        if (!response.ok) {
            const message = await getErrorMessage(response);
            showAuthMessage(message, true);
            return;
        }

        showAuthMessage("Signed in successfully! Redirecting…");
        setTimeout(() => {
            window.location.href = "index.html";
        }, 300);
    } catch (error) {
        showAuthMessage("Could not reach the server. Please try again.", true);
        console.error("Login error:", error);
    }
}

async function handleLogout() {
    try {
        await fetch(`${API_BASE}/auth/logout`, {
            method: "POST",
            credentials: "include"
        });
    } catch (error) {
        console.error("Logout error:", error);
    } finally {
        window.location.href = "login.html";
    }
}

async function checkAuth() {
    try {
        const response = await fetch(`${API_BASE}/auth/me`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            window.location.href = "login.html";
            return null;
        }

        const user = await response.json();
        return user;
    } catch (error) {
        window.location.href = "login.html";
        return null;
    }
}

function updateAuthUI(user) {
    if (!user) return;

    const nameDisplay = document.getElementById("user-name-display");
    const avatarEl = document.getElementById("user-avatar");
    const userMenu = document.getElementById("user-menu");
    const welcomeNameEl = document.getElementById("welcome-name");

    if (welcomeNameEl) {
        welcomeNameEl.textContent = user.name || "User";
    }

    if (nameDisplay) {
        nameDisplay.textContent = user.name || "User";
    }

    if (avatarEl) {
        const initial = (user.name || "U").charAt(0).toUpperCase();
        avatarEl.textContent = initial;
    }

    if (userMenu) {
        userMenu.classList.remove("hidden");
    }
}

function initUserMenu() {
    const trigger = document.getElementById("user-menu-trigger");
    const menu = document.getElementById("user-menu");
    const logoutButton = document.getElementById("header-logout");

    if (!trigger || !menu) return;

    trigger.addEventListener("click", function(event) {
        event.stopPropagation();
        const isOpen = menu.classList.contains("open");
        menu.classList.toggle("open", !isOpen);
        trigger.setAttribute("aria-expanded", String(!isOpen));
    });

    document.addEventListener("click", function(event) {
        if (!menu.contains(event.target)) {
            menu.classList.remove("open");
            trigger.setAttribute("aria-expanded", "false");
        }
    });

    if (logoutButton) {
        logoutButton.addEventListener("click", async function() {
            await handleLogout();
        });
    }
}

async function changePassword(currentPassword, newPassword) {
    try {
        const response = await fetch(`${API_BASE}/auth/change-password`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ currentPassword, newPassword }),
            credentials: "include"
        });

        if (!response.ok) {
            const message = await getErrorMessage(response);
            throw new Error(message);
        }

        return { success: true };
    } catch (error) {
        return { success: false, message: "Could not change password. Please check your current password and try again." };
    }
}

async function deleteAccount() {
    try {
        const response = await fetch(`${API_BASE}/auth/account`, {
            method: "DELETE",
            credentials: "include"
        });

        if (!response.ok && response.status !== 204) {
            const message = await getErrorMessage(response);
            throw new Error(message);
        }

        return { success: true };
    } catch (error) {
        return { success: false, message: "Could not delete your account. Please try again." };
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const toggle = document.querySelector('.nav-toggle');
    const nav = document.querySelector('.main-nav');
    if (toggle && nav) {
        toggle.addEventListener('click', () => {
            nav.classList.toggle('open');
            const expanded = nav.classList.contains('open');
            toggle.setAttribute('aria-expanded', String(expanded));
        });
    }
});

async function loadNotifications() {
    try {
        const response = await fetch(`${API_BASE}/notifications`, {
            credentials: "include"
        });
        if (!response.ok) return [];
        return await response.json();
    } catch {
        return [];
    }
}

function renderNotificationBadge(count) {
    const badge = document.getElementById("notification-badge");
    if (!badge) return;
    if (count > 0) {
        badge.textContent = count > 9 ? "9+" : count;
        badge.style.display = "flex";
    } else {
        badge.style.display = "none";
    }
}

function renderNotificationsList(notifications) {
    const list = document.getElementById("notification-list");
    const empty = document.getElementById("notification-empty");
    if (!list) return;
    list.innerHTML = "";
    if (!notifications || notifications.length === 0) {
        if (empty) empty.style.display = "block";
        return;
    }
    if (empty) empty.style.display = "none";
    notifications.forEach(n => {
        const item = document.createElement("div");
        const severity = (n.severity || "").toUpperCase();
        const severityClass = severity === "HIGH" ? "notification-item--high" : severity === "MEDIUM" ? "notification-item--medium" : severity === "LOW" ? "notification-item--low" : "";
        item.className = `notification-item ${severityClass}`;
        item.innerHTML = `
            <div class="notification-item-icon">${n.icon || "🔔"}</div>
            <div class="notification-item-content">
                <div class="notification-item-title">${n.title || "Notification"}</div>
                <div class="notification-item-text">${n.description || ""}</div>
            </div>
        `;
        list.appendChild(item);
    });
}

async function refreshNotifications() {
    const notifications = await loadNotifications();
    renderNotificationBadge(notifications.length);
    renderNotificationsList(notifications);
}

function initNotifications() {
    const trigger = document.getElementById("notification-trigger");
    const dropdown = document.getElementById("notification-dropdown");
    if (!trigger || !dropdown) return;

    trigger.addEventListener("click", async function(event) {
        event.stopPropagation();
        const isOpen = dropdown.classList.contains("open");
        dropdown.classList.toggle("open", !isOpen);
        trigger.setAttribute("aria-expanded", String(!isOpen));
        if (!isOpen) {
            await refreshNotifications();
        }
    });

    document.addEventListener("click", function(event) {
        if (!dropdown.contains(event.target)) {
            dropdown.classList.remove("open");
            trigger.setAttribute("aria-expanded", "false");
        }
    });
}

function initSearch() {
    const searchInput = document.getElementById("global-search");
    const searchForm = document.getElementById("global-search-form");
    if (!searchInput || !searchForm) return;

    searchForm.addEventListener("submit", function(event) {
        event.preventDefault();
        const q = searchInput.value.trim();
        if (q) {
            window.location.href = `search-results.html?q=${encodeURIComponent(q)}`;
        }
    });
}
