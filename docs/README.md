# Purchase & Warranty Tracker — Technical Documentation

## 1. Executive Overview

The **Purchase & Warranty Tracker** is an enterprise-grade, full-stack Java web application designed to track consumer and institutional purchases, digitized receipt references, warranty lifecycles, and ongoing repair/service histories.

The application allows users to maintain a centralized product catalog, track purchase transactions across various payment methods, calculate warranty expiration milestones dynamically with visual status indicators (`ACTIVE`, `EXPIRED`, `VOID`), log maintenance and repair costs, and view consolidated financial and asset metrics through an interactive analytics dashboard.

---

## 2. System Architecture

The application adopts a decoupled, layered **Model-View-Controller (MVC) / REST API** architecture ensuring separation of concerns, scalability, and testability.

```text
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│     Vanilla HTML5 / Modern CSS3 / Client-Side JavaScript ES6+          │
│   (dashboard.html, index.html, purchases.html, warranties.html, ...)   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ HTTP / JSON (REST APIs)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            REST CONTROLLERS                            │
│  ProductController | PurchaseController | ReceiptController            │
│  WarrantyController | ServiceRecordController | DashboardController    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             SERVICE LAYER                              │
│  Business logic, input validation, date computations, integrity checks │
│  ProductService | PurchaseService | ReceiptService                     │
│  WarrantyService | ServiceRecordService | DashboardService             │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        DATA ACCESS LAYER (DAO)                         │
│  Spring JDBC (JdbcTemplate) & Spring Data JPA (JpaRepository)          │
│  ProductRepository | PurchaseRepository | ReceiptRepository            │
│  WarrantyRepository | ServiceRecordRepository                          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ JDBC / Connection Pool (HikariCP)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            DATABASE ENGINE                             │
│                    MySQL 8.0+ (InnoDB, UTF-8 MB4)                      │
└────────────────────────────────────────────────────────────────────────┘
```

### Architectural Highlights:
- **Presentation Layer**: Pure vanilla HTML5, semantic CSS3, and modern async JavaScript (`fetch`, Promises). No bloated JavaScript frameworks or external CDN dependencies.
- **Controller Layer**: RESTful endpoints with `@RestController`, structured DTO serialization, `@Valid` bean validations, and centralized `@RestControllerAdvice` exception mapping (`ApiError`).
- **Service Layer**: Encapsulates transactional business logic, automated expiry computations (`startDate + durationMonths`), real-time status transitions, referential integrity guards, and duplicate prevention.
- **Data Access Layer**: Hybrid persistence leveraging high-performance Spring `JdbcTemplate` for direct SQL mapping alongside Spring Data JPA `JpaRepository` abstraction.
- **Database Engine**: Relational MySQL database enforcing strict foreign keys, cascade deletes, checks, and unique constraints.

---

## 3. Technology Stack

- **Backend Runtime**: Java 21 LTS
- **Backend Framework**: Spring Boot 3.5.6 (Web, Validation, JDBC, Data JPA)
- **Database Engine**: MySQL 8.x (InnoDB Engine, utf8mb4)
- **Connection Pool**: HikariCP
- **Frontend Stack**: Semantic HTML5, CSS3, Vanilla ES6+ JavaScript
- **Testing Frameworks**: JUnit 5, Mockito, Spring WebMvcTest / MockMvc
- **Build Tool**: Apache Maven 3.9+

---

## 4. Prerequisites

Before running the project locally, ensure you have:
1. **Java Development Kit (JDK)**: Version 21 or higher (`java -version`).
2. **Apache Maven**: Version 3.8+ (`mvn -v`) or use the included wrapper.
3. **MySQL Server**: Version 8.0+ running on port `3306`.
4. **Modern Web Browser**: Chrome, Firefox, Edge, or Safari.

---

## 5. Installation & Setup Guide

### Step 1: Database Setup
1. Open MySQL CLI or MySQL Workbench.
2. Execute the DDL schema script:
   ```sql
   source database/schema.sql;
   ```
3. (Optional) Load sample seed data:
   ```sql
   source database/sample-data.sql;
   ```

### Step 2: Configure Environment Variables or Properties
Set the database credentials as environment variables or copy the example configuration:
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/purchase_warranty_tracker"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
```

### Step 3: Run the Backend Application
Navigate to the `backend` folder and run with the `mysql` Spring profile:
```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
The REST API will initialize on `http://localhost:8080`.

### Step 4: Open the Frontend
Open `frontend/dashboard.html` directly in your browser, or serve the `frontend/` directory using VS Code Live Server or any static web server:
```powershell
# Example using Python static server from repository root
python -m http.server 3000
```
Navigate to `http://localhost:3000/frontend/dashboard.html`.

---

## 6. Test Suite & Verification Summary

The project includes an automated test suite comprising **96 JUnit 5 and Mockito tests** across 10 specialized test classes:

| Test Class | Target Component | Type | Test Count | Status |
| :--- | :--- | :--- | :---: | :---: |
| `ProductControllerTest` | `ProductController` | WebMvc Slice Test | **6** | PASS |
| `ProductServiceTest` | `ProductService` | Mockito Unit Test | **5** | PASS |
| `PurchaseControllerTest` | `PurchaseController` | WebMvc Slice Test | **8** | PASS |
| `PurchaseServiceTest` | `PurchaseService` | Mockito Unit Test | **12** | PASS |
| `ReceiptControllerTest` | `ReceiptController` | WebMvc Slice Test | **6** | PASS |
| `ReceiptServiceTest` | `ReceiptService` | Mockito Unit Test | **12** | PASS |
| `WarrantyControllerTest` | `WarrantyController` | WebMvc Slice Test | **8** | PASS |
| `WarrantyServiceTest` | `WarrantyService` | Mockito Unit Test | **17** | PASS |
| `ServiceRecordControllerTest` | `ServiceRecordController` | WebMvc Slice Test | **8** | PASS |
| `ServiceRecordServiceTest` | `ServiceRecordService` | Mockito Unit Test | **12** | PASS |
| `DashboardControllerTest` | `DashboardController` | WebMvc Slice Test | **1** | PASS |
| `DashboardServiceTest` | `DashboardService` | Mockito Unit Test | **2** | PASS |
| **TOTAL** | | | **97 Tests** | **100% PASS** |

To execute the entire automated test suite:
```powershell
cd backend
mvn test
```

