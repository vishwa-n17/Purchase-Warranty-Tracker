const purchasesApiUrl = `${API_BASE}/purchases`;
const productsApiUrl = `${API_BASE}/products`;

const purchaseForm = document.getElementById("purchase-form");
const purchaseTableBody = document.getElementById("purchase-table-body");
const productSelect = document.getElementById("product-id");
const messageEl = document.getElementById("message");

const receiptSection = document.getElementById("receipt-section");
const receiptForm = document.getElementById("receipt-form");
const receiptMessageEl = document.getElementById("receipt-message");
const receiptPurchaseInfo = document.getElementById("receipt-purchase-info");
const receiptPurchaseIdInput = document.getElementById("receipt-purchase-id");
const receiptFilePathInput = document.getElementById("receipt-file-path");
const receiptDateInput = document.getElementById("receipt-date");
const saveReceiptButton = document.getElementById("save-receipt-button");
const deleteReceiptButton = document.getElementById("delete-receipt-button");
const closeReceiptButton = document.getElementById("close-receipt-button");

let productsCache = [];
let productsMap = new Map();
let currentReceiptExists = false;

function showMessage(text, isError = false) {
    messageEl.textContent = text;
    messageEl.className = `message ${isError ? "error" : "success"} visible`;
}

function showReceiptMessage(text, isError = false) {
    receiptMessageEl.textContent = text;
    receiptMessageEl.className = `message ${isError ? "error" : "success"} visible`;
}

function getPaymentBadge(method) {
    const map = {
        "UPI": "badge-upi",
        "CARD": "badge-card",
        "CASH": "badge-cash",
        "BANK_TRANSFER": "badge-transfer",
        "OTHER": "badge-other"
    };
    const cls = map[method] || "badge-secondary";
    const label = method === "BANK_TRANSFER" ? "BANK TRANSFER" : (method || "N/A");
    return `<span class="badge ${cls}">${label}</span>`;
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
        console.error("Purchases products load error:", error);
    }
}

function getPurchaseFromForm() {
    return {
        productId: parseInt(productSelect.value, 10),
        purchaseDate: document.getElementById("purchase-date").value,
        purchasePrice: parseFloat(document.getElementById("purchase-price").value),
        storeName: document.getElementById("store-name").value.trim(),
        paymentMethod: document.getElementById("payment-method").value
    };
}

function showTableLoading() {
    purchaseTableBody.innerHTML = `
        <tr>
            <td colspan="7">
                <div class="loading-state">
                    <div class="loading-state-spinner"></div>
                    <div class="loading-state-title">Loading purchases…</div>
                </div>
            </td>
        </tr>
    `;
}

function showTableEmpty() {
    purchaseTableBody.innerHTML = `
        <tr>
            <td colspan="7">
                <div class="empty-state">
                    <div class="empty-state-icon">🛒</div>
                    <div class="empty-state-title">No purchases recorded yet</div>
                    <div class="empty-state-text">Record your first purchase using the form above.</div>
                </div>
            </td>
        </tr>
    `;
}

async function loadPurchases() {
    try {
        showTableLoading();
        const response = await fetch(purchasesApiUrl, {
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));
        const purchases = await response.json();
        renderPurchases(purchases);
    } catch (error) {
        showMessage("Could not load purchases.", true);
        showTableEmpty();
        console.error("Purchases load error:", error);
    }
}

function getProductDisplayName(productId) {
    const product = productsMap.get(productId);
    if (!product) return `Product #${productId}`;
    const brandModel = [product.brand, product.model].filter(Boolean).join(" ");
    return brandModel ? `${product.name} (${brandModel})` : product.name;
}

