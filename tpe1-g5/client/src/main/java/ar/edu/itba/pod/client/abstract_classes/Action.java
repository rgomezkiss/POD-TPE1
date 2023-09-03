package ar.edu.itba.pod.client.abstract_classes;

@FunctionalInterface
public interface Action {
    void execute(AbstractParams params);
}