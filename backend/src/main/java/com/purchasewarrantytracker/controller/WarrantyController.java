package com.purchasewarrantytracker.controller;

import java.net.URI;
import java.util.List;

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

import com.purchasewarrantytracker.model.Warranty;
import com.purchasewarrantytracker.service.WarrantyService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@Profile("mysql")
public class WarrantyController {

    private final WarrantyService warrantyService;

    public WarrantyController(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
    }

    @PostMapping("/api/warranties")
    public ResponseEntity<Warranty> create(@Valid @RequestBody Warranty warranty) {
        Warranty createdWarranty = warrantyService.create(warranty);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdWarranty.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdWarranty);
    }

    @GetMapping("/api/warranties")
    public List<Warranty> getAll() {
        return warrantyService.getAll();
    }

    @GetMapping("/api/warranties/{id}")
    public Warranty getById(@PathVariable @Positive(message = "Warranty ID must be a positive number") long id) {
        return warrantyService.getById(id);
    }

    @GetMapping("/api/products/{productId}/warranty")
    public Warranty getByProductId(@PathVariable @Positive(message = "Product ID must be a positive number") long productId) {
        return warrantyService.getByProductId(productId);
    }

    @PutMapping("/api/warranties/{id}")
    public Warranty update(@PathVariable @Positive(message = "Warranty ID must be a positive number") long id,
                           @Valid @RequestBody Warranty warranty) {
        return warrantyService.update(id, warranty);
    }

    @DeleteMapping("/api/warranties/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Warranty ID must be a positive number") long id) {
        warrantyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

