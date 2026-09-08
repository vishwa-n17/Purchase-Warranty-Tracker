package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.User;
import com.purchasewarrantytracker.model.Warranty;
import com.purchasewarrantytracker.service.WarrantyService;
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
@RequestMapping("/api/warranties")
@Validated
public class WarrantyController {

    private final WarrantyService warrantyService;

    public WarrantyController(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
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
    public ResponseEntity<Warranty> create(@Valid @RequestBody Warranty warranty) {
        Long userId = getCurrentUserId();
        Warranty createdWarranty = warrantyService.create(userId, warranty);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdWarranty.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdWarranty);
    }

    @GetMapping
    public List<Warranty> getAll() {
        Long userId = getCurrentUserId();
        return warrantyService.getAll(userId);
    }

    @GetMapping("/{id}")
    public Warranty getById(@PathVariable @Positive(message = "Warranty ID must be a positive number") long id) {
        Long userId = getCurrentUserId();
        return warrantyService.getById(userId, id);
    }

    @GetMapping("/products/{productId}/warranty")
    public Warranty getByProductId(@PathVariable @Positive(message = "Product ID must be a positive number") long productId) {
        Long userId = getCurrentUserId();
        return warrantyService.getByProductId(userId, productId);
    }

    @PutMapping("/{id}")
    public Warranty update(@PathVariable @Positive(message = "Warranty ID must be a positive number") long id,
                           @Valid @RequestBody Warranty warranty) {
        Long userId = getCurrentUserId();
        return warrantyService.update(userId, id, warranty);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Warranty ID must be a positive number") long id) {
        Long userId = getCurrentUserId();
        warrantyService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
