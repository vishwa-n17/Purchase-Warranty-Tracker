const API_BASE = "http://localhost:8080/api";

function showMessage(text, isError = false) {
    const messageEl = document.getElementById("message");
    if (!messageEl) return;
    messageEl.textContent = text;
    messageEl.className = `message ${isError ? "error" : "success"} visible`;
}

async function loadSettings() {
    try {
        const response = await fetch(`${API_BASE}/auth/me`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            window.location.href = "login.html";
            return;
        }

        const user = await response.json();

        document.getElementById("settings-name").textContent = user.name || "-";
        document.getElementById("settings-email").textContent = user.email || "-";
        document.getElementById("settings-user-id").textContent = user.id != null ? `#${user.id}` : "-";
    } catch (error) {
        showMessage("Unable to load account information. Ensure the backend is running.", true);
        console.error("Settings load error:", error);
    }
}

document.getElementById("password-form").addEventListener("submit", async function(event) {
    event.preventDefault();

    const currentPassword = document.getElementById("current-password").value.trim();
    const newPassword = document.getElementById("new-password").value;
    const confirmNewPassword = document.getElementById("confirm-new-password").value;

    if (newPassword !== confirmNewPassword) {
        showMessage("New password and confirmation do not match.", true);
        return;
    }

    if (newPassword.length < 6) {
        showMessage("New password must be at least 6 characters long.", true);
        return;
    }

    const result = await changePassword(currentPassword, newPassword);

    if (result.success) {
        showMessage("Password changed successfully.", false);
        document.getElementById("password-form").reset();
    } else {
        showMessage(result.message || "Failed to change password.", true);
    }
});

document.getElementById("logout-button").addEventListener("click", async function() {
    await handleLogout();
});

loadSettings();
