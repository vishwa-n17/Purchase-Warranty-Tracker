package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.Receipt;
import com.purchasewarrantytracker.model.User;
import com.purchasewarrantytracker.service.ReceiptService;
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

@RestController
@RequestMapping("/api/purchases/{purchaseId}/receipt")
@Validated
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
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
    public ResponseEntity<Receipt> create(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId,
            @Valid @RequestBody Receipt receipt) {
        Long userId = getCurrentUserId();
        Receipt createdReceipt = receiptService.create(userId, purchaseId, receipt);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(createdReceipt);
    }

    @GetMapping
    public Receipt getByPurchaseId(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId) {
        Long userId = getCurrentUserId();
        return receiptService.getByPurchaseId(userId, purchaseId);
    }

    @PutMapping
    public Receipt update(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId,
            @Valid @RequestBody Receipt receipt) {
        Long userId = getCurrentUserId();
        return receiptService.update(userId, purchaseId, receipt);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId) {
        Long userId = getCurrentUserId();
        receiptService.delete(userId, purchaseId);
        return ResponseEntity.noContent().build();
    }
}
