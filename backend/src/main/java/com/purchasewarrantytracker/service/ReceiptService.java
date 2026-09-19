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

    public Receipt create(Long userId, long purchaseId, Receipt receipt) {
        validatePurchaseId(purchaseId);
        validateReceipt(receipt);
        verifyPurchaseExistsAndOwned(userId, purchaseId);

        if (receiptRepository.existsByPurchaseIdAndUserId(purchaseId, userId)) {
            throw new IllegalArgumentException("A receipt is already associated with purchase ID " + purchaseId);
        }

        receipt.setPurchaseId(purchaseId);
        receipt.setUserId(userId);
        return receiptRepository.save(receipt);
    }

    public Receipt getByPurchaseId(Long userId, long purchaseId) {
        validatePurchaseId(purchaseId);
        verifyPurchaseExistsAndOwned(userId, purchaseId);
        return receiptRepository.findByPurchaseIdAndUserId(purchaseId, userId)
                .orElseThrow(() -> new ReceiptNotFoundException(purchaseId));
    }

    public Receipt update(Long userId, long purchaseId, Receipt receipt) {
        validatePurchaseId(purchaseId);
        validateReceipt(receipt);
        verifyPurchaseExistsAndOwned(userId, purchaseId);

        Receipt existingReceipt = receiptRepository.findByPurchaseIdAndUserId(purchaseId, userId)
                .orElseThrow(() -> new ReceiptNotFoundException(purchaseId));

        receipt.setId(existingReceipt.getId());
        receipt.setPurchaseId(purchaseId);
        receipt.setUserId(userId);
        receiptRepository.update(receipt);
        return receipt;
    }

    public void delete(Long userId, long purchaseId) {
        validatePurchaseId(purchaseId);
        verifyPurchaseExistsAndOwned(userId, purchaseId);

        if (!receiptRepository.existsByPurchaseIdAndUserId(purchaseId, userId)) {
            throw new ReceiptNotFoundException(purchaseId);
        }

        receiptRepository.deleteByPurchaseIdAndUserId(purchaseId, userId);
    }

    private void verifyPurchaseExistsAndOwned(Long userId, long purchaseId) {
        if (!purchaseRepository.existsByIdAndUserId(purchaseId, userId)) {
            throw new PurchaseNotFoundException(purchaseId);
        }
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

