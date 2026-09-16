package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProductLifecycleDTO(
        Long productId,
        String name,
        String category,
        String brand,
        String model,
        String serialNumber,
        String notes,
        PurchaseSummaryDTO purchase,
        WarrantyIntelligenceDTO warranty,
        List<ServiceRecordSummaryDTO> serviceRecords,
        ReceiptSummaryDTO receipt,
        OwnershipCostDTO ownershipCost,
        ProductHealthDTO health,
        ProductStatusDTO status
) {}
