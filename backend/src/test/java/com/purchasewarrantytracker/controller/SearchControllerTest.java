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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@ContextConfiguration(classes = {SearchController.class, GlobalExceptionHandler.class, TestSecurityConfig.class})
class SearchControllerTest {

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
    void searchReturnsResults() throws Exception {
        when(intelligenceService.search(any(), any())).thenReturn(
                new SearchResultDTO(
                        List.of(new ProductSearchDTO(1L, "Laptop", "Electronics")),
                        List.of(),
                        List.of(),
                        List.of()
                )
        );

        mockMvc.perform(get("/api/search?q=Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].name").value("Laptop"));
    }

    @Test
    void searchWithEmptyQueryReturnsEmpty() throws Exception {
        when(intelligenceService.search(any(), any())).thenReturn(
                new SearchResultDTO(List.of(), List.of(), List.of(), List.of())
        );

        mockMvc.perform(get("/api/search?q="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty());
    }
}
