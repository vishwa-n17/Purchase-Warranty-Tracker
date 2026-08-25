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

    @Mock
    private WarrantyRepository warrantyRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WarrantyService warrantyService;

    @Test
    void createSavesValidWarrantyAndCalculatesExpiryDate() {
        LocalDate startDate = LocalDate.now();
        Warranty warranty = new Warranty(null, 1L, startDate, 12, null, "Lenovo", null);
        Product product = sampleProduct(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warrantyRepository.existsByProductId(1L)).thenReturn(false);
        when(warrantyRepository.save(warranty)).thenAnswer(invocation -> {
            Warranty w = invocation.getArgument(0);
            w.setId(10L);
            return w;
        });

        Warranty saved = warrantyService.create(warranty);

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        assertEquals(startDate.plusMonths(12), saved.getExpiryDate());
        assertEquals(WarrantyStatus.ACTIVE, saved.getStatus());
        verify(warrantyRepository).save(warranty);
    }

    @Test
    void createSetsStatusExpiredWhenEndDateInPast() {
        LocalDate startDate = LocalDate.now().minusMonths(24);
        Warranty warranty = new Warranty(null, 1L, startDate, 12, null, "Lenovo", null);
        Product product = sampleProduct(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warrantyRepository.existsByProductId(1L)).thenReturn(false);
        when(warrantyRepository.save(warranty)).thenAnswer(invocation -> {
            Warranty w = invocation.getArgument(0);
            w.setId(10L);
            return w;
        });

        Warranty saved = warrantyService.create(warranty);

        assertEquals(WarrantyStatus.EXPIRED, saved.getStatus());
        assertEquals(startDate.plusMonths(12), saved.getExpiryDate());
    }

    @Test
    void createRejectsNonExistentProduct() {
        Warranty warranty = new Warranty(null, 999L, LocalDate.now(), 12, null, "Lenovo", null);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> warrantyService.create(warranty));
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateWarrantyForSameProduct() {
        Warranty warranty = new Warranty(null, 1L, LocalDate.now(), 12, null, "Lenovo", null);
        Product product = sampleProduct(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warrantyRepository.existsByProductId(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> warrantyService.create(warranty));
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void createRejectsNullOrZeroDuration() {
        Warranty warranty = new Warranty(null, 1L, LocalDate.now(), 0, null, "Lenovo", null);

        assertThrows(IllegalArgumentException.class, () -> warrantyService.create(warranty));
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingStartDate() {
        Warranty warranty = new Warranty(null, 1L, null, 12, null, "Lenovo", null);

        assertThrows(IllegalArgumentException.class, () -> warrantyService.create(warranty));
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingProvider() {
        Warranty warranty = new Warranty(null, 1L, LocalDate.now(), 12, null, "  ", null);

        assertThrows(IllegalArgumentException.class, () -> warrantyService.create(warranty));
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void getAllReturnsWarranties() {
        Warranty w1 = new Warranty(1L, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE);
        Warranty w2 = new Warranty(2L, 2L, LocalDate.now().minusMonths(24), 12, LocalDate.now().minusMonths(12), "Samsung", WarrantyStatus.ACTIVE);

        when(warrantyRepository.findAll()).thenReturn(List.of(w1, w2));

        List<Warranty> results = warrantyService.getAll();

        assertEquals(2, results.size());
        assertEquals(WarrantyStatus.ACTIVE, results.get(0).getStatus());
        assertEquals(WarrantyStatus.EXPIRED, results.get(1).getStatus());
    }

    @Test
    void getByIdReturnsWarranty() {
        Warranty warranty = new Warranty(1L, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE);
        when(warrantyRepository.findById(1L)).thenReturn(Optional.of(warranty));

        Warranty result = warrantyService.getById(1L);

        assertEquals(1L, result.getId());
        verify(warrantyRepository).findById(1L);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(warrantyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WarrantyNotFoundException.class, () -> warrantyService.getById(99L));
    }

    @Test
    void getByProductIdReturnsWarrantyWhenFound() {
        Product product = sampleProduct(1L);
        Warranty warranty = new Warranty(1L, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warrantyRepository.findByProductId(1L)).thenReturn(Optional.of(warranty));

        Warranty result = warrantyService.getByProductId(1L);

        assertEquals(1L, result.getId());
        assertEquals("Lenovo", result.getWarrantyProvider());
    }

    @Test
    void getByProductIdThrowsWhenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> warrantyService.getByProductId(99L));
        verify(warrantyRepository, never()).findByProductId(99L);
    }

    @Test
    void getByProductIdThrowsWhenWarrantyNotFound() {
        Product product = sampleProduct(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warrantyRepository.findByProductId(1L)).thenReturn(Optional.empty());

        assertThrows(WarrantyNotFoundException.class, () -> warrantyService.getByProductId(1L));
    }

    @Test
    void updateCalculatesNewExpiryAndUpdates() {
        LocalDate startDate = LocalDate.now();
        Warranty existing = new Warranty(1L, 1L, startDate, 12, startDate.plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE);
        Warranty updatedDetails = new Warranty(null, 1L, startDate, 24, null, "Lenovo Premium", null);
        Product product = sampleProduct(1L);

        when(warrantyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warrantyRepository.findByProductId(1L)).thenReturn(Optional.of(existing));
        when(warrantyRepository.update(updatedDetails)).thenReturn(true);

        Warranty result = warrantyService.update(1L, updatedDetails);

        assertEquals(1L, result.getId());
        assertEquals(startDate.plusMonths(24), result.getExpiryDate());
        assertEquals("Lenovo Premium", result.getWarrantyProvider());
        verify(warrantyRepository).update(updatedDetails);
    }

    @Test
    void updateThrowsWhenWarrantyNotFound() {
        Warranty updatedDetails = new Warranty(null, 1L, LocalDate.now(), 24, null, "Lenovo Premium", null);
        when(warrantyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WarrantyNotFoundException.class, () -> warrantyService.update(99L, updatedDetails));
        verify(warrantyRepository, never()).update(any());
    }

    @Test
    void deleteRemovesWarranty() {
        Warranty existing = new Warranty(1L, 1L, LocalDate.now(), 12, LocalDate.now().plusMonths(12), "Lenovo", WarrantyStatus.ACTIVE);
        when(warrantyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(warrantyRepository.deleteById(1L)).thenReturn(true);

        warrantyService.delete(1L);

        verify(warrantyRepository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenWarrantyNotFound() {
        when(warrantyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WarrantyNotFoundException.class, () -> warrantyService.delete(99L));
        verify(warrantyRepository, never()).deleteById(99L);
    }

    private Product sampleProduct(Long id) {
        return new Product(id, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SERIAL-1", "Notes");
    }
}

