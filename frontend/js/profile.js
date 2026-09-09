const API_BASE = "http://localhost:8080/api";

function formatCurrency(amount) {
    return `₹${Number(amount).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function showMessage(text, isError = false) {
    const messageEl = document.getElementById("message");
    if (!messageEl) return;
    messageEl.textContent = text;
    messageEl.className = `message ${isError ? "error" : "success"} visible`;
}

async function loadProfile() {
    try {
        const [userResponse, dashboardResponse] = await Promise.all([
            fetch(`${API_BASE}/auth/me`, { method: "GET", credentials: "include" }),
            fetch(`${API_BASE}/dashboard/summary`, { method: "GET", credentials: "include" })
        ]);

        if (!userResponse.ok) {
            window.location.href = "login.html";
            return;
        }

        const user = await userResponse.json();
        const dashboard = dashboardResponse.ok ? await dashboardResponse.json() : null;

        document.getElementById("profile-name").textContent = user.name || "User";
        document.getElementById("profile-email").textContent = user.email || "-";
        document.getElementById("profile-detail-name").textContent = user.name || "-";
        document.getElementById("profile-detail-email").textContent = user.email || "-";
        document.getElementById("profile-detail-id").textContent = user.id != null ? `#${user.id}` : "-";

        const avatarEl = document.getElementById("profile-avatar");
        if (avatarEl) {
            avatarEl.textContent = (user.name || "U").charAt(0).toUpperCase();
        }

        if (dashboard) {
            document.getElementById("profile-products").textContent = Number(dashboard.totalProductsCount || 0).toLocaleString("en-IN");
            document.getElementById("profile-purchase-spend").textContent = formatCurrency(dashboard.totalPurchaseSpend || 0);
            document.getElementById("profile-service-spend").textContent = formatCurrency(dashboard.totalServiceSpend || 0);
            document.getElementById("profile-active-warranties").textContent = Number(dashboard.activeWarrantiesCount || 0).toLocaleString("en-IN");
            document.getElementById("profile-expired-warranties").textContent = Number(dashboard.expiredWarrantiesCount || 0).toLocaleString("en-IN");
        } else {
            document.getElementById("profile-products").textContent = "0";
            document.getElementById("profile-purchase-spend").textContent = formatCurrency(0);
            document.getElementById("profile-service-spend").textContent = formatCurrency(0);
            document.getElementById("profile-active-warranties").textContent = "0";
            document.getElementById("profile-expired-warranties").textContent = "0";
        }
    } catch (error) {
        showMessage("Unable to load profile data. Ensure the backend is running.", true);
        console.error("Profile error:", error);
    }
}

loadProfile();
