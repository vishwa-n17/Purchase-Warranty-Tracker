package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.exception.ProductInUseException;
import com.purchasewarrantytracker.exception.ProductNotFoundException;
import com.purchasewarrantytracker.model.Product;
import com.purchasewarrantytracker.repository.ProductRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("mysql")
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product getById(long id) {
        validateId(id);
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product update(long id, Product product) {
        validateId(id);
        validateProduct(product);
        getById(id);
        product.setId(id);
        productRepository.update(product);
        return product;
    }

    public void delete(long id) {
        validateId(id);
        getById(id);
        if (productRepository.hasPurchases(id)) {
            throw new ProductInUseException(id);
        }
        productRepository.deleteById(id);
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
