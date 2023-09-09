package ar.edu.itba.pod.server.exceptions;

public class TicketAlreadyExistsException extends RuntimeException {
    private final static String MESSAGE = "Ticket already assigned";

    public TicketAlreadyExistsException() {
        super(MESSAGE);
    }
}
