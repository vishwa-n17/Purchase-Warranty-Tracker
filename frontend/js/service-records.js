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
        "REPAIR": "service-type-badge--repair",
        "MAINTENANCE": "service-type-badge--maintenance",
        "INSPECTION": "service-type-badge--inspection",
        "UPGRADE": "service-type-badge--upgrade"
    };
    const cls = map[type] || "service-type-badge--other";
    return `<span class="service-type-badge ${cls}">${type || "N/A"}</span>`;
}

function formatCurrency(amount) {
    return `₹${Number(amount).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
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
        console.error("Service records products load error:", error);
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
        const response = await fetch(serviceRecordsApiUrl, {
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));
        const records = await response.json();
        renderServiceRecords(records);
    } catch (error) {
        showMessage("Could not load service records.", true);
        showTableEmpty();
        console.error("Service records load error:", error);
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
            <td style="width:48px"></td>
            <td></td>
            <td></td>
            <td class="text-date"></td>
            <td></td>
            <td class="text-currency"></td>
            <td></td>
            <td style="width:140px">
                <button type="button" class="btn btn-sm btn-primary edit">Edit</button>
                <button type="button" class="btn btn-sm btn-danger delete">Delete</button>
            </td>
        `;

        const productId = record.product?.id || record.productId;
        const productName = record.product?.name ? record.product.name : getProductDisplayName(productId);

        const cells = row.querySelectorAll("td");
        cells[0].innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>`;
        cells[0].style.cssText = "width:48px;text-align:center;color:var(--color-text-tertiary)";
        cells[1].textContent = productName;
        cells[2].innerHTML = getServiceTypeBadge(record.serviceType);
        cells[3].textContent = record.serviceDate;
        cells[3].className = "text-date";
        cells[4].textContent = record.provider;
        cells[5].textContent = formatCurrency(record.cost);
        cells[5].className = "text-currency";
        cells[6].textContent = record.description;

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
            body: JSON.stringify(payload),
            credentials: "include"
        });

        if (!response.ok) throw new Error(await getErrorMessage(response));

        resetForm();
        showMessage(`Service record ${id ? "updated" : "saved"} successfully.`);
        loadServiceRecords();
    } catch (error) {
        showMessage("Could not save the service record. Please try again.", true);
        console.error("Service record save error:", error);
    }
});

async function deleteServiceRecord(id, productName) {
    if (!window.confirm(`Delete service record #${id} for "${productName}"?`)) return;

    try {
        const response = await fetch(`${serviceRecordsApiUrl}/${id}`, {
            method: "DELETE",
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));

        showMessage(`Service record #${id} deleted successfully.`);
        if (serviceRecordIdInput.value === String(id)) {
            resetForm();
        }
        loadServiceRecords();
    } catch (error) {
        showMessage("Could not delete the service record. Please try again.", true);
        console.error("Service record delete error:", error);
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
