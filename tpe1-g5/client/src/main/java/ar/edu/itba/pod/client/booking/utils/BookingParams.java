package ar.edu.itba.pod.client.booking.utils;

import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.ServerAddress;

import java.util.UUID;

public class BookingParams extends AbstractParams {

    private final String ride;
    private final String slot;
    private final String slotTo;
    private final UUID visitorId;

    public BookingParams(ServerAddress serverAddress, String action, Integer day, String ride, String slot, String slotTo) {
        super(serverAddress, action, day);
        this.ride = ride;
        this.slot = slot;
        this.slotTo = slotTo;
        this.visitorId = null;
    }

    public BookingParams(ServerAddress serverAddress, String action, Integer day, String ride, String slot, UUID visitorID) {
        super(serverAddress, action, day);
        this.ride = ride;
        this.slot = slot;
        this.slotTo = null;
        this.visitorId = visitorID;
    }

    public String getRide() {
        return ride;
    }
    public String getSlot() {
        return slot;
    }
    public String getSlotTo() {
        return slotTo;
    }
    public UUID getVisitorId() {
        return visitorId;
    }
}
