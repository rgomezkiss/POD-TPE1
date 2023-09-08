package ar.edu.itba.pod.server.exceptions;

public class AttractionAlreadyExistsException extends RuntimeException {
    private final static String MESSAGE = "Attraction already exists";

    public AttractionAlreadyExistsException() {
        super(MESSAGE);
    }
}
