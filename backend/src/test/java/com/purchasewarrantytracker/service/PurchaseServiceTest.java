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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    private static final long TEST_USER_ID = 1L;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void createSavesAValidPurchase() {
        Purchase purchase = samplePurchase(null, TEST_USER_ID, 1L, new BigDecimal("49999.00"));
        Product product = new Product(1L, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SERIAL-1", "Notes");

        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));
        when(purchaseRepository.save(purchase)).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        Purchase saved = purchaseService.create(TEST_USER_ID, purchase);

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        verify(purchaseRepository).save(purchase);
    }

    @Test
    void createRejectsNonExistentProduct() {
        Purchase purchase = samplePurchase(null, TEST_USER_ID, 999L, new BigDecimal("49999.00"));
        when(productRepository.findByIdAndUserId(999L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> purchaseService.create(TEST_USER_ID, purchase));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void createRejectsNegativePrice() {
        Purchase purchase = samplePurchase(null, TEST_USER_ID, 1L, new BigDecimal("-10.00"));

        assertThrows(IllegalArgumentException.class, () -> purchaseService.create(TEST_USER_ID, purchase));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void createRejectsEmptyStoreName() {
        Purchase purchase = new Purchase(null, TEST_USER_ID, 1L, LocalDate.now(), new BigDecimal("100.00"), "  ", PaymentMethod.UPI);

        assertThrows(IllegalArgumentException.class, () -> purchaseService.create(TEST_USER_ID, purchase));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void getAllReturnsPurchases() {
        when(purchaseRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(
                samplePurchase(1L, TEST_USER_ID, 1L, new BigDecimal("100.00")),
                samplePurchase(2L, TEST_USER_ID, 2L, new BigDecimal("200.00"))
        ));

        List<Purchase> purchases = purchaseService.getAll(TEST_USER_ID);

        assertEquals(2, purchases.size());
        verify(purchaseRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    void getByIdReturnsPurchase() {
        Purchase purchase = samplePurchase(1L, TEST_USER_ID, 1L, new BigDecimal("100.00"));
        when(purchaseRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(purchase));

        Purchase result = purchaseService.getById(TEST_USER_ID, 1L);

        assertEquals(1L, result.getId());
        verify(purchaseRepository).findByIdAndUserId(1L, TEST_USER_ID);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(purchaseRepository.findByIdAndUserId(99L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class, () -> purchaseService.getById(TEST_USER_ID, 99L));
    }

    @Test
    void getByProductIdReturnsPurchasesWhenProductExists() {
        Product product = new Product(1L, "Laptop", "Electronics", null, null, null, null);
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));
        when(purchaseRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(List.of(
                samplePurchase(1L, TEST_USER_ID, 1L, new BigDecimal("100.00"))
        ));

        List<Purchase> results = purchaseService.getByProductId(TEST_USER_ID, 1L);

        assertEquals(1, results.size());
        verify(purchaseRepository).findByProductIdAndUserId(1L, TEST_USER_ID);
    }

    @Test
    void getByProductIdThrowsWhenProductNotFound() {
        when(productRepository.findByIdAndUserId(99L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> purchaseService.getByProductId(TEST_USER_ID, 99L));
        verify(purchaseRepository, never()).findByProductIdAndUserId(eq(99L), any(Long.class));
    }

    @Test
    void updateModifiesExistingPurchase() {
        Purchase existingPurchase = samplePurchase(1L, TEST_USER_ID, 1L, new BigDecimal("100.00"));
        Purchase updatedDetails = samplePurchase(null, TEST_USER_ID, 1L, new BigDecimal("150.00"));
        Product product = new Product(1L, "Laptop", "Electronics", null, null, null, null);

        when(purchaseRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(existingPurchase));
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));
        when(purchaseRepository.update(updatedDetails)).thenReturn(true);

        Purchase result = purchaseService.update(TEST_USER_ID, 1L, updatedDetails);

        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("150.00"), result.getPurchasePrice());
        verify(purchaseRepository).update(updatedDetails);
    }

    @Test
    void deleteRemovesExistingPurchase() {
        Purchase existing = samplePurchase(1L, TEST_USER_ID, 1L, new BigDecimal("100.00"));
        when(purchaseRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(existing));
        when(purchaseRepository.deleteByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);

        purchaseService.delete(TEST_USER_ID, 1L);

        verify(purchaseRepository).deleteByIdAndUserId(1L, TEST_USER_ID);
    }

    @Test
    void deleteThrowsWhenPurchaseNotFound() {
        when(purchaseRepository.findByIdAndUserId(99L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class, () -> purchaseService.getById(TEST_USER_ID, 99L));
        verify(purchaseRepository, never()).deleteByIdAndUserId(eq(99L), any(Long.class));
    }

    private Purchase samplePurchase(Long id, Long userId, Long productId, BigDecimal price) {
        return new Purchase(id, userId, productId, LocalDate.of(2026, 6, 15), price, "Campus Store", PaymentMethod.UPI);
    }
}
