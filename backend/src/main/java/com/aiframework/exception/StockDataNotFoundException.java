package com.aiframework.exception;

/**
 * Exception thrown when stock data is not found or unavailable
 */
public class StockDataNotFoundException extends RuntimeException {

    public StockDataNotFoundException(String message) {
        super(message);
    }

    public StockDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
