package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.exception.PurchaseNotFoundException;
import com.purchasewarrantytracker.exception.ReceiptNotFoundException;
import com.purchasewarrantytracker.model.Receipt;
import com.purchasewarrantytracker.repository.PurchaseRepository;
import com.purchasewarrantytracker.repository.ReceiptRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final PurchaseRepository purchaseRepository;

    public ReceiptService(ReceiptRepository receiptRepository, PurchaseRepository purchaseRepository) {
        this.receiptRepository = receiptRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public Receipt create(long purchaseId, Receipt receipt) {
        validatePurchaseId(purchaseId);
        validateReceipt(receipt);
        verifyPurchaseExists(purchaseId);

        if (receiptRepository.existsByPurchaseId(purchaseId)) {
            throw new IllegalArgumentException("A receipt is already associated with purchase ID " + purchaseId);
        }

        receipt.setPurchaseId(purchaseId);
        return receiptRepository.save(receipt);
    }

    public Receipt getByPurchaseId(long purchaseId) {
        validatePurchaseId(purchaseId);
        verifyPurchaseExists(purchaseId);
        return receiptRepository.findByPurchaseId(purchaseId)
                .orElseThrow(() -> new ReceiptNotFoundException(purchaseId));
    }

    public Receipt update(long purchaseId, Receipt receipt) {
        validatePurchaseId(purchaseId);
        validateReceipt(receipt);
        verifyPurchaseExists(purchaseId);

        Receipt existingReceipt = receiptRepository.findByPurchaseId(purchaseId)
                .orElseThrow(() -> new ReceiptNotFoundException(purchaseId));

        receipt.setId(existingReceipt.getId());
        receipt.setPurchaseId(purchaseId);
        receiptRepository.update(receipt);
        return receipt;
    }

    public void delete(long purchaseId) {
        validatePurchaseId(purchaseId);
        verifyPurchaseExists(purchaseId);

        if (!receiptRepository.existsByPurchaseId(purchaseId)) {
            throw new ReceiptNotFoundException(purchaseId);
        }

        receiptRepository.deleteByPurchaseId(purchaseId);
    }

    private void validatePurchaseId(long purchaseId) {
        if (purchaseId <= 0) {
            throw new IllegalArgumentException("Purchase ID must be a positive number");
        }
    }

    private void verifyPurchaseExists(long purchaseId) {
        if (!purchaseRepository.existsById(purchaseId)) {
            throw new PurchaseNotFoundException(purchaseId);
        }
    }

    private void validateReceipt(Receipt receipt) {
        if (receipt == null) {
            throw new IllegalArgumentException("Receipt data is required");
        }
        if (receipt.getReceiptFilePath() == null || receipt.getReceiptFilePath().isBlank()) {
            throw new IllegalArgumentException("Receipt file path is required");
        }
        if (receipt.getReceiptDate() == null) {
            throw new IllegalArgumentException("Receipt date is required");
        }
    }
}

