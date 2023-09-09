package ar.edu.itba.pod.server.exceptions;

public class CapacityAlreadyAssignedException extends RuntimeException {
    private final static String MESSAGE = "Capacity already assigned";

    public CapacityAlreadyAssignedException() {
        super(MESSAGE);
    }
}
