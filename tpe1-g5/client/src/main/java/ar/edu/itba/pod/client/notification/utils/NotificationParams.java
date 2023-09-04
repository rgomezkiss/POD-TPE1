package ar.edu.itba.pod.client.notification.utils;

import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.ServerAddress;

public class NotificationParams extends AbstractParams {

    private final String outPath;

    public NotificationParams(ServerAddress serverAddress, String action, Integer day, String outPath) {
        super(serverAddress, action, day);
        this.outPath = outPath;
    }

    public String getOuthPath() {
        return outPath;
    }

}
