package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ClaimReadinessDTO(
        Boolean ready,
        Integer totalItems,
        Integer readyItems,
        List<String> missingItems
) {}
