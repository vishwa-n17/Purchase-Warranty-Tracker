package com.purchasewarrantytracker.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Purchase {

    private Long id;

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be a positive number")
    private Long productId;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.00", message = "Purchase price cannot be negative")
    private BigDecimal purchasePrice;

    @NotBlank(message = "Store name is required")
    @Size(max = 150, message = "Store name must be at most 150 characters")
    private String storeName;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    public Purchase() {
    }

    public Purchase(Long id, Long productId, LocalDate purchaseDate, BigDecimal purchasePrice,
                    String storeName, PaymentMethod paymentMethod) {
        this.id = id;
        this.productId = productId;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
        this.storeName = storeName;
        this.paymentMethod = paymentMethod;
    }

    public boolean hasReceipt(Receipt receipt) {
        return receipt != null && id != null && id.equals(receipt.getPurchaseId());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
