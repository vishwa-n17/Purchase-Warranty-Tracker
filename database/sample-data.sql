USE purchase_warranty_tracker;

INSERT INTO products (name, category, brand, model, serial_number, notes)
VALUES
    ('Laptop', 'Electronics', 'Lenovo', 'IdeaPad Slim 3', 'LNV-IS3-2026-001', 'Used for college work.'),
    ('Water Purifier', 'Home Appliance', 'Aquaguard', 'Sure Delight', 'AQ-SD-2026-002', 'Annual service recommended.'),
    ('Smartphone', 'Electronics', 'Samsung', 'Galaxy A54', 'SM-A546B-2026-003', 'Primary phone.'),
    ('Desk Chair', 'Furniture', 'Green Soul', 'Jupiter Superb', 'GS-JS-2026-004', 'Ergonomic office chair.');

INSERT INTO purchases (product_id, purchase_date, purchase_price, store_name, payment_method)
VALUES
    (1, '2026-06-15', 54999.00, 'Campus Electronics', 'UPI'),
    (2, '2026-07-02', 12499.00, 'Home Store', 'CARD'),
    (3, '2026-07-20', 31999.00, 'Galaxy Hub Store', 'BANK_TRANSFER'),
    (4, '2026-08-05', 8499.00, 'Comfort Furnishings', 'CASH');

INSERT INTO receipts (purchase_id, receipt_file_path, receipt_date)
VALUES
    (1, 'receipts/2026/laptop-lenovo-ideapad.pdf', '2026-06-15'),
    (2, 'receipts/2026/water-purifier-aquaguard.pdf', '2026-07-02'),
    (3, 'receipts/2026/samsung-galaxy-a54-invoice.pdf', '2026-07-20');

INSERT INTO warranties (product_id, start_date, duration_months, expiry_date, warranty_provider, status)
VALUES
    (1, '2026-06-15', 12, '2027-06-15', 'Lenovo', 'ACTIVE'),
    (2, '2026-07-02', 24, '2028-07-02', 'Aquaguard', 'ACTIVE'),
    (3, '2026-07-20', 12, '2027-07-20', 'Samsung Care', 'ACTIVE');

INSERT INTO service_records (product_id, service_date, problem_description, service_cost, service_notes)
VALUES
    (2, '2026-08-10', 'Water flow was slow.', 0.00, 'Filter was cleaned under warranty.');
