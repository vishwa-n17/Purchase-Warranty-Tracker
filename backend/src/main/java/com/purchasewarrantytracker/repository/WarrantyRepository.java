package com.purchasewarrantytracker.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.purchasewarrantytracker.model.Warranty;
import com.purchasewarrantytracker.model.WarrantyStatus;

@Repository
public class WarrantyRepository {

    private static final RowMapper<Warranty> WARRANTY_ROW_MAPPER = (resultSet, rowNumber) ->
            new Warranty(
                    resultSet.getLong("id"),
                    resultSet.getLong("product_id"),
                    resultSet.getDate("start_date").toLocalDate(),
                    resultSet.getInt("duration_months"),
                    resultSet.getDate("expiry_date").toLocalDate(),
                    resultSet.getString("warranty_provider"),
                    WarrantyStatus.valueOf(resultSet.getString("status"))
            );

    private final JdbcTemplate jdbcTemplate;

    public WarrantyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Warranty save(Warranty warranty) {
        String sql = "INSERT INTO warranties (product_id, start_date, duration_months, expiry_date, warranty_provider, status) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, warranty.getProductId());
            statement.setDate(2, Date.valueOf(warranty.getStartDate()));
            statement.setInt(3, warranty.getDurationMonths());
            statement.setDate(4, Date.valueOf(warranty.getExpiryDate()));
            statement.setString(5, warranty.getWarrantyProvider());
            statement.setString(6, warranty.getStatus().name());
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Warranty was created but no generated ID was returned");
        }
        warranty.setId(generatedId.longValue());
        return warranty;
    }

    public List<Warranty> findAll() {
        return jdbcTemplate.query(
                "SELECT id, product_id, start_date, duration_months, expiry_date, warranty_provider, status FROM warranties ORDER BY expiry_date ASC, id DESC",
                WARRANTY_ROW_MAPPER
        );
    }

    public Optional<Warranty> findById(long id) {
        List<Warranty> warranties = jdbcTemplate.query(
                "SELECT id, product_id, start_date, duration_months, expiry_date, warranty_provider, status FROM warranties WHERE id = ?",
                WARRANTY_ROW_MAPPER,
                id
        );
        return warranties.stream().findFirst();
    }

    public Optional<Warranty> findByProductId(long productId) {
        List<Warranty> warranties = jdbcTemplate.query(
                "SELECT id, product_id, start_date, duration_months, expiry_date, warranty_provider, status FROM warranties WHERE product_id = ?",
                WARRANTY_ROW_MAPPER,
                productId
        );
        return warranties.stream().findFirst();
    }

    public boolean update(Warranty warranty) {
        String sql = "UPDATE warranties SET product_id = ?, start_date = ?, duration_months = ?, expiry_date = ?, warranty_provider = ?, status = ? WHERE id = ?";
        int updatedRows = jdbcTemplate.update(sql,
                warranty.getProductId(),
                Date.valueOf(warranty.getStartDate()),
                warranty.getDurationMonths(),
                Date.valueOf(warranty.getExpiryDate()),
                warranty.getWarrantyProvider(),
                warranty.getStatus().name(),
                warranty.getId()
        );
        return updatedRows == 1;
    }

    public boolean deleteById(long id) {
        return jdbcTemplate.update("DELETE FROM warranties WHERE id = ?", id) == 1;
    }

    public boolean existsById(long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warranties WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public boolean existsByProductId(long productId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warranties WHERE product_id = ?", Integer.class, productId);
        return count != null && count > 0;
    }
}

