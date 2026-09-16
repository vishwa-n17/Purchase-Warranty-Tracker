package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.model.DashboardDTO;
import com.purchasewarrantytracker.model.DashboardInsightsDTO;
import com.purchasewarrantytracker.model.ProductHealthDTO;
import com.purchasewarrantytracker.model.RecentActivityDTO;
import com.purchasewarrantytracker.model.ServiceAnalyticsDTO;
import com.purchasewarrantytracker.model.WarrantyOverviewDTO;
import com.purchasewarrantytracker.security.AuthenticatedUserProvider;
import com.purchasewarrantytracker.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DashboardController(DashboardService dashboardService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.dashboardService = dashboardService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardDTO> getSummary() {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(dashboardService.getDashboardSummary(userId));
    }

    @GetMapping("/insights")
    public ResponseEntity<DashboardInsightsDTO> getInsights() {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(dashboardService.getDashboardInsights(userId));
    }

    @GetMapping("/warranty-overview")
    public ResponseEntity<WarrantyOverviewDTO> getWarrantyOverview() {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(dashboardService.getWarrantyOverview(userId));
    }

    @GetMapping("/service-analytics")
    public ResponseEntity<ServiceAnalyticsDTO> getServiceAnalytics() {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(dashboardService.getServiceAnalytics(userId));
    }

    @GetMapping("/activity")
    public ResponseEntity<List<RecentActivityDTO>> getActivity() {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(dashboardService.getRecentActivity(userId));
    }

    @GetMapping("/health")
    public ResponseEntity<List<ProductHealthDTO>> getHealth() {
        Long userId = authenticatedUserProvider.getCurrentUser().getId();
        return ResponseEntity.ok(dashboardService.getProductHealths(userId));
    }
}
