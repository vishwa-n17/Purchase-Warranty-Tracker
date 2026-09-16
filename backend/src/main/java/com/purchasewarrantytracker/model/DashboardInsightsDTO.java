package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.util.List;

public record DashboardInsightsDTO(
        BigDecimal totalPurchaseSpend,
        BigDecimal totalServiceSpend,
        BigDecimal totalOwnershipCost,
        BigDecimal averageProductCost,
        String mostExpensiveProduct,
        BigDecimal serviceCostPercentage,
        Long totalProductsCount,
        Long activeWarrantiesCount,
        Long expiredWarrantiesCount,
        Long expiringSoonCount,
        List<WarrantyActionItemDTO> warrantyActions,
        List<RecentActivityDTO> recentActivity,
        List<ProductHealthDTO> productHealths
) {}
