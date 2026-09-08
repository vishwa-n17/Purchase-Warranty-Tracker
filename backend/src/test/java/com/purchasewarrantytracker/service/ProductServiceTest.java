package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.exception.ProductInUseException;
import com.purchasewarrantytracker.exception.ProductNotFoundException;
import com.purchasewarrantytracker.model.Product;
import com.purchasewarrantytracker.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final long TEST_USER_ID = 1L;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createSavesAValidProduct() {
        Product product = product("Laptop", "Electronics");
        product.setUserId(TEST_USER_ID);
        when(productRepository.save(product)).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(1L);
            return savedProduct;
        });

        Product savedProduct = productService.create(TEST_USER_ID, product);

        assertEquals(1L, savedProduct.getId());
        verify(productRepository).save(product);
    }

    @Test
    void createRejectsProductWithoutName() {
        Product product = product(" ", "Electronics");
        product.setUserId(TEST_USER_ID);

        assertThrows(IllegalArgumentException.class, () -> productService.create(TEST_USER_ID, product));

        verify(productRepository, never()).save(any());
    }

    @Test
    void getByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findByIdAndUserId(99L, TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getById(TEST_USER_ID, 99L));
    }

    @Test
    void updateSetsPathIdAndCallsRepository() {
        Product existingProduct = product("Laptop", "Electronics");
        existingProduct.setId(1L);
        existingProduct.setUserId(TEST_USER_ID);
        Product changedProduct = product("Updated Laptop", "Electronics");
        changedProduct.setUserId(TEST_USER_ID);
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(existingProduct));
        when(productRepository.update(changedProduct)).thenReturn(true);

        Product updatedProduct = productService.update(TEST_USER_ID, 1L, changedProduct);

        assertEquals(1L, updatedProduct.getId());
        verify(productRepository).update(changedProduct);
    }

    @Test
    void deleteRejectsProductWithPurchases() {
        Product existingProduct = product("Laptop", "Electronics");
        existingProduct.setId(1L);
        existingProduct.setUserId(TEST_USER_ID);
        when(productRepository.findByIdAndUserId(1L, TEST_USER_ID)).thenReturn(Optional.of(existingProduct));
        when(productRepository.hasPurchases(1L, TEST_USER_ID)).thenReturn(true);

        assertThrows(ProductInUseException.class, () -> productService.delete(TEST_USER_ID, 1L));

        verify(productRepository, never()).deleteByIdAndUserId(1L, TEST_USER_ID);
    }

    private Product product(String name, String category) {
        Product p = new Product();
        p.setName(name);
        p.setCategory(category);
        p.setBrand("Lenovo");
        p.setModel("IdeaPad");
        p.setSerialNumber("SERIAL-1");
        p.setNotes("Test product");
        return p;
    }
}
