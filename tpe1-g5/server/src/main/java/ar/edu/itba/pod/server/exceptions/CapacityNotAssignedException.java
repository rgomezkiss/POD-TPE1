package ar.edu.itba.pod.server.exceptions;

public class CapacityNotAssignedException extends RuntimeException {
    private final static String MESSAGE = "Capacity has not been assigned";

    public CapacityNotAssignedException() {
        super(MESSAGE);
    }
}
