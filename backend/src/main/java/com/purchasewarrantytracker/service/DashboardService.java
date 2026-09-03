package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.model.DashboardDTO;
import com.purchasewarrantytracker.model.Product;
import com.purchasewarrantytracker.model.Purchase;
import com.purchasewarrantytracker.model.Warranty;
import com.purchasewarrantytracker.model.WarrantyStatus;
import com.purchasewarrantytracker.repository.ProductRepository;
import com.purchasewarrantytracker.repository.PurchaseRepository;
import com.tracker.entity.ServiceRecord;
import com.tracker.repository.ServiceRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final WarrantyService warrantyService;
    private final ServiceRecordRepository serviceRecordRepository;

    public DashboardService(ProductRepository productRepository,
                            PurchaseRepository purchaseRepository,
                            WarrantyService warrantyService,
                            ServiceRecordRepository serviceRecordRepository) {
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
        this.warrantyService = warrantyService;
        this.serviceRecordRepository = serviceRecordRepository;
    }

    public DashboardDTO getDashboardSummary() {
        List<Product> products = productRepository.findAll();
        long totalProductsCount = products != null ? products.size() : 0L;

        List<Purchase> purchases = purchaseRepository.findAll();
        BigDecimal totalPurchaseSpend = BigDecimal.ZERO;
        if (purchases != null) {
            totalPurchaseSpend = purchases.stream()
                    .map(Purchase::getPurchasePrice)
                    .filter(price -> price != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        List<ServiceRecord> serviceRecords = serviceRecordRepository.findAll();
        BigDecimal totalServiceSpend = BigDecimal.ZERO;
        if (serviceRecords != null) {
            totalServiceSpend = serviceRecords.stream()
                    .map(ServiceRecord::getCost)
                    .filter(cost -> cost != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        List<Warranty> warranties = warrantyService.getAll();
        long activeWarrantiesCount = 0L;
        long expiredWarrantiesCount = 0L;
        if (warranties != null) {
            activeWarrantiesCount = warranties.stream()
                    .filter(w -> w.getStatus() == WarrantyStatus.ACTIVE)
                    .count();
            expiredWarrantiesCount = warranties.stream()
                    .filter(w -> w.getStatus() == WarrantyStatus.EXPIRED)
                    .count();
        }

        return new DashboardDTO(
                totalProductsCount,
                totalPurchaseSpend,
                totalServiceSpend,
                activeWarrantiesCount,
                expiredWarrantiesCount
        );
    }
}

