package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ServiceSearchDTO(
        Long id,
        String provider,
        BigDecimal cost,
        LocalDate serviceDate
) {}
