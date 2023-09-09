package ar.edu.itba.pod.server.exceptions;

public class NegativeSlotSizeException extends RuntimeException {
    private final static String MESSAGE = "Slot size must be a positive number";

    public NegativeSlotSizeException() {
        super(MESSAGE);
    }
}

