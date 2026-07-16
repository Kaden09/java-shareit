package ru.practicum.shareit.exception;

public class ErrorResponse {
    private final String error;
    private final String message;

    public ErrorResponse(String message) {
        this.error = message;
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}