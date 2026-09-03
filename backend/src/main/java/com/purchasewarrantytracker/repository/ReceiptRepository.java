package com.purchasewarrantytracker.repository;

import com.purchasewarrantytracker.model.Receipt;
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
public class ReceiptRepository {

    private static final RowMapper<Receipt> RECEIPT_ROW_MAPPER = (resultSet, rowNumber) ->
            new Receipt(
                    resultSet.getLong("id"),
                    resultSet.getLong("purchase_id"),
                    resultSet.getString("receipt_file_path"),
                    resultSet.getDate("receipt_date").toLocalDate()
            );

    private final JdbcTemplate jdbcTemplate;

    public ReceiptRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Receipt save(Receipt receipt) {
        String sql = "INSERT INTO receipts (purchase_id, receipt_file_path, receipt_date) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, receipt.getPurchaseId());
            statement.setString(2, receipt.getReceiptFilePath());
            statement.setDate(3, Date.valueOf(receipt.getReceiptDate()));
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Receipt was created but no generated ID was returned");
        }
        receipt.setId(generatedId.longValue());
        return receipt;
    }

    public Optional<Receipt> findById(long id) {
        List<Receipt> receipts = jdbcTemplate.query(
                "SELECT id, purchase_id, receipt_file_path, receipt_date FROM receipts WHERE id = ?",
                RECEIPT_ROW_MAPPER,
                id
        );
        return receipts.stream().findFirst();
    }

    public Optional<Receipt> findByPurchaseId(long purchaseId) {
        List<Receipt> receipts = jdbcTemplate.query(
                "SELECT id, purchase_id, receipt_file_path, receipt_date FROM receipts WHERE purchase_id = ?",
                RECEIPT_ROW_MAPPER,
                purchaseId
        );
        return receipts.stream().findFirst();
    }

    public boolean update(Receipt receipt) {
        String sql = "UPDATE receipts SET receipt_file_path = ?, receipt_date = ? WHERE purchase_id = ?";
        int updatedRows = jdbcTemplate.update(sql,
                receipt.getReceiptFilePath(),
                Date.valueOf(receipt.getReceiptDate()),
                receipt.getPurchaseId()
        );
        return updatedRows == 1;
    }

    public boolean deleteByPurchaseId(long purchaseId) {
        return jdbcTemplate.update("DELETE FROM receipts WHERE purchase_id = ?", purchaseId) == 1;
    }

    public boolean existsByPurchaseId(long purchaseId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM receipts WHERE purchase_id = ?", Integer.class, purchaseId);
        return count != null && count > 0;
    }
}

