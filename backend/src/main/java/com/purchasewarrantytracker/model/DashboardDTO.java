package com.purchasewarrantytracker.model;

import java.math.BigDecimal;

public record DashboardDTO(
        Long totalProductsCount,
        BigDecimal totalPurchaseSpend,
        BigDecimal totalServiceSpend,
        Long activeWarrantiesCount,
        Long expiredWarrantiesCount
) {}

