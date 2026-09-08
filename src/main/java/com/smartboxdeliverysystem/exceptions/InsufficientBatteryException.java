package com.smartboxdeliverysystem.exceptions;

public class InsufficientBatteryException extends RuntimeException {
    public InsufficientBatteryException(String txref) {
        super("Box " + txref + " battery is below the required 25% threshold for loading");
    }
}
