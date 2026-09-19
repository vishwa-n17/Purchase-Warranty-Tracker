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
        const userResponse = await fetch(`${API_BASE}/auth/me`, { method: "GET", credentials: "include" });

        if (!userResponse.ok) {
            window.location.href = "login.html";
            return;
        }

        const user = await userResponse.json();

        document.getElementById("profile-name").textContent = user.name || "User";
        document.getElementById("profile-email").textContent = user.email || "-";
        document.getElementById("profile-detail-name").textContent = user.name || "-";
        document.getElementById("profile-detail-email").textContent = user.email || "-";

        const avatarEl = document.getElementById("profile-avatar");
        if (avatarEl) {
            avatarEl.textContent = (user.name || "U").charAt(0).toUpperCase();
        }

        loadProfileStats();
    } catch (error) {
        showMessage("Profile information is temporarily unavailable.", true);
        console.error("Profile error:", error);
    }
}

async function loadProfileStats() {
    try {
        const response = await fetch(`${API_BASE}/dashboard/summary`, { method: "GET", credentials: "include" });
        if (!response.ok) throw new Error("Failed to load profile statistics.");

        const dashboard = await response.json();

        document.getElementById("profile-products").textContent = Number(dashboard.totalProductsCount || 0).toLocaleString("en-IN");
        document.getElementById("profile-purchase-spend").textContent = formatCurrency(dashboard.totalPurchaseSpend || 0);
        document.getElementById("profile-service-spend").textContent = formatCurrency(dashboard.totalServiceSpend || 0);
        document.getElementById("profile-active-warranties").textContent = Number(dashboard.activeWarrantiesCount || 0).toLocaleString("en-IN");
    } catch (error) {
        document.getElementById("profile-products").textContent = "—";
        document.getElementById("profile-purchase-spend").textContent = "—";
        document.getElementById("profile-service-spend").textContent = "—";
        document.getElementById("profile-active-warranties").textContent = "—";
        console.error("Profile stats error:", error);
    }
}

loadProfile();
