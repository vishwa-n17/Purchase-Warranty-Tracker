package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.exception.WarrantyNotFoundException;
import com.purchasewarrantytracker.model.Warranty;
import com.purchasewarrantytracker.model.WarrantyStatus;
import com.purchasewarrantytracker.service.WarrantyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(WarrantyController.class)
@ActiveProfiles("mysql")
class WarrantyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WarrantyService warrantyService;

    @Test
    void createReturnsCreatedWarrantyAndLocationHeader() throws Exception {
        Warranty warranty = new Warranty(1L, 1L, LocalDate.of(2026, 6, 15), 12, LocalDate.of(2027, 6, 15), "Lenovo", WarrantyStatus.ACTIVE);
        when(warrantyService.create(any(Warranty.class))).thenReturn(warranty);

        mockMvc.perform(post("/api/warranties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"startDate\":\"2026-06-15\",\"durationMonths\":12,\"warrantyProvider\":\"Lenovo\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/warranties/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.warrantyProvider").value("Lenovo"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getAllReturnsWarranties() throws Exception {
        when(warrantyService.getAll()).thenReturn(List.of(
                new Warranty(1L, 1L, LocalDate.of(2026, 6, 15), 12, LocalDate.of(2027, 6, 15), "Lenovo", WarrantyStatus.ACTIVE)
        ));

        mockMvc.perform(get("/api/warranties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].warrantyProvider").value("Lenovo"));
    }

    @Test
    void getByIdReturnsWarranty() throws Exception {
        when(warrantyService.getById(1L)).thenReturn(
                new Warranty(1L, 1L, LocalDate.of(2026, 6, 15), 12, LocalDate.of(2027, 6, 15), "Lenovo", WarrantyStatus.ACTIVE)
        );

        mockMvc.perform(get("/api/warranties/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.warrantyProvider").value("Lenovo"));
    }

    @Test
    void getByIdNotFoundReturns404() throws Exception {
        when(warrantyService.getById(99L)).thenThrow(new WarrantyNotFoundException(99L));

        mockMvc.perform(get("/api/warranties/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getByProductIdReturnsWarranty() throws Exception {
        when(warrantyService.getByProductId(1L)).thenReturn(
                new Warranty(1L, 1L, LocalDate.of(2026, 6, 15), 12, LocalDate.of(2027, 6, 15), "Lenovo", WarrantyStatus.ACTIVE)
        );

        mockMvc.perform(get("/api/products/1/warranty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1));
    }

    @Test
    void updateReturnsUpdatedWarranty() throws Exception {
        Warranty updated = new Warranty(1L, 1L, LocalDate.of(2026, 6, 15), 24, LocalDate.of(2028, 6, 15), "Lenovo Premium", WarrantyStatus.ACTIVE);
        when(warrantyService.update(eq(1L), any(Warranty.class))).thenReturn(updated);

        mockMvc.perform(put("/api/warranties/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"startDate\":\"2026-06-15\",\"durationMonths\":24,\"warrantyProvider\":\"Lenovo Premium\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warrantyProvider").value("Lenovo Premium"))
                .andExpect(jsonPath("$.durationMonths").value(24));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        doNothing().when(warrantyService).delete(1L);

        mockMvc.perform(delete("/api/warranties/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidWarrantyDataReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/warranties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":-1,\"durationMonths\":0,\"warrantyProvider\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}

