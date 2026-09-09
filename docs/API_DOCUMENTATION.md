# REST API Documentation — Purchase & Warranty Tracker

Base URL: `http://localhost:8080`

---

## 1. System Health

### Check Backend Health
- **Route**: `GET /api/health`
- **Response Status**: `200 OK`
- **Response Body**:
  ```json
  {
    "status": "UP",
    "message": "Purchase & Warranty Tracker backend is running",
    "timestamp": "2026-08-28T15:15:00.000Z"
  }
  ```

---

## 2. Products API (`/api/products`)

### 2.1 Get All Products
- **Route**: `GET /api/products`
- **Response Status**: `200 OK`
- **Response Body**:
  ```json
  [
    {
      "id": 1,
      "name": "Laptop",
      "category": "Electronics",
      "brand": "Lenovo",
      "model": "IdeaPad Slim 3",
      "serialNumber": "LNV-IS3-2026-001",
      "notes": "Used for college work."
    }
  ]
  ```

### 2.2 Get Product By ID
- **Route**: `GET /api/products/{id}`
- **Response Status**: `200 OK` (or `404 Not Found`)
- **Response Body**:
  ```json
  {
    "id": 1,
    "name": "Laptop",
    "category": "Electronics",
    "brand": "Lenovo",
    "model": "IdeaPad Slim 3",
    "serialNumber": "LNV-IS3-2026-001",
    "notes": "Used for college work."
  }
  ```

### 2.3 Create Product
- **Route**: `POST /api/products`
- **Response Status**: `201 Created` (Headers: `Location: http://localhost:8080/api/products/1`)
- **Request Body**:
  ```json
  {
    "name": "Smartphone",
    "category": "Electronics",
    "brand": "Samsung",
    "model": "Galaxy A54",
    "serialNumber": "SM-A546B-2026-003",
    "notes": "Primary device"
  }
  ```

### 2.4 Update Product
- **Route**: `PUT /api/products/{id}`
- **Response Status**: `200 OK` (or `404 Not Found`, `400 Bad Request`)
- **Request Body**:
  ```json
  {
    "name": "Smartphone (Updated)",
    "category": "Electronics",
    "brand": "Samsung",
    "model": "Galaxy A54 5G",
    "serialNumber": "SM-A546B-2026-003",
    "notes": "Updated notes"
  }
  ```

### 2.5 Delete Product
- **Route**: `DELETE /api/products/{id}`
- **Response Status**: `204 No Content` (or `409 Conflict` if product has purchases, `404 Not Found`)

---

## 3. Purchases API (`/api/purchases`)

### 3.1 Get All Purchases
- **Route**: `GET /api/purchases`
- **Response Status**: `200 OK`
- **Response Body**:
  ```json
  [
    {
      "id": 1,
      "productId": 1,
      "purchaseDate": "2026-06-15",
      "purchasePrice": 54999.00,
      "storeName": "Campus Electronics",
      "paymentMethod": "UPI"
    }
  ]
  ```

### 3.2 Get Purchase By ID
- **Route**: `GET /api/purchases/{id}`
- **Response Status**: `200 OK` (or `404 Not Found`)

### 3.3 Get Purchases By Product ID
- **Route**: `GET /api/products/{productId}/purchases`
- **Response Status**: `200 OK` (or `404 Not Found`)

### 3.4 Create Purchase
- **Route**: `POST /api/purchases`
- **Response Status**: `201 Created` (Headers: `Location: /api/purchases/1`)
- **Request Body**:
  ```json
  {
    "productId": 1,
    "purchaseDate": "2026-06-15",
    "purchasePrice": 54999.00,
    "storeName": "Campus Electronics",
    "paymentMethod": "UPI"
  }
  ```

### 3.5 Update Purchase
- **Route**: `PUT /api/purchases/{id}`
- **Response Status**: `200 OK` (or `404 Not Found`, `400 Bad Request`)

### 3.6 Delete Purchase
- **Route**: `DELETE /api/purchases/{id}`
- **Response Status**: `204 No Content` (or `404 Not Found`)

---

## 4. Receipts API (`/api/purchases/{purchaseId}/receipt`)

### 4.1 Get Receipt for Purchase
- **Route**: `GET /api/purchases/{purchaseId}/receipt`
- **Response Status**: `200 OK` (or `404 Not Found`)
- **Response Body**:
  ```json
  {
    "id": 1,
    "purchaseId": 1,
    "receiptFilePath": "receipts/2026/laptop-lenovo-ideapad.pdf",
    "receiptDate": "2026-06-15"
  }
  ```

