package ar.edu.itba.pod.server.exceptions;

public class SlotSizeNotEnoughException extends RuntimeException {
    private final static String MESSAGE = "Slot size not enough";

    public SlotSizeNotEnoughException() {
        super(MESSAGE);
    }
}