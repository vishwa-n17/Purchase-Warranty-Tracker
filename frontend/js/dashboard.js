const dashboardApiUrl = `${API_BASE}/dashboard/summary`;

const totalProductsEl = document.getElementById("total-products");
const totalSpentEl = document.getElementById("total-spent");
const spendBreakdownEl = document.getElementById("spend-breakdown");
const serviceSpendEl = document.getElementById("service-spend");
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
        const response = await fetch(dashboardApiUrl, {
            credentials: "include"
        });
        if (!response.ok) {
            throw new Error("Failed to load dashboard summary.");
        }

        const data = await response.json();

        const totalPurchase = Number(data.totalPurchaseSpend || 0);
        const totalService = Number(data.totalServiceSpend || 0);
        const totalSpent = totalPurchase + totalService;

        totalProductsEl.textContent = Number(data.totalProductsCount || 0).toLocaleString("en-IN");
        totalSpentEl.textContent = formatCurrency(totalSpent);
        serviceSpendEl.textContent = formatCurrency(totalService);
        spendBreakdownEl.textContent = `Purchases: ${formatCurrency(totalPurchase)} · Service: ${formatCurrency(totalService)}`;
        activeWarrantiesEl.textContent = Number(data.activeWarrantiesCount || 0).toLocaleString("en-IN");
        expiredWarrantiesEl.textContent = Number(data.expiredWarrantiesCount || 0).toLocaleString("en-IN");

        showMessage("Dashboard metrics up to date.");
    } catch (error) {
        totalProductsEl.textContent = "—";
        totalSpentEl.textContent = "—";
        serviceSpendEl.textContent = "—";
        spendBreakdownEl.textContent = "—";
        activeWarrantiesEl.textContent = "—";
        expiredWarrantiesEl.textContent = "—";
        showMessage("Dashboard information is temporarily unavailable.", true);
        console.error("Dashboard error:", error);
    }
}

function renderWarrantyAction(overview) {
    const container = document.getElementById("warranty-action-content");
    if (!container) return;
    if (!overview || !overview.items || overview.items.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">✅</div><div class="empty-state-title">No warranty actions required</div></div>';
        return;
    }

    const expiringSoon = overview.items.filter(item => item.status === "ACTIVE" && item.daysRemaining != null && item.daysRemaining >= 0 && item.daysRemaining <= 30);
    const expired = overview.items.filter(item => item.status === "EXPIRED" || (item.daysRemaining != null && item.daysRemaining < 0));
    const voidPolicies = overview.items.filter(item => item.status === "VOID");

    let html = "";
    if (expiringSoon.length > 0) {
        html += `<h3 class="card-title" style="margin-bottom:var(--spacing-sm);">Expiring Soon</h3>`;
        html += renderWarrantyTable(expiringSoon);
    }
    if (expired.length > 0) {
        html += `<h3 class="card-title" style="margin-top:var(--spacing-md);margin-bottom:var(--spacing-sm);">Expired</h3>`;
        html += renderWarrantyTable(expired);
    }
    if (voidPolicies.length > 0) {
        html += `<h3 class="card-title" style="margin-top:var(--spacing-md);margin-bottom:var(--spacing-sm);">Void Policies</h3>`;
        html += renderWarrantyTable(voidPolicies);
    }
    container.innerHTML = html;
}

