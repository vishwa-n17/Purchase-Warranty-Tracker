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
    const error = await response.json().catch(() => null);
    return error?.message || "Something went wrong. Please try again.";
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
        cells[1].textContent = product.name;
        cells[2].textContent = product.category;
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

    html += `<div class="stats-grid">
        <div class="stat-card">
            <div class="stat-card-label">Purchases</div>
            <div class="stat-card-value">${Number(lifecycle.purchaseCount || 0).toLocaleString("en-IN")}</div>
        </div>
        <div class="stat-card card-purple">
            <div class="stat-card-label">Total Spend</div>
            <div class="stat-card-value">${formatCurrency(lifecycle.totalSpend || 0)}</div>
        </div>
        <div class="stat-card card-green">
            <div class="stat-card-label">Active Warranties</div>
            <div class="stat-card-value">${Number(lifecycle.activeWarrantyCount || 0).toLocaleString("en-IN")}</div>
        </div>
        <div class="stat-card card-info">
            <div class="stat-card-label">Service Records</div>
            <div class="stat-card-value">${Number(lifecycle.serviceRecordCount || 0).toLocaleString("en-IN")}</div>
        </div>
    </div>`;

    html += `<div class="stats-grid" style="margin-top:var(--spacing-lg);">
        <div class="stat-card">
            <div class="stat-card-label">Purchase Cost</div>
            <div class="stat-card-value">${formatCurrency(ownershipCost?.purchaseCost || 0)}</div>
        </div>
        <div class="stat-card card-info">
            <div class="stat-card-label">Service Cost</div>
            <div class="stat-card-value">${formatCurrency(ownershipCost?.serviceCost || 0)}</div>
        </div>
        <div class="stat-card card-purple">
            <div class="stat-card-label">Total Ownership Cost</div>
            <div class="stat-card-value">${formatCurrency(ownershipCost?.totalOwnershipCost || 0)}</div>
        </div>
    </div>`;

    if (warrantyIntel) {
        html += `<div class="stats-grid" style="margin-top:var(--spacing-lg);">
            <div class="stat-card">
                <div class="stat-card-label">Warranty Coverage</div>
                <div class="stat-card-value">${warrantyIntel.activeWarrantyCount || 0} active</div>
            </div>
            <div class="stat-card card-green">
                <div class="stat-card-label">Days Remaining</div>
                <div class="stat-card-value">${warrantyIntel.daysRemaining != null ? warrantyIntel.daysRemaining : "-"}</div>
            </div>
            <div class="stat-card card-purple">
                <div class="stat-card-label">Claim Readiness</div>
                <div class="stat-card-value">${warrantyIntel.claimReadinessScore != null ? warrantyIntel.claimReadinessScore + "%" : "-"}</div>
            </div>
        </div>`;
        if (warrantyIntel.missingDocuments && warrantyIntel.missingDocuments.length > 0) {
            html += `<div class="card" style="margin-top:var(--spacing-md);">
                <div class="card-header"><h3 class="card-title">Missing Documents</h3></div>
                <div class="card-body">
                    <ul>${warrantyIntel.missingDocuments.map(doc => `<li>${doc}</li>`).join("")}</ul>
                </div>
            </div>`;
        }
    }

    if (health) {
        html += `<div class="stats-grid" style="margin-top:var(--spacing-lg);">
            <div class="stat-card">
                <div class="stat-card-label">Health Score</div>
                <div class="stat-card-value">${health.healthScore != null ? health.healthScore + "/100" : "-"}</div>
            </div>
            <div class="stat-card card-green">
                <div class="stat-card-label">Status</div>
                <div class="stat-card-value">${getStatusBadge(health.status)}</div>
            </div>
            <div class="stat-card card-purple">
                <div class="stat-card-label">Warranty Status</div>
                <div class="stat-card-value">${health.warrantyStatus || "-"}</div>
            </div>
        </div>`;
        if (health.alerts && health.alerts.length > 0) {
            html += `<div class="card" style="margin-top:var(--spacing-md);">
                <div class="card-header"><h3 class="card-title">Alerts</h3></div>
                <div class="card-body">
                    <ul>${health.alerts.map(alert => `<li>${alert}</li>`).join("")}</ul>
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