function renderPurchases(purchases) {
    purchaseTableBody.innerHTML = "";
    if (!purchases || purchases.length === 0) {
        showTableEmpty();
        return;
    }

    purchases.forEach(purchase => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td style="width:48px"></td>
            <td></td>
            <td class="text-date"></td>
            <td class="text-currency"></td>
            <td></td>
            <td></td>
            <td>
                <button type="button" class="btn btn-sm btn-secondary receipt-btn">Receipt</button>
            </td>
            <td style="width:140px">
                <button type="button" class="btn btn-sm btn-primary edit">Edit</button>
                <button type="button" class="btn btn-sm btn-danger delete">Delete</button>
            </td>
        `;

        const cells = row.querySelectorAll("td");
        cells[0].innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>`;
        cells[0].style.cssText = "width:48px;text-align:center;color:var(--color-text-tertiary)";
        cells[1].textContent = getProductDisplayName(purchase.productId);
        cells[2].textContent = purchase.purchaseDate;
        cells[2].className = "text-date";
        cells[3].textContent = formatCurrency(purchase.purchasePrice);
        cells[3].className = "text-currency";
        cells[4].textContent = purchase.storeName;
        cells[5].innerHTML = getPaymentBadge(purchase.paymentMethod);

        row.querySelector(".receipt-btn").addEventListener("click", () => openReceiptSection(purchase));
        row.querySelector(".edit").addEventListener("click", () => fillPurchaseFormForEdit(purchase));
        row.querySelector(".delete").addEventListener("click", () => deletePurchase(purchase.id, getProductDisplayName(purchase.productId)));

        purchaseTableBody.appendChild(row);
    });
}

function fillPurchaseFormForEdit(purchase) {
    document.getElementById("purchase-id").value = purchase.id;
    productSelect.value = purchase.productId;
    document.getElementById("purchase-date").value = purchase.purchaseDate;
    document.getElementById("purchase-price").value = purchase.purchasePrice;
    document.getElementById("store-name").value = purchase.storeName;
    document.getElementById("payment-method").value = purchase.paymentMethod;
    document.getElementById("submit-purchase-button").textContent = "Update purchase";
    showMessage(`Editing purchase #${purchase.id} (${getProductDisplayName(purchase.productId)}).`);
    document.getElementById("purchase-form").scrollIntoView({ behavior: "smooth", block: "start" });
}

function resetPurchaseForm() {
    purchaseForm.reset();
    document.getElementById("purchase-id").value = "";
    document.getElementById("submit-purchase-button").textContent = "Save purchase";
}

purchaseForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const id = document.getElementById("purchase-id").value;
    const method = id ? "PUT" : "POST";
    const url = id ? `${purchasesApiUrl}/${id}` : purchasesApiUrl;

    try {
        const response = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(getPurchaseFromForm()),
            credentials: "include"
        });

        if (!response.ok) throw new Error(await getErrorMessage(response));

        resetPurchaseForm();
        showMessage(`Purchase ${id ? "updated" : "recorded"} successfully.`);
        loadPurchases();
    } catch (error) {
        showMessage("Could not save the purchase. Please try again.", true);
        console.error("Purchase save error:", error);
    }
});

async function deletePurchase(id, productName) {
    if (!window.confirm(`Delete purchase #${id} for "${productName}"? Any attached receipt will also be deleted.`)) return;
    try {
        const response = await fetch(`${purchasesApiUrl}/${id}`, {
            method: "DELETE",
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));

        showMessage("Purchase deleted successfully.");
        if (receiptPurchaseIdInput.value === String(id)) {
            closeReceiptSection();
        }
        loadPurchases();
    } catch (error) {
        showMessage("Could not delete the purchase. Please try again.", true);
        console.error("Purchase delete error:", error);
    }
}

