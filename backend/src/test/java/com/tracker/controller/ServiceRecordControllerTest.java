package com.tracker.controller;

import com.purchasewarrantytracker.model.Product;

import com.tracker.entity.ServiceRecord;
import com.tracker.entity.ServiceType;
import com.tracker.service.ServiceRecordService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

import com.purchasewarrantytracker.exception.GlobalExceptionHandler;
import org.springframework.test.context.ContextConfiguration;

@WebMvcTest(controllers = ServiceRecordController.class)
@ContextConfiguration(classes = {ServiceRecordController.class, GlobalExceptionHandler.class})
class ServiceRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceRecordService serviceRecordService;

    @Test
    void getAllRecordsReturnsOk() throws Exception {
        Product product = new Product(1L, "Laptop", "Electronics", null, null, null, null);
        ServiceRecord record = new ServiceRecord(1L, product, LocalDate.of(2026, 8, 10),
                "Tech Care", "Fan cleaned", new BigDecimal("500.00"), ServiceType.MAINTENANCE);

        when(serviceRecordService.getAllRecords()).thenReturn(List.of(record));

        mockMvc.perform(get("/api/service-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].provider").value("Tech Care"));
    }

    @Test
    void getRecordByIdReturnsOk() throws Exception {
        Product product = new Product(1L, "Laptop", "Electronics", null, null, null, null);
        ServiceRecord record = new ServiceRecord(1L, product, LocalDate.of(2026, 8, 10),
                "Tech Care", "Fan cleaned", new BigDecimal("500.00"), ServiceType.MAINTENANCE);

        when(serviceRecordService.getRecordById(1L)).thenReturn(record);

        mockMvc.perform(get("/api/service-records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Fan cleaned"));
    }

    @Test
    void getRecordByIdNotFoundReturns404() throws Exception {
        when(serviceRecordService.getRecordById(99L)).thenThrow(new EntityNotFoundException("Service record with ID 99 was not found"));

        mockMvc.perform(get("/api/service-records/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getRecordsByProductIdReturnsOk() throws Exception {
        Product product = new Product(1L, "Laptop", "Electronics", null, null, null, null);
        ServiceRecord record = new ServiceRecord(1L, product, LocalDate.of(2026, 8, 10),
                "Tech Care", "Fan cleaned", new BigDecimal("500.00"), ServiceType.MAINTENANCE);

        when(serviceRecordService.getRecordsByProduct(1L)).thenReturn(List.of(record));

        mockMvc.perform(get("/api/service-records/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].provider").value("Tech Care"));
    }

    @Test
    void createRecordReturns201CreatedAndLocationHeader() throws Exception {
        Product product = new Product(1L, "Laptop", "Electronics", null, null, null, null);
        ServiceRecord record = new ServiceRecord(1L, product, LocalDate.of(2026, 8, 10),
                "Tech Care", "Fan cleaned", new BigDecimal("500.00"), ServiceType.MAINTENANCE);

        when(serviceRecordService.createRecord(any(ServiceRecord.class))).thenReturn(record);

        mockMvc.perform(post("/api/service-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"product\":{\"id\":1},\"serviceDate\":\"2026-08-10\",\"provider\":\"Tech Care\",\"description\":\"Fan cleaned\",\"cost\":500.00,\"serviceType\":\"MAINTENANCE\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/service-records/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.provider").value("Tech Care"));
    }

    @Test
    void createRecordWithInvalidDataReturns400BadRequest() throws Exception {
        mockMvc.perform(post("/api/service-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provider\":\"\",\"cost\":-5.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateRecordReturns200Ok() throws Exception {
        Product product = new Product(1L, "Laptop", "Electronics", null, null, null, null);
        ServiceRecord updated = new ServiceRecord(1L, product, LocalDate.of(2026, 8, 15),
                "New Provider", "Repaired hinge", new BigDecimal("750.00"), ServiceType.REPAIR);

        when(serviceRecordService.updateRecord(eq(1L), any(ServiceRecord.class))).thenReturn(updated);

        mockMvc.perform(put("/api/service-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"product\":{\"id\":1},\"serviceDate\":\"2026-08-15\",\"provider\":\"New Provider\",\"description\":\"Repaired hinge\",\"cost\":750.00,\"serviceType\":\"REPAIR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("New Provider"))
                .andExpect(jsonPath("$.serviceType").value("REPAIR"));
    }

    @Test
    void deleteRecordReturns204NoContent() throws Exception {
        doNothing().when(serviceRecordService).deleteRecord(1L);

        mockMvc.perform(delete("/api/service-records/1"))
                .andExpect(status().isNoContent());
    }
}

