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

    public Purchase create(Purchase purchase) {
        validatePurchase(purchase);
        verifyProductExists(purchase.getProductId());
        return purchaseRepository.save(purchase);
    }

    public List<Purchase> getAll() {
        return purchaseRepository.findAll();
    }

    public Purchase getById(long id) {
        validateId(id);
        return purchaseRepository.findById(id).orElseThrow(() -> new PurchaseNotFoundException(id));
    }

    public List<Purchase> getByProductId(long productId) {
        validateProductId(productId);
        verifyProductExists(productId);
        return purchaseRepository.findByProductId(productId);
    }

    public Purchase update(long id, Purchase purchase) {
        validateId(id);
        validatePurchase(purchase);
        getById(id);
        verifyProductExists(purchase.getProductId());
        purchase.setId(id);
        purchaseRepository.update(purchase);
        return purchase;
    }

    public void delete(long id) {
        validateId(id);
        getById(id);
        purchaseRepository.deleteById(id);
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

