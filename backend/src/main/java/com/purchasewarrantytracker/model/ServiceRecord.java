package com.purchasewarrantytracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ServiceRecord {

    private Long id;
    private Long productId;
    private LocalDate serviceDate;
    private String problemDescription;
    private BigDecimal serviceCost;
    private String serviceNotes;

    public ServiceRecord() {
    }

    public ServiceRecord(Long id, Long productId, LocalDate serviceDate, String problemDescription,
                         BigDecimal serviceCost, String serviceNotes) {
        this.id = id;
        this.productId = productId;
        this.serviceDate = serviceDate;
        this.problemDescription = problemDescription;
        this.serviceCost = serviceCost;
        this.serviceNotes = serviceNotes;
    }

    public boolean wasFreeOfCharge() {
        return serviceCost != null && BigDecimal.ZERO.compareTo(serviceCost) == 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }
    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
    public BigDecimal getServiceCost() { return serviceCost; }
    public void setServiceCost(BigDecimal serviceCost) { this.serviceCost = serviceCost; }
    public String getServiceNotes() { return serviceNotes; }
    public void setServiceNotes(String serviceNotes) { this.serviceNotes = serviceNotes; }
}
