package com.smartboxdeliverysystem.exceptions;

public class BoxNotFoundException extends RuntimeException {
    public BoxNotFoundException(String txref) {
        super("No box found with txref: " + txref);
    }
}
