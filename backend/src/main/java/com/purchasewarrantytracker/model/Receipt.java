package com.purchasewarrantytracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class Receipt {

    private Long id;
    private Long userId;
    private Long purchaseId;

    @NotBlank(message = "Receipt file path is required")
    @Size(max = 500, message = "Receipt file path must be at most 500 characters")
    private String receiptFilePath;

    private String imageFileName;
    private String imageContentType;

    @JsonIgnore
    private byte[] imageData;

    @NotNull(message = "Receipt date is required")
    private LocalDate receiptDate;

    public Receipt() {
    }

    public Receipt(Long id, Long purchaseId, String receiptFilePath, LocalDate receiptDate) {
        this.id = id;
        this.userId = null;
        this.purchaseId = purchaseId;
        this.receiptFilePath = receiptFilePath;
        this.receiptDate = receiptDate;
    }

    public Receipt(Long id, Long userId, Long purchaseId, String receiptFilePath, LocalDate receiptDate) {
        this.id = id;
        this.userId = userId;
        this.purchaseId = purchaseId;
        this.receiptFilePath = receiptFilePath;
        this.receiptDate = receiptDate;
    }

    public boolean hasFileReference() {
        return receiptFilePath != null && !receiptFilePath.isBlank();
    }

    public boolean hasUploadedImage() {
        return imageData != null && imageData.length > 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Long purchaseId) { this.purchaseId = purchaseId; }
    public String getReceiptFilePath() { return receiptFilePath; }
    public void setReceiptFilePath(String receiptFilePath) { this.receiptFilePath = receiptFilePath; }
    public String getImageFileName() { return imageFileName; }
    public void setImageFileName(String imageFileName) { this.imageFileName = imageFileName; }
    public String getImageContentType() { return imageContentType; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }
    @JsonIgnore
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }
}
