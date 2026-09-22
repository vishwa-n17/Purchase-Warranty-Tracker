package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.exception.PurchaseNotFoundException;
import com.purchasewarrantytracker.exception.ReceiptNotFoundException;
import com.purchasewarrantytracker.model.Receipt;
import com.purchasewarrantytracker.repository.PurchaseRepository;
import com.purchasewarrantytracker.repository.ReceiptRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

@Service
public class ReceiptService {

    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;

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
        return receiptRepository.findImageByPurchaseIdAndUserId(purchaseId, userId)
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

    public Receipt uploadImage(Long userId, long purchaseId, MultipartFile image, String receiptReference,
                               LocalDate receiptDate) {
        validatePurchaseId(purchaseId);
        verifyPurchaseExistsAndOwned(userId, purchaseId);
        validateImage(image);
        if (receiptDate == null) {
            throw new IllegalArgumentException("Receipt date is required");
        }

        Receipt receipt = receiptRepository.findByPurchaseIdAndUserId(purchaseId, userId)
                .orElseGet(Receipt::new);
        receipt.setUserId(userId);
        receipt.setPurchaseId(purchaseId);
        receipt.setReceiptDate(receiptDate);
        String safeFileName = safeFileName(image.getOriginalFilename());
        String reference = receiptReference == null || receiptReference.isBlank()
                ? safeFileName : receiptReference.trim();
        validateReference(reference);
        receipt.setReceiptFilePath(reference);
        receipt.setImageFileName(safeFileName);
        receipt.setImageContentType(normalizeImageType(image.getContentType()));
        try {
            receipt.setImageData(image.getBytes());
        } catch (IOException ex) {
            throw new IllegalArgumentException("The receipt image could not be read");
        }

        if (receipt.getId() == null) {
            return receiptRepository.save(receipt);
        }
        receiptRepository.updateImage(receipt);
        return receipt;
    }

    public Receipt getUploadedImage(Long userId, long purchaseId) {
        validatePurchaseId(purchaseId);
        verifyPurchaseExistsAndOwned(userId, purchaseId);
        Receipt receipt = receiptRepository.findImageByPurchaseIdAndUserId(purchaseId, userId)
                .orElseThrow(() -> new ReceiptNotFoundException(purchaseId));
        if (!receipt.hasUploadedImage()) {
            throw new ReceiptNotFoundException(purchaseId);
        }
        return receipt;
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
        validateReference(receipt.getReceiptFilePath());
        if (receipt.getReceiptDate() == null) {
            throw new IllegalArgumentException("Receipt date is required");
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Choose a receipt image to upload");
        }
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Receipt images must be 5 MB or smaller");
        }
        String type = normalizeImageType(image.getContentType());
        byte[] header;
        try {
            header = image.getBytes();
        } catch (IOException ex) {
            throw new IllegalArgumentException("The receipt image could not be read");
        }
        boolean valid = ("image/jpeg".equals(type) && header.length >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF)
                || ("image/png".equals(type) && header.length >= 8 && (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47)
                || ("image/webp".equals(type) && header.length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P');
        if (!valid) {
            throw new IllegalArgumentException("Upload a valid JPG, PNG, or WebP receipt image");
        }
    }

    private String normalizeImageType(String contentType) {
        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (!type.equals("image/jpeg") && !type.equals("image/png") && !type.equals("image/webp")) {
            throw new IllegalArgumentException("Upload a JPG, PNG, or WebP receipt image");
        }
        return type;
    }

    private String safeFileName(String originalFileName) {
        String name = originalFileName == null ? "receipt-image" : originalFileName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._ -]", "_");
        if (name.isBlank() || name.equals(".") || name.equals("..")) name = "receipt-image";
        return name.length() > 150 ? name.substring(0, 150) : name;
    }

    private void validateReference(String reference) {
        if (reference == null || reference.isBlank() || reference.length() > 500
                || reference.contains("..") || reference.startsWith("/") || reference.startsWith("\\")
                || reference.matches("^[A-Za-z]:.*")) {
            throw new IllegalArgumentException("Enter a valid receipt reference");
        }
    }
}

