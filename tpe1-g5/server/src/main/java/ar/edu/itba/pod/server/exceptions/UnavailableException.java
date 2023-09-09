package ar.edu.itba.pod.server.exceptions;

public class UnavailableException extends RuntimeException {

    public UnavailableException(final String message){
        super(message);
    }

}
