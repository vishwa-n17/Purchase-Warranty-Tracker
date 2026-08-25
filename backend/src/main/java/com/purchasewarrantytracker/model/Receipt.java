package com.purchasewarrantytracker.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class Receipt {

    private Long id;
    private Long purchaseId;

    @NotBlank(message = "Receipt file path is required")
    @Size(max = 500, message = "Receipt file path must be at most 500 characters")
    private String receiptFilePath;

    @NotNull(message = "Receipt date is required")
    private LocalDate receiptDate;

    public Receipt() {
    }

    public Receipt(Long id, Long purchaseId, String receiptFilePath, LocalDate receiptDate) {
        this.id = id;
        this.purchaseId = purchaseId;
        this.receiptFilePath = receiptFilePath;
        this.receiptDate = receiptDate;
    }

    public boolean hasFileReference() {
        return receiptFilePath != null && !receiptFilePath.isBlank();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Long purchaseId) { this.purchaseId = purchaseId; }
    public String getReceiptFilePath() { return receiptFilePath; }
    public void setReceiptFilePath(String receiptFilePath) { this.receiptFilePath = receiptFilePath; }
    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }
}
