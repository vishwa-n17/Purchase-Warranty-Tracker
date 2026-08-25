const apiUrl = "http://localhost:8080/api/products";
const form = document.getElementById("product-form");
const tableBody = document.getElementById("product-table-body");
const message = document.getElementById("message");

function showMessage(text, isError = false) {
    message.textContent = text;
    message.className = `message ${isError ? "error" : "success"}`;
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
    return error?.message || "The request could not be completed.";
}

async function loadProducts() {
    try {
        const response = await fetch(apiUrl);
        if (!response.ok) throw new Error(await getErrorMessage(response));
        renderProducts(await response.json());
    } catch (error) {
        showMessage(error.message || "Could not load products.", true);
    }
}

function renderProducts(products) {
    tableBody.innerHTML = "";
    if (products.length === 0) {
        tableBody.innerHTML = "<tr><td colspan=\"5\">No products saved yet.</td></tr>";
        return;
    }
    products.forEach((product) => {
        const row = document.createElement("tr");
        row.innerHTML = `<td></td><td></td><td></td><td></td><td><button type="button" class="edit">Edit</button><button type="button" class="delete secondary">Delete</button></td>`;
        const cells = row.querySelectorAll("td");
        cells[0].textContent = product.name;
        cells[1].textContent = product.category;
        cells[2].textContent = [product.brand, product.model].filter(Boolean).join(" / ") || "-";
        cells[3].textContent = product.serialNumber || "-";
        row.querySelector(".edit").addEventListener("click", () => fillFormForEdit(product));
        row.querySelector(".delete").addEventListener("click", () => deleteProduct(product.id, product.name));
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
            body: JSON.stringify(getProductFromForm())
        });
        if (!response.ok) throw new Error(await getErrorMessage(response));
        resetForm();
        showMessage(`Product ${id ? "updated" : "created"} successfully.`);
        loadProducts();
    } catch (error) {
        showMessage(error.message || "Could not save the product.", true);
    }
});

async function deleteProduct(id, name) {
    if (!window.confirm(`Delete ${name}?`)) return;
    try {
        const response = await fetch(`${apiUrl}/${id}`, { method: "DELETE" });
        if (!response.ok) throw new Error(await getErrorMessage(response));
        showMessage("Product deleted successfully.");
        loadProducts();
    } catch (error) {
        showMessage(error.message || "Could not delete the product.", true);
    }
}

document.getElementById("refresh-button").addEventListener("click", loadProducts);
document.getElementById("cancel-edit-button").addEventListener("click", resetForm);
loadProducts();
