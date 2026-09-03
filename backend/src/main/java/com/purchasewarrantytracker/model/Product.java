package com.purchasewarrantytracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must be at most 150 characters")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must be at most 100 characters")
    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Size(max = 100, message = "Brand must be at most 100 characters")
    @Column(name = "brand", length = 100)
    private String brand;

    @Size(max = 100, message = "Model must be at most 100 characters")
    @Column(name = "model", length = 100)
    private String model;

    @Size(max = 150, message = "Serial number must be at most 150 characters")
    @Column(name = "serial_number", unique = true, length = 150)
    private String serialNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public Product() {
    }

    public Product(Long id, String name, String category, String brand, String model, String serialNumber, String notes) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.model = model;
        this.serialNumber = serialNumber;
        this.notes = notes;
    }

    public boolean hasSerialNumber() {
        return serialNumber != null && !serialNumber.isBlank();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}