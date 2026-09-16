package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.Product;
import com.purchasewarrantytracker.security.AuthenticatedUserProvider;
import com.purchasewarrantytracker.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private final ProductService productService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ProductController(ProductService productService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.productService = productService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody Product product) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        product.setUserId(userId);
        Product createdProduct = productService.create(userId, product);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdProduct);
    }

    @GetMapping
    public List<Product> getAll() {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return productService.getAll(userId);
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable @Positive(message = "Product ID must be a positive number") long id) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return productService.getById(userId, id);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable @Positive(message = "Product ID must be a positive number") long id,
                          @Valid @RequestBody Product product) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return productService.update(userId, id, product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Product ID must be a positive number") long id) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        productService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
