package com.voltstream.exception;

public class InvalidVehicleException extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidVehicleException(String message) {
        super(message);
    }
}