const API_BASE = "http://localhost:8080/api";
const serviceRecordsApiUrl = `${API_BASE}/service-records`;
const productsApiUrl = `${API_BASE}/products`;

const serviceForm = document.getElementById("service-record-form");
const serviceTableBody = document.getElementById("service-table-body");
const productSelect = document.getElementById("product-id");
const messageEl = document.getElementById("message");
const serviceRecordIdInput = document.getElementById("service-record-id");
const serviceDateInput = document.getElementById("service-date");
const serviceTypeSelect = document.getElementById("service-type");
const providerInput = document.getElementById("provider");
const costInput = document.getElementById("cost");
const descriptionInput = document.getElementById("description");
const submitServiceButton = document.getElementById("submit-service-button");
const cancelEditButton = document.getElementById("cancel-edit-button");
const refreshButton = document.getElementById("refresh-button");

let productsCache = [];
let productsMap = new Map();

function showMessage(text, isError = false) {
    messageEl.textContent = text;
    messageEl.className = `message ${isError ? "error" : "success"} visible`;
}

function getServiceTypeBadge(type) {
    const map = {
        "REPAIR": "badge-repair",
        "MAINTENANCE": "badge-maintenance",
        "INSPECTION": "badge-inspection",
        "UPGRADE": "badge-upgrade"
    };
    const cls = map[type] || "badge-secondary";
    return `<span class="badge ${cls}">${type || "N/A"}</span>`;
}

function formatCurrency(amount) {
    return `₹${Number(amount).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
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

function showTableLoading() {
    serviceTableBody.innerHTML = `
        <tr>
            <td colspan="7">
                <div class="loading-state">
                    <div class="loading-state-spinner"></div>
                    <div class="loading-state-title">Loading service records…</div>
                </div>
            </td>
        </tr>
    `;
}

function showTableEmpty() {
    serviceTableBody.innerHTML = `
        <tr>
            <td colspan="7">
                <div class="empty-state">
                    <div class="empty-state-icon">🔧</div>
                    <div class="empty-state-title">No service records yet</div>
                    <div class="empty-state-text">Log your first service record using the form above.</div>
                </div>
            </td>
        </tr>
    `;
}

async function loadServiceRecords() {
    try {
        showTableLoading();
        const response = await fetch(serviceRecordsApiUrl);
        if (!response.ok) throw new Error(await getErrorMessage(response));
        const records = await response.json();
        renderServiceRecords(records);
    } catch (error) {
        showMessage(error.message || "Could not load service records.", true);
        showTableEmpty();
    }
}

function renderServiceRecords(records) {
    serviceTableBody.innerHTML = "";
    if (!records || records.length === 0) {
        showTableEmpty();
        return;
    }

    records.forEach(record => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td></td>
            <td></td>
            <td class="text-date"></td>
            <td></td>
            <td class="text-currency"></td>
            <td></td>
            <td>
                <button type="button" class="btn btn-sm btn-primary edit">Edit</button>
                <button type="button" class="btn btn-sm btn-danger delete">Delete</button>
            </td>
        `;

        const productId = record.product?.id || record.productId;
        const productName = record.product?.name ? record.product.name : getProductDisplayName(productId);

        const cells = row.querySelectorAll("td");
        cells[0].textContent = productName;
        cells[1].innerHTML = getServiceTypeBadge(record.serviceType);
        cells[2].textContent = record.serviceDate;
        cells[2].className = "text-date";
        cells[3].textContent = record.provider;
        cells[4].textContent = formatCurrency(record.cost);
        cells[4].className = "text-currency";
        cells[5].textContent = record.description;

        row.querySelector(".edit").addEventListener("click", () => fillFormForEdit(record));
        row.querySelector(".delete").addEventListener("click", () => deleteServiceRecord(record.id, productName));

        serviceTableBody.appendChild(row);
    });
}

function fillFormForEdit(record) {
    serviceRecordIdInput.value = record.id;
    productSelect.value = record.product?.id || record.productId || "";
    serviceDateInput.value = record.serviceDate;
    serviceTypeSelect.value = record.serviceType || "REPAIR";
    providerInput.value = record.provider;
    costInput.value = record.cost;
    descriptionInput.value = record.description;
    submitServiceButton.textContent = "Update service record";
    const productName = record.product?.name || getProductDisplayName(record.product?.id || record.productId);
    showMessage(`Editing service record #${record.id} for "${productName}".`);
    document.getElementById("service-record-form").scrollIntoView({ behavior: "smooth", block: "start" });
}

function resetForm() {
    serviceForm.reset();
    serviceRecordIdInput.value = "";
    submitServiceButton.textContent = "Save service record";
}

serviceForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const cost = parseFloat(costInput.value);
    if (isNaN(cost) || cost < 0) {
        showMessage("Cost cannot be negative.", true);
        return;
    }

    const payload = {
        product: { id: parseInt(productSelect.value, 10) },
        serviceDate: serviceDateInput.value,
        serviceType: serviceTypeSelect.value,
        provider: providerInput.value.trim(),
        cost: cost,
        description: descriptionInput.value.trim()
    };

    const id = serviceRecordIdInput.value;
    const method = id ? "PUT" : "POST";
    const url = id ? `${serviceRecordsApiUrl}/${id}` : serviceRecordsApiUrl;

    try {
        const response = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error(await getErrorMessage(response));

        resetForm();
        showMessage(`Service record ${id ? "updated" : "saved"} successfully.`);
        loadServiceRecords();
    } catch (error) {
        showMessage(error.message || "Could not save the service record.", true);
    }
});

async function deleteServiceRecord(id, productName) {
    if (!window.confirm(`Delete service record #${id} for "${productName}"?`)) return;

    try {
        const response = await fetch(`${serviceRecordsApiUrl}/${id}`, { method: "DELETE" });
        if (!response.ok) throw new Error(await getErrorMessage(response));

        showMessage(`Service record #${id} deleted successfully.`);
        if (serviceRecordIdInput.value === String(id)) {
            resetForm();
        }
        loadServiceRecords();
    } catch (error) {
        showMessage(error.message || "Could not delete the service record.", true);
    }
}

refreshButton.addEventListener("click", async () => {
    await loadProducts();
    await loadServiceRecords();
});

cancelEditButton.addEventListener("click", resetForm);

async function init() {
    await loadProducts();
    await loadServiceRecords();
}

init();
