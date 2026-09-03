package com.tracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.purchasewarrantytracker.model.Product;
import com.tracker.entity.ServiceRecord;
import com.tracker.entity.ServiceType;
import com.tracker.repository.ProductRepository;
import com.tracker.repository.ServiceRecordRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ServiceRecordServiceTest {

    @Mock
    private ServiceRecordRepository serviceRecordRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ServiceRecordService serviceRecordService;

    @Test
    void createRecordSavesValidRecord() {
        Product product = sampleProduct(1L);
        ServiceRecord record = sampleRecord(null, product);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(serviceRecordRepository.save(record)).thenAnswer(invocation -> {
            ServiceRecord r = invocation.getArgument(0);
            r.setId(10L);
            return r;
        });

        ServiceRecord saved = serviceRecordService.createRecord(record);

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        assertEquals("Tech Care", saved.getProvider());
        verify(serviceRecordRepository).save(record);
    }

    @Test
    void createRecordThrowsWhenProductNotFound() {
        Product product = sampleProduct(999L);
        ServiceRecord record = sampleRecord(null, product);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.createRecord(record));
        verify(serviceRecordRepository, never()).save(any());
    }

    @Test
    void createRecordThrowsWhenProductIsNull() {
        ServiceRecord record = sampleRecord(null, null);

        assertThrows(IllegalArgumentException.class, () -> serviceRecordService.createRecord(record));
        verify(serviceRecordRepository, never()).save(any());
    }

    @Test
    void getAllRecordsReturnsAll() {
        Product product = sampleProduct(1L);
        when(serviceRecordRepository.findAll()).thenReturn(List.of(
                sampleRecord(1L, product),
                sampleRecord(2L, product)
        ));

        List<ServiceRecord> records = serviceRecordService.getAllRecords();

        assertEquals(2, records.size());
        verify(serviceRecordRepository).findAll();
    }

    @Test
    void getRecordByIdReturnsRecordWhenExists() {
        Product product = sampleProduct(1L);
        ServiceRecord record = sampleRecord(1L, product);

        when(serviceRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        ServiceRecord result = serviceRecordService.getRecordById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Tech Care", result.getProvider());
    }

    @Test
    void getRecordByIdThrowsWhenNotFound() {
        when(serviceRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.getRecordById(99L));
    }

    @Test
    void getRecordsByProductReturnsRecordsWhenProductExists() {
        Product product = sampleProduct(1L);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(serviceRecordRepository.findByProductId(1L)).thenReturn(List.of(
                sampleRecord(1L, product)
        ));

        List<ServiceRecord> records = serviceRecordService.getRecordsByProduct(1L);

        assertEquals(1, records.size());
        verify(serviceRecordRepository).findByProductId(1L);
    }

    @Test
    void getRecordsByProductThrowsWhenProductNotFound() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.getRecordsByProduct(999L));
        verify(serviceRecordRepository, never()).findByProductId(999L);
    }

    @Test
    void updateRecordUpdatesFieldsSuccessfully() {
        Product product = sampleProduct(1L);
        ServiceRecord existing = sampleRecord(1L, product);
        ServiceRecord updatedDetails = new ServiceRecord(null, product, LocalDate.of(2026, 8, 15),
                "Updated Care", "Replaced battery", new BigDecimal("1200.00"), ServiceType.REPAIR);

        when(serviceRecordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(serviceRecordRepository.save(existing)).thenReturn(existing);

        ServiceRecord result = serviceRecordService.updateRecord(1L, updatedDetails);

        assertEquals("Updated Care", result.getProvider());
        assertEquals("Replaced battery", result.getDescription());
        assertEquals(new BigDecimal("1200.00"), result.getCost());
        verify(serviceRecordRepository).save(existing);
    }

    @Test
    void updateRecordThrowsWhenRecordNotFound() {
        ServiceRecord updatedDetails = sampleRecord(null, sampleProduct(1L));

        when(serviceRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.updateRecord(99L, updatedDetails));
        verify(serviceRecordRepository, never()).save(any());
    }

    @Test
    void deleteRecordDeletesSuccessfully() {
        when(serviceRecordRepository.existsById(1L)).thenReturn(true);

        serviceRecordService.deleteRecord(1L);

        verify(serviceRecordRepository).deleteById(1L);
    }

    @Test
    void deleteRecordThrowsWhenNotFound() {
        when(serviceRecordRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.deleteRecord(99L));
        verify(serviceRecordRepository, never()).deleteById(99L);
    }

    private Product sampleProduct(Long id) {
        return new Product(id, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SERIAL-1", "Notes");
    }

    private ServiceRecord sampleRecord(Long id, Product product) {
        return new ServiceRecord(id, product, LocalDate.of(2026, 8, 10), "Tech Care",
                "Cleaned cooling fan", new BigDecimal("450.00"), ServiceType.MAINTENANCE);
    }
}

