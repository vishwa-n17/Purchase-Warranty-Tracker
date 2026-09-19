package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ServiceRecordSummaryDTO(
        Long id,
        LocalDate serviceDate,
        String provider,
        String description,
        BigDecimal cost,
        String serviceType
) {}
