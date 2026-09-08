package com.tracker.controller;

import com.purchasewarrantytracker.model.User;
import com.tracker.entity.ServiceRecord;
import com.tracker.service.ServiceRecordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/service-records")
@CrossOrigin(origins = "*")
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;

    public ServiceRecordController(ServiceRecordService serviceRecordService) {
        this.serviceRecordService = serviceRecordService;
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

    @GetMapping
    public List<ServiceRecord> getAllRecords(@RequestParam(required = false) Long productId) {
        Long userId = getCurrentUserId();
        if (productId != null) {
            return serviceRecordService.getRecordsByProduct(userId, productId);
        }
        return serviceRecordService.getAllRecords(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRecord> getRecordById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(serviceRecordService.getRecordById(userId, id));
    }

    @GetMapping("/product/{productId}")
    public List<ServiceRecord> getRecordsByProductId(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        return serviceRecordService.getRecordsByProduct(userId, productId);
    }

    @PostMapping
    public ResponseEntity<ServiceRecord> createRecord(@Valid @RequestBody ServiceRecord record) {
        Long userId = getCurrentUserId();
        ServiceRecord createdRecord = serviceRecordService.createRecord(userId, record);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRecord.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdRecord);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceRecord> updateRecord(@PathVariable Long id,
                                                      @Valid @RequestBody ServiceRecord updatedRecord) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(serviceRecordService.updateRecord(userId, id, updatedRecord));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        serviceRecordService.deleteRecord(userId, id);
        return ResponseEntity.noContent().build();
    }
}
