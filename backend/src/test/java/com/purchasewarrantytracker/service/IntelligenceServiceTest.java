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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntelligenceServiceTest {

    private static final long TEST_USER_ID = 1L;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private WarrantyRepository warrantyRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private ServiceRecordRepository serviceRecordRepository;

    @Mock
    private WarrantyService warrantyService;

    @InjectMocks
    private IntelligenceService intelligenceService;

    @Test
    void getWarrantyIntelligenceReturnsActiveStatus() {
        com.purchasewarrantytracker.model.Warranty warranty = new com.purchasewarrantytracker.model.Warranty(
                1L, TEST_USER_ID, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE
        );
        when(warrantyRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(warranty));
        doNothing().when(warrantyService).refreshStatus(any());

        WarrantyIntelligenceDTO dto = intelligenceService.getWarrantyIntelligence(TEST_USER_ID, 1L);

        assertNotNull(dto);
        assertEquals("Lenovo", dto.warrantyProvider());
        assertTrue(dto.daysRemaining() > 0);
        assertEquals("ACTIVE", dto.status());
    }

    @Test
    void getWarrantyIntelligenceReturnsNullWhenNoWarranty() {
        when(warrantyRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.empty());

        WarrantyIntelligenceDTO dto = intelligenceService.getWarrantyIntelligence(TEST_USER_ID, 1L);

        assertNull(dto);
    }

    @Test
    void getClaimReadinessReturnsReadyWhenAllInfoPresent() {
        Product product = new Product(1L, TEST_USER_ID, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SN-1", "Notes");
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));

        Purchase purchase = new Purchase(1L, TEST_USER_ID, 1L, LocalDate.now(), new BigDecimal("1000"), "Store", PaymentMethod.CARD);
        when(purchaseRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(List.of(purchase));

        com.purchasewarrantytracker.model.Warranty warranty = new com.purchasewarrantytracker.model.Warranty(
                1L, TEST_USER_ID, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE
        );
        when(warrantyRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(warranty));

        Receipt receipt = new Receipt(1L, TEST_USER_ID, 1L, "receipts/2026/laptop.pdf", LocalDate.now());
        when(receiptRepository.findByPurchaseIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(receipt));

        ClaimReadinessDTO dto = intelligenceService.getClaimReadiness(TEST_USER_ID, 1L);

        assertTrue(dto.ready());
        assertEquals(7, dto.readyItems());
        assertTrue(dto.missingItems().isEmpty());
    }

    @Test
    void getClaimReadinessReturnsMissingItems() {
        Product product = new Product(1L, TEST_USER_ID, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SN-1", "Notes");
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));

        when(purchaseRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(List.of());

        when(warrantyRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.empty());

        ClaimReadinessDTO dto = intelligenceService.getClaimReadiness(TEST_USER_ID, 1L);

        assertFalse(dto.ready());
        assertTrue(dto.missingItems().contains("Purchase information"));
        assertTrue(dto.missingItems().contains("Warranty information"));
        assertTrue(dto.missingItems().contains("Purchase receipt"));
    }

    @Test
    void getOwnershipCostCalculatesCorrectly() {
        Purchase purchase = new Purchase(1L, TEST_USER_ID, 1L, LocalDate.now(), new BigDecimal("1000.00"), "Store", PaymentMethod.CARD);
        when(purchaseRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(List.of(purchase));

        Product product = new Product(1L, TEST_USER_ID, "Laptop", "Electronics", null, null, null, null);
        ServiceRecord sr = new ServiceRecord(1L, product, LocalDate.now(), "Tech", "Repair", new BigDecimal("200.00"), ServiceType.REPAIR);
        when(serviceRecordRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(List.of(sr));

        OwnershipCostDTO dto = intelligenceService.getOwnershipCost(TEST_USER_ID, 1L);

        assertEquals(new BigDecimal("1000.00"), dto.purchaseCost());
        assertEquals(new BigDecimal("200.00"), dto.serviceCost());
        assertEquals(new BigDecimal("1200.00"), dto.totalCost());
    }

    @Test
    void getProductHealthReturnsValidScore() {
        Product product = new Product(1L, TEST_USER_ID, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SN-1", "Notes");
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));

        Purchase purchase = new Purchase(1L, TEST_USER_ID, 1L, LocalDate.now(), new BigDecimal("1000.00"), "Store", PaymentMethod.CARD);
        when(purchaseRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(List.of(purchase));

        com.purchasewarrantytracker.model.Warranty warranty = new com.purchasewarrantytracker.model.Warranty(
                1L, TEST_USER_ID, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE
        );
        when(warrantyRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(warranty));
        doNothing().when(warrantyService).refreshStatus(any());

        ProductHealthDTO dto = intelligenceService.getProductHealth(TEST_USER_ID, 1L);

        assertNotNull(dto);
        assertTrue(dto.score() >= 0 && dto.score() <= 100);
        assertNotNull(dto.label());
    }

    @Test
    void getProductStatusReturnsProtectedWhenActive() {
        com.purchasewarrantytracker.model.Warranty warranty = new com.purchasewarrantytracker.model.Warranty(
                1L, TEST_USER_ID, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE
        );
        when(warrantyRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(warranty));
        doNothing().when(warrantyService).refreshStatus(any());

        ProductStatusDTO dto = intelligenceService.getProductStatus(TEST_USER_ID, 1L);

        assertEquals("PROTECTED", dto.status());
    }

    @Test
    void searchReturnsResultsMatchingQuery() {
        Product product = new Product(1L, TEST_USER_ID, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SN-1", "Notes");
        when(productRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(product));

        Purchase purchase = new Purchase(1L, TEST_USER_ID, 1L, LocalDate.now(), new BigDecimal("1000.00"), "Store", PaymentMethod.CARD);
        when(purchaseRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(purchase));

        com.purchasewarrantytracker.model.Warranty warranty = new com.purchasewarrantytracker.model.Warranty(
                1L, TEST_USER_ID, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE
        );
        when(warrantyService.getAll(TEST_USER_ID)).thenReturn(List.of(warranty));

        SearchResultDTO dto = intelligenceService.search(TEST_USER_ID, "Laptop");

        assertEquals(1, dto.products().size());
        assertEquals("Laptop", dto.products().get(0).name());
    }

    @Test
    void compareProductsRequiresTwoToThreeProducts() {
        assertThrows(IllegalArgumentException.class, () -> intelligenceService.compareProducts(TEST_USER_ID, List.of(1L)));
        assertThrows(IllegalArgumentException.class, () -> intelligenceService.compareProducts(TEST_USER_ID, List.of()));
    }
}
