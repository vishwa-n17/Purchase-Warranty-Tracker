package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.model.HealthResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class HealthService {

    public HealthResponse getHealth() {
        return new HealthResponse("UP", "Purchase & Warranty Tracker backend is running", Instant.now());
    }
}
