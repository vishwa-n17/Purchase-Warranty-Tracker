CREATE DATABASE IF NOT EXISTS purchase_warranty_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE purchase_warranty_tracker;

CREATE TABLE IF NOT EXISTS users (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    brand VARCHAR(100),
    model VARCHAR(100),
    serial_number VARCHAR(150) UNIQUE,
    notes TEXT,
    CONSTRAINT fk_products_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS purchases (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    product_id INT UNSIGNED NOT NULL,
    purchase_date DATE NOT NULL,
    purchase_price DECIMAL(10, 2) NOT NULL,
    store_name VARCHAR(150) NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'UPI', 'BANK_TRANSFER', 'OTHER') NOT NULL,
    CONSTRAINT chk_purchases_price_non_negative CHECK (purchase_price >= 0),
    CONSTRAINT fk_purchases_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_purchases_product FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS receipts (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    purchase_id INT UNSIGNED NOT NULL UNIQUE,
    receipt_file_path VARCHAR(500) NOT NULL,
    receipt_date DATE NOT NULL,
    CONSTRAINT fk_receipts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_receipts_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS warranties (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    product_id INT UNSIGNED NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    duration_months SMALLINT UNSIGNED NOT NULL,
    expiry_date DATE NOT NULL,
    warranty_provider VARCHAR(150) NOT NULL,
    status ENUM('ACTIVE', 'EXPIRED', 'VOID') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_warranties_duration CHECK (duration_months > 0),
    CONSTRAINT chk_warranties_dates CHECK (expiry_date >= start_date),
    CONSTRAINT fk_warranties_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_warranties_product FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS service_records (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    product_id INT UNSIGNED NOT NULL,
    service_date DATE NOT NULL,
    provider VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    service_type VARCHAR(50) NOT NULL,
    CONSTRAINT chk_service_records_cost_non_negative CHECK (cost >= 0),
    CONSTRAINT fk_service_records_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_service_records_product FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;
