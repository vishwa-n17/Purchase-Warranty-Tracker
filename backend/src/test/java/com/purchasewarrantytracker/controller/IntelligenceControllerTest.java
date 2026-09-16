package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.config.TestSecurityConfig;
import com.purchasewarrantytracker.exception.GlobalExceptionHandler;
import com.purchasewarrantytracker.model.*;
import com.purchasewarrantytracker.security.AuthenticatedUserProvider;
import com.purchasewarrantytracker.service.IntelligenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntelligenceController.class)
@ContextConfiguration(classes = {IntelligenceController.class, GlobalExceptionHandler.class, TestSecurityConfig.class})
class IntelligenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IntelligenceService intelligenceService;

    @MockBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        User currentUser = new User(1L, "Test User", "test@example.com", "encoded", null);
        when(authenticatedUserProvider.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void getLifecycleReturnsOk() throws Exception {
        ProductLifecycleDTO dto = new ProductLifecycleDTO(
                1L, "Laptop", "Electronics", "Lenovo", "IdeaPad", "SN-1", "Notes",
                new PurchaseSummaryDTO(1L, LocalDate.now(), new BigDecimal("1000"), "Store", "CARD"),
                new WarrantyIntelligenceDTO(1L, "Lenovo", LocalDate.now(), 12, LocalDate.now().plusMonths(12), "ACTIVE", 365L, 0.0, 100.0, "ACTIVE"),
                List.of(),
                null,
                new OwnershipCostDTO(new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("1000")),
                new ProductHealthDTO(1L, "Laptop", 100, "GOOD", "Good"),
                new ProductStatusDTO("PROTECTED", "#059669", "🟢")
        );

        when(intelligenceService.getProductLifecycle(any(), any())).thenReturn(dto);

        mockMvc.perform(get("/api/intelligence/products/1/lifecycle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.health.score").value(100));
    }

    @Test
    void getTimelineReturnsOk() throws Exception {
        when(intelligenceService.getProductTimeline(any(), any())).thenReturn(List.of(
                new TimelineEventDTO(null, "PRODUCT", "Product Registered", "Laptop", "📦", "#4f46e5")
        ));

        mockMvc.perform(get("/api/intelligence/products/1/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("PRODUCT"));
    }

    @Test
    void getWarrantyIntelligenceReturnsOk() throws Exception {
        when(intelligenceService.getWarrantyIntelligence(any(), any())).thenReturn(
                new WarrantyIntelligenceDTO(1L, "Lenovo", LocalDate.now(), 12, LocalDate.now().plusMonths(12), "ACTIVE", 365L, 0.0, 100.0, "ACTIVE")
        );

        mockMvc.perform(get("/api/intelligence/products/1/warranty-intelligence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warrantyProvider").value("Lenovo"));
    }

    @Test
    void getClaimReadinessReturnsOk() throws Exception {
        when(intelligenceService.getClaimReadiness(any(), any())).thenReturn(
                new ClaimReadinessDTO(true, 6, 6, List.of())
        );

        mockMvc.perform(get("/api/intelligence/products/1/claim-readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ready").value(true));
    }

    @Test
    void getOwnershipCostReturnsOk() throws Exception {
        when(intelligenceService.getOwnershipCost(any(), any())).thenReturn(
                new OwnershipCostDTO(new BigDecimal("1000"), new BigDecimal("200"), new BigDecimal("1200"))
        );

        mockMvc.perform(get("/api/intelligence/products/1/ownership-cost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(1200));
    }

    @Test
    void getHealthReturnsOk() throws Exception {
        when(intelligenceService.getProductHealth(any(), any())).thenReturn(
                new ProductHealthDTO(1L, "Laptop", 100, "GOOD", "Good")
        );

        mockMvc.perform(get("/api/intelligence/products/1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(100));
    }

    @Test
    void getStatusReturnsOk() throws Exception {
        when(intelligenceService.getProductStatus(any(), any())).thenReturn(
                new ProductStatusDTO("PROTECTED", "#059669", "🟢")
        );

        mockMvc.perform(get("/api/intelligence/products/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROTECTED"));
    }
}
