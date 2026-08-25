package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.Receipt;
import com.purchasewarrantytracker.service.ReceiptService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/purchases/{purchaseId}/receipt")
@Validated
@Profile("mysql")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping
    public ResponseEntity<Receipt> create(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId,
            @Valid @RequestBody Receipt receipt) {
        Receipt createdReceipt = receiptService.create(purchaseId, receipt);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(createdReceipt);
    }

    @GetMapping
    public Receipt getByPurchaseId(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId) {
        return receiptService.getByPurchaseId(purchaseId);
    }

    @PutMapping
    public Receipt update(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId,
            @Valid @RequestBody Receipt receipt) {
        return receiptService.update(purchaseId, receipt);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "Purchase ID must be a positive number") long purchaseId) {
        receiptService.delete(purchaseId);
        return ResponseEntity.noContent().build();
    }
}

