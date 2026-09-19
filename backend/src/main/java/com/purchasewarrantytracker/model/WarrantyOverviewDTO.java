package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.util.List;

public record WarrantyOverviewDTO(
        Long protectedCount,
        Long expiringSoonCount,
        Long expiredCount,
        List<WarrantyStatusItemDTO> items
) {}
