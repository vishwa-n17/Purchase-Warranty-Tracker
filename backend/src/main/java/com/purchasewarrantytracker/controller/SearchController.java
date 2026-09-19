package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.*;
import com.purchasewarrantytracker.security.AuthenticatedUserProvider;
import com.purchasewarrantytracker.service.IntelligenceService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@Validated
public class SearchController {

    private final IntelligenceService intelligenceService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public SearchController(IntelligenceService intelligenceService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.intelligenceService = intelligenceService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping
    public ResponseEntity<SearchResultDTO> search(@RequestParam String q) {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(new SearchResultDTO(List.of(), List.of(), List.of(), List.of()));
        }
        return ResponseEntity.ok(intelligenceService.search(userId, q.trim()));
    }
}
