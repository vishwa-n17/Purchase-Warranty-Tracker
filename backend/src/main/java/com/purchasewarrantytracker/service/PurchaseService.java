package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.exception.ProductNotFoundException;
import com.purchasewarrantytracker.exception.PurchaseNotFoundException;
import com.purchasewarrantytracker.model.Purchase;
import com.purchasewarrantytracker.repository.ProductRepository;
import com.purchasewarrantytracker.repository.PurchaseRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    public PurchaseService(PurchaseRepository purchaseRepository, ProductRepository productRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
    }

    public Purchase create(Long userId, Purchase purchase) {
        validatePurchase(purchase);
        verifyProductExistsAndOwned(userId, purchase.getProductId());
        purchase.setUserId(userId);
        return purchaseRepository.save(purchase);
    }

    public List<Purchase> getAll(Long userId) {
        return purchaseRepository.findByUserId(userId);
    }

    public Purchase getById(Long userId, long id) {
        return purchaseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
    }

    public List<Purchase> getByProductId(Long userId, long productId) {
        validateProductId(productId);
        verifyProductExistsAndOwned(userId, productId);
        return purchaseRepository.findByProductIdAndUserId(productId, userId);
    }

    public Purchase update(Long userId, long id, Purchase purchase) {
        validateId(id);
        validatePurchase(purchase);
        getById(userId, id);
        verifyProductExistsAndOwned(userId, purchase.getProductId());
        purchase.setId(id);
        purchase.setUserId(userId);
        purchaseRepository.update(purchase);
        return purchase;
    }

    public void delete(Long userId, long id) {
        validateId(id);
        getById(userId, id);
        purchaseRepository.deleteByIdAndUserId(id, userId);
    }

    private void verifyProductExistsAndOwned(Long userId, long productId) {
        productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Purchase ID must be a positive number");
        }
    }

    private void validateProductId(long productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
    }

    private void verifyProductExists(long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void validatePurchase(Purchase purchase) {
        if (purchase == null) {
            throw new IllegalArgumentException("Purchase data is required");
        }
        if (purchase.getProductId() == null || purchase.getProductId() <= 0) {
            throw new IllegalArgumentException("A valid product ID is required");
        }
        if (purchase.getPurchaseDate() == null) {
            throw new IllegalArgumentException("Purchase date is required");
        }
        if (purchase.getPurchasePrice() == null || purchase.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Purchase price cannot be negative");
        }
        if (purchase.getStoreName() == null || purchase.getStoreName().isBlank()) {
            throw new IllegalArgumentException("Store name is required");
        }
        if (purchase.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }
}

