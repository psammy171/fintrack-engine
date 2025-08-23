package com.sammedsp.fintrack.exceptions;

public class EntityNotFoundException extends Exception {

    public EntityNotFoundException(String entityName, String id) {
        super(entityName + " with id " + id + " not found");
    }
}
