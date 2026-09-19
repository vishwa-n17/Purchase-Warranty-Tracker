package com.purchasewarrantytracker.model;

import java.util.List;

public record SearchResultDTO(
        List<ProductSearchDTO> products,
        List<PurchaseSearchDTO> purchases,
        List<WarrantySearchDTO> warranties,
        List<ServiceSearchDTO> services
) {}
