package ar.edu.itba.pod.server.exceptions;

public class InvalidDayException extends RuntimeException {
    private final static String MESSAGE = "Invalid day";

    public InvalidDayException() {
        super(MESSAGE);
    }
}
