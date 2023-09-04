package ar.edu.itba.pod.client.notification.utils;

import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.ServerAddress;

public class NotificationParams extends AbstractParams {
    private final String rideName;
    private final String visitorId;

    public NotificationParams(ServerAddress serverAddress, String action, Integer day, String rideName, String visitorId) {
        super(serverAddress, action, day);
        this.rideName = rideName;
        this.visitorId = visitorId;
    }

    public String getRideName() {
        return rideName;
    }

    public String getVisitorId() {
        return visitorId;
    }
}
