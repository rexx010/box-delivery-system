package com.smartboxdeliverysystem.exceptions;

public class DuplicateTxrefException extends RuntimeException {
    public DuplicateTxrefException(String txref) {
        super("A box with txref '" + txref + "' already exists");
    }
}
