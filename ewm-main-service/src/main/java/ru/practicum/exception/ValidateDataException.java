package ru.practicum.exception;

public class ValidateDataException extends RuntimeException {
    public ValidateDataException(String message) {
        super(message);
    }
}
