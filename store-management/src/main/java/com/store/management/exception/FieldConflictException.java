package com.store.management.exception;

import java.util.Map;

public class FieldConflictException extends RuntimeException {
    public final Map<String, String> errors;

    public FieldConflictException(Map<String, String> errors) {
        this.errors = errors;
    }
}
