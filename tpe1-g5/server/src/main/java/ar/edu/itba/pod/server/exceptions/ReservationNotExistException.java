package ar.edu.itba.pod.server.exceptions;

public class ReservationNotExistException extends RuntimeException {
    private final static String MESSAGE = "Reservations not exist";

    public ReservationNotExistException() {
        super(MESSAGE);
    }
}