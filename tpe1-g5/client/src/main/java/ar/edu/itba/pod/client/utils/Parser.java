package ar.edu.itba.pod.client.utils;

@FunctionalInterface
public interface Parser<T extends AbstractParams> {
    T parse(String[] args);
}