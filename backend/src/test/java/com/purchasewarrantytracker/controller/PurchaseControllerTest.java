package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.exception.PurchaseNotFoundException;
import com.purchasewarrantytracker.model.PaymentMethod;
import com.purchasewarrantytracker.model.Purchase;
import com.purchasewarrantytracker.service.PurchaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurchaseController.class)
@ActiveProfiles("mysql")
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseService purchaseService;

    @Test
    void createReturnsCreatedPurchaseAndLocationHeader() throws Exception {
        Purchase purchase = new Purchase(1L, 1L, LocalDate.of(2026, 6, 15), new BigDecimal("54999.00"), "Campus Store", PaymentMethod.UPI);
        when(purchaseService.create(any(Purchase.class))).thenReturn(purchase);

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"purchaseDate\":\"2026-06-15\",\"purchasePrice\":54999.00,\"storeName\":\"Campus Store\",\"paymentMethod\":\"UPI\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/purchases/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.storeName").value("Campus Store"))
                .andExpect(jsonPath("$.paymentMethod").value("UPI"));
    }

    @Test
    void getAllReturnsPurchases() throws Exception {
        when(purchaseService.getAll()).thenReturn(List.of(
                new Purchase(1L, 1L, LocalDate.of(2026, 6, 15), new BigDecimal("54999.00"), "Campus Store", PaymentMethod.UPI)
        ));

        mockMvc.perform(get("/api/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].paymentMethod").value("UPI"));
    }

    @Test
    void getByIdReturnsPurchase() throws Exception {
        when(purchaseService.getById(1L)).thenReturn(
                new Purchase(1L, 1L, LocalDate.of(2026, 6, 15), new BigDecimal("54999.00"), "Campus Store", PaymentMethod.UPI)
        );

        mockMvc.perform(get("/api/purchases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.storeName").value("Campus Store"));
    }

    @Test
    void getByIdNotFoundReturns404() throws Exception {
        when(purchaseService.getById(99L)).thenThrow(new PurchaseNotFoundException(99L));

        mockMvc.perform(get("/api/purchases/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getByProductIdReturnsPurchases() throws Exception {
        when(purchaseService.getByProductId(1L)).thenReturn(List.of(
                new Purchase(1L, 1L, LocalDate.of(2026, 6, 15), new BigDecimal("54999.00"), "Campus Store", PaymentMethod.UPI)
        ));

        mockMvc.perform(get("/api/products/1/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(1));
    }

    @Test
    void updateReturnsUpdatedPurchase() throws Exception {
        Purchase updated = new Purchase(1L, 1L, LocalDate.of(2026, 6, 15), new BigDecimal("49999.00"), "Updated Store", PaymentMethod.CARD);
        when(purchaseService.update(eq(1L), any(Purchase.class))).thenReturn(updated);

        mockMvc.perform(put("/api/purchases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"purchaseDate\":\"2026-06-15\",\"purchasePrice\":49999.00,\"storeName\":\"Updated Store\",\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("Updated Store"))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        doNothing().when(purchaseService).delete(1L);

        mockMvc.perform(delete("/api/purchases/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidPurchaseDataReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":-1,\"purchasePrice\":-10.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}

