package com.purchasewarrantytracker.model;

import java.time.LocalDate;

public record ReceiptSummaryDTO(
        Long id,
        String receiptFilePath,
        LocalDate receiptDate
) {}
