package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseSummaryDTO(
        Long id,
        LocalDate purchaseDate,
        BigDecimal purchasePrice,
        String storeName,
        String paymentMethod
) {}
