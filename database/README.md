# Database

The MySQL database name is `purchase_warranty_tracker`.

- `schema.sql` creates the five core tables and their relationships.
- `sample-data.sql` adds two example products and related records. Run it only after `schema.sql`.

Receipt documents are not stored in MySQL. The `receipts.receipt_file_path` column stores a path or reference to the file instead.
