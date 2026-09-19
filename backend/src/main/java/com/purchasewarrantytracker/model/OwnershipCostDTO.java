package com.purchasewarrantytracker.model;

import java.math.BigDecimal;

public record OwnershipCostDTO(
        BigDecimal purchaseCost,
        BigDecimal serviceCost,
        BigDecimal totalCost
) {}
