const API_BASE = "http://localhost:8080/api";
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
    messageEl.className = `message ${isError ? "error" : "success"}`;
}

function showReceiptMessage(text, isError = false) {
    receiptMessageEl.textContent = text;
    receiptMessageEl.className = `message ${isError ? "error" : "success"}`;
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

function getPurchaseFromForm() {
    return {
        productId: parseInt(productSelect.value, 10),
        purchaseDate: document.getElementById("purchase-date").value,
        purchasePrice: parseFloat(document.getElementById("purchase-price").value),
        storeName: document.getElementById("store-name").value.trim(),
        paymentMethod: document.getElementById("payment-method").value
    };
}

async function loadPurchases() {
    try {
        const response = await fetch(purchasesApiUrl);
        if (!response.ok) throw new Error(await getErrorMessage(response));
        const purchases = await response.json();
        renderPurchases(purchases);
    } catch (error) {
        showMessage(error.message || "Could not load purchases.", true);
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
        purchaseTableBody.innerHTML = '<tr><td colspan="7">No purchases saved yet.</td></tr>';
        return;
    }

    purchases.forEach(purchase => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td>
                <button type="button" class="receipt-btn secondary">Receipt</button>
            </td>
            <td>
                <button type="button" class="edit">Edit</button>
                <button type="button" class="delete secondary">Delete</button>
            </td>
        `;

        const cells = row.querySelectorAll("td");
        cells[0].textContent = getProductDisplayName(purchase.productId);
        cells[1].textContent = purchase.purchaseDate;
        cells[2].textContent = `₹${Number(purchase.purchasePrice).toFixed(2)}`;
        cells[3].textContent = purchase.storeName;
        cells[4].textContent = purchase.paymentMethod;

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
    window.scrollTo({ top: 0, behavior: "smooth" });
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
            body: JSON.stringify(getPurchaseFromForm())
        });

        if (!response.ok) throw new Error(await getErrorMessage(response));

        resetPurchaseForm();
        showMessage(`Purchase ${id ? "updated" : "recorded"} successfully.`);
        loadPurchases();
    } catch (error) {
        showMessage(error.message || "Could not save the purchase.", true);
    }
});

async function deletePurchase(id, productName) {
    if (!window.confirm(`Delete purchase #${id} for "${productName}"? Any attached receipt will also be deleted.`)) return;
    try {
        const response = await fetch(`${purchasesApiUrl}/${id}`, { method: "DELETE" });
        if (!response.ok) throw new Error(await getErrorMessage(response));

        showMessage("Purchase deleted successfully.");
        if (receiptPurchaseIdInput.value === String(id)) {
            closeReceiptSection();
        }
        loadPurchases();
    } catch (error) {
        showMessage(error.message || "Could not delete the purchase.", true);
    }
}

async function openReceiptSection(purchase) {
    receiptPurchaseIdInput.value = purchase.id;
    receiptPurchaseInfo.textContent = `#${purchase.id} - ${getProductDisplayName(purchase.productId)} (Date: ${purchase.purchaseDate}, Price: ₹${Number(purchase.purchasePrice).toFixed(2)})`;
    receiptMessageEl.textContent = "";
    receiptSection.style.display = "block";
    receiptSection.scrollIntoView({ behavior: "smooth" });

    try {
        const response = await fetch(`${purchasesApiUrl}/${purchase.id}/receipt`);
        if (response.ok) {
            const receipt = await response.json();
            currentReceiptExists = true;
            receiptFilePathInput.value = receipt.receiptFilePath;
            receiptDateInput.value = receipt.receiptDate;
            saveReceiptButton.textContent = "Update receipt";
            deleteReceiptButton.style.display = "inline-block";
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
        showReceiptMessage(error.message || "Error checking receipt status.", true);
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
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error(await getErrorMessage(response));

        currentReceiptExists = true;
        saveReceiptButton.textContent = "Update receipt";
        deleteReceiptButton.style.display = "inline-block";
        showReceiptMessage(`Receipt ${method === "PUT" ? "updated" : "attached"} successfully.`);
        showMessage(`Receipt saved for purchase #${purchaseId}.`);
    } catch (error) {
        showReceiptMessage(error.message || "Could not save the receipt.", true);
    }
});

deleteReceiptButton.addEventListener("click", async () => {
    const purchaseId = receiptPurchaseIdInput.value;
    if (!purchaseId) return;

    if (!window.confirm(`Delete receipt for purchase #${purchaseId}?`)) return;

    try {
        const response = await fetch(`${purchasesApiUrl}/${purchaseId}/receipt`, { method: "DELETE" });
        if (!response.ok) throw new Error(await getErrorMessage(response));

        currentReceiptExists = false;
        receiptFilePathInput.value = "";
        saveReceiptButton.textContent = "Attach receipt";
        deleteReceiptButton.style.display = "none";
        showReceiptMessage("Receipt deleted successfully.");
        showMessage(`Receipt deleted for purchase #${purchaseId}.`);
    } catch (error) {
        showReceiptMessage(error.message || "Could not delete the receipt.", true);
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

