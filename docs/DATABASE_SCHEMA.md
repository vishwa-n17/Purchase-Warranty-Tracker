# Database Schema Documentation — Purchase & Warranty Tracker

Database Name: `purchase_warranty_tracker`  
Default Charset: `utf8mb4` (Collation: `utf8mb4_unicode_ci`)  
Storage Engine: `InnoDB` (ACID Compliant)

---

## 1. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    PRODUCTS ||--o{ PURCHASES : "has (1:N)"
    PRODUCTS ||--o| WARRANTIES : "covered_by (1:1)"
    PRODUCTS ||--o{ SERVICE_RECORDS : "undergoes (1:N)"
    PURCHASES ||--o| RECEIPTS : "documented_by (1:1)"

    PRODUCTS {
        INT UNSIGNED id PK "AUTO_INCREMENT"
        VARCHAR(150) name "NOT NULL"
        VARCHAR(100) category "NOT NULL"
        VARCHAR(100) brand "NULL"
        VARCHAR(100) model "NULL"
        VARCHAR(150) serial_number "UNIQUE, NULL"
        TEXT notes "NULL"
    }

    PURCHASES {
        INT UNSIGNED id PK "AUTO_INCREMENT"
        INT UNSIGNED product_id FK "NOT NULL"
        DATE purchase_date "NOT NULL"
        DECIMAL(10_2) purchase_price "NOT NULL, CHECK >= 0"
        VARCHAR(150) store_name "NOT NULL"
        ENUM payment_method "NOT NULL: CASH, CARD, UPI, BANK_TRANSFER, OTHER"
    }

    RECEIPTS {
        INT UNSIGNED id PK "AUTO_INCREMENT"
        INT UNSIGNED purchase_id FK "NOT NULL, UNIQUE"
        VARCHAR(500) receipt_file_path "NOT NULL"
        DATE receipt_date "NOT NULL"
    }

    WARRANTIES {
        INT UNSIGNED id PK "AUTO_INCREMENT"
        INT UNSIGNED product_id FK "NOT NULL, UNIQUE"
        DATE start_date "NOT NULL"
        SMALLINT UNSIGNED duration_months "NOT NULL, CHECK > 0"
        DATE expiry_date "NOT NULL, CHECK expiry_date >= start_date"
        VARCHAR(150) warranty_provider "NOT NULL"
        ENUM status "NOT NULL: ACTIVE, EXPIRED, VOID (DEFAULT 'ACTIVE')"
    }

    SERVICE_RECORDS {
        INT UNSIGNED id PK "AUTO_INCREMENT"
        INT UNSIGNED product_id FK "NOT NULL"
        DATE service_date "NOT NULL"
        VARCHAR(100) provider "NOT NULL"
        TEXT description "NOT NULL"
        DECIMAL(10_2) cost "NOT NULL, DEFAULT 0.00, CHECK >= 0"
        VARCHAR(50) service_type "NOT NULL: REPAIR, MAINTENANCE, INSPECTION, UPGRADE"
    }
```

---

## 2. Relational Schema & Constraints

### 2.1 `products` Table
Primary entity representing tracked physical or electronic goods.

| Column | Data Type | Modifiers | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INT UNSIGNED` | `PRIMARY KEY AUTO_INCREMENT` | Unique product identifier |
| `name` | `VARCHAR(150)` | `NOT NULL` | Name or title of the product |
| `category` | `VARCHAR(100)` | `NOT NULL` | Category (e.g., Electronics, Home Appliance) |
| `brand` | `VARCHAR(100)` | `NULL` | Manufacturer brand name |
| `model` | `VARCHAR(100)` | `NULL` | Model identifier or number |
| `serial_number`| `VARCHAR(150)` | `UNIQUE, NULL` | Hardware serial number |
| `notes` | `TEXT` | `NULL` | User remarks or notes |

---

### 2.2 `purchases` Table
Stores purchasing transactions. Linked to a product ($1:N$).

| Column | Data Type | Modifiers | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INT UNSIGNED` | `PRIMARY KEY AUTO_INCREMENT` | Unique purchase transaction ID |
| `product_id` | `INT UNSIGNED` | `NOT NULL, FK -> products(id)` | Referenced product ID |
| `purchase_date`| `DATE` | `NOT NULL` | Date when item was bought |
| `purchase_price`| `DECIMAL(10,2)`| `NOT NULL, CHECK (purchase_price >= 0)`| Price paid in currency |
| `store_name` | `VARCHAR(150)` | `NOT NULL` | Vendor or store name |
| `payment_method`| `ENUM` | `NOT NULL ('CASH','CARD','UPI','BANK_TRANSFER','OTHER')` | Mode of payment |

**Foreign Key Constraint**:
- `fk_purchases_product`: `FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT ON UPDATE CASCADE`  
  *(Prevents accidental product deletion if historical purchase transactions exist)*.

---

### 2.3 `receipts` Table
Maintains receipt metadata and file paths ($1:1$ with `purchases`).

| Column | Data Type | Modifiers | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INT UNSIGNED` | `PRIMARY KEY AUTO_INCREMENT` | Unique receipt identifier |
| `purchase_id` | `INT UNSIGNED` | `NOT NULL UNIQUE, FK -> purchases(id)` | Associated purchase record |
| `receipt_file_path`| `VARCHAR(500)`| `NOT NULL` | Path or reference to receipt file |
| `receipt_date` | `DATE` | `NOT NULL` | Date indicated on invoice/receipt |

**Foreign Key Constraint**:
- `fk_receipts_purchase`: `FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE ON UPDATE CASCADE`.

---

### 2.4 `warranties` Table
Stores warranty duration, computed expiry date, and status ($1:1$ with `products`).

| Column | Data Type | Modifiers | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INT UNSIGNED` | `PRIMARY KEY AUTO_INCREMENT` | Unique warranty identifier |
| `product_id` | `INT UNSIGNED` | `NOT NULL UNIQUE, FK -> products(id)` | Associated product record |
| `start_date` | `DATE` | `NOT NULL` | Warranty commencement date |
| `duration_months`| `SMALLINT UNSIGNED`| `NOT NULL, CHECK (duration_months > 0)` | Duration coverage in months |
| `expiry_date` | `DATE` | `NOT NULL, CHECK (expiry_date >= start_date)` | Calculated expiration date |
| `warranty_provider`| `VARCHAR(150)` | `NOT NULL` | Provider (OEM, retail, insurance) |
| `status` | `ENUM` | `NOT NULL ('ACTIVE','EXPIRED','VOID') DEFAULT 'ACTIVE'` | Current status |

**Foreign Key Constraint**:
- `fk_warranties_product`: `FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE ON UPDATE CASCADE`.

---

### 2.5 `service_records` Table
Logs post-purchase service, maintenance, inspections, and repairs ($1:N$ with `products`).

| Column | Data Type | Modifiers | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INT UNSIGNED` | `PRIMARY KEY AUTO_INCREMENT` | Unique service record ID |
| `product_id` | `INT UNSIGNED` | `NOT NULL, FK -> products(id)` | Serviced product ID |
| `service_date` | `DATE` | `NOT NULL` | Date service was performed |
| `provider` | `VARCHAR(100)` | `NOT NULL` | Service technician or center |
| `description` | `TEXT` | `NOT NULL` | Problem diagnosis and fix details |
| `cost` | `DECIMAL(10,2)`| `NOT NULL DEFAULT 0.00, CHECK (cost >= 0)` | Repair/service expense |
| `service_type` | `VARCHAR(50)` | `NOT NULL` (`REPAIR`,`MAINTENANCE`,`INSPECTION`,`UPGRADE`) | Classification of service |

**Foreign Key Constraint**:
- `fk_service_records_product`: `FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE ON UPDATE CASCADE`.

