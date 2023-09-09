package ar.edu.itba.pod.server.exceptions;

public class InvalidSlotException extends RuntimeException {
    private final static String MESSAGE = "Invalid slot";

    public InvalidSlotException() {
        super(MESSAGE);
    }
}
