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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private ReceiptService receiptService;

    @Test
    void createSavesValidReceipt() {
        Receipt receipt = new Receipt(null, null, "receipts/invoice.pdf", LocalDate.of(2026, 6, 15));
        when(purchaseRepository.existsById(1L)).thenReturn(true);
        when(receiptRepository.existsByPurchaseId(1L)).thenReturn(false);
        when(receiptRepository.save(receipt)).thenAnswer(invocation -> {
            Receipt r = invocation.getArgument(0);
            r.setId(5L);
            return r;
        });

        Receipt result = receiptService.create(1L, receipt);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(1L, result.getPurchaseId());
        verify(receiptRepository).save(receipt);
    }

    @Test
    void createThrowsWhenPurchaseNotFound() {
        Receipt receipt = new Receipt(null, null, "receipts/invoice.pdf", LocalDate.of(2026, 6, 15));
        when(purchaseRepository.existsById(99L)).thenReturn(false);

        assertThrows(PurchaseNotFoundException.class, () -> receiptService.create(99L, receipt));
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void createRejectsEmptyFilePath() {
        Receipt receipt = new Receipt(null, null, "  ", LocalDate.of(2026, 6, 15));

        assertThrows(IllegalArgumentException.class, () -> receiptService.create(1L, receipt));
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void createRejectsNullDate() {
        Receipt receipt = new Receipt(null, null, "receipts/invoice.pdf", null);

        assertThrows(IllegalArgumentException.class, () -> receiptService.create(1L, receipt));
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenReceiptAlreadyExists() {
        Receipt receipt = new Receipt(null, null, "receipts/invoice.pdf", LocalDate.of(2026, 6, 15));
        when(purchaseRepository.existsById(1L)).thenReturn(true);
        when(receiptRepository.existsByPurchaseId(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> receiptService.create(1L, receipt));
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void getByPurchaseIdReturnsReceipt() {
        Receipt receipt = new Receipt(5L, 1L, "receipts/invoice.pdf", LocalDate.of(2026, 6, 15));
        when(purchaseRepository.existsById(1L)).thenReturn(true);
        when(receiptRepository.findByPurchaseId(1L)).thenReturn(Optional.of(receipt));

        Receipt result = receiptService.getByPurchaseId(1L);

        assertEquals(5L, result.getId());
        verify(receiptRepository).findByPurchaseId(1L);
    }

    @Test
    void getByPurchaseIdThrowsWhenPurchaseNotFound() {
        when(purchaseRepository.existsById(99L)).thenReturn(false);

        assertThrows(PurchaseNotFoundException.class, () -> receiptService.getByPurchaseId(99L));
    }

    @Test
    void getByPurchaseIdThrowsWhenReceiptNotFound() {
        when(purchaseRepository.existsById(1L)).thenReturn(true);
        when(receiptRepository.findByPurchaseId(1L)).thenReturn(Optional.empty());

        assertThrows(ReceiptNotFoundException.class, () -> receiptService.getByPurchaseId(1L));
    }

    @Test
    void updateModifiesExistingReceipt() {
        Receipt existing = new Receipt(5L, 1L, "receipts/old.pdf", LocalDate.of(2026, 6, 15));
        Receipt updated = new Receipt(null, null, "receipts/new.pdf", LocalDate.of(2026, 6, 16));

        when(purchaseRepository.existsById(1L)).thenReturn(true);
        when(receiptRepository.findByPurchaseId(1L)).thenReturn(Optional.of(existing));
        when(receiptRepository.update(updated)).thenReturn(true);

        Receipt result = receiptService.update(1L, updated);

        assertEquals(5L, result.getId());
        assertEquals(1L, result.getPurchaseId());
        assertEquals("receipts/new.pdf", result.getReceiptFilePath());
        verify(receiptRepository).update(updated);
    }

    @Test
    void updateThrowsWhenReceiptNotFound() {
        Receipt updated = new Receipt(null, null, "receipts/new.pdf", LocalDate.of(2026, 6, 16));
        when(purchaseRepository.existsById(1L)).thenReturn(true);
        when(receiptRepository.findByPurchaseId(1L)).thenReturn(Optional.empty());

        assertThrows(ReceiptNotFoundException.class, () -> receiptService.update(1L, updated));
        verify(receiptRepository, never()).update(any());
    }

    @Test
    void deleteRemovesReceipt() {
        when(purchaseRepository.existsById(1L)).thenReturn(true);
        when(receiptRepository.existsByPurchaseId(1L)).thenReturn(true);
        when(receiptRepository.deleteByPurchaseId(1L)).thenReturn(true);

        receiptService.delete(1L);

        verify(receiptRepository).deleteByPurchaseId(1L);
    }

    @Test
    void deleteThrowsWhenReceiptNotFound() {
        when(purchaseRepository.existsById(1L)).thenReturn(true);
        when(receiptRepository.existsByPurchaseId(1L)).thenReturn(false);

        assertThrows(ReceiptNotFoundException.class, () -> receiptService.delete(1L));
        verify(receiptRepository, never()).deleteByPurchaseId(1L);
    }
}

