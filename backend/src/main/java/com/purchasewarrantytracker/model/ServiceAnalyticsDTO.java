package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ServiceAnalyticsDTO(
        Long totalServices,
        BigDecimal totalServiceCost,
        String mostCommonServiceType,
        LocalDate latestServiceDate,
        BigDecimal averageServiceCost
) {}
