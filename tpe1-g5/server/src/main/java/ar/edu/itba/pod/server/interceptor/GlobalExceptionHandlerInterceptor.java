package ar.edu.itba.pod.server.interceptor;

import ar.edu.itba.pod.server.exceptions.*;
import com.google.rpc.Code;
import io.grpc.*;
import io.grpc.protobuf.StatusProto;

import java.util.HashMap;
import java.util.Map;

public class GlobalExceptionHandlerInterceptor implements ServerInterceptor {

    @Override
    public <T, R> ServerCall.Listener<T> interceptCall(
            ServerCall<T, R> serverCall, Metadata headers, ServerCallHandler<T, R> serverCallHandler
    ) {
        ServerCall.Listener<T> delegate = serverCallHandler.startCall(serverCall, headers);
        return new ExceptionHandler<>(delegate, serverCall, headers);
    }

    private static class ExceptionHandler<T, R> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<T> {
        private final ServerCall<T, R> delegate;
        private final Metadata headers;
        private final Map<Class<? extends Throwable>, Code> errorCodesByException = new HashMap<>();

        ExceptionHandler(ServerCall.Listener<T> listener, ServerCall<T, R> serverCall, Metadata headers) {
            super(listener);
            this.delegate = serverCall;
            this.headers = headers;
            errorCodesByException.put(AlreadyExistsException.class, Code.ALREADY_EXISTS);
            errorCodesByException.put(InvalidException.class, Code.INVALID_ARGUMENT);
            errorCodesByException.put(NotFoundException.class, Code.NOT_FOUND);
            errorCodesByException.put(UnavailableException.class, Code.UNAVAILABLE);
            errorCodesByException.put(IllegalArgumentException.class, Code.UNKNOWN);
        }

        @Override
        public void onHalfClose() {
            try {
                super.onHalfClose();
            } catch (RuntimeException ex) {
                handleException(ex, delegate, headers);
            }
        }

        private void handleException(RuntimeException exception, ServerCall<T, R> serverCall, Metadata headers) {
            Throwable error = exception;
            if (!errorCodesByException.containsKey(error.getClass())) {
                // Si la excepción vino "wrappeada" entonces necesitamos preguntar por la causa.
                error = error.getCause();
                if (error == null || !errorCodesByException.containsKey(error.getClass())) {
                    // Una excepción NO esperada.
                    serverCall.close(Status.UNKNOWN, headers);
                    return;
                }
            }
            // Una excepción esperada.
            com.google.rpc.Status rpcStatus = com.google.rpc.Status.newBuilder()
                    .setCode(errorCodesByException.get(error.getClass()).getNumber())
                    .setMessage(error.getMessage())
                    .build();
            StatusRuntimeException statusRuntimeException = StatusProto.toStatusRuntimeException(rpcStatus);
            Status newStatus = Status.fromThrowable(statusRuntimeException);
            serverCall.close(newStatus, headers);
        }
    }

}