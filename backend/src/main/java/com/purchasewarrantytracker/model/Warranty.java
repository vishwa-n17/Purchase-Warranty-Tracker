package com.purchasewarrantytracker.model;

import java.time.LocalDate;

public class Warranty {

    private Long id;
    private Long productId;
    private LocalDate startDate;
    private Integer durationMonths;
    private LocalDate expiryDate;
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
