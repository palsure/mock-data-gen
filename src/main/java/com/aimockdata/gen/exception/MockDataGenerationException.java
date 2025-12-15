package com.aimockdata.gen.exception;

/**
 * Exception thrown when mock data generation fails.
 */
public class MockDataGenerationException extends Exception {
    
    /**
     * Creates a new MockDataGenerationException with the specified message.
     * 
     * @param message Error message
     */
    public MockDataGenerationException(String message) {
        super(message);
    }
    
    /**
     * Creates a new MockDataGenerationException with the specified message and cause.
     * 
     * @param message Error message
     * @param cause The cause of the exception
     */
    public MockDataGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

