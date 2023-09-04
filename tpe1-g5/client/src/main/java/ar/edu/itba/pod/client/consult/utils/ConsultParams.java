package ar.edu.itba.pod.client.consult.utils;

import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.ServerAddress;

import java.util.UUID;

public class ConsultParams extends AbstractParams {
    private final String ride;
    private final UUID visitorId;

    public ConsultParams(ServerAddress serverAddress, String action, Integer day, String ride, UUID visitorID) {
        super(serverAddress, action, day);
        this.ride = ride;
        this.visitorId = visitorID;
    }

    public String getRide() {
        return ride;
    }
    public UUID getVisitorId() {
        return visitorId;
    }
}
