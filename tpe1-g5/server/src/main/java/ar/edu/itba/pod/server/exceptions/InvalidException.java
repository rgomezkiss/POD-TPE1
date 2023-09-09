package ar.edu.itba.pod.server.exceptions;

public class InvalidException extends RuntimeException {

    public InvalidException(final String message){
        super(message);
    }

}
