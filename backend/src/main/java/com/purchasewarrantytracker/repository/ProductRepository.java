package com.purchasewarrantytracker.repository;

import com.purchasewarrantytracker.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("mysql")
public class ProductRepository {

    private static final RowMapper<Product> PRODUCT_ROW_MAPPER = (resultSet, rowNumber) ->
            new Product(
                    resultSet.getLong("id"),
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
        String sql = "INSERT INTO products (name, category, brand, model, serial_number, notes) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory());
            statement.setString(3, product.getBrand());
            statement.setString(4, product.getModel());
            statement.setString(5, product.getSerialNumber());
            statement.setString(6, product.getNotes());
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
        return jdbcTemplate.query("SELECT id, name, category, brand, model, serial_number, notes FROM products ORDER BY id", PRODUCT_ROW_MAPPER);
    }

    public Optional<Product> findById(long id) {
        List<Product> products = jdbcTemplate.query(
                "SELECT id, name, category, brand, model, serial_number, notes FROM products WHERE id = ?",
                PRODUCT_ROW_MAPPER,
                id
        );
        return products.stream().findFirst();
    }

    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, category = ?, brand = ?, model = ?, serial_number = ?, notes = ? WHERE id = ?";
        int updatedRows = jdbcTemplate.update(sql,
                product.getName(), product.getCategory(), product.getBrand(), product.getModel(),
                product.getSerialNumber(), product.getNotes(), product.getId());
        return updatedRows == 1;
    }

    public boolean hasPurchases(long productId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM purchases WHERE product_id = ?", Integer.class, productId);
        return count != null && count > 0;
    }

    public boolean deleteById(long id) {
        return jdbcTemplate.update("DELETE FROM products WHERE id = ?", id) == 1;
    }
}
