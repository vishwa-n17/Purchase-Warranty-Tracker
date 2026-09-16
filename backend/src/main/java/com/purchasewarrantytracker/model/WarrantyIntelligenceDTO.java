package com.purchasewarrantytracker.model;

import java.time.LocalDate;

public record WarrantyIntelligenceDTO(
        Long id,
        String warrantyProvider,
        LocalDate startDate,
        Integer durationMonths,
        LocalDate expiryDate,
        String status,
        Long daysRemaining,
        Double percentageElapsed,
        Double percentageRemaining,
        String displayStatus
) {}
