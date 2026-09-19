const warrantiesApiUrl = `${API_BASE}/warranties`;
const productsApiUrl = `${API_BASE}/products`;

const warrantyForm = document.getElementById("warranty-form");
const warrantyTableBody = document.getElementById("warranty-table-body");
const productSelect = document.getElementById("product-id");
const messageEl = document.getElementById("message");
const warrantyIdInput = document.getElementById("warranty-id");
const startDateInput = document.getElementById("start-date");
const durationMonthsInput = document.getElementById("duration-months");
const warrantyProviderInput = document.getElementById("warranty-provider");
const warrantyStatusSelect = document.getElementById("warranty-status");
const statusGroup = document.getElementById("status-group");
const submitWarrantyButton = document.getElementById("submit-warranty-button");
const cancelEditButton = document.getElementById("cancel-edit-button");
const refreshButton = document.getElementById("refresh-button");

let productsCache = [];
let productsMap = new Map();

function showMessage(text, isError = false) {
    messageEl.textContent = text;
    messageEl.className = `message ${isError ? "error" : "success"} visible`;
}

function getStatusBadge(status) {
    switch (status) {
        case "ACTIVE":
            return '<span class="warranty-status-chip warranty-status-chip--active">ACTIVE</span>';
        case "EXPIRING":
            return '<span class="warranty-status-chip warranty-status-chip--expiring">EXPIRING</span>';
        case "EXPIRED":
            return '<span class="warranty-status-chip warranty-status-chip--expired">EXPIRED</span>';
        case "VOID":
            return '<span class="warranty-status-chip warranty-status-chip--void">VOID</span>';
        default:
            return `<span class="warranty-status-chip" style="background:var(--color-gray-100);color:var(--color-gray-600);">${status || "UNKNOWN"}</span>`;
    }
}

