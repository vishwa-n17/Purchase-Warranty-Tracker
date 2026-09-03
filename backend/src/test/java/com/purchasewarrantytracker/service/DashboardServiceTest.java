package com.purchasewarrantytracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.purchasewarrantytracker.model.DashboardDTO;
import com.purchasewarrantytracker.model.PaymentMethod;
import com.purchasewarrantytracker.model.Product;
import com.purchasewarrantytracker.model.Purchase;
import com.purchasewarrantytracker.model.Warranty;
import com.purchasewarrantytracker.model.WarrantyStatus;
import com.purchasewarrantytracker.repository.ProductRepository;
import com.purchasewarrantytracker.repository.PurchaseRepository;
import com.tracker.entity.ServiceRecord;
import com.tracker.entity.ServiceType;
import com.tracker.repository.ServiceRecordRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private WarrantyService warrantyService;

    @Mock
    private ServiceRecordRepository serviceRecordRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardSummaryComputesCorrectMetrics() {
        Product p1 = new Product(1L, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SN-1", "Notes");
        Product p2 = new Product(2L, "Purifier", "Appliance", "Aquaguard", "Delight", "SN-2", "Notes");
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        Purchase pur1 = new Purchase(1L, 1L, LocalDate.now(), new BigDecimal("50000.00"), "Store A", PaymentMethod.UPI);
        Purchase pur2 = new Purchase(2L, 2L, LocalDate.now(), new BigDecimal("12000.00"), "Store B", PaymentMethod.CARD);
        when(purchaseRepository.findAll()).thenReturn(List.of(pur1, pur2));

        Product entityProduct = new Product(1L, "Laptop", "Electronics", null, null, null, null);
        ServiceRecord sr1 = new ServiceRecord(1L, entityProduct, LocalDate.now(), "Care", "Fixed fan", new BigDecimal("500.00"), ServiceType.REPAIR);
        ServiceRecord sr2 = new ServiceRecord(2L, entityProduct, LocalDate.now(), "Care", "Maintenance", new BigDecimal("300.00"), ServiceType.MAINTENANCE);
        when(serviceRecordRepository.findAll()).thenReturn(List.of(sr1, sr2));

        Warranty w1 = new Warranty(1L, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE);
        Warranty w2 = new Warranty(2L, 2L, LocalDate.now().minusMonths(24), 12, LocalDate.now().minusMonths(12), "Aquaguard", WarrantyStatus.EXPIRED);
        when(warrantyService.getAll()).thenReturn(List.of(w1, w2));

        DashboardDTO summary = dashboardService.getDashboardSummary();

        assertNotNull(summary);
        assertEquals(2L, summary.totalProductsCount());
        assertEquals(new BigDecimal("62000.00"), summary.totalPurchaseSpend());
        assertEquals(new BigDecimal("800.00"), summary.totalServiceSpend());
        assertEquals(1L, summary.activeWarrantiesCount());
        assertEquals(1L, summary.expiredWarrantiesCount());
    }

    @Test
    void getDashboardSummaryHandlesEmptyDataGracefully() {
        when(productRepository.findAll()).thenReturn(List.of());
        when(purchaseRepository.findAll()).thenReturn(List.of());
        when(serviceRecordRepository.findAll()).thenReturn(List.of());
        when(warrantyService.getAll()).thenReturn(List.of());

        DashboardDTO summary = dashboardService.getDashboardSummary();

        assertNotNull(summary);
        assertEquals(0L, summary.totalProductsCount());
        assertEquals(BigDecimal.ZERO, summary.totalPurchaseSpend());
        assertEquals(BigDecimal.ZERO, summary.totalServiceSpend());
        assertEquals(0L, summary.activeWarrantiesCount());
        assertEquals(0L, summary.expiredWarrantiesCount());
    }
}