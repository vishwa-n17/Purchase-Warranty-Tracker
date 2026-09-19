package com.purchasewarrantytracker.model;

import java.time.LocalDate;

public record RecentActivityDTO(
        String type,
        String title,
        String description,
        LocalDate date
) {}
