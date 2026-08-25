package com.purchasewarrantytracker.exception;

public class PurchaseNotFoundException extends RuntimeException {

    public PurchaseNotFoundException(long id) {
        super("Purchase with ID " + id + " was not found");
    }
}

