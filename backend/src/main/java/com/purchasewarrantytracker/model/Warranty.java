package com.purchasewarrantytracker.model;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class Warranty {

    private Long id;

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be a positive number")
    private Long productId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Duration in months is required")
    @Positive(message = "Duration months must be greater than zero")
    private Integer durationMonths;

    private LocalDate expiryDate;

    @NotBlank(message = "Warranty provider is required")
    @Size(max = 150, message = "Warranty provider must be at most 150 characters")
    private String warrantyProvider;

    private WarrantyStatus status;

    public Warranty() {
    }

    public Warranty(Long id, Long productId, LocalDate startDate, Integer durationMonths,
                    LocalDate expiryDate, String warrantyProvider, WarrantyStatus status) {
        this.id = id;
        this.productId = productId;
        this.startDate = startDate;
        this.durationMonths = durationMonths;
        this.expiryDate = expiryDate;
        this.warrantyProvider = warrantyProvider;
        this.status = status;
    }

    public boolean isExpiredOn(LocalDate date) {
        return expiryDate != null && expiryDate.isBefore(date);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getWarrantyProvider() { return warrantyProvider; }
    public void setWarrantyProvider(String warrantyProvider) { this.warrantyProvider = warrantyProvider; }
    public WarrantyStatus getStatus() { return status; }
    public void setStatus(WarrantyStatus status) { this.status = status; }
}
