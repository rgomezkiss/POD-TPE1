package ar.edu.itba.pod.client.utils;

@FunctionalInterface
public interface Action {
    void execute(AbstractParams params);
}