package ar.edu.itba.pod.client.utils;

import io.grpc.ManagedChannel;

@FunctionalInterface
public interface Action<T extends AbstractParams> {
    void execute(T params, ManagedChannel channel);
}