const API_BASE = "http://localhost:8080/api";

function showAuthMessage(text, isError = false) {
    const messageEl = document.getElementById("message");
    if (!messageEl) return;
    messageEl.textContent = text;
    messageEl.className = `message ${isError ? "error" : "success"} visible`;
}

async function getErrorMessage(response) {
    try {
        const error = await response.json();
        return error?.message || "The request could not be completed.";
    } catch {
        return "The request could not be completed.";
    }
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
            window.location.href = "dashboard.html";
        }, 800);
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
        return { success: false, message: error.message };
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
        return { success: false, message: error.message };
    }
}