function renderWarrantyTable(items) {
    if (!items || items.length === 0) return "";
    return `<div class="table-wrapper">
        <table class="data-table data-table--enhanced">
            <thead>
                <tr>
                    <th>Product</th>
                    <th>Provider</th>
                    <th>Expiry Date</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                ${items.map(item => `
                    <tr>
                        <td>${item.productName || `Product #${item.productId}`}</td>
                        <td>${item.provider || "-"}</td>
                        <td>${item.expiryDate || "-"}</td>
                        <td>${getStatusBadge(item.status)}</td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    </div>`;
}

function getStatusBadge(status) {
    switch ((status || "").toUpperCase()) {
        case "ACTIVE":
        case "IN_SERVICE":
            return '<span class="badge badge-active">ACTIVE</span>';
        case "EXPIRED":
            return '<span class="badge badge-expired">EXPIRED</span>';
        case "VOID":
            return '<span class="badge badge-void">VOID</span>';
        case "NEEDS_SERVICE":
            return '<span class="badge badge-warning">NEEDS SERVICE</span>';
        default:
            return `<span class="badge badge-secondary">${status || "UNKNOWN"}</span>`;
    }
}

function renderHealth(healths) {
    const container = document.getElementById("health-content");
    if (!container) return;
    if (!healths || healths.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">🏥</div><div class="empty-state-title">No product health data available</div></div>';
        return;
    }
    container.innerHTML = `<div class="table-wrapper">
        <table class="data-table data-table--enhanced">
            <thead>
                <tr>
                    <th>Product</th>
                    <th>Health Score</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                ${healths.map(h => `
                    <tr>
                        <td>${h.productName || `Product #${h.productId}`}</td>
                        <td>${h.score != null ? h.score + "/100" : "-"}</td>
                        <td>${h.label || "-"}</td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    </div>`;
}

function renderActivity(activities) {
    const container = document.getElementById("activity-content");
    if (!container) return;
    if (!activities || activities.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📅</div><div class="empty-state-title">No recent activity</div></div>';
        return;
    }
    container.innerHTML = `<div class="timeline">
        ${activities.map(act => `
            <div class="timeline-item">
                <div class="timeline-marker ${act.type === 'PURCHASE' ? 'timeline-marker--purchase' : act.type === 'WARRANTY' ? 'timeline-marker--warranty' : act.type === 'SERVICE' ? 'timeline-marker--service' : 'timeline-marker--default'}"></div>
                <div class="timeline-content">
                    <div class="timeline-date">${act.date || "-"}</div>
                    <div class="timeline-title">${act.title || "Activity"}</div>
                    <div class="timeline-text">${act.description || ""}</div>
                </div>
            </div>
        `).join("")}
    </div>`;
}

async function loadDashboardExtensions() {
    const overviewContainer = document.getElementById("warranty-action-content");
    const healthContainer = document.getElementById("health-content");
    const activityContainer = document.getElementById("activity-content");

    if (overviewContainer) {
        overviewContainer.innerHTML = '<div class="loading-state"><div class="loading-state-spinner"></div><div class="loading-state-title">Loading warranty actions…</div></div>';
    }
    if (healthContainer) {
        healthContainer.innerHTML = '<div class="loading-state"><div class="loading-state-spinner"></div><div class="loading-state-title">Loading product health…</div></div>';
    }
    if (activityContainer) {
        activityContainer.innerHTML = '<div class="loading-state"><div class="loading-state-spinner"></div><div class="loading-state-title">Loading recent activity…</div></div>';
    }

    try {
        const [overview, health, activity] = await Promise.all([
            getWarrantyOverview(),
            getHealthOverview(),
            getRecentActivity()
        ]);
        renderWarrantyAction(overview);
        renderHealth(health);
        renderActivity(activity);
    } catch (error) {
        console.error("Dashboard extensions error:", error);
        if (overviewContainer) {
            overviewContainer.innerHTML = '<div class="empty-state"><div class="empty-state-icon">⚠️</div><div class="empty-state-title">Warranty information is temporarily unavailable.</div><div class="empty-state-text">Please try again later.</div></div>';
        }
        if (healthContainer) {
            healthContainer.innerHTML = '<div class="empty-state"><div class="empty-state-icon">🏥</div><div class="empty-state-title">Product health information is temporarily unavailable.</div><div class="empty-state-text">Please try again later.</div></div>';
        }
        if (activityContainer) {
            activityContainer.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📅</div><div class="empty-state-title">Recent activity is temporarily unavailable.</div><div class="empty-state-text">Please try again later.</div></div>';
        }
    }
}

if (refreshButton) {
    refreshButton.addEventListener("click", async () => {
        await loadDashboardSummary();
        await loadDashboardExtensions();
    });
}

loadDashboardSummary();
loadDashboardExtensions();
