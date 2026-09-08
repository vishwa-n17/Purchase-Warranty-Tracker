package com.purchasewarrantytracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.purchasewarrantytracker.exception.ProductNotFoundException;
import com.purchasewarrantytracker.exception.WarrantyNotFoundException;
import com.purchasewarrantytracker.model.Product;
import com.purchasewarrantytracker.model.Warranty;
import com.purchasewarrantytracker.model.WarrantyStatus;
import com.purchasewarrantytracker.repository.ProductRepository;
import com.purchasewarrantytracker.repository.WarrantyRepository;

@ExtendWith(MockitoExtension.class)
class WarrantyServiceTest {

    private static final long TEST_USER_ID = 1L;

    @Mock
    private WarrantyRepository warrantyRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WarrantyService warrantyService;

    @Test
    void createSavesValidWarrantyAndCalculatesExpiryDate() {
        LocalDate startDate = LocalDate.now();
        Warranty warranty = new Warranty(null, TEST_USER_ID, 1L, startDate, 12, null, "Lenovo", null);
        Product product = sampleProduct(1L);

        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));
        when(warrantyRepository.existsByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(false);
        when(warrantyRepository.save(warranty)).thenAnswer(invocation -> {
            Warranty w = invocation.getArgument(0);
            w.setId(10L);
            return w;
        });

        Warranty saved = warrantyService.create(TEST_USER_ID, warranty);

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        assertEquals(startDate.plusMonths(12), saved.getExpiryDate());
        assertEquals(WarrantyStatus.ACTIVE, saved.getStatus());
        verify(warrantyRepository).save(warranty);
    }

    @Test
    void createSetsStatusExpiredWhenEndDateInPast() {
        LocalDate startDate = LocalDate.now().minusMonths(24);
        Warranty warranty = new Warranty(null, TEST_USER_ID, 1L, startDate, 12, null, "Lenovo", null);
        Product product = sampleProduct(1L);

        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));
        when(warrantyRepository.existsByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(false);
        when(warrantyRepository.save(warranty)).thenAnswer(invocation -> {
            Warranty w = invocation.getArgument(0);
            w.setId(10L);
            return w;
        });

        Warranty saved = warrantyService.create(TEST_USER_ID, warranty);

        assertEquals(WarrantyStatus.EXPIRED, saved.getStatus());
        assertEquals(startDate.plusMonths(12), saved.getExpiryDate());
    }

    @Test
    void createRejectsNonExistentProduct() {
        Warranty warranty = new Warranty(null, TEST_USER_ID, 999L, LocalDate.now(), 12, null, "Lenovo", null);
        when(productRepository.findByIdAndUserId(999L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> warrantyService.create(TEST_USER_ID, warranty));
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateWarrantyForSameProduct() {
        Warranty warranty = new Warranty(null, TEST_USER_ID, 1L, LocalDate.now(), 12, null, "Lenovo", null);
        Product product = sampleProduct(1L);

        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));
        when(warrantyRepository.existsByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> warrantyService.create(TEST_USER_ID, warranty));
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void createRejectsNullOrZeroDuration() {
        Warranty warranty = new Warranty(null, TEST_USER_ID, 1L, LocalDate.now(), 0, null, "Lenovo", null);

        assertThrows(IllegalArgumentException.class, () -> warrantyService.create(TEST_USER_ID, warranty));
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingStartDate() {
        Warranty warranty = new Warranty(null, TEST_USER_ID, 1L, null, 12, null, "Lenovo", null);

        assertThrows(IllegalArgumentException.class, () -> warrantyService.create(TEST_USER_ID, warranty));
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingProvider() {
        Warranty warranty = new Warranty(null, TEST_USER_ID, 1L, LocalDate.now(), 12, null, "  ", null);

        assertThrows(IllegalArgumentException.class, () -> warrantyService.create(TEST_USER_ID, warranty));
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void getAllReturnsAllWarranties() {
        Product product = sampleProduct(1L);
        Warranty warranty1 = new Warranty(1L, TEST_USER_ID, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE);
        Warranty warranty2 = new Warranty(2L, TEST_USER_ID, 1L, LocalDate.now(), 24, LocalDate.now().plusMonths(24), "Lenovo Premium", WarrantyStatus.ACTIVE);

        when(warrantyRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(warranty1, warranty2));

        List<Warranty> warranties = warrantyService.getAll(TEST_USER_ID);

        assertEquals(2, warranties.size());
        verify(warrantyRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    void getByIdReturnsWarrantyWhenExists() {
        Warranty warranty = new Warranty(1L, TEST_USER_ID, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE);
        when(warrantyRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(warranty));

        Warranty result = warrantyService.getById(TEST_USER_ID, 1L);

        assertEquals(1L, result.getId());
        assertEquals("Lenovo", result.getWarrantyProvider());
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(warrantyRepository.findByIdAndUserId(99L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(WarrantyNotFoundException.class, () -> warrantyService.getById(TEST_USER_ID, 99L));
    }

    @Test
    void getByProductIdReturnsWarrantyWhenFound() {
        Product product = sampleProduct(1L);
        Warranty warranty = new Warranty(1L, TEST_USER_ID, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE);

        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));
        when(warrantyRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(warranty));

        Warranty result = warrantyService.getByProductId(TEST_USER_ID, 1L);

        assertEquals(1L, result.getId());
        assertEquals("Lenovo", result.getWarrantyProvider());
    }

    @Test
    void getByProductIdThrowsWhenProductNotFound() {
        when(productRepository.findByIdAndUserId(99L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> warrantyService.getByProductId(TEST_USER_ID, 99L));
        verify(warrantyRepository, never()).findByProductIdAndUserId(eq(99L), any(Long.class));
    }

    @Test
    void getByProductIdThrowsWhenWarrantyNotFound() {
        Product product = sampleProduct(1L);
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));
        when(warrantyRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(WarrantyNotFoundException.class, () -> warrantyService.getByProductId(TEST_USER_ID, 1L));
    }

    @Test
    void updateUpdatesWarrantySuccessfully() {
        LocalDate startDate = LocalDate.now();
        Warranty existing = new Warranty(1L, TEST_USER_ID, 1L, startDate, 24, startDate.plusMonths(24), "Lenovo", WarrantyStatus.ACTIVE);
        Warranty updatedDetails = new Warranty(null, TEST_USER_ID, 1L, startDate, 24, null, "Lenovo Premium", null);

        when(warrantyRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(sampleProduct(1L)));
        when(warrantyRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(existing));
        when(warrantyRepository.update(updatedDetails)).thenReturn(true);

        Warranty result = warrantyService.update(TEST_USER_ID, 1L, updatedDetails);

        assertEquals(1L, result.getId());
        assertEquals(startDate.plusMonths(24), result.getExpiryDate());
        assertEquals("Lenovo Premium", result.getWarrantyProvider());
        verify(warrantyRepository).update(updatedDetails);
    }

    @Test
    void updateThrowsWhenWarrantyNotFound() {
        Warranty updatedDetails = new Warranty(null, TEST_USER_ID, 1L, LocalDate.now(), 24, null, "Lenovo Premium", null);
        when(warrantyRepository.findByIdAndUserId(99L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(WarrantyNotFoundException.class, () -> warrantyService.update(TEST_USER_ID, 99L, updatedDetails));
        verify(warrantyRepository, never()).update(any());
    }

    @Test
    void deleteRemovesWarranty() {
        Warranty existing = new Warranty(1L, TEST_USER_ID, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE);
        when(warrantyRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(existing));
        when(warrantyRepository.deleteByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);

        warrantyService.delete(TEST_USER_ID, 1L);

        verify(warrantyRepository).deleteByIdAndUserId(1L, TEST_USER_ID);
    }

    @Test
    void deleteThrowsWhenWarrantyNotFound() {
        when(warrantyRepository.findByIdAndUserId(99L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(WarrantyNotFoundException.class, () -> warrantyService.delete(TEST_USER_ID, 99L));
        verify(warrantyRepository, never()).deleteByIdAndUserId(eq(99L), any(Long.class));
    }

    private Product sampleProduct(Long id) {
        return new Product(id, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SERIAL-1", "Notes");
    }
}
