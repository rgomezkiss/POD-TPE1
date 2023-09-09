package ar.edu.itba.pod.server.exceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException(final String message){
        super(message);
    }

}
