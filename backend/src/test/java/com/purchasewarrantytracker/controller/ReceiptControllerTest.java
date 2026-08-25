package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.exception.PurchaseNotFoundException;
import com.purchasewarrantytracker.exception.ReceiptNotFoundException;
import com.purchasewarrantytracker.model.Receipt;
import com.purchasewarrantytracker.service.ReceiptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

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

@WebMvcTest(ReceiptController.class)
@ActiveProfiles("mysql")
class ReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptService receiptService;

    @Test
    void createReceiptReturnsCreated() throws Exception {
        Receipt receipt = new Receipt(1L, 1L, "receipts/invoice.pdf", LocalDate.of(2026, 6, 15));
        when(receiptService.create(eq(1L), any(Receipt.class))).thenReturn(receipt);

        mockMvc.perform(post("/api/purchases/1/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiptFilePath\":\"receipts/invoice.pdf\",\"receiptDate\":\"2026-06-15\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/purchases/1/receipt"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.receiptFilePath").value("receipts/invoice.pdf"));
    }

    @Test
    void getReceiptReturnsReceipt() throws Exception {
        Receipt receipt = new Receipt(1L, 1L, "receipts/invoice.pdf", LocalDate.of(2026, 6, 15));
        when(receiptService.getByPurchaseId(1L)).thenReturn(receipt);

        mockMvc.perform(get("/api/purchases/1/receipt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptFilePath").value("receipts/invoice.pdf"))
                .andExpect(jsonPath("$.receiptDate").value("2026-06-15"));
    }

    @Test
    void getReceiptThrowsNotFoundWhenReceiptMissing() throws Exception {
        when(receiptService.getByPurchaseId(1L)).thenThrow(new ReceiptNotFoundException(1L));

        mockMvc.perform(get("/api/purchases/1/receipt"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getReceiptThrowsNotFoundWhenPurchaseMissing() throws Exception {
        when(receiptService.getByPurchaseId(99L)).thenThrow(new PurchaseNotFoundException(99L));

        mockMvc.perform(get("/api/purchases/99/receipt"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateReceiptReturnsUpdatedReceipt() throws Exception {
        Receipt updated = new Receipt(1L, 1L, "receipts/updated.pdf", LocalDate.of(2026, 6, 20));
        when(receiptService.update(eq(1L), any(Receipt.class))).thenReturn(updated);

        mockMvc.perform(put("/api/purchases/1/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiptFilePath\":\"receipts/updated.pdf\",\"receiptDate\":\"2026-06-20\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptFilePath").value("receipts/updated.pdf"));
    }

    @Test
    void deleteReceiptReturnsNoContent() throws Exception {
        doNothing().when(receiptService).delete(1L);

        mockMvc.perform(delete("/api/purchases/1/receipt"))
                .andExpect(status().isNoContent());
    }
}

