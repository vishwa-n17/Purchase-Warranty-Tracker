package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.exception.ProductNotFoundException;
import com.purchasewarrantytracker.exception.PurchaseNotFoundException;
import com.purchasewarrantytracker.model.PaymentMethod;
import com.purchasewarrantytracker.model.Product;
import com.purchasewarrantytracker.model.Purchase;
import com.purchasewarrantytracker.repository.ProductRepository;
import com.purchasewarrantytracker.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void createSavesAValidPurchase() {
        Purchase purchase = samplePurchase(null, 1L, new BigDecimal("49999.00"));
        Product product = new Product(1L, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SERIAL-1", "Notes");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(purchaseRepository.save(purchase)).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        Purchase saved = purchaseService.create(purchase);

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        verify(purchaseRepository).save(purchase);
    }

    @Test
    void createRejectsNonExistentProduct() {
        Purchase purchase = samplePurchase(null, 999L, new BigDecimal("49999.00"));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> purchaseService.create(purchase));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void createRejectsNegativePrice() {
        Purchase purchase = samplePurchase(null, 1L, new BigDecimal("-10.00"));

        assertThrows(IllegalArgumentException.class, () -> purchaseService.create(purchase));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void createRejectsEmptyStoreName() {
        Purchase purchase = new Purchase(null, 1L, LocalDate.now(), new BigDecimal("100.00"), "  ", PaymentMethod.UPI);

        assertThrows(IllegalArgumentException.class, () -> purchaseService.create(purchase));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void getAllReturnsPurchases() {
        when(purchaseRepository.findAll()).thenReturn(List.of(
                samplePurchase(1L, 1L, new BigDecimal("100.00")),
                samplePurchase(2L, 2L, new BigDecimal("200.00"))
        ));

        List<Purchase> purchases = purchaseService.getAll();

        assertEquals(2, purchases.size());
        verify(purchaseRepository).findAll();
    }

    @Test
    void getByIdReturnsPurchase() {
        Purchase purchase = samplePurchase(1L, 1L, new BigDecimal("100.00"));
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        Purchase result = purchaseService.getById(1L);

        assertEquals(1L, result.getId());
        verify(purchaseRepository).findById(1L);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class, () -> purchaseService.getById(99L));
    }

    @Test
    void getByProductIdReturnsPurchasesWhenProductExists() {
        Product product = new Product(1L, "Laptop", "Electronics", null, null, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(purchaseRepository.findByProductId(1L)).thenReturn(List.of(
                samplePurchase(1L, 1L, new BigDecimal("100.00"))
        ));

        List<Purchase> results = purchaseService.getByProductId(1L);

        assertEquals(1, results.size());
        verify(purchaseRepository).findByProductId(1L);
    }

    @Test
    void getByProductIdThrowsWhenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> purchaseService.getByProductId(99L));
        verify(purchaseRepository, never()).findByProductId(99L);
    }

    @Test
    void updateModifiesExistingPurchase() {
        Purchase existingPurchase = samplePurchase(1L, 1L, new BigDecimal("100.00"));
        Purchase updatedDetails = samplePurchase(null, 1L, new BigDecimal("150.00"));
        Product product = new Product(1L, "Laptop", "Electronics", null, null, null, null);

        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(existingPurchase));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(purchaseRepository.update(updatedDetails)).thenReturn(true);

        Purchase result = purchaseService.update(1L, updatedDetails);

        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("150.00"), result.getPurchasePrice());
        verify(purchaseRepository).update(updatedDetails);
    }

    @Test
    void deleteRemovesExistingPurchase() {
        Purchase existing = samplePurchase(1L, 1L, new BigDecimal("100.00"));
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(purchaseRepository.deleteById(1L)).thenReturn(true);

        purchaseService.delete(1L);

        verify(purchaseRepository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenPurchaseNotFound() {
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class, () -> purchaseService.delete(99L));
        verify(purchaseRepository, never()).deleteById(99L);
    }

    private Purchase samplePurchase(Long id, Long productId, BigDecimal price) {
        return new Purchase(id, productId, LocalDate.of(2026, 6, 15), price, "Campus Store", PaymentMethod.UPI);
    }
}

