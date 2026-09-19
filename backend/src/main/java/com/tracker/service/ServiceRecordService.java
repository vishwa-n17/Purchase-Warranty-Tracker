package com.tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.purchasewarrantytracker.model.Product;
import com.tracker.entity.ServiceRecord;
import com.tracker.repository.ProductRepository;
import com.tracker.repository.ServiceRecordRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ServiceRecordService {

    private final ServiceRecordRepository serviceRecordRepository;
    private final ProductRepository productRepository;

    public ServiceRecordService(ServiceRecordRepository serviceRecordRepository, ProductRepository productRepository) {
        this.serviceRecordRepository = serviceRecordRepository;
        this.productRepository = productRepository;
    }

    public List<ServiceRecord> getAllRecords(Long userId) {
        return serviceRecordRepository.findByUserId(userId);
    }

    public List<ServiceRecord> getRecordsByProduct(Long userId, Long productId) {
        if (productId == null || !productRepository.existsByIdAndUserId(productId, userId)) {
            throw new EntityNotFoundException("Product with ID " + productId + " was not found");
        }
        return serviceRecordRepository.findByProductIdAndUserId(productId, userId);
    }

    public ServiceRecord getRecordById(Long userId, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Service record ID cannot be null");
        }
        return serviceRecordRepository.findById(id)
                .filter(record -> userId.equals(record.getUserId()))
                .orElseThrow(() -> new EntityNotFoundException("Service record with ID " + id + " was not found"));
    }

    public ServiceRecord createRecord(Long userId, ServiceRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Service record data is required");
        }
        if (record.getProduct() == null || record.getProduct().getId() == null) {
            throw new IllegalArgumentException("A valid product with ID is required");
        }

        Product product = productRepository.findByIdAndUserId(record.getProduct().getId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Product with ID " + record.getProduct().getId() + " was not found"));

        record.setProduct(product);
        record.setUserId(userId);
        return serviceRecordRepository.save(record);
    }

    public ServiceRecord updateRecord(Long userId, Long id, ServiceRecord updatedRecord) {
        if (id == null || updatedRecord == null) {
            throw new IllegalArgumentException("ID and updated record data are required");
        }

        ServiceRecord existingRecord = getRecordById(userId, id);

        if (updatedRecord.getProduct() != null && updatedRecord.getProduct().getId() != null) {
            Product product = productRepository.findByIdAndUserId(updatedRecord.getProduct().getId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Product with ID " + updatedRecord.getProduct().getId() + " was not found"));
            existingRecord.setProduct(product);
        }

        existingRecord.setServiceDate(updatedRecord.getServiceDate());
        existingRecord.setProvider(updatedRecord.getProvider());
        existingRecord.setDescription(updatedRecord.getDescription());
        existingRecord.setCost(updatedRecord.getCost());
        if (updatedRecord.getServiceType() != null) {
            existingRecord.setServiceType(updatedRecord.getServiceType());
        }

        return serviceRecordRepository.save(existingRecord);
    }

    public void deleteRecord(Long userId, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Service record ID cannot be null");
        }
        getRecordById(userId, id);
        serviceRecordRepository.deleteById(id);
    }
}

