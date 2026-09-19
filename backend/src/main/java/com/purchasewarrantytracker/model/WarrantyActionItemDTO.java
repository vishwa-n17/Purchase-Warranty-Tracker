package com.purchasewarrantytracker.model;

public record WarrantyActionItemDTO(
        Long productId,
        String productName,
        String action,
        String severity,
        Long daysRemaining
) {}
