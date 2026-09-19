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
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.purchasewarrantytracker.model.Product;
import com.purchasewarrantytracker.model.User;
import com.tracker.entity.ServiceRecord;
import com.tracker.entity.ServiceType;
import com.tracker.repository.ProductRepository;
import com.tracker.repository.ServiceRecordRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ServiceRecordServiceTest {

    private static final long TEST_USER_ID = 1L;

    @Mock
    private ServiceRecordRepository serviceRecordRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ServiceRecordService serviceRecordService;

    @Test
    void createRecordSavesValidRecord() {
        Product product = sampleProduct(1L);
        ServiceRecord record = sampleRecord(null, TEST_USER_ID, product);

        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));
        when(serviceRecordRepository.save(record)).thenAnswer(invocation -> {
            ServiceRecord r = invocation.getArgument(0);
            r.setId(10L);
            return r;
        });

        ServiceRecord saved = serviceRecordService.createRecord(TEST_USER_ID, record);

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        assertEquals("Tech Care", saved.getProvider());
        verify(serviceRecordRepository).save(record);
    }

    @Test
    void createRecordThrowsWhenProductNotFound() {
        Product product = sampleProduct(999L);
        ServiceRecord record = sampleRecord(null, TEST_USER_ID, product);

        when(productRepository.findByIdAndUserId(999L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.createRecord(TEST_USER_ID, record));
        verify(serviceRecordRepository, never()).save(any());
    }

    @Test
    void createRecordThrowsWhenProductIsNull() {
        ServiceRecord record = sampleRecord(null, TEST_USER_ID, null);

        assertThrows(IllegalArgumentException.class, () -> serviceRecordService.createRecord(TEST_USER_ID, record));
        verify(serviceRecordRepository, never()).save(any());
    }

    @Test
    void createRecordRejectsOtherUsersProduct() {
        Product otherUsersProduct = sampleProduct(1L);
        ServiceRecord record = sampleRecord(null, TEST_USER_ID, otherUsersProduct);

        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.createRecord(TEST_USER_ID, record));
        verify(serviceRecordRepository, never()).save(any());
    }

    @Test
    void getAllRecordsReturnsAll() {
        Product product = sampleProduct(1L);
        when(serviceRecordRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(
                sampleRecord(1L, TEST_USER_ID, product),
                sampleRecord(2L, TEST_USER_ID, product)
        ));

        List<ServiceRecord> records = serviceRecordService.getAllRecords(TEST_USER_ID);

        assertEquals(2, records.size());
        verify(serviceRecordRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    void getRecordByIdReturnsRecordWhenExists() {
        Product product = sampleProduct(1L);
        ServiceRecord record = sampleRecord(1L, TEST_USER_ID, product);

        when(serviceRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        ServiceRecord result = serviceRecordService.getRecordById(TEST_USER_ID, 1L);

        assertEquals(1L, result.getId());
        assertEquals("Tech Care", result.getProvider());
    }

    @Test
    void getRecordByIdThrowsWhenNotFound() {
        when(serviceRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.getRecordById(TEST_USER_ID, 99L));
    }

    @Test
    void getRecordsByProductReturnsRecordsWhenProductExists() {
        Product product = sampleProduct(1L);
        when(productRepository.existsByIdAndUserId(1L, TEST_USER_ID)).thenReturn(true);
        when(serviceRecordRepository.findByProductIdAndUserId(1L, TEST_USER_ID)).thenReturn(List.of(
                sampleRecord(1L, TEST_USER_ID, product)
        ));

        List<ServiceRecord> records = serviceRecordService.getRecordsByProduct(TEST_USER_ID, 1L);

        assertEquals(1, records.size());
        verify(serviceRecordRepository).findByProductIdAndUserId(1L, TEST_USER_ID);
    }

    @Test
    void getRecordsByProductThrowsWhenProductNotFound() {
        when(productRepository.existsByIdAndUserId(999L, TEST_USER_ID)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.getRecordsByProduct(TEST_USER_ID, 999L));
        verify(serviceRecordRepository, never()).findByProductIdAndUserId(eq(999L), any(Long.class));
    }

    @Test
    void getRecordsByProductRejectsOtherUsersProduct() {
        when(productRepository.existsByIdAndUserId(1L, TEST_USER_ID)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.getRecordsByProduct(TEST_USER_ID, 1L));
        verify(serviceRecordRepository, never()).findByProductIdAndUserId(eq(1L), any(Long.class));
    }

    @Test
    void updateRecordUpdatesFieldsSuccessfully() {
        Product product = sampleProduct(1L);
        ServiceRecord existing = sampleRecord(1L, TEST_USER_ID, product);
        ServiceRecord updatedDetails = new ServiceRecord(null, TEST_USER_ID, product, LocalDate.of(2026, 8, 15),
                "Updated Care", "Replaced battery", new BigDecimal("1200.00"), ServiceType.REPAIR);

        when(serviceRecordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(product));
        when(serviceRecordRepository.save(existing)).thenReturn(existing);

        ServiceRecord result = serviceRecordService.updateRecord(TEST_USER_ID, 1L, updatedDetails);

        assertEquals("Updated Care", result.getProvider());
        assertEquals("Replaced battery", result.getDescription());
        assertEquals(new BigDecimal("1200.00"), result.getCost());
        verify(serviceRecordRepository).save(existing);
    }

    @Test
    void updateRecordThrowsWhenRecordNotFound() {
        ServiceRecord updatedDetails = sampleRecord(null, TEST_USER_ID, sampleProduct(1L));

        when(serviceRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.updateRecord(TEST_USER_ID, 99L, updatedDetails));
        verify(serviceRecordRepository, never()).save(any());
    }

    @Test
    void updateRecordRejectsOtherUsersProduct() {
        Product product = sampleProduct(1L);
        ServiceRecord existing = sampleRecord(1L, TEST_USER_ID, product);
        ServiceRecord updatedDetails = new ServiceRecord(null, TEST_USER_ID, product, LocalDate.of(2026, 8, 15),
                "Updated Care", "Replaced battery", new BigDecimal("1200.00"), ServiceType.REPAIR);

        when(serviceRecordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.updateRecord(TEST_USER_ID, 1L, updatedDetails));
        verify(serviceRecordRepository, never()).save(any());
    }

    @Test
    void deleteRecordDeletesSuccessfully() {
        Product product = sampleProduct(1L);
        ServiceRecord record = sampleRecord(1L, TEST_USER_ID, product);
        when(serviceRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        serviceRecordService.deleteRecord(TEST_USER_ID, 1L);

        verify(serviceRecordRepository).deleteById(1L);
    }

    @Test
    void deleteRecordThrowsWhenNotFound() {
        when(serviceRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceRecordService.deleteRecord(TEST_USER_ID, 99L));
        verify(serviceRecordRepository, never()).deleteById(99L);
    }

    private Product sampleProduct(Long id) {
        return new Product(id, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SERIAL-1", "Notes");
    }

    private ServiceRecord sampleRecord(Long id, Long userId, Product product) {
        return new ServiceRecord(id, userId, product, LocalDate.of(2026, 8, 10), "Tech Care",
                "Cleaned cooling fan", new BigDecimal("450.00"), ServiceType.MAINTENANCE);
    }
}
