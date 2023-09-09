package ar.edu.itba.pod.server.exceptions;

public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(final String message){
        super(message);
    }

}
