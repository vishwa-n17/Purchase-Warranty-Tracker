package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TimelineEventDTO(
        LocalDate date,
        String type,
        String title,
        String description,
        String icon,
        String color
) {}
