package ar.edu.itba.pod.client.admin.utils;

import ar.edu.itba.pod.client.abstract_classes.AbstractParams;

public class AdminParams extends AbstractParams {
    private final String inputPath; // o archivo ya parseado...
    private final String ride;
    private final Integer capacity;

    public AdminParams(String serverAddress, String action, Integer day, String inputPath) {
        super(serverAddress, action, day);
        this.inputPath = inputPath;
        this.ride = null;
        this.capacity = null;
    }

    public AdminParams(String serverAddress, String action, Integer day, String ride, Integer capacity) {
        super(serverAddress, action, day);
        this.ride = ride;
        this.capacity = capacity;
        this.inputPath = null;
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getRide() {
        return ride;
    }

    public Integer getCapacity() {
        return capacity;
    }
}
