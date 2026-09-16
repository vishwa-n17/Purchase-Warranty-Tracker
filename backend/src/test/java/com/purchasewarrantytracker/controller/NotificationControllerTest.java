package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.config.TestSecurityConfig;
import com.purchasewarrantytracker.exception.GlobalExceptionHandler;
import com.purchasewarrantytracker.model.NotificationDTO;
import com.purchasewarrantytracker.model.User;
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

@WebMvcTest(NotificationController.class)
@ContextConfiguration(classes = {NotificationController.class, GlobalExceptionHandler.class, TestSecurityConfig.class})
class NotificationControllerTest {

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
    void getNotificationsReturnsOk() throws Exception {
        when(intelligenceService.getNotifications(any())).thenReturn(List.of(
                new NotificationDTO("1", "WARRANTY", "Warranty Expiring", "Laptop warranty expires soon", "medium", false)
        ));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Warranty Expiring"));
    }
}
