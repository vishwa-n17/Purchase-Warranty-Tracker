package com.purchasewarrantytracker.exception;

public class ProductInUseException extends RuntimeException {

    public ProductInUseException(long id) {
        super("Product with ID " + id + " cannot be deleted because it has purchase records");
    }
}
