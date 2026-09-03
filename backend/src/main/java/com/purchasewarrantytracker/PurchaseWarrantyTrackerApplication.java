package com.purchasewarrantytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.purchasewarrantytracker", "com.tracker"})
@EntityScan(basePackages = {"com.purchasewarrantytracker", "com.tracker"})
@EnableJpaRepositories(basePackages = {"com.purchasewarrantytracker", "com.tracker"})
public class PurchaseWarrantyTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PurchaseWarrantyTrackerApplication.class, args);
    }
}