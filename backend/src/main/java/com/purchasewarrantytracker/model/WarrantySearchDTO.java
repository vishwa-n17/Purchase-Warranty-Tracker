package com.purchasewarrantytracker.model;

import java.time.LocalDate;

public record WarrantySearchDTO(
        Long id,
        String warrantyProvider,
        LocalDate expiryDate,
        String status
) {}
