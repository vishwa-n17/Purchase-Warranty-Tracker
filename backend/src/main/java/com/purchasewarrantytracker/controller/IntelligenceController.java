package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.*;
import com.purchasewarrantytracker.security.AuthenticatedUserProvider;
import com.purchasewarrantytracker.service.IntelligenceService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intelligence")
@Validated
public class IntelligenceController {

    private final IntelligenceService intelligenceService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public IntelligenceController(IntelligenceService intelligenceService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.intelligenceService = intelligenceService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping("/products/{productId}/lifecycle")
    public ResponseEntity<ProductLifecycleDTO> getLifecycle(@PathVariable @Positive(message = "Product ID must be positive") long productId) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(intelligenceService.getProductLifecycle(userId, productId));
    }

    @GetMapping("/products/{productId}/timeline")
    public ResponseEntity<List<TimelineEventDTO>> getTimeline(@PathVariable @Positive(message = "Product ID must be positive") long productId) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(intelligenceService.getProductTimeline(userId, productId));
    }

    @GetMapping("/products/{productId}/warranty-intelligence")
    public ResponseEntity<WarrantyIntelligenceDTO> getWarrantyIntelligence(@PathVariable @Positive(message = "Product ID must be positive") long productId) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        WarrantyIntelligenceDTO dto = intelligenceService.getWarrantyIntelligence(userId, productId);
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/products/{productId}/claim-readiness")
    public ResponseEntity<ClaimReadinessDTO> getClaimReadiness(@PathVariable @Positive(message = "Product ID must be positive") long productId) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(intelligenceService.getClaimReadiness(userId, productId));
    }

    @GetMapping("/products/{productId}/ownership-cost")
    public ResponseEntity<OwnershipCostDTO> getOwnershipCost(@PathVariable @Positive(message = "Product ID must be positive") long productId) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(intelligenceService.getOwnershipCost(userId, productId));
    }

    @GetMapping("/products/{productId}/health")
    public ResponseEntity<ProductHealthDTO> getHealth(@PathVariable @Positive(message = "Product ID must be positive") long productId) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(intelligenceService.getProductHealth(userId, productId));
    }

    @GetMapping("/products/{productId}/status")
    public ResponseEntity<ProductStatusDTO> getStatus(@PathVariable @Positive(message = "Product ID must be positive") long productId) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(intelligenceService.getProductStatus(userId, productId));
    }
}
