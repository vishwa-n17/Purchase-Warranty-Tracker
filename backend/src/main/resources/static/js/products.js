const apiUrl = `${API_BASE}/products`;
const form = document.getElementById("product-form");
const tableBody = document.getElementById("product-table-body");
const message = document.getElementById("message");
const detailSection = document.getElementById("product-detail-section");
const detailContent = document.getElementById("product-detail-content");
const closeDetailButton = document.getElementById("close-detail-button");

function showMessage(text, isError = false) {
    message.textContent = text;
    message.className = `message ${isError ? "error" : "success"} visible`;
}

function getProductFromForm() {
    return {
        name: document.getElementById("name").value.trim(),
        category: document.getElementById("category").value.trim(),
        brand: document.getElementById("brand").value.trim() || null,
        model: document.getElementById("model").value.trim() || null,
        serialNumber: document.getElementById("serial-number").value.trim() || null,
        notes: document.getElementById("notes").value.trim() || null
    };
}

async function getErrorMessage(response) {
    const status = response.status;
    if (status === 401) return "Please sign in again.";
    if (status === 403) return "You don't have permission to perform this action.";
    if (status === 404) return "The requested information could not be found.";
    if (status === 409) return "This information already exists.";
    return "Something went wrong. Please try again.";
}

function showTableLoading() {
    tableBody.innerHTML = `
        <tr>
            <td colspan="5">
                <div class="loading-state">
                    <div class="loading-state-spinner"></div>
                    <div class="loading-state-title">Loading products…</div>
                </div>
            </td>
        </tr>
    `;
}

function showTableEmpty() {
    tableBody.innerHTML = `
        <tr>
            <td colspan="5">
                <div class="empty-state">
                    <div class="empty-state-icon">📦</div>
                    <div class="empty-state-title">No products yet</div>
                    <div class="empty-state-text">Add your first product using the form above.</div>
                </div>
            </td>
        </tr>
    `;
}

async function loadProducts() {
    try {
        showTableLoading();
        const response = await fetch(apiUrl, {
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));
        const products = await response.json();
        renderProducts(products);
    } catch (error) {
        showMessage("Could not load products.", true);
        showTableEmpty();
        console.error("Products load error:", error);
    }
}

function renderProducts(products) {
    tableBody.innerHTML = "";
    if (!products || products.length === 0) {
        showTableEmpty();
        return;
    }

    products.forEach((product) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td style="width:48px"></td>
            <td></td><td></td><td></td><td></td>
            <td style="width:180px">
                <button type="button" class="btn btn-sm btn-secondary view">View</button>
                <button type="button" class="btn btn-sm btn-primary edit">Edit</button>
                <button type="button" class="btn btn-sm btn-danger delete">Delete</button>
            </td>
        `;
        const cells = row.querySelectorAll("td");
        cells[0].innerHTML = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>`;
        cells[0].style.cssText = "width:48px;text-align:center;color:var(--color-text-tertiary)";
        cells[1].innerHTML = `<strong>${product.name}</strong>`;
        cells[2].innerHTML = product.category ? `<span class="badge badge-secondary">${product.category}</span>` : "-";
        cells[3].textContent = [product.brand, product.model].filter(Boolean).join(" / ") || "-";
        cells[4].textContent = product.serialNumber || "-";

        row.querySelector(".edit").addEventListener("click", () => fillFormForEdit(product));
        row.querySelector(".delete").addEventListener("click", () => deleteProduct(product.id, product.name));
        row.querySelector(".view").addEventListener("click", () => loadProductIntelligence(product.id));
        tableBody.appendChild(row);
    });
}

function fillFormForEdit(product) {
    document.getElementById("product-id").value = product.id;
    document.getElementById("name").value = product.name;
    document.getElementById("category").value = product.category;
    document.getElementById("brand").value = product.brand || "";
    document.getElementById("model").value = product.model || "";
    document.getElementById("serial-number").value = product.serialNumber || "";
    document.getElementById("notes").value = product.notes || "";
    showMessage(`Editing ${product.name}.`);
    document.getElementById("name").scrollIntoView({ behavior: "smooth", block: "center" });
}

function resetForm() {
    form.reset();
    document.getElementById("product-id").value = "";
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const id = document.getElementById("product-id").value;
    const method = id ? "PUT" : "POST";
    const url = id ? `${apiUrl}/${id}` : apiUrl;
    try {
        const response = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(getProductFromForm()),
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));
        resetForm();
        showMessage(`Product ${id ? "updated" : "created"} successfully.`);
        loadProducts();
    } catch (error) {
        showMessage("Could not save the product. Please try again.", true);
        console.error("Product save error:", error);
    }
});

