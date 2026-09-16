package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.exception.ProductNotFoundException;
import com.purchasewarrantytracker.model.*;
import com.purchasewarrantytracker.repository.ProductRepository;
import com.purchasewarrantytracker.repository.PurchaseRepository;
import com.purchasewarrantytracker.repository.ReceiptRepository;
import com.purchasewarrantytracker.repository.WarrantyRepository;
import com.tracker.entity.ServiceRecord;
import com.tracker.entity.ServiceType;
import com.tracker.repository.ServiceRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IntelligenceService {

    private static final long EXPIRING_SOON_DAYS = 30;

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final WarrantyRepository warrantyRepository;
    private final ReceiptRepository receiptRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final WarrantyService warrantyService;

    public IntelligenceService(ProductRepository productRepository,
                               PurchaseRepository purchaseRepository,
                               WarrantyRepository warrantyRepository,
                               ReceiptRepository receiptRepository,
                               ServiceRecordRepository serviceRecordRepository,
                               WarrantyService warrantyService) {
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
        this.warrantyRepository = warrantyRepository;
        this.receiptRepository = receiptRepository;
        this.serviceRecordRepository = serviceRecordRepository;
        this.warrantyService = warrantyService;
    }

    public ProductLifecycleDTO getProductLifecycle(Long userId, Long productId) {
        Product product = productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        PurchaseSummaryDTO purchase = getPurchaseSummary(userId, productId);
        WarrantyIntelligenceDTO warranty = getWarrantyIntelligence(userId, productId);
        List<ServiceRecordSummaryDTO> services = getServiceSummaries(userId, productId);
        ReceiptSummaryDTO receipt = getReceiptSummary(userId, productId);
        OwnershipCostDTO cost = calculateOwnershipCost(purchase, services);
        ProductHealthDTO health = calculateProductHealth(userId, productId, product.getName(), purchase, warranty, services, cost);
        ProductStatusDTO status = determineProductStatus(warranty, services);

        return new ProductLifecycleDTO(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getBrand(),
                product.getModel(),
                product.getSerialNumber(),
                product.getNotes(),
                purchase,
                warranty,
                services,
                receipt,
                cost,
                health,
                status
        );
    }

    public List<TimelineEventDTO> getProductTimeline(Long userId, Long productId) {
        Product product = productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<TimelineEventDTO> events = new ArrayList<>();

        events.add(new TimelineEventDTO(
                null,
                "PRODUCT",
                "Product Registered",
                product.getName(),
                "📦",
                "#4f46e5"
        ));

        purchaseRepository.findByProductIdAndUserId(productId, userId).forEach(p -> {
            events.add(new TimelineEventDTO(
                    p.getPurchaseDate(),
                    "PURCHASE",
                    "Purchase Recorded",
                    p.getStoreName() + " · " + formatCurrency(p.getPurchasePrice()),
                    "🛒",
                    "#059669"
            ));
        });

        warrantyRepository.findByProductIdAndUserId(productId, userId).ifPresent(w -> {
            events.add(new TimelineEventDTO(
                    w.getStartDate(),
                    "WARRANTY",
                    "Warranty Registered",
                    w.getDurationMonths() + " months · " + w.getWarrantyProvider(),
                    "🛡️",
                    "#d97706"
            ));
        });

        List<Purchase> purchasesForProduct = purchaseRepository.findByProductIdAndUserId(productId, userId);
        if (!purchasesForProduct.isEmpty()) {
            Long firstPurchaseId = purchasesForProduct.get(0).getId();
            receiptRepository.findByPurchaseIdAndUserId(firstPurchaseId, userId).ifPresent(r -> {
                events.add(new TimelineEventDTO(
                        r.getReceiptDate(),
                        "RECEIPT",
                        "Receipt Added",
                        r.getReceiptFilePath(),
                        "📄",
                        "#0891b2"
                ));
            });
        }

        serviceRecordRepository.findByProductIdAndUserId(productId, userId).forEach(s -> {
            events.add(new TimelineEventDTO(
                    s.getServiceDate(),
                    "SERVICE",
                    "Service Recorded",
                    s.getServiceType() + " · " + formatCurrency(s.getCost()),
                    "🔧",
                    "#7c3aed"
            ));
        });

        WarrantyIntelligenceDTO warranty = getWarrantyIntelligence(userId, productId);
        if (warranty != null && warranty.daysRemaining() != null) {
            String statusText;
            if (warranty.daysRemaining() > 0) {
                statusText = "Warranty Active";
            } else {
                statusText = "Warranty Expired";
            }
            events.add(new TimelineEventDTO(
                    LocalDate.now(),
                    "STATUS",
                    statusText,
                    warranty.displayStatus(),
                    warranty.daysRemaining() > 0 ? "🟢" : "🔴",
                    warranty.daysRemaining() > 0 ? "#059669" : "#dc2626"
            ));
        }

        return events.stream()
                .sorted(Comparator.comparing(TimelineEventDTO::date, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public WarrantyIntelligenceDTO getWarrantyIntelligence(Long userId, Long productId) {
        Optional<com.purchasewarrantytracker.model.Warranty> optWarranty = warrantyRepository.findByProductIdAndUserId(productId, userId);
        if (optWarranty.isEmpty()) {
            return null;
        }

        com.purchasewarrantytracker.model.Warranty w = optWarranty.get();
        warrantyService.refreshStatus(w);

        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, w.getExpiryDate());

        long totalDurationDays = ChronoUnit.DAYS.between(w.getStartDate(), w.getExpiryDate());
        long elapsedDays = ChronoUnit.DAYS.between(w.getStartDate(), today);

        double percentageElapsed = totalDurationDays > 0 ? (double) elapsedDays / totalDurationDays * 100 : 0;
        double percentageRemaining = totalDurationDays > 0 ? (double) daysRemaining / totalDurationDays * 100 : 0;

        percentageElapsed = Math.max(0, Math.min(100, percentageElapsed));
        percentageRemaining = Math.max(0, Math.min(100, percentageRemaining));

        String displayStatus;
        if (w.getStatus() == WarrantyStatus.VOID) {
            displayStatus = "VOID";
        } else if (daysRemaining < 0) {
            displayStatus = "EXPIRED " + Math.abs(daysRemaining) + " days ago";
        } else if (daysRemaining <= EXPIRING_SOON_DAYS) {
            displayStatus = "EXPIRING SOON · " + daysRemaining + " days remaining";
        } else {
            displayStatus = "ACTIVE · " + daysRemaining + " days remaining";
        }

        return new WarrantyIntelligenceDTO(
                w.getId(),
                w.getWarrantyProvider(),
                w.getStartDate(),
                w.getDurationMonths(),
                w.getExpiryDate(),
                w.getStatus() != null ? w.getStatus().name() : "UNKNOWN",
                daysRemaining,
                Math.round(percentageElapsed * 100.0) / 100.0,
                Math.round(percentageRemaining * 100.0) / 100.0,
                displayStatus
        );
    }

    public ClaimReadinessDTO getClaimReadiness(Long userId, Long productId) {
        Product product = productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<String> missing = new ArrayList<>();
        int readyItems = 0;

        if (product.getName() != null && !product.getName().isBlank()) readyItems++;
        else missing.add("Product name");

        Optional<Purchase> optPurchase = purchaseRepository.findByProductIdAndUserId(productId, userId).stream().findFirst();
        if (optPurchase.isPresent()) {
            readyItems++;
            if (optPurchase.get().getPurchaseDate() != null) readyItems++;
            else missing.add("Purchase date");
            if (optPurchase.get().getStoreName() != null && !optPurchase.get().getStoreName().isBlank()) readyItems++;
            else missing.add("Store name");
        } else {
            missing.add("Purchase information");
            missing.add("Purchase date");
            missing.add("Store name");
        }

        Optional<com.purchasewarrantytracker.model.Warranty> optWarranty = warrantyRepository.findByProductIdAndUserId(productId, userId);
        if (optWarranty.isPresent()) {
            readyItems++;
            if (optWarranty.get().getWarrantyProvider() != null && !optWarranty.get().getWarrantyProvider().isBlank()) readyItems++;
            else missing.add("Warranty provider");
        } else {
            missing.add("Warranty information");
            missing.add("Warranty provider");
        }

        List<Purchase> purchasesForProduct = purchaseRepository.findByProductIdAndUserId(productId, userId);
        boolean hasReceipt = false;
        if (!purchasesForProduct.isEmpty()) {
            hasReceipt = receiptRepository.findByPurchaseIdAndUserId(purchasesForProduct.get(0).getId(), userId).isPresent();
        }
        if (hasReceipt) {
            readyItems++;
        } else {
            missing.add("Purchase receipt");
        }

        boolean ready = missing.isEmpty();
        return new ClaimReadinessDTO(ready, readyItems + missing.size(), readyItems, missing);
    }

    public OwnershipCostDTO getOwnershipCost(Long userId, Long productId) {
        PurchaseSummaryDTO purchase = getPurchaseSummary(userId, productId);
        List<ServiceRecordSummaryDTO> services = getServiceSummaries(userId, productId);
        return calculateOwnershipCost(purchase, services);
    }

    public ProductHealthDTO getProductHealth(Long userId, Long productId) {
        Product product = productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        PurchaseSummaryDTO purchase = getPurchaseSummary(userId, productId);
        WarrantyIntelligenceDTO warranty = getWarrantyIntelligence(userId, productId);
        List<ServiceRecordSummaryDTO> services = getServiceSummaries(userId, productId);
        OwnershipCostDTO cost = calculateOwnershipCost(purchase, services);
        return calculateProductHealth(userId, productId, product.getName(), purchase, warranty, services, cost);
    }

    public ProductStatusDTO getProductStatus(Long userId, Long productId) {
        WarrantyIntelligenceDTO warranty = getWarrantyIntelligence(userId, productId);
        List<ServiceRecordSummaryDTO> services = getServiceSummaries(userId, productId);
        return determineProductStatus(warranty, services);
    }

    public DashboardInsightsDTO getDashboardInsights(Long userId) {
        List<Product> products = productRepository.findByUserId(userId);
        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        List<ServiceRecord> serviceRecords = serviceRecordRepository.findByUserId(userId);
        List<com.purchasewarrantytracker.model.Warranty> warranties = warrantyService.getAll(userId);

        BigDecimal totalPurchaseSpend = BigDecimal.ZERO;
        if (purchases != null) {
            totalPurchaseSpend = purchases.stream()
                    .map(Purchase::getPurchasePrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal totalServiceSpend = BigDecimal.ZERO;
        if (serviceRecords != null) {
            totalServiceSpend = serviceRecords.stream()
                    .map(ServiceRecord::getCost)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal totalOwnershipCost = totalPurchaseSpend.add(totalServiceSpend);

        BigDecimal averageProductCost = BigDecimal.ZERO;
        String mostExpensiveProduct = null;
        BigDecimal maxCost = BigDecimal.ZERO;

        if (products != null && !products.isEmpty()) {
            Map<Long, BigDecimal> productCosts = new HashMap<>();
            for (Product p : products) {
                PurchaseSummaryDTO ps = getPurchaseSummary(userId, p.getId());
                List<ServiceRecordSummaryDTO> ss = getServiceSummaries(userId, p.getId());
                OwnershipCostDTO oc = calculateOwnershipCost(ps, ss);
                productCosts.put(p.getId(), oc.totalCost());
                if (oc.totalCost().compareTo(maxCost) > 0) {
                    maxCost = oc.totalCost();
                    mostExpensiveProduct = p.getName();
                }
            }

            BigDecimal sum = productCosts.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageProductCost = sum.divide(BigDecimal.valueOf(products.size()), 2, RoundingMode.HALF_UP);
        }

        BigDecimal serviceCostPercentage = BigDecimal.ZERO;
        if (totalOwnershipCost.compareTo(BigDecimal.ZERO) > 0) {
            serviceCostPercentage = totalServiceSpend
                    .divide(totalOwnershipCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        long activeCount = warranties.stream()
                .filter(w -> w.getStatus() == WarrantyStatus.ACTIVE)
                .count();
        long expiredCount = warranties.stream()
                .filter(w -> w.getStatus() == WarrantyStatus.EXPIRED)
                .count();

        LocalDate today = LocalDate.now();
        long expiringSoonCount = warranties.stream()
                .filter(w -> w.getStatus() == WarrantyStatus.ACTIVE)
                .filter(w -> {
                    long days = ChronoUnit.DAYS.between(today, w.getExpiryDate());
                    return days >= 0 && days <= EXPIRING_SOON_DAYS;
                })
                .count();

        List<WarrantyActionItemDTO> warrantyActions = new ArrayList<>();
        if (expiringSoonCount > 0) {
            warranties.stream()
                    .filter(w -> w.getStatus() == WarrantyStatus.ACTIVE)
                    .filter(w -> {
                        long days = ChronoUnit.DAYS.between(today, w.getExpiryDate());
                        return days >= 0 && days <= EXPIRING_SOON_DAYS;
                    })
                    .forEach(w -> {
                        productRepository.findByIdAndUserId(w.getProductId(), userId).ifPresent(p -> {
                            long days = ChronoUnit.DAYS.between(today, w.getExpiryDate());
                            String severity = days <= 7 ? "high" : "medium";
                            warrantyActions.add(new WarrantyActionItemDTO(
                                    p.getId(),
                                    p.getName(),
                                    "Warranty expires in " + days + " days",
                                    severity,
                                    days
                            ));
                        });
                    });
        }

        warranties.stream()
                .filter(w -> w.getStatus() == WarrantyStatus.EXPIRED)
                .forEach(w -> {
                    productRepository.findByIdAndUserId(w.getProductId(), userId).ifPresent(p -> {
                        warrantyActions.add(new WarrantyActionItemDTO(
                                p.getId(),
                                p.getName(),
                                "Warranty expired",
                                "high",
                                -1L
                        ));
                    });
                });

        List<ProductHealthDTO> productHealths = products.stream()
                .map(p -> calculateProductHealth(userId, p.getId(), p.getName(), getPurchaseSummary(userId, p.getId()),
                        getWarrantyIntelligence(userId, p.getId()),
                        getServiceSummaries(userId, p.getId()),
                        calculateOwnershipCost(getPurchaseSummary(userId, p.getId()), getServiceSummaries(userId, p.getId()))))
                .collect(Collectors.toList());

        List<RecentActivityDTO> recentActivity = getRecentActivity(userId, products, purchases, warranties, serviceRecords);

        return new DashboardInsightsDTO(
                totalPurchaseSpend,
                totalServiceSpend,
                totalOwnershipCost,
                averageProductCost,
                mostExpensiveProduct,
                serviceCostPercentage,
                (long) products.size(),
                activeCount,
                expiredCount,
                expiringSoonCount,
                warrantyActions,
                recentActivity,
                productHealths
        );
    }

    public WarrantyOverviewDTO getWarrantyOverview(Long userId) {
        List<com.purchasewarrantytracker.model.Warranty> warranties = warrantyService.getAll(userId);
        LocalDate today = LocalDate.now();

        long protectedCount = warranties.stream()
                .filter(w -> w.getStatus() == WarrantyStatus.ACTIVE)
                .filter(w -> ChronoUnit.DAYS.between(today, w.getExpiryDate()) > EXPIRING_SOON_DAYS)
                .count();

        long expiringSoonCount = warranties.stream()
                .filter(w -> w.getStatus() == WarrantyStatus.ACTIVE)
                .filter(w -> {
                    long days = ChronoUnit.DAYS.between(today, w.getExpiryDate());
                    return days >= 0 && days <= EXPIRING_SOON_DAYS;
                })
                .count();

        long expiredCount = warranties.stream()
                .filter(w -> w.getStatus() == WarrantyStatus.EXPIRED)
                .count();

        List<WarrantyStatusItemDTO> items = warranties.stream()
                .map(w -> {
                    long daysRemaining = ChronoUnit.DAYS.between(today, w.getExpiryDate());
                    long totalDurationDays = ChronoUnit.DAYS.between(w.getStartDate(), w.getExpiryDate());
                    double percentageRemaining = totalDurationDays > 0
                            ? Math.max(0, (double) daysRemaining / totalDurationDays * 100)
                            : 0;
                    return new WarrantyStatusItemDTO(
                            w.getProductId(),
                            productRepository.findByIdAndUserId(w.getProductId(), userId)
                                    .map(Product::getName).orElse("Unknown"),
                            w.getWarrantyProvider(),
                            w.getExpiryDate(),
                            w.getStatus() != null ? w.getStatus().name() : "UNKNOWN",
                            daysRemaining,
                            Math.round(percentageRemaining * 100.0) / 100.0
                    );
                })
                .collect(Collectors.toList());

        return new WarrantyOverviewDTO(protectedCount, expiringSoonCount, expiredCount, items);
    }

    public ServiceAnalyticsDTO getServiceAnalytics(Long userId) {
        List<ServiceRecord> serviceRecords = serviceRecordRepository.findByUserId(userId);

        long totalServices = serviceRecords != null ? serviceRecords.size() : 0;

        BigDecimal totalServiceCost = BigDecimal.ZERO;
        if (serviceRecords != null) {
            totalServiceCost = serviceRecords.stream()
                    .map(ServiceRecord::getCost)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Map<ServiceType, Long> typeCounts = new HashMap<>();
        if (serviceRecords != null) {
            serviceRecords.forEach(s -> {
                ServiceType type = s.getServiceType() != null ? s.getServiceType() : ServiceType.REPAIR;
                typeCounts.merge(type, 1L, Long::sum);
            });
        }

        String mostCommonServiceType = typeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().name())
                .orElse("N/A");

        LocalDate latestServiceDate = null;
        if (serviceRecords != null && !serviceRecords.isEmpty()) {
            latestServiceDate = serviceRecords.stream()
                    .map(ServiceRecord::getServiceDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(null);
        }

        BigDecimal averageServiceCost = BigDecimal.ZERO;
        if (totalServices > 0) {
            averageServiceCost = totalServiceCost.divide(BigDecimal.valueOf(totalServices), 2, RoundingMode.HALF_UP);
        }

        return new ServiceAnalyticsDTO(
                totalServices,
                totalServiceCost,
                mostCommonServiceType,
                latestServiceDate,
                averageServiceCost
        );
    }

    public List<RecentActivityDTO> getRecentActivity(Long userId) {
        List<Product> products = productRepository.findByUserId(userId);
        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        List<com.purchasewarrantytracker.model.Warranty> warranties = warrantyService.getAll(userId);
        List<ServiceRecord> serviceRecords = serviceRecordRepository.findByUserId(userId);

        return getRecentActivity(userId, products, purchases, warranties, serviceRecords);
    }

    public List<ProductComparisonDTO> compareProducts(Long userId, List<Long> productIds) {
        if (productIds == null || productIds.size() < 2 || productIds.size() > 3) {
            throw new IllegalArgumentException("Please select 2 to 3 products to compare");
        }

        return productIds.stream()
                .map(id -> {
                    Product product = productRepository.findByIdAndUserId(id, userId)
                            .orElseThrow(() -> new ProductNotFoundException(id));
                    PurchaseSummaryDTO purchase = getPurchaseSummary(userId, id);
                    WarrantyIntelligenceDTO warranty = getWarrantyIntelligence(userId, id);
                    List<ServiceRecordSummaryDTO> services = getServiceSummaries(userId, id);
                    OwnershipCostDTO cost = calculateOwnershipCost(purchase, services);
                    ProductHealthDTO health = calculateProductHealth(userId, id, product.getName(), purchase, warranty, services, cost);

                    BigDecimal serviceCost = services.stream()
                            .map(ServiceRecordSummaryDTO::cost)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new ProductComparisonDTO(
                            product.getId(),
                            product.getName(),
                            purchase != null ? purchase.purchasePrice() : BigDecimal.ZERO,
                            serviceCost,
                            cost.totalCost(),
                            warranty != null ? warranty.status() : "NONE",
                            warranty != null ? warranty.daysRemaining() : null,
                            services.size(),
                            health != null ? health.score() : 0
                    );
                })
                .collect(Collectors.toList());
    }

    public SearchResultDTO search(Long userId, String query) {
        String lowerQuery = query.toLowerCase();
        List<ProductSearchDTO> productResults = productRepository.findByUserId(userId).stream()
                .filter(p -> matchesQuery(p, lowerQuery))
                .map(p -> new ProductSearchDTO(p.getId(), p.getName(), p.getCategory()))
                .collect(Collectors.toList());

        List<PurchaseSearchDTO> purchaseResults = purchaseRepository.findByUserId(userId).stream()
                .filter(p -> matchesQuery(p, lowerQuery))
                .map(p -> new PurchaseSearchDTO(p.getId(), p.getStoreName(), p.getPurchasePrice(), p.getPurchaseDate()))
                .collect(Collectors.toList());

        List<WarrantySearchDTO> warrantyResults = warrantyService.getAll(userId).stream()
                .filter(w -> matchesQuery(w, lowerQuery))
                .map(w -> new WarrantySearchDTO(w.getId(), w.getWarrantyProvider(), w.getExpiryDate(), w.getStatus() != null ? w.getStatus().name() : "UNKNOWN"))
                .collect(Collectors.toList());

        List<ServiceSearchDTO> serviceResults = serviceRecordRepository.findByUserId(userId).stream()
                .filter(s -> matchesQuery(s, lowerQuery))
                .map(s -> new ServiceSearchDTO(s.getId(), s.getProvider(), s.getCost(), s.getServiceDate()))
                .collect(Collectors.toList());

        return new SearchResultDTO(productResults, purchaseResults, warrantyResults, serviceResults);
    }

    public List<NotificationDTO> getNotifications(Long userId) {
        List<NotificationDTO> notifications = new ArrayList<>();

        List<com.purchasewarrantytracker.model.Warranty> warranties = warrantyService.getAll(userId);
        LocalDate today = LocalDate.now();

        warranties.stream()
                .filter(w -> w.getStatus() == WarrantyStatus.ACTIVE)
                .forEach(w -> {
                    long days = ChronoUnit.DAYS.between(today, w.getExpiryDate());
                    if (days >= 0 && days <= EXPIRING_SOON_DAYS) {
                        productRepository.findByIdAndUserId(w.getProductId(), userId).ifPresent(p -> {
                            String severity = days <= 7 ? "high" : "medium";
                            notifications.add(new NotificationDTO(
                                    "warranty-expiring-" + w.getId(),
                                    "WARRANTY",
                                    "Warranty Expiring Soon",
                                    p.getName() + " warranty expires in " + days + " days",
                                    severity,
                                    false
                            ));
                        });
                    }
                });

        warranties.stream()
                .filter(w -> w.getStatus() == WarrantyStatus.EXPIRED)
                .forEach(w -> {
                    productRepository.findByIdAndUserId(w.getProductId(), userId).ifPresent(p -> {
                        notifications.add(new NotificationDTO(
                                "warranty-expired-" + w.getId(),
                                "WARRANTY",
                                "Warranty Expired",
                                p.getName() + " warranty has expired",
                                "high",
                                false
                        ));
                    });
                });

        List<Product> products = productRepository.findByUserId(userId);
        for (Product p : products) {
            ClaimReadinessDTO readiness = getClaimReadiness(userId, p.getId());
            if (!readiness.ready()) {
                notifications.add(new NotificationDTO(
                        "claim-prep-" + p.getId(),
                        "WARRANTY",
                        "Claim Preparation Needed",
                        p.getName() + " is missing: " + String.join(", ", readiness.missingItems()),
                        "medium",
                        false
                ));
            }
        }

        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        for (Purchase purchase : purchases) {
            boolean hasReceipt = receiptRepository.findByPurchaseIdAndUserId(purchase.getId(), userId).isPresent();
            if (!hasReceipt) {
                productRepository.findByIdAndUserId(purchase.getProductId(), userId).ifPresent(p -> {
                    notifications.add(new NotificationDTO(
                            "receipt-missing-" + purchase.getId(),
                            "DOCUMENT",
                            "Receipt Missing",
                            "No receipt attached for " + p.getName() + " purchase",
                            "low",
                            false
                    ));
                });
            }
        }

        return notifications;
    }

    private PurchaseSummaryDTO getPurchaseSummary(Long userId, Long productId) {
        List<Purchase> purchases = purchaseRepository.findByProductIdAndUserId(productId, userId);
        if (purchases == null || purchases.isEmpty()) {
            return null;
        }
        Purchase p = purchases.get(0);
        return new PurchaseSummaryDTO(p.getId(), p.getPurchaseDate(), p.getPurchasePrice(), p.getStoreName(), p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null);
    }

    private List<ServiceRecordSummaryDTO> getServiceSummaries(Long userId, Long productId) {
        List<ServiceRecord> records = serviceRecordRepository.findByProductIdAndUserId(productId, userId);
        if (records == null) return List.of();
        return records.stream()
                .map(s -> new ServiceRecordSummaryDTO(
                        s.getId(),
                        s.getServiceDate(),
                        s.getProvider(),
                        s.getDescription(),
                        s.getCost(),
                        s.getServiceType().name()
                ))
                .collect(Collectors.toList());
    }

    private ReceiptSummaryDTO getReceiptSummary(Long userId, Long productId) {
        List<Purchase> purchases = purchaseRepository.findByProductIdAndUserId(productId, userId);
        if (purchases == null || purchases.isEmpty()) {
            return null;
        }
        Optional<Receipt> optReceipt = receiptRepository.findByPurchaseIdAndUserId(purchases.get(0).getId(), userId);
        return optReceipt.map(r -> new ReceiptSummaryDTO(r.getId(), r.getReceiptFilePath(), r.getReceiptDate())).orElse(null);
    }

    private OwnershipCostDTO calculateOwnershipCost(PurchaseSummaryDTO purchase, List<ServiceRecordSummaryDTO> services) {
        BigDecimal purchaseCost = purchase != null && purchase.purchasePrice() != null ? purchase.purchasePrice() : BigDecimal.ZERO;
        BigDecimal serviceCost = services != null ? services.stream()
                .map(ServiceRecordSummaryDTO::cost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
        return new OwnershipCostDTO(purchaseCost, serviceCost, purchaseCost.add(serviceCost));
    }

    private ProductHealthDTO calculateProductHealth(Long userId, Long productId, String productName, PurchaseSummaryDTO purchase,
                                                    WarrantyIntelligenceDTO warranty, List<ServiceRecordSummaryDTO> services,
                                                    OwnershipCostDTO cost) {
        int score = 100;
        List<String> positives = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (warranty != null && warranty.daysRemaining() != null && warranty.daysRemaining() > 0) {
            positives.add("Warranty currently active");
        } else if (warranty != null && warranty.daysRemaining() != null && warranty.daysRemaining() <= 0) {
            score -= 25;
            warnings.add("Warranty expired");
        }

        if (warranty != null && warranty.daysRemaining() != null && warranty.daysRemaining() > 0 && warranty.daysRemaining() <= EXPIRING_SOON_DAYS) {
            score -= 10;
            warnings.add("Warranty expiring soon");
        }

        if (services != null) {
            int serviceCount = services.size();
            if (serviceCount == 0) {
                positives.add("No service records");
            } else if (serviceCount == 1) {
                positives.add("Only 1 recorded service");
            } else if (serviceCount <= 3) {
                score -= 5;
                warnings.add(serviceCount + " service records");
            } else {
                score -= 15;
                warnings.add(serviceCount + " service records");
            }
        }

        BigDecimal purchasePrice = purchase != null && purchase.purchasePrice() != null ? purchase.purchasePrice() : BigDecimal.ZERO;
        BigDecimal totalServiceCost = cost != null && cost.serviceCost() != null ? cost.serviceCost() : BigDecimal.ZERO;

        if (purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            double serviceRatio = totalServiceCost.divide(purchasePrice, 4, RoundingMode.HALF_UP).doubleValue();
            if (serviceRatio <= 0.05) {
                positives.add("Low service cost relative to purchase price");
            } else if (serviceRatio <= 0.15) {
                score -= 5;
                warnings.add("Moderate service cost relative to purchase price");
            } else {
                score -= 15;
                warnings.add("High service cost relative to purchase price");
            }
        }

        score = Math.max(0, Math.min(100, score));

        String label;
        if (score >= 80) label = "GOOD";
        else if (score >= 50) label = "FAIR";
        else label = "NEEDS ATTENTION";

        StringBuilder explanation = new StringBuilder();
        if (!positives.isEmpty()) {
            explanation.append("✓ ").append(String.join("\n✓ ", positives));
        }
        if (!warnings.isEmpty()) {
            if (explanation.length() > 0) explanation.append("\n");
            explanation.append("⚠ ").append(String.join("\n⚠ ", warnings));
        }

        return new ProductHealthDTO(productId, productName, score, label, explanation.toString());
    }

    private ProductStatusDTO determineProductStatus(WarrantyIntelligenceDTO warranty, List<ServiceRecordSummaryDTO> services) {
        boolean hasService = services != null && !services.isEmpty();

        if (warranty != null && warranty.daysRemaining() != null) {
            if (warranty.daysRemaining() > 0 && warranty.daysRemaining() <= EXPIRING_SOON_DAYS) {
                return new ProductStatusDTO("ATTENTION NEEDED", "#d97706", "⚠");
            } else if (warranty.daysRemaining() <= 0) {
                if (hasService) {
                    return new ProductStatusDTO("MAINTENANCE RECORDED", "#7c3aed", "🔧");
                }
                return new ProductStatusDTO("WARRANTY EXPIRED", "#dc2626", "🔴");
            }
        }

        if (hasService) {
            return new ProductStatusDTO("MAINTENANCE RECORDED", "#7c3aed", "🔧");
        }

        return new ProductStatusDTO("PROTECTED", "#059669", "🟢");
    }

    private List<RecentActivityDTO> getRecentActivity(Long userId, List<Product> products, List<Purchase> purchases,
                                                      List<com.purchasewarrantytracker.model.Warranty> warranties,
                                                      List<ServiceRecord> serviceRecords) {
        List<RecentActivityDTO> activities = new ArrayList<>();

        products.stream()
                .max(Comparator.comparing(Product::getId))
                .ifPresent(p -> activities.add(new RecentActivityDTO(
                        "PRODUCT",
                        "Product Added",
                        p.getName() + " (" + p.getCategory() + ")",
                        LocalDate.now()
                )));

        if (purchases != null) {
            purchases.stream()
                    .max(Comparator.comparing(Purchase::getId))
                    .ifPresent(p -> {
                        productRepository.findByIdAndUserId(p.getProductId(), userId).ifPresent(prod -> {
                            activities.add(new RecentActivityDTO(
                                    "PURCHASE",
                                    "Purchase Recorded",
                                    prod.getName() + " · " + formatCurrency(p.getPurchasePrice()) + " · " + p.getStoreName(),
                                    p.getPurchaseDate()
                            ));
                        });
                    });
        }

        if (warranties != null) {
            warranties.stream()
                    .max(Comparator.comparing(com.purchasewarrantytracker.model.Warranty::getId))
                    .ifPresent(w -> {
                        productRepository.findByIdAndUserId(w.getProductId(), userId).ifPresent(prod -> {
                            activities.add(new RecentActivityDTO(
                                    "WARRANTY",
                                    "Warranty Added",
                                    prod.getName() + " · " + w.getDurationMonths() + " months",
                                    w.getStartDate()
                            ));
                        });
                    });
        }

        if (serviceRecords != null) {
            serviceRecords.stream()
                    .max(Comparator.comparing(ServiceRecord::getId))
                    .ifPresent(s -> {
                        activities.add(new RecentActivityDTO(
                                "SERVICE",
                                "Service Record Added",
                                s.getServiceType().name() + " · " + formatCurrency(s.getCost()),
                                s.getServiceDate()
                        ));
                    });
        }

        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDTO::date, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .collect(Collectors.toList());
    }

    private boolean matchesQuery(Product p, String query) {
        return (p.getName() != null && p.getName().toLowerCase().contains(query))
                || (p.getCategory() != null && p.getCategory().toLowerCase().contains(query))
                || (p.getBrand() != null && p.getBrand().toLowerCase().contains(query))
                || (p.getModel() != null && p.getModel().toLowerCase().contains(query));
    }

    private boolean matchesQuery(Purchase p, String query) {
        return (p.getStoreName() != null && p.getStoreName().toLowerCase().contains(query))
                || String.valueOf(p.getPurchasePrice()).contains(query)
                || String.valueOf(p.getPurchaseDate()).contains(query);
    }

    private boolean matchesQuery(com.purchasewarrantytracker.model.Warranty w, String query) {
        return (w.getWarrantyProvider() != null && w.getWarrantyProvider().toLowerCase().contains(query))
                || String.valueOf(w.getExpiryDate()).contains(query)
                || (w.getStatus() != null && w.getStatus().name().toLowerCase().contains(query));
    }

    private boolean matchesQuery(ServiceRecord s, String query) {
        return (s.getProvider() != null && s.getProvider().toLowerCase().contains(query))
                || (s.getDescription() != null && s.getDescription().toLowerCase().contains(query))
                || String.valueOf(s.getServiceType()).contains(query)
                || String.valueOf(s.getServiceDate()).contains(query);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return "₹" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
