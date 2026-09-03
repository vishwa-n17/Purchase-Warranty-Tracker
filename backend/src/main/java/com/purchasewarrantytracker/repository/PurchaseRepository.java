package com.purchasewarrantytracker.repository;

import com.purchasewarrantytracker.model.PaymentMethod;
import com.purchasewarrantytracker.model.Purchase;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class PurchaseRepository {

    private static final RowMapper<Purchase> PURCHASE_ROW_MAPPER = (resultSet, rowNumber) ->
            new Purchase(
                    resultSet.getLong("id"),
                    resultSet.getLong("product_id"),
                    resultSet.getDate("purchase_date").toLocalDate(),
                    resultSet.getBigDecimal("purchase_price"),
                    resultSet.getString("store_name"),
                    PaymentMethod.valueOf(resultSet.getString("payment_method"))
            );

    private final JdbcTemplate jdbcTemplate;

    public PurchaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Purchase save(Purchase purchase) {
        String sql = "INSERT INTO purchases (product_id, purchase_date, purchase_price, store_name, payment_method) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, purchase.getProductId());
            statement.setDate(2, Date.valueOf(purchase.getPurchaseDate()));
            statement.setBigDecimal(3, purchase.getPurchasePrice());
            statement.setString(4, purchase.getStoreName());
            statement.setString(5, purchase.getPaymentMethod().name());
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Purchase was created but no generated ID was returned");
        }
        purchase.setId(generatedId.longValue());
        return purchase;
    }

    public List<Purchase> findAll() {
        return jdbcTemplate.query(
                "SELECT id, product_id, purchase_date, purchase_price, store_name, payment_method FROM purchases ORDER BY purchase_date DESC, id DESC",
                PURCHASE_ROW_MAPPER
        );
    }

    public Optional<Purchase> findById(long id) {
        List<Purchase> purchases = jdbcTemplate.query(
                "SELECT id, product_id, purchase_date, purchase_price, store_name, payment_method FROM purchases WHERE id = ?",
                PURCHASE_ROW_MAPPER,
                id
        );
        return purchases.stream().findFirst();
    }

    public List<Purchase> findByProductId(long productId) {
        return jdbcTemplate.query(
                "SELECT id, product_id, purchase_date, purchase_price, store_name, payment_method FROM purchases WHERE product_id = ? ORDER BY purchase_date DESC, id DESC",
                PURCHASE_ROW_MAPPER,
                productId
        );
    }

    public boolean update(Purchase purchase) {
        String sql = "UPDATE purchases SET product_id = ?, purchase_date = ?, purchase_price = ?, store_name = ?, payment_method = ? WHERE id = ?";
        int updatedRows = jdbcTemplate.update(sql,
                purchase.getProductId(),
                Date.valueOf(purchase.getPurchaseDate()),
                purchase.getPurchasePrice(),
                purchase.getStoreName(),
                purchase.getPaymentMethod().name(),
                purchase.getId()
        );
        return updatedRows == 1;
    }

    public boolean deleteById(long id) {
        return jdbcTemplate.update("DELETE FROM purchases WHERE id = ?", id) == 1;
    }

    public boolean existsById(long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM purchases WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }
}