### 4.2 Attach Receipt to Purchase
- **Route**: `POST /api/purchases/{purchaseId}/receipt`
- **Response Status**: `201 Created` (or `400 Bad Request` if duplicate exists)
- **Request Body**:
  ```json
  {
    "receiptFilePath": "receipts/2026/laptop-lenovo-ideapad.pdf",
    "receiptDate": "2026-06-15"
  }
  ```

### 4.3 Update Receipt
- **Route**: `PUT /api/purchases/{purchaseId}/receipt`
- **Response Status**: `200 OK`

### 4.4 Delete Receipt
- **Route**: `DELETE /api/purchases/{purchaseId}/receipt`
- **Response Status**: `204 No Content`

---

## 5. Warranties API (`/api/warranties`)

### 5.1 Get All Warranties
- **Route**: `GET /api/warranties`
- **Response Status**: `200 OK`
- **Response Body**:
  ```json
  [
    {
      "id": 1,
      "productId": 1,
      "startDate": "2026-06-15",
      "durationMonths": 12,
      "expiryDate": "2027-06-15",
      "warrantyProvider": "Lenovo",
      "status": "ACTIVE"
    }
  ]
  ```

### 5.2 Get Warranty By ID
- **Route**: `GET /api/warranties/{id}`
- **Response Status**: `200 OK` (or `404 Not Found`)

### 5.3 Get Warranty By Product ID
- **Route**: `GET /api/products/{productId}/warranty`
- **Response Status**: `200 OK` (or `404 Not Found`)

### 5.4 Create Warranty
- **Route**: `POST /api/warranties`
- **Response Status**: `201 Created` (or `400 Bad Request` on duplicate product)
- **Request Body**:
  ```json
  {
    "productId": 1,
    "startDate": "2026-06-15",
    "durationMonths": 12,
    "warrantyProvider": "Lenovo"
  }
  ```

### 5.5 Update Warranty
- **Route**: `PUT /api/warranties/{id}`
- **Response Status**: `200 OK` (or `404 Not Found`)

### 5.6 Delete Warranty
- **Route**: `DELETE /api/warranties/{id}`
- **Response Status**: `204 No Content`

---

## 6. Service Records API (`/api/service-records`)

### 6.1 Get All Service Records
- **Route**: `GET /api/service-records` *(Optional query parameter: `?productId=1`)*
- **Response Status**: `200 OK`
- **Response Body**:
  ```json
  [
    {
      "id": 1,
      "product": {
        "id": 1,
        "name": "Laptop",
        "category": "Electronics",
        "brand": "Lenovo",
        "model": "IdeaPad Slim 3"
      },
      "serviceDate": "2026-08-10",
      "provider": "Authorized Service Center",
      "description": "Cleaned internal cooling fan and replaced thermal paste.",
      "cost": 450.00,
      "serviceType": "MAINTENANCE"
    }
  ]
  ```

### 6.2 Get Service Record By ID
- **Route**: `GET /api/service-records/{id}`
- **Response Status**: `200 OK` (or `404 Not Found`)

### 6.3 Get Service Records By Product ID
- **Route**: `GET /api/service-records/product/{productId}`
- **Response Status**: `200 OK` (or `404 Not Found`)

### 6.4 Create Service Record
- **Route**: `POST /api/service-records`
- **Response Status**: `201 Created` (or `400 Bad Request`)
- **Request Body**:
  ```json
  {
    "product": { "id": 1 },
    "serviceDate": "2026-08-10",
    "provider": "Authorized Service Center",
    "description": "Cleaned internal cooling fan.",
    "cost": 450.00,
    "serviceType": "MAINTENANCE"
  }
  ```

### 6.5 Update Service Record
- **Route**: `PUT /api/service-records/{id}`
- **Response Status**: `200 OK`

### 6.6 Delete Service Record
- **Route**: `DELETE /api/service-records/{id}`
- **Response Status**: `204 No Content`

---

## 7. Dashboard API (`/api/dashboard`)

### 7.1 Get System Summary Analytics
- **Route**: `GET /api/dashboard/summary`
- **Response Status**: `200 OK`
- **Response Body**:
  ```json
  {
    "totalProductsCount": 4,
    "totalPurchaseSpend": 107996.00,
    "totalServiceSpend": 450.00,
    "activeWarrantiesCount": 3,
    "expiredWarrantiesCount": 0
  }
  ```

---

## 8. Error Response Schema

All unhandled exceptions and validation errors are intercepted and serialized into a uniform `ApiError` JSON response:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Product name is required",
  "timestamp": "2026-08-28T15:20:00.000Z"
}
```

