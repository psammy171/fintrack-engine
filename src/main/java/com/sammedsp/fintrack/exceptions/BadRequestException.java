package com.sammedsp.fintrack.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message){
        super(message);
    }
}
