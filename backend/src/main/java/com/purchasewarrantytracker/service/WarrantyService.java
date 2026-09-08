package com.purchasewarrantytracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.purchasewarrantytracker.exception.ProductNotFoundException;
import com.purchasewarrantytracker.exception.WarrantyNotFoundException;
import com.purchasewarrantytracker.model.Warranty;
import com.purchasewarrantytracker.model.WarrantyStatus;
import com.purchasewarrantytracker.repository.ProductRepository;
import com.purchasewarrantytracker.repository.WarrantyRepository;

@Service
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final ProductRepository productRepository;

    public WarrantyService(WarrantyRepository warrantyRepository, ProductRepository productRepository) {
        this.warrantyRepository = warrantyRepository;
        this.productRepository = productRepository;
    }

    public Warranty create(Long userId, Warranty warranty) {
        validateWarranty(warranty);
        verifyProductExistsAndOwned(userId, warranty.getProductId());

        if (warrantyRepository.existsByProductIdAndUserId(warranty.getProductId(), userId)) {
            throw new IllegalArgumentException("A warranty already exists for product ID " + warranty.getProductId());
        }

        applyCalculatedFields(warranty);
        warranty.setUserId(userId);
        return warrantyRepository.save(warranty);
    }

    public List<Warranty> getAll(Long userId) {
        List<Warranty> warranties = warrantyRepository.findByUserId(userId);
        warranties.forEach(this::refreshStatus);
        return warranties;
    }

    public Warranty getById(Long userId, long id) {
        Warranty warranty = warrantyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new WarrantyNotFoundException(id));
        refreshStatus(warranty);
        return warranty;
    }

    public Warranty getByProductId(Long userId, long productId) {
        validateProductId(productId);
        verifyProductExistsAndOwned(userId, productId);
        Warranty warranty = warrantyRepository.findByProductIdAndUserId(productId, userId)
                .orElseThrow(() -> new WarrantyNotFoundException("Warranty for product ID " + productId + " was not found"));
        refreshStatus(warranty);
        return warranty;
    }

    public Warranty update(Long userId, long id, Warranty warranty) {
        validateId(id);
        validateWarranty(warranty);
        getById(userId, id);
        verifyProductExistsAndOwned(userId, warranty.getProductId());

        Optional<Warranty> existingForProduct = warrantyRepository.findByProductIdAndUserId(warranty.getProductId(), userId);
        if (existingForProduct.isPresent() && !existingForProduct.get().getId().equals(id)) {
            throw new IllegalArgumentException("A warranty already exists for product ID " + warranty.getProductId());
        }

        applyCalculatedFields(warranty);
        warranty.setId(id);
        warranty.setUserId(userId);
        warrantyRepository.update(warranty);
        return warranty;
    }

    public void delete(Long userId, long id) {
        validateId(id);
        getById(userId, id);
        warrantyRepository.deleteByIdAndUserId(id, userId);
    }

    private void verifyProductExistsAndOwned(Long userId, long productId) {
        productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void applyCalculatedFields(Warranty warranty) {
        LocalDate calculatedExpiry = warranty.getStartDate().plusMonths(warranty.getDurationMonths());
        warranty.setExpiryDate(calculatedExpiry);
        if (warranty.getStatus() != WarrantyStatus.VOID) {
            if (LocalDate.now().isAfter(calculatedExpiry)) {
                warranty.setStatus(WarrantyStatus.EXPIRED);
            } else {
                warranty.setStatus(WarrantyStatus.ACTIVE);
            }
        }
    }

    private void refreshStatus(Warranty warranty) {
        if (warranty != null && warranty.getStatus() != WarrantyStatus.VOID && warranty.getExpiryDate() != null) {
            if (LocalDate.now().isAfter(warranty.getExpiryDate())) {
                warranty.setStatus(WarrantyStatus.EXPIRED);
            } else {
                warranty.setStatus(WarrantyStatus.ACTIVE);
            }
        }
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Warranty ID must be a positive number");
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

    private void validateWarranty(Warranty warranty) {
        if (warranty == null) {
            throw new IllegalArgumentException("Warranty data is required");
        }
        if (warranty.getProductId() == null || warranty.getProductId() <= 0) {
            throw new IllegalArgumentException("A valid product ID is required");
        }
        if (warranty.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (warranty.getDurationMonths() == null || warranty.getDurationMonths() <= 0) {
            throw new IllegalArgumentException("Duration months must be greater than zero");
        }
        if (warranty.getWarrantyProvider() == null || warranty.getWarrantyProvider().isBlank()) {
            throw new IllegalArgumentException("Warranty provider is required");
        }
    }
}