async function deleteProduct(id, name) {
    if (!window.confirm(`Delete ${name}?`)) return;
    try {
        const response = await fetch(`${apiUrl}/${id}`, {
            method: "DELETE",
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));
        showMessage("Product deleted successfully.");
        loadProducts();
    } catch (error) {
        showMessage("Could not delete the product. Please try again.", true);
        console.error("Product delete error:", error);
    }
}

function showDetailLoading() {
    detailContent.innerHTML = `
        <div class="loading-state">
            <div class="loading-state-spinner"></div>
            <div class="loading-state-title">Loading product intelligence…</div>
        </div>
    `;
}

function hideDetail() {
    detailSection.classList.add("hidden");
    detailContent.innerHTML = "";
}

function formatCurrency(amount) {
    return `₹${Number(amount).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
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

async function loadProductIntelligence(productId) {
    showDetailLoading();
    detailSection.classList.remove("hidden");
    try {
        const [lifecycle, warrantyIntel, ownershipCost, health, timeline] = await Promise.all([
            getLifecycle(productId),
            getWarrantyIntelligence(productId),
            getOwnershipCost(productId),
            getProductHealth(productId),
            getTimeline(productId)
        ]);
        renderProductIntelligence(lifecycle, warrantyIntel, ownershipCost, health, timeline);
    } catch (error) {
        detailContent.innerHTML = `<div class="empty-state"><div class="empty-state-icon">⚠️</div><div class="empty-state-title">Could not load product intelligence</div><div class="empty-state-text">Something went wrong. Please try again.</div></div>`;
        console.error("Product intelligence error:", error);
    }
}

function renderProductIntelligence(lifecycle, warrantyIntel, ownershipCost, health, timeline) {
    let html = "";

    html += `<div class="intelligence-grid">
        <div class="intelligence-card">
            <div class="intelligence-card-header">
                <div class="intelligence-card-icon" style="background:var(--color-primary-light);color:var(--color-primary);">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
                </div>
                <div class="intelligence-card-title">Lifecycle</div>
            </div>
            <div class="intelligence-card-body">
                <div class="intelligence-metric">
                    <span class="intelligence-metric-value">${Number(lifecycle.purchaseCount || 0).toLocaleString("en-IN")}</span>
                    <span class="intelligence-metric-label">Purchases</span>
                </div>
                <div class="intelligence-metric">
                    <span class="intelligence-metric-value">${Number(lifecycle.activeWarrantyCount || 0).toLocaleString("en-IN")}</span>
                    <span class="intelligence-metric-label">Active Warranties</span>
                </div>
                <div class="intelligence-metric">
                    <span class="intelligence-metric-value">${Number(lifecycle.serviceRecordCount || 0).toLocaleString("en-IN")}</span>
                    <span class="intelligence-metric-label">Service Records</span>
                </div>
            </div>
        </div>

        <div class="intelligence-card">
            <div class="intelligence-card-header">
                <div class="intelligence-card-icon" style="background:var(--color-success-light);color:var(--color-success);">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                </div>
                <div class="intelligence-card-title">Ownership Cost</div>
            </div>
            <div class="intelligence-card-body">
                <div class="intelligence-metric">
                    <span class="intelligence-metric-value">${formatCurrency(ownershipCost?.purchaseCost || 0)}</span>
                    <span class="intelligence-metric-label">Purchase Cost</span>
                </div>
                <div class="intelligence-metric">
                    <span class="intelligence-metric-value">${formatCurrency(ownershipCost?.serviceCost || 0)}</span>
                    <span class="intelligence-metric-label">Service Cost</span>
                </div>
                <div class="intelligence-metric">
                    <span class="intelligence-metric-value" style="color:var(--color-primary);">${formatCurrency(ownershipCost?.totalOwnershipCost || 0)}</span>
                    <span class="intelligence-metric-label">Total Ownership Cost</span>
                </div>
            </div>
        </div>

        <div class="intelligence-card">
            <div class="intelligence-card-header">
                <div class="intelligence-card-icon" style="background:var(--color-purple-light);color:var(--color-purple);">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                </div>
                <div class="intelligence-card-title">Warranty Intelligence</div>
            </div>
            <div class="intelligence-card-body">
                <div class="intelligence-metric">
                    <span class="intelligence-metric-value">${warrantyIntel?.activeWarrantyCount || 0}</span>
                    <span class="intelligence-metric-label">Active Warranties</span>
                </div>
                <div class="intelligence-metric">
                    <span class="intelligence-metric-value">${warrantyIntel?.daysRemaining != null ? warrantyIntel.daysRemaining : "-"}</span>
                    <span class="intelligence-metric-label">Days Remaining</span>
                </div>
                <div class="intelligence-metric">
                    <span class="intelligence-metric-value">${warrantyIntel?.claimReadinessScore != null ? warrantyIntel.claimReadinessScore + "%" : "-"}</span>
                    <span class="intelligence-metric-label">Claim Readiness</span>
                </div>
            </div>
        </div>
    </div>`;

    if (warrantyIntel && warrantyIntel.missingDocuments && warrantyIntel.missingDocuments.length > 0) {
        html += `<div class="card" style="margin-top:var(--spacing-lg);">
            <div class="card-header card-header--bordered">
                <div class="card-header-left">
                    <div class="card-header-icon card-header-icon--secondary">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
                    </div>
                    <div>
                        <h3 class="card-title">Missing Documents</h3>
                        <p class="card-header-subtitle">Required for warranty claims</p>
                    </div>
                </div>
            </div>
            <div class="card-body">
                <ul style="padding-left: var(--spacing-lg); color: var(--color-text-secondary); line-height: 1.8;">${warrantyIntel.missingDocuments.map(doc => `<li>${doc}</li>`).join("")}</ul>
            </div>
        </div>`;
    }

    if (health) {
        html += `<div class="intelligence-grid" style="margin-top:var(--spacing-lg);">
            <div class="intelligence-card">
                <div class="intelligence-card-header">
                    <div class="intelligence-card-icon" style="background:var(--color-warning-light);color:var(--color-warning);">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
                    </div>
                    <div class="intelligence-card-title">Product Health</div>
                </div>
                <div class="intelligence-card-body">
                    <div class="intelligence-metric">
                        <span class="intelligence-metric-value">${health.healthScore != null ? health.healthScore + "/100" : "-"}</span>
                        <span class="intelligence-metric-label">Health Score</span>
                    </div>
                    <div class="intelligence-metric">
                        <span class="intelligence-metric-value">${health.status || "-"}</span>
                        <span class="intelligence-metric-label">Status</span>
                    </div>
                    <div class="intelligence-metric">
                        <span class="intelligence-metric-value">${health.warrantyStatus || "-"}</span>
                        <span class="intelligence-metric-label">Warranty Status</span>
                    </div>
                </div>
            </div>
        </div>`;
        if (health.alerts && health.alerts.length > 0) {
            html += `<div class="card" style="margin-top:var(--spacing-lg);">
                <div class="card-header card-header--bordered">
                    <div class="card-header-left">
                        <div class="card-header-icon card-header-icon--secondary">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                        </div>
                        <div>
                            <h3 class="card-title">Alerts</h3>
                            <p class="card-header-subtitle">Health and warranty notices</p>
                        </div>
                    </div>
                </div>
                <div class="card-body">
                    <ul style="padding-left: var(--spacing-lg); color: var(--color-text-secondary); line-height: 1.8;">${health.alerts.map(alert => `<li>${alert}</li>`).join("")}</ul>
                </div>
            </div>`;
        }
    }

    if (timeline && timeline.length > 0) {
        html += `<div class="card" style="margin-top:var(--spacing-lg);">
            <div class="card-header card-header--bordered">
                <div class="card-header-left">
                    <div class="card-header-icon card-header-icon--secondary">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
                    </div>
                    <div>
                        <h3 class="card-title">Timeline</h3>
                        <p class="card-header-subtitle">Chronological history</p>
                    </div>
                </div>
            </div>
            <div class="card-body">
                <div class="timeline">
                    ${timeline.map(event => `
                        <div class="timeline-item">
                            <div class="timeline-marker ${event.type === 'PURCHASE' ? 'timeline-marker--purchase' : event.type === 'WARRANTY' ? 'timeline-marker--warranty' : event.type === 'SERVICE' ? 'timeline-marker--service' : 'timeline-marker--default'}"></div>
                            <div class="timeline-content">
                                <div class="timeline-date">${event.date || "-"}</div>
                                <div class="timeline-title">${event.title || "Event"}</div>
                                <div class="timeline-text">${event.description || ""}</div>
                            </div>
                        </div>
                    `).join("")}
                </div>
            </div>
        </div>`;
    }

    detailContent.innerHTML = html;
}

if (closeDetailButton) {
    closeDetailButton.addEventListener("click", hideDetail);
}

document.getElementById("refresh-button").addEventListener("click", loadProducts);
document.getElementById("cancel-edit-button").addEventListener("click", resetForm);
loadProducts();
