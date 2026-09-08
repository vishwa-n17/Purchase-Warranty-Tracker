package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.exception.PurchaseNotFoundException;
import com.purchasewarrantytracker.exception.ReceiptNotFoundException;
import com.purchasewarrantytracker.model.Receipt;
import com.purchasewarrantytracker.repository.PurchaseRepository;
import com.purchasewarrantytracker.repository.ReceiptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
class ReceiptServiceTest {

    private static final long TEST_USER_ID = 1L;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private ReceiptService receiptService;

    @Test
    void createSavesValidReceipt() {
        Receipt receipt = new Receipt(null, TEST_USER_ID, null, "receipts/invoice.pdf", LocalDate.of(2026, 6, 15));
        when(purchaseRepository.existsByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);
        when(receiptRepository.existsByPurchaseIdAndUserId(1L, TEST_USER_ID)).thenReturn(false);
        when(receiptRepository.save(receipt)).thenAnswer(invocation -> {
            Receipt r = invocation.getArgument(0);
            r.setId(5L);
            return r;
        });

        Receipt result = receiptService.create(TEST_USER_ID, 1L, receipt);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(1L, result.getPurchaseId());
        verify(receiptRepository).save(receipt);
    }

    @Test
    void createThrowsWhenPurchaseNotFound() {
        Receipt receipt = new Receipt(null, TEST_USER_ID, null, "receipts/invoice.pdf", LocalDate.of(2026, 6, 15));
        when(purchaseRepository.existsByIdAndUserId(99L, TEST_USER_ID)).thenReturn(false);

        assertThrows(PurchaseNotFoundException.class, () -> receiptService.create(TEST_USER_ID, 99L, receipt));
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void createRejectsEmptyFilePath() {
        Receipt receipt = new Receipt(null, TEST_USER_ID, null, "  ", LocalDate.of(2026, 6, 15));

        assertThrows(IllegalArgumentException.class, () -> receiptService.create(TEST_USER_ID, 1L, receipt));
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void createRejectsNullDate() {
        Receipt receipt = new Receipt(null, TEST_USER_ID, null, "receipts/invoice.pdf", null);

        assertThrows(IllegalArgumentException.class, () -> receiptService.create(TEST_USER_ID, 1L, receipt));
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenReceiptAlreadyExists() {
        Receipt receipt = new Receipt(null, TEST_USER_ID, null, "receipts/invoice.pdf", LocalDate.of(2026, 6, 15));
        when(purchaseRepository.existsByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);
        when(receiptRepository.existsByPurchaseIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> receiptService.create(TEST_USER_ID, 1L, receipt));
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void getByPurchaseIdReturnsReceipt() {
        Receipt receipt = new Receipt(5L, TEST_USER_ID, 1L, "receipts/invoice.pdf", LocalDate.of(2026, 6, 15));
        when(purchaseRepository.existsByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);
        when(receiptRepository.findByPurchaseIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(receipt));

        Receipt result = receiptService.getByPurchaseId(TEST_USER_ID, 1L);

        assertEquals(5L, result.getId());
        verify(receiptRepository).findByPurchaseIdAndUserId(1L, TEST_USER_ID);
    }

    @Test
    void getByPurchaseIdThrowsWhenPurchaseNotFound() {
        when(purchaseRepository.existsByIdAndUserId(99L, TEST_USER_ID)).thenReturn(false);

        assertThrows(PurchaseNotFoundException.class, () -> receiptService.getByPurchaseId(TEST_USER_ID, 99L));
    }

    @Test
    void getByPurchaseIdThrowsWhenReceiptNotFound() {
        when(purchaseRepository.existsByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);
        when(receiptRepository.findByPurchaseIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ReceiptNotFoundException.class, () -> receiptService.getByPurchaseId(TEST_USER_ID, 1L));
    }

    @Test
    void updateModifiesExistingReceipt() {
        Receipt existing = new Receipt(5L, TEST_USER_ID, 1L, "receipts/old.pdf", LocalDate.of(2026, 6, 15));
        Receipt updated = new Receipt(null, TEST_USER_ID, null, "receipts/new.pdf", LocalDate.of(2026, 6, 16));

        when(purchaseRepository.existsByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);
        when(receiptRepository.findByPurchaseIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(existing));
        when(receiptRepository.update(updated)).thenReturn(true);

        Receipt result = receiptService.update(TEST_USER_ID, 1L, updated);

        assertEquals(5L, result.getId());
        assertEquals(1L, result.getPurchaseId());
        assertEquals("receipts/new.pdf", result.getReceiptFilePath());
        verify(receiptRepository).update(updated);
    }

    @Test
    void updateThrowsWhenReceiptNotFound() {
        Receipt updated = new Receipt(null, TEST_USER_ID, null, "receipts/new.pdf", LocalDate.of(2026, 6, 16));
        when(purchaseRepository.existsByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);
        when(receiptRepository.findByPurchaseIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ReceiptNotFoundException.class, () -> receiptService.update(TEST_USER_ID, 1L, updated));
        verify(receiptRepository, never()).update(any());
    }

    @Test
    void deleteRemovesReceipt() {
        when(purchaseRepository.existsByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);
        when(receiptRepository.existsByPurchaseIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);
        when(receiptRepository.deleteByPurchaseIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);

        receiptService.delete(TEST_USER_ID, 1L);

        verify(receiptRepository).deleteByPurchaseIdAndUserId(1L, TEST_USER_ID);
    }

    @Test
    void deleteThrowsWhenReceiptNotFound() {
        when(purchaseRepository.existsByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);
        when(receiptRepository.existsByPurchaseIdAndUserId(1L, TEST_USER_ID)).thenReturn(false);

        assertThrows(ReceiptNotFoundException.class, () -> receiptService.delete(TEST_USER_ID, 1L));
        verify(receiptRepository, never()).deleteByPurchaseIdAndUserId(eq(1L), any(Long.class));
    }
}
