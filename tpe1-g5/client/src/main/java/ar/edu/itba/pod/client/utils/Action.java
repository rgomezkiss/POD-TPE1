package ar.edu.itba.pod.client.utils;

import io.grpc.ManagedChannel;

@FunctionalInterface
public interface Action {
    void execute(AbstractParams params, ManagedChannel channel);
}