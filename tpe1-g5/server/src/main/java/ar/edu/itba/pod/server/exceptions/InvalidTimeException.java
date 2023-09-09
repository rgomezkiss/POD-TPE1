package ar.edu.itba.pod.server.exceptions;

public class InvalidTimeException extends RuntimeException {
    private final static String MESSAGE = "Invalid time, it must be HH:MM";

    public InvalidTimeException() {
        super(MESSAGE);
    }
}

