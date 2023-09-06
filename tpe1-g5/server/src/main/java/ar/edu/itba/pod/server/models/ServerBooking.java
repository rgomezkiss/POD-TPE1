package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.grpc.booking.BookRequest;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ServerBooking {
    // Estos 3 campos como dependen del mapa podrían no estar
    private final String attractionName;
    private final int day;
    private LocalTime slot;

    // Esto hace falta
    private final UUID userId;
    private boolean isConfirmed;

    // TODO: Horario de confirmación de la reserva... u horario de cuando se haya realizado
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public ServerBooking(String attractionName, int day, LocalTime slot, UUID userId, boolean isConfirmed) {
        this.attractionName = attractionName;
        this.day = day;
        this.slot = slot;
        this.userId = userId;
        this.isConfirmed = isConfirmed;
    }

    public ServerBooking(BookRequest bookRequest) {
        this.attractionName = bookRequest.getAttractionName();
        this.day = bookRequest.getDay();
        this.slot = LocalTime.parse(bookRequest.getTimeSlot(), formatter);
        this.userId = UUID.fromString(bookRequest.getUUID());
        this.isConfirmed = false;
    }

    public LocalTime getSlot() {
        return slot;
    }

    public void setSlot(LocalTime slot) {
        this.slot = slot;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean pending) {
        isConfirmed = pending;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public int getDay() {
        return day;
    }

    // TODO: implementar hash e equals dependiendo si puedo o no repetir reservas
}
