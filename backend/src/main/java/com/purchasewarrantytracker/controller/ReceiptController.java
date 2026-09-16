package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.Receipt;
import com.purchasewarrantytracker.security.AuthenticatedUserProvider;
import com.purchasewarrantytracker.service.ReceiptService;
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

@RestController
@RequestMapping("/api/purchases/{purchaseId}/receipt")
@Validated
public class ReceiptController {

    private final ReceiptService receiptService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ReceiptController(ReceiptService receiptService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.receiptService = receiptService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping
    public ResponseEntity<Receipt> create(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId,
            @Valid @RequestBody Receipt receipt) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        Receipt createdReceipt = receiptService.create(userId, purchaseId, receipt);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(createdReceipt);
    }

    @GetMapping
    public Receipt getByPurchaseId(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return receiptService.getByPurchaseId(userId, purchaseId);
    }

    @PutMapping
    public Receipt update(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId,
            @Valid @RequestBody Receipt receipt) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return receiptService.update(userId, purchaseId, receipt);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        receiptService.delete(userId, purchaseId);
        return ResponseEntity.noContent().build();
    }
}
