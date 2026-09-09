const API_BASE = "http://localhost:8080/api";
const dashboardApiUrl = `${API_BASE}/dashboard/summary`;

const totalProductsEl = document.getElementById("total-products");
const totalSpentEl = document.getElementById("total-spent");
const spendBreakdownEl = document.getElementById("spend-breakdown");
const activeWarrantiesEl = document.getElementById("active-warranties");
const expiredWarrantiesEl = document.getElementById("expired-warranties");
const messageEl = document.getElementById("message");
const refreshButton = document.getElementById("refresh-button");

function showMessage(text, isError = false) {
    messageEl.textContent = text;
    messageEl.className = `message ${isError ? "error" : "success"} visible`;
}

function formatCurrency(amount) {
    return `₹${Number(amount).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

async function loadDashboardSummary() {
    try {
        showMessage("Loading metrics…");
        const response = await fetch(dashboardApiUrl);
        if (!response.ok) {
            throw new Error(`Failed to load dashboard summary with status ${response.status}`);
        }

        const data = await response.json();

        const totalPurchase = Number(data.totalPurchaseSpend || 0);
        const totalService = Number(data.totalServiceSpend || 0);
        const totalSpent = totalPurchase + totalService;

        totalProductsEl.textContent = Number(data.totalProductsCount || 0).toLocaleString("en-IN");
        totalSpentEl.textContent = formatCurrency(totalSpent);
        spendBreakdownEl.textContent = `Purchases: ${formatCurrency(totalPurchase)} · Service: ${formatCurrency(totalService)}`;
        activeWarrantiesEl.textContent = Number(data.activeWarrantiesCount || 0).toLocaleString("en-IN");
        expiredWarrantiesEl.textContent = Number(data.expiredWarrantiesCount || 0).toLocaleString("en-IN");

        showMessage("Dashboard metrics up to date.");
    } catch (error) {
        totalProductsEl.textContent = "0";
        totalSpentEl.textContent = "₹0.00";
        spendBreakdownEl.textContent = "Purchases: ₹0.00 · Service: ₹0.00";
        activeWarrantiesEl.textContent = "0";
        expiredWarrantiesEl.textContent = "0";
        showMessage("Unable to load live metrics. Ensure the Spring Boot backend is running.", true);
        console.error("Dashboard error:", error);
    }
}

refreshButton.addEventListener("click", loadDashboardSummary);
loadDashboardSummary();
