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

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createSavesAValidProduct() {
        Product product = product("Laptop", "Electronics");
        when(productRepository.save(product)).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(1L);
            return savedProduct;
        });

        Product savedProduct = productService.create(product);

        assertEquals(1L, savedProduct.getId());
        verify(productRepository).save(product);
    }

    @Test
    void createRejectsProductWithoutName() {
        Product product = product(" ", "Electronics");

        assertThrows(IllegalArgumentException.class, () -> productService.create(product));

        verify(productRepository, never()).save(any());
    }

    @Test
    void getByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getById(99L));
    }

    @Test
    void updateSetsPathIdAndCallsRepository() {
        Product existingProduct = product("Laptop", "Electronics");
        existingProduct.setId(1L);
        Product changedProduct = product("Updated Laptop", "Electronics");
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.update(changedProduct)).thenReturn(true);

        Product updatedProduct = productService.update(1L, changedProduct);

        assertEquals(1L, updatedProduct.getId());
        verify(productRepository).update(changedProduct);
    }

    @Test
    void deleteRejectsProductWithPurchases() {
        Product existingProduct = product("Laptop", "Electronics");
        existingProduct.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.hasPurchases(1L)).thenReturn(true);

        assertThrows(ProductInUseException.class, () -> productService.delete(1L));

        verify(productRepository, never()).deleteById(1L);
    }

    private Product product(String name, String category) {
        return new Product(null, name, category, "Lenovo", "IdeaPad", "SERIAL-1", "Test product");
    }
}
