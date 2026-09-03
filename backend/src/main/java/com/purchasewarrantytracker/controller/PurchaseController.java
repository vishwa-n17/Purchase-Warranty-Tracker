package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.Purchase;
import com.purchasewarrantytracker.service.PurchaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Validated
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/api/purchases")
    public ResponseEntity<Purchase> create(@Valid @RequestBody Purchase purchase) {
        Purchase createdPurchase = purchaseService.create(purchase);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPurchase.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdPurchase);
    }

    @GetMapping("/api/purchases")
    public List<Purchase> getAll() {
        return purchaseService.getAll();
    }

    @GetMapping("/api/purchases/{id}")
    public Purchase getById(@PathVariable @Positive(message = "Purchase ID must be a positive number") long id) {
        return purchaseService.getById(id);
    }

    @GetMapping("/api/products/{productId}/purchases")
    public List<Purchase> getByProductId(@PathVariable @Positive(message = "Product ID must be a positive number") long productId) {
        return purchaseService.getByProductId(productId);
    }

    @PutMapping("/api/purchases/{id}")
    public Purchase update(@PathVariable @Positive(message = "Purchase ID must be a positive number") long id,
                           @Valid @RequestBody Purchase purchase) {
        return purchaseService.update(id, purchase);
    }

    @DeleteMapping("/api/purchases/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Purchase ID must be a positive number") long id) {
        purchaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

