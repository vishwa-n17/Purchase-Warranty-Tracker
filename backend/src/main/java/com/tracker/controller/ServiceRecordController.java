package com.tracker.controller;

import com.tracker.entity.ServiceRecord;
import com.tracker.service.ServiceRecordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public List<ServiceRecord> getAllRecords(@RequestParam(required = false) Long productId) {
        if (productId != null) {
            return serviceRecordService.getRecordsByProduct(productId);
        }
        return serviceRecordService.getAllRecords();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRecord> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceRecordService.getRecordById(id));
    }

    @GetMapping("/product/{productId}")
    public List<ServiceRecord> getRecordsByProductId(@PathVariable Long productId) {
        return serviceRecordService.getRecordsByProduct(productId);
    }

    @PostMapping
    public ResponseEntity<ServiceRecord> createRecord(@Valid @RequestBody ServiceRecord record) {
        ServiceRecord createdRecord = serviceRecordService.createRecord(record);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRecord.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdRecord);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceRecord> updateRecord(@PathVariable Long id,
                                                      @Valid @RequestBody ServiceRecord updatedRecord) {
        return ResponseEntity.ok(serviceRecordService.updateRecord(id, updatedRecord));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        serviceRecordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}

