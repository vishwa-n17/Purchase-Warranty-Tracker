package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.DashboardDTO;
import com.purchasewarrantytracker.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void getSummaryReturnsOkAndDashboardDTO() throws Exception {
        DashboardDTO dto = new DashboardDTO(
                4L,
                new BigDecimal("107996.00"),
                new BigDecimal("0.00"),
                3L,
                0L
        );

        when(dashboardService.getDashboardSummary()).thenReturn(dto);

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductsCount").value(4))
                .andExpect(jsonPath("$.totalPurchaseSpend").value(107996.00))
                .andExpect(jsonPath("$.activeWarrantiesCount").value(3))
                .andExpect(jsonPath("$.expiredWarrantiesCount").value(0));
    }
}

