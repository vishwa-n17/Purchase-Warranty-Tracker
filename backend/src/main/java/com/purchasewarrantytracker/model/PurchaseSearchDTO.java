package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseSearchDTO(
        Long id,
        String storeName,
        BigDecimal purchasePrice,
        LocalDate purchaseDate
) {}
