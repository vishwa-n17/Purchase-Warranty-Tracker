package com.purchasewarrantytracker.model;

public record NotificationDTO(
        String id,
        String type,
        String title,
        String message,
        String severity,
        Boolean read
) {}
