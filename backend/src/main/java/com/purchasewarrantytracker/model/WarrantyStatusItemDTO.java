package com.purchasewarrantytracker.model;

import java.time.LocalDate;

public record WarrantyStatusItemDTO(
        Long productId,
        String productName,
        String provider,
        LocalDate expiryDate,
        String status,
        Long daysRemaining,
        Double percentageRemaining
) {}
