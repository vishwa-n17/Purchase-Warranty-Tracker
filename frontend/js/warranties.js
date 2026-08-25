const API_BASE = "http://localhost:8080/api";
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
    messageEl.className = `message ${isError ? "error" : "success"}`;
}

async function getErrorMessage(response) {
    const error = await response.json().catch(() => null);
    return error?.message || "The request could not be completed.";
}

async function loadProducts() {
    try {
        const response = await fetch(productsApiUrl);
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
        showMessage(error.message || "Could not load products for selection.", true);
    }
}

function getProductDisplayName(productId) {
    const product = productsMap.get(productId);
    if (!product) return `Product #${productId}`;
    const brandModel = [product.brand, product.model].filter(Boolean).join(" ");
    return brandModel ? `${product.name} (${brandModel})` : product.name;
}

function getStatusBadge(status) {
    switch (status) {
        case "ACTIVE":
            return '<span class="badge badge-active">ACTIVE</span>';
        case "EXPIRED":
            return '<span class="badge badge-expired">EXPIRED</span>';
        case "VOID":
            return '<span class="badge badge-void">VOID</span>';
        default:
            return `<span class="badge">${status || "UNKNOWN"}</span>`;
    }
}

async function loadWarranties() {
    try {
        warrantyTableBody.innerHTML = '<tr><td colspan="7">Loading warranties…</td></tr>';
        const response = await fetch(warrantiesApiUrl);
        if (!response.ok) throw new Error(await getErrorMessage(response));
        const warranties = await response.json();
        renderWarranties(warranties);
    } catch (error) {
        warrantyTableBody.innerHTML = '<tr><td colspan="7">Failed to load warranties.</td></tr>';
        showMessage(error.message || "Could not load warranties.", true);
    }
}

function renderWarranties(warranties) {
    warrantyTableBody.innerHTML = "";
    if (!warranties || warranties.length === 0) {
        warrantyTableBody.innerHTML = '<tr><td colspan="7">No warranties recorded yet.</td></tr>';
        return;
    }

    warranties.forEach(warranty => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td>
                <button type="button" class="edit">Edit</button>
                <button type="button" class="delete secondary">Delete</button>
            </td>
        `;

        const cells = row.querySelectorAll("td");
        cells[0].textContent = getProductDisplayName(warranty.productId);
        cells[1].textContent = warranty.warrantyProvider;
        cells[2].textContent = warranty.startDate;
        cells[3].textContent = `${warranty.durationMonths} ${warranty.durationMonths === 1 ? "month" : "months"}`;
        cells[4].textContent = warranty.expiryDate || "-";
        cells[5].innerHTML = getStatusBadge(warranty.status);

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
    window.scrollTo({ top: 0, behavior: "smooth" });
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
            body: JSON.stringify(getWarrantyFromForm())
        });

        if (!response.ok) throw new Error(await getErrorMessage(response));

        const savedWarranty = await response.json();
        resetForm();
        showMessage(`Warranty for "${getProductDisplayName(savedWarranty.productId)}" ${id ? "updated" : "saved"} successfully.`);
        loadWarranties();
    } catch (error) {
        showMessage(error.message || "Could not save the warranty.", true);
    }
});

async function deleteWarranty(id, productName) {
    if (!window.confirm(`Delete warranty for "${productName}"?`)) return;

    try {
        const response = await fetch(`${warrantiesApiUrl}/${id}`, { method: "DELETE" });
        if (!response.ok) throw new Error(await getErrorMessage(response));

        showMessage(`Warranty for "${productName}" deleted successfully.`);
        if (warrantyIdInput.value === String(id)) {
            resetForm();
        }
        loadWarranties();
    } catch (error) {
        showMessage(error.message || "Could not delete the warranty.", true);
    }
}

refreshButton.addEventListener("click", async () => {
    await loadProducts();
    await loadWarranties();
});

cancelEditButton.addEventListener("click", resetForm);

async function init() {
    await loadProducts();
    await loadWarranties();
}

init();

