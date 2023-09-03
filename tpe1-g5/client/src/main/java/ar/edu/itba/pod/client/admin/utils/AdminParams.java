package ar.edu.itba.pod.client.admin.utils;

import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.ServerAddress;

public class AdminParams extends AbstractParams {
    private final String inputPath;
    private final String ride;
    private final Integer capacity;

    public AdminParams(ServerAddress serverAddress, String action, String inputPath) {
        super(serverAddress, action, null);
        this.inputPath = inputPath;
        this.ride = null;
        this.capacity = null;
    }

    public AdminParams(ServerAddress serverAddress, String action, Integer day, String ride, Integer capacity) {
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
