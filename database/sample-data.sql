USE purchase_warranty_tracker;

-- Demo user: password is "password123" (BCrypt hashed)
INSERT IGNORE INTO users (name, email, password)
VALUES ('Demo User', 'demo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p092ld9xL5mF4f8Yf5qYqO');

INSERT INTO products (user_id, name, category, brand, model, serial_number, notes)
VALUES
    (1, 'Laptop', 'Electronics', 'Lenovo', 'IdeaPad Slim 3', 'LNV-IS3-2026-001', 'Used for college work.'),
    (1, 'Water Purifier', 'Home Appliance', 'Aquaguard', 'Sure Delight', 'AQ-SD-2026-002', 'Annual service recommended.'),
    (1, 'Smartphone', 'Electronics', 'Samsung', 'Galaxy A54', 'SM-A546B-2026-003', 'Primary phone.'),
    (1, 'Desk Chair', 'Furniture', 'Green Soul', 'Jupiter Superb', 'GS-JS-2026-004', 'Ergonomic office chair.');

INSERT INTO purchases (user_id, product_id, purchase_date, purchase_price, store_name, payment_method)
VALUES
    (1, 1, '2026-06-15', 54999.00, 'Campus Electronics', 'UPI'),
    (1, 2, '2026-07-02', 12499.00, 'Home Store', 'CARD'),
    (1, 3, '2026-07-20', 31999.00, 'Galaxy Hub Store', 'BANK_TRANSFER'),
    (1, 4, '2026-08-05', 8499.00, 'Comfort Furnishings', 'CASH');

INSERT INTO receipts (user_id, purchase_id, receipt_file_path, receipt_date)
VALUES
    (1, 1, 'receipts/2026/laptop-lenovo-ideapad.pdf', '2026-06-15'),
    (1, 2, 'receipts/2026/water-purifier-aquaguard.pdf', '2026-07-02'),
    (1, 3, 'receipts/2026/samsung-galaxy-a54-invoice.pdf', '2026-07-20');

INSERT INTO warranties (user_id, product_id, start_date, duration_months, expiry_date, warranty_provider, status)
VALUES
    (1, 1, '2026-06-15', 12, '2027-06-15', 'Lenovo', 'ACTIVE'),
    (1, 2, '2026-07-02', 24, '2028-07-02', 'Aquaguard', 'ACTIVE'),
    (1, 3, '2026-07-20', 12, '2027-07-20', 'Samsung Care', 'ACTIVE');

INSERT INTO service_records (user_id, product_id, service_date, provider, description, cost, service_type)
VALUES
    (1, 2, '2026-08-10', 'Aquaguard', 'Water flow was slow.', 0.00, 'MAINTENANCE');
