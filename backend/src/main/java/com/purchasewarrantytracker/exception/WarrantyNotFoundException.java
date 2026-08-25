package com.purchasewarrantytracker.exception;

public class WarrantyNotFoundException extends RuntimeException {

    public WarrantyNotFoundException(long id) {
        super("Warranty with ID " + id + " was not found");
    }

    public WarrantyNotFoundException(String message) {
        super(message);
    }
}

