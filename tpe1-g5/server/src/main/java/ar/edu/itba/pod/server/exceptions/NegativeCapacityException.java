package ar.edu.itba.pod.server.exceptions;

public class NegativeCapacityException extends RuntimeException {
    private final static String MESSAGE = "Capacity must be a positive number";

    public NegativeCapacityException() {
        super(MESSAGE);
    }
}
