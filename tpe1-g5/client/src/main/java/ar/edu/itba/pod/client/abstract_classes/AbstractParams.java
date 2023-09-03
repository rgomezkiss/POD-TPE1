package ar.edu.itba.pod.client.abstract_classes;

public abstract class AbstractParams {
    private final String serverAddress;
    private final String action;
    private final Integer day;

    protected AbstractParams(String serverAddress, String action, Integer day) {
        this.serverAddress = serverAddress;
        this.action = action;
        this.day = day;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getAction() {
        return action;
    }

    public Integer getDay() {
        return day;
    }
}
