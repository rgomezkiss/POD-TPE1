package ar.edu.itba.pod.client.booking.utils;

import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.ServerAddress;

public class BookingParams extends AbstractParams {
    private final String ride;
    private final String slot;
    private final String slotTo;
    private final String visitorId;

    public BookingParams(ServerAddress serverAddress, String action) {
        super(serverAddress, action, null);
        this.ride = null;
        this.slot = null;
        this.slotTo = null;
        this.visitorId = null;
    }

    public BookingParams(ServerAddress serverAddress, String action, Integer day, String ride, String slot, String slotTo, String visitorId) {
        super(serverAddress, action, day);
        this.ride = ride;
        this.slot = slot;
        this.slotTo = slotTo;
        this.visitorId = visitorId;
    }

    public BookingParams(ServerAddress serverAddress, String action, Integer day, String ride, String slot, String visitorID) {
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
    public String getVisitorId() {
        return visitorId;
    }
}
