package ar.edu.itba.pod.server.models;

import java.time.LocalTime;
import java.util.UUID;

public class ServerBooking {
    // Estos 3 campos como dependen del mapa podrían no estar
    private final String attractionName;
    private final int day;
    private LocalTime slot;

    // Esto hace falta
    private final UUID userId;
    private boolean isPending;

    public ServerBooking(String attractionName, int day, LocalTime slot, UUID userId, boolean isPending) {
        this.attractionName = attractionName;
        this.day = day;
        this.slot = slot;
        this.userId = userId;
        this.isPending = isPending;
    }

    public LocalTime getSlot() {
        return slot;
    }

    public void setSlot(LocalTime slot) {
        this.slot = slot;
    }

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    // TODO: implementar hash e equals dependiendo si puedo o no repetir reservas
}
