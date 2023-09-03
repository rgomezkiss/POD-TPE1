package ar.edu.itba.pod.client.utils;

public abstract class AbstractParams {
    private final ServerAddress serverAddress;
    private final String action;
    private final Integer day;

    protected AbstractParams(ServerAddress serverAddress, String action, Integer day) {
        this.serverAddress = serverAddress;
        this.action = action;
        this.day = day;
    }

    public ServerAddress getServerAddress() {
        return serverAddress;
    }
    public String getAction() {
        return action;
    }
    public Integer getDay() {
        return day;
    }
}
