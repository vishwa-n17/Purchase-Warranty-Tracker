package com.purchasewarrantytracker.model;

import java.math.BigDecimal;

public record ProductComparisonDTO(
        Long productId,
        String productName,
        BigDecimal purchaseCost,
        BigDecimal serviceCost,
        BigDecimal totalCost,
        String warrantyStatus,
        Long daysRemaining,
        Integer serviceCount,
        Integer healthScore
) {}