async function loadWarrantyOverview() {
    try {
        const response = await fetch(`${API_BASE}/dashboard/warranty-overview`, {
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getApiErrorMessage(response));
        const data = await response.json();
        renderWarrantyOverview(data);
    } catch (error) {
        const container = document.getElementById("warranty-overview-content");
        if (container) {
            container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">⚠️</div><div class="empty-state-title">Could not load warranty overview</div><div class="empty-state-text">Warranty information is temporarily unavailable.</div></div>';
        }
        console.error("Warranty overview error:", error);
    }
}

function renderWarrantyOverview(data) {
    const container = document.getElementById("warranty-overview-content");
    if (!container) return;
    if (!data || !data.items || data.items.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">🛡️</div><div class="empty-state-title">No warranty data available</div></div>';
        return;
    }

    const expiringSoon = data.items.filter(item => item.status === "ACTIVE" && item.daysRemaining != null && item.daysRemaining >= 0 && item.daysRemaining <= 30);
    const expired = data.items.filter(item => item.status === "EXPIRED" || (item.daysRemaining != null && item.daysRemaining < 0));
    const voidPolicies = data.items.filter(item => item.status === "VOID");

    let html = "";
    if (expiringSoon.length > 0) {
        html += `<h3 class="card-title" style="margin-bottom:var(--spacing-sm);">Expiring Soon</h3>`;
        html += renderOverviewTable(expiringSoon);
    }
    if (expired.length > 0) {
        html += `<h3 class="card-title" style="margin-top:var(--spacing-md);margin-bottom:var(--spacing-sm);">Expired</h3>`;
        html += renderOverviewTable(expired);
    }
    if (voidPolicies.length > 0) {
        html += `<h3 class="card-title" style="margin-top:var(--spacing-md);margin-bottom:var(--spacing-sm);">Void Policies</h3>`;
        html += renderOverviewTable(voidPolicies);
    }
    if (!html) {
        html = '<div class="empty-state"><div class="empty-state-icon">✅</div><div class="empty-state-title">No warranty actions required</div></div>';
    }
    container.innerHTML = html;
}

function renderOverviewTable(items) {
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

async function getErrorMessage(response) {
    const status = response.status;
    if (status === 401) return "Please sign in again.";
    if (status === 403) return "You don't have permission to perform this action.";
    if (status === 404) return "The requested information could not be found.";
    if (status === 409) return "This information already exists.";
    return "Something went wrong. Please try again.";
}

async function loadProducts() {
    try {
        const response = await fetch(productsApiUrl, {
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));
        productsCache = await response.json();
        productsMap.clear();

        productSelect.innerHTML = '<option value="">-- Select a product --</option>';
        productsCache.forEach(product => {
            productsMap.set(product.id, product);
            const option = document.createElement("option");
            option.value = product.id;
            const details = [product.brand, product.model].filter(Boolean).join(" ");
            option.textContent = details ? `${product.name} (${details})` : product.name;
            productSelect.appendChild(option);
        });
    } catch (error) {
        showMessage("Could not load products for selection.", true);
        console.error("Warranty products load error:", error);
    }
}

function getProductDisplayName(productId) {
    const product = productsMap.get(productId);
    if (!product) return `Product #${productId}`;
    const brandModel = [product.brand, product.model].filter(Boolean).join(" ");
    return brandModel ? `${product.name} (${brandModel})` : product.name;
}

function showTableLoading() {
    warrantyTableBody.innerHTML = `
        <tr>
            <td colspan="7">
                <div class="loading-state">
                    <div class="loading-state-spinner"></div>
                    <div class="loading-state-title">Loading warranties…</div>
                </div>
            </td>
        </tr>
    `;
}

function showTableEmpty() {
    warrantyTableBody.innerHTML = `
        <tr>
            <td colspan="7">
                <div class="empty-state">
                    <div class="empty-state-icon">🛡️</div>
                    <div class="empty-state-title">No warranties recorded yet</div>
                    <div class="empty-state-text">Add your first warranty using the form above.</div>
                </div>
            </td>
        </tr>
    `;
}

async function loadWarranties() {
    try {
        showTableLoading();
        const response = await fetch(warrantiesApiUrl, {
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));
        const warranties = await response.json();
        renderWarranties(warranties);
    } catch (error) {
        showMessage("Could not load warranties.", true);
        showTableEmpty();
        console.error("Warranties load error:", error);
    }
}

function renderWarranties(warranties) {
    warrantyTableBody.innerHTML = "";
    if (!warranties || warranties.length === 0) {
        showTableEmpty();
        return;
    }

    warranties.forEach(warranty => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td style="width:48px"></td>
            <td></td>
            <td></td>
            <td class="text-date"></td>
            <td></td>
            <td class="text-date"></td>
            <td></td>
            <td style="width:140px">
                <button type="button" class="btn btn-sm btn-primary edit">Edit</button>
                <button type="button" class="btn btn-sm btn-danger delete">Delete</button>
            </td>
        `;

        const cells = row.querySelectorAll("td");
        cells[0].innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`;
        cells[0].style.cssText = "width:48px;text-align:center;color:var(--color-text-tertiary)";
        cells[1].textContent = getProductDisplayName(warranty.productId);
        cells[2].textContent = warranty.warrantyProvider;
        cells[3].textContent = warranty.startDate;
        cells[3].className = "text-date";
        cells[4].textContent = `${warranty.durationMonths} ${warranty.durationMonths === 1 ? "month" : "months"}`;
        cells[5].textContent = warranty.expiryDate || "-";
        cells[5].className = "text-date";
        cells[6].innerHTML = getStatusBadge(warranty.status);

        row.querySelector(".edit").addEventListener("click", () => fillFormForEdit(warranty));
        row.querySelector(".delete").addEventListener("click", () => deleteWarranty(warranty.id, getProductDisplayName(warranty.productId)));

        warrantyTableBody.appendChild(row);
    });
}

function getWarrantyFromForm() {
    const payload = {
        productId: parseInt(productSelect.value, 10),
        startDate: startDateInput.value,
        durationMonths: parseInt(durationMonthsInput.value, 10),
        warrantyProvider: warrantyProviderInput.value.trim()
    };

    if (warrantyIdInput.value && warrantyStatusSelect.value) {
        payload.status = warrantyStatusSelect.value;
    }

    return payload;
}

function fillFormForEdit(warranty) {
    warrantyIdInput.value = warranty.id;
    productSelect.value = warranty.productId;
    startDateInput.value = warranty.startDate;
    durationMonthsInput.value = warranty.durationMonths;
    warrantyProviderInput.value = warranty.warrantyProvider;
    warrantyStatusSelect.value = warranty.status || "ACTIVE";
    statusGroup.style.display = "block";
    submitWarrantyButton.textContent = "Update warranty";
    showMessage(`Editing warranty #${warranty.id} for "${getProductDisplayName(warranty.productId)}".`);
    document.getElementById("warranty-form").scrollIntoView({ behavior: "smooth", block: "start" });
}

function resetForm() {
    warrantyForm.reset();
    warrantyIdInput.value = "";
    statusGroup.style.display = "none";
    submitWarrantyButton.textContent = "Save warranty";
}

warrantyForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const duration = parseInt(durationMonthsInput.value, 10);
    if (!duration || duration <= 0) {
        showMessage("Duration must be greater than zero.", true);
        return;
    }

    const id = warrantyIdInput.value;
    const method = id ? "PUT" : "POST";
    const url = id ? `${warrantiesApiUrl}/${id}` : warrantiesApiUrl;

    try {
        const response = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(getWarrantyFromForm()),
            credentials: "include"
        });

        if (!response.ok) throw new Error(await getErrorMessage(response));

        const savedWarranty = await response.json();
        resetForm();
        showMessage(`Warranty for "${getProductDisplayName(savedWarranty.productId)}" ${id ? "updated" : "saved"} successfully.`);
        loadWarranties();
    } catch (error) {
        showMessage("Could not save the warranty. Please try again.", true);
        console.error("Warranty save error:", error);
    }
});

async function deleteWarranty(id, productName) {
    if (!window.confirm(`Delete warranty for "${productName}"?`)) return;

    try {
        const response = await fetch(`${warrantiesApiUrl}/${id}`, {
            method: "DELETE",
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));

        showMessage(`Warranty for "${productName}" deleted successfully.`);
        if (warrantyIdInput.value === String(id)) {
            resetForm();
        }
        loadWarranties();
    } catch (error) {
        showMessage("Could not delete the warranty. Please try again.", true);
        console.error("Warranty delete error:", error);
    }
}

refreshButton.addEventListener("click", async () => {
    await loadProducts();
    await loadWarranties();
    await loadWarrantyOverview();
});

cancelEditButton.addEventListener("click", resetForm);

async function init() {
    await loadProducts();
    await loadWarranties();
    await loadWarrantyOverview();
}

init();
