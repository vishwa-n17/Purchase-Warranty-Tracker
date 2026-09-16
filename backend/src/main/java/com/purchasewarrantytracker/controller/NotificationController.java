package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.NotificationDTO;
import com.purchasewarrantytracker.security.AuthenticatedUserProvider;
import com.purchasewarrantytracker.service.IntelligenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final IntelligenceService intelligenceService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public NotificationController(IntelligenceService intelligenceService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.intelligenceService = intelligenceService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(intelligenceService.getNotifications(userId));
    }
}
