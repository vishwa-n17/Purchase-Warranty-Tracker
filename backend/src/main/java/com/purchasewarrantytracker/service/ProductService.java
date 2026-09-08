package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.exception.ProductInUseException;
import com.purchasewarrantytracker.exception.ProductNotFoundException;
import com.purchasewarrantytracker.model.Product;
import com.purchasewarrantytracker.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(Long userId, Product product) {
        validateProduct(product);
        product.setUserId(userId);
        return productRepository.save(product);
    }

    public List<Product> getAll(Long userId) {
        return productRepository.findByUserId(userId);
    }

    public Product getById(Long userId, long id) {
        return productRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product update(Long userId, long id, Product product) {
        validateId(id);
        validateProduct(product);
        getById(userId, id);
        product.setId(id);
        product.setUserId(userId);
        productRepository.update(product);
        return product;
    }

    public void delete(Long userId, long id) {
        validateId(id);
        getById(userId, id);
        if (productRepository.hasPurchases(id, userId)) {
            throw new ProductInUseException(id);
        }
        productRepository.deleteByIdAndUserId(id, userId);
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
    }

    private void validateProduct(Product product) {
        if (product == null || isBlank(product.getName()) || isBlank(product.getCategory())) {
            throw new IllegalArgumentException("Product name and category are required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
