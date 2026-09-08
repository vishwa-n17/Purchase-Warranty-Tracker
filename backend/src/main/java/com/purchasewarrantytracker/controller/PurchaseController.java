package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.Purchase;
import com.purchasewarrantytracker.model.User;
import com.purchasewarrantytracker.service.PurchaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/api/purchases")
@Validated
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    @PostMapping
    public ResponseEntity<Purchase> create(@Valid @RequestBody Purchase purchase) {
        Long userId = getCurrentUserId();
        Purchase createdPurchase = purchaseService.create(userId, purchase);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPurchase.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdPurchase);
    }

    @GetMapping
    public List<Purchase> getAll() {
        Long userId = getCurrentUserId();
        return purchaseService.getAll(userId);
    }

    @GetMapping("/{id}")
    public Purchase getById(@PathVariable @Positive(message = "Purchase ID must be a positive number") long id) {
        Long userId = getCurrentUserId();
        return purchaseService.getById(userId, id);
    }

    @GetMapping("/products/{productId}/purchases")
    public List<Purchase> getByProductId(@PathVariable @Positive(message = "Product ID must be a positive number") long productId) {
        Long userId = getCurrentUserId();
        return purchaseService.getByProductId(userId, productId);
    }

    @PutMapping("/{id}")
    public Purchase update(@PathVariable @Positive(message = "Purchase ID must be a positive number") long id,
                           @Valid @RequestBody Purchase purchase) {
        Long userId = getCurrentUserId();
        return purchaseService.update(userId, id, purchase);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Purchase ID must be a positive number") long id) {
        Long userId = getCurrentUserId();
        purchaseService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
