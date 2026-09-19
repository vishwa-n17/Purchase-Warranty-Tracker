package com.purchasewarrantytracker.repository;

import com.purchasewarrantytracker.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    private static final RowMapper<Product> PRODUCT_ROW_MAPPER = (resultSet, rowNumber) ->
            new Product(
                    resultSet.getLong("id"),
                    resultSet.getLong("user_id"),
                    resultSet.getString("name"),
                    resultSet.getString("category"),
                    resultSet.getString("brand"),
                    resultSet.getString("model"),
                    resultSet.getString("serial_number"),
                    resultSet.getString("notes")
            );

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Product save(Product product) {
        String sql = "INSERT INTO products (user_id, name, category, brand, model, serial_number, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, product.getUserId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getCategory());
            statement.setString(4, product.getBrand());
            statement.setString(5, product.getModel());
            statement.setString(6, product.getSerialNumber());
            statement.setString(7, product.getNotes());
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Product was created but no generated ID was returned");
        }
        product.setId(generatedId.longValue());
        return product;
    }

    public List<Product> findAll() {
        return jdbcTemplate.query("SELECT id, user_id, name, category, brand, model, serial_number, notes FROM products ORDER BY id", PRODUCT_ROW_MAPPER);
    }

    public List<Product> findByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT id, user_id, name, category, brand, model, serial_number, notes FROM products WHERE user_id = ? ORDER BY id",
                PRODUCT_ROW_MAPPER,
                userId
        );
    }

    public Optional<Product> findById(long id) {
        List<Product> products = jdbcTemplate.query(
                "SELECT id, user_id, name, category, brand, model, serial_number, notes FROM products WHERE id = ?",
                PRODUCT_ROW_MAPPER,
                id
        );
        return products.stream().findFirst();
    }

    public Optional<Product> findByIdAndUserId(long id, Long userId) {
        List<Product> products = jdbcTemplate.query(
                "SELECT id, user_id, name, category, brand, model, serial_number, notes FROM products WHERE id = ? AND user_id = ?",
                PRODUCT_ROW_MAPPER,
                id, userId
        );
        return products.stream().findFirst();
    }

    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, category = ?, brand = ?, model = ?, serial_number = ?, notes = ? WHERE id = ? AND user_id = ?";
        int updatedRows = jdbcTemplate.update(sql,
                product.getName(), product.getCategory(), product.getBrand(), product.getModel(),
                product.getSerialNumber(), product.getNotes(), product.getId(), product.getUserId());
        return updatedRows == 1;
    }

    public boolean hasPurchases(long productId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM purchases WHERE product_id = ?", Integer.class, productId);
        return count != null && count > 0;
    }

    public boolean hasPurchases(long productId, Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM purchases WHERE product_id = ? AND user_id = ?", Integer.class, productId, userId);
        return count != null && count > 0;
    }

    public boolean deleteById(long id) {
        return jdbcTemplate.update("DELETE FROM products WHERE id = ?", id) == 1;
    }

    public boolean deleteByIdAndUserId(long id, Long userId) {
        return jdbcTemplate.update("DELETE FROM products WHERE id = ? AND user_id = ?", id, userId) == 1;
    }
}
