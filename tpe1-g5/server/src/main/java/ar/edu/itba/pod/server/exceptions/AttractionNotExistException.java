package ar.edu.itba.pod.server.exceptions;

public class AttractionNotExistException extends RuntimeException {
    private final static String MESSAGE = "Attraction not exist";

    public AttractionNotExistException() {
        super(MESSAGE);
    }
}

