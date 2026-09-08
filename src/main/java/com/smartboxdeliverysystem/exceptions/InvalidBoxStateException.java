package com.smartboxdeliverysystem.exceptions;

public class InvalidBoxStateException extends RuntimeException {
    public InvalidBoxStateException(String txref, String currentState) {
        super("Box " + txref + " cannot be loaded from its current state: " + currentState);
    }
}
