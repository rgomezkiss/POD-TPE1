package ar.edu.itba.pod.server.exceptions;

public class ReservationAlreadyConfirmedException extends RuntimeException {
    private final static String MESSAGE = "Reservations has been already confirmed";

    public ReservationAlreadyConfirmedException() {
        super(MESSAGE);
    }
}
