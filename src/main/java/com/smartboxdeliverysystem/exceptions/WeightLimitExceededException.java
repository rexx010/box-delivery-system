package com.smartboxdeliverysystem.exceptions;

public class WeightLimitExceededException extends RuntimeException {
    public WeightLimitExceededException(String txref) {
        super("Loading these items would exceed the weight limit for box: " + txref);
    }
}
