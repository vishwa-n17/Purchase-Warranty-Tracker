package com.purchasewarrantytracker.exception;

public class ReceiptNotFoundException extends RuntimeException {

    public ReceiptNotFoundException(long purchaseId) {
        super("Receipt for purchase ID " + purchaseId + " was not found");
    }
}