async function openReceiptSection(purchase) {
    receiptPurchaseIdInput.value = purchase.id;
    receiptPurchaseInfo.textContent = `#${purchase.id} - ${getProductDisplayName(purchase.productId)} (Date: ${purchase.purchaseDate}, Price: ${formatCurrency(purchase.purchasePrice)})`;
    receiptMessageEl.textContent = "";
    receiptSection.style.display = "block";
    receiptSection.scrollIntoView({ behavior: "smooth", block: "start" });

    try {
        const response = await fetch(`${purchasesApiUrl}/${purchase.id}/receipt`, {
            credentials: "include"
        });
        if (response.ok) {
            const receipt = await response.json();
            currentReceiptExists = true;
            receiptFilePathInput.value = receipt.receiptFilePath;
            receiptDateInput.value = receipt.receiptDate;
            saveReceiptButton.textContent = "Update receipt";
            deleteReceiptButton.style.display = "inline-flex";
            showReceiptMessage("Receipt found for this purchase.");
        } else if (response.status === 404) {
            currentReceiptExists = false;
            receiptFilePathInput.value = "";
            receiptDateInput.value = purchase.purchaseDate || new Date().toISOString().split("T")[0];
            saveReceiptButton.textContent = "Attach receipt";
            deleteReceiptButton.style.display = "none";
            showReceiptMessage("No receipt currently attached to this purchase. You can add one below.");
        } else {
            throw new Error(await getErrorMessage(response));
        }
    } catch (error) {
        currentReceiptExists = false;
        receiptFilePathInput.value = "";
        receiptDateInput.value = purchase.purchaseDate || "";
        saveReceiptButton.textContent = "Attach receipt";
        deleteReceiptButton.style.display = "none";
        showReceiptMessage("Could not check receipt status. Please try again.", true);
        console.error("Receipt check error:", error);
    }
}

function closeReceiptSection() {
    receiptSection.style.display = "none";
    receiptForm.reset();
    receiptPurchaseIdInput.value = "";
    currentReceiptExists = false;
}

receiptForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const purchaseId = receiptPurchaseIdInput.value;
    if (!purchaseId) return;

    const payload = {
        receiptFilePath: receiptFilePathInput.value.trim(),
        receiptDate: receiptDateInput.value
    };

    const method = currentReceiptExists ? "PUT" : "POST";
    const url = `${purchasesApiUrl}/${purchaseId}/receipt`;

    try {
        const response = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
            credentials: "include"
        });

        if (!response.ok) throw new Error(await getErrorMessage(response));

        currentReceiptExists = true;
        saveReceiptButton.textContent = "Update receipt";
        deleteReceiptButton.style.display = "inline-flex";
        showReceiptMessage(`Receipt ${method === "PUT" ? "updated" : "attached"} successfully.`);
        showMessage(`Receipt saved for purchase #${purchaseId}.`);
    } catch (error) {
        showReceiptMessage("Could not save the receipt. Please try again.", true);
        console.error("Receipt save error:", error);
    }
});

deleteReceiptButton.addEventListener("click", async () => {
    const purchaseId = receiptPurchaseIdInput.value;
    if (!purchaseId) return;

    if (!window.confirm(`Delete receipt for purchase #${purchaseId}?`)) return;

    try {
        const response = await fetch(`${purchasesApiUrl}/${purchaseId}/receipt`, {
            method: "DELETE",
            credentials: "include"
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));

        currentReceiptExists = false;
        receiptFilePathInput.value = "";
        saveReceiptButton.textContent = "Attach receipt";
        deleteReceiptButton.style.display = "none";
        showReceiptMessage("Receipt deleted successfully.");
        showMessage(`Receipt deleted for purchase #${purchaseId}.`);
    } catch (error) {
        showReceiptMessage("Could not delete the receipt. Please try again.", true);
        console.error("Receipt delete error:", error);
    }
});

document.getElementById("refresh-button").addEventListener("click", () => {
    loadProducts();
    loadPurchases();
});

document.getElementById("cancel-edit-button").addEventListener("click", resetPurchaseForm);
closeReceiptButton.addEventListener("click", closeReceiptSection);

async function init() {
    await loadProducts();
    await loadPurchases();
}

init();
