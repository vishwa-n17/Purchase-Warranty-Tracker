package com.purchasewarrantytracker.model;

public record ProductHealthDTO(
        Long productId,
        String productName,
        Integer score,
        String label,
        String explanation
) {}
