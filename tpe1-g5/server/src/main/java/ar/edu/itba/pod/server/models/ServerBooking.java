package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.grpc.booking.BookRequest;
import ar.edu.itba.pod.server.utils.CommonUtils;

import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

public class ServerBooking {
    private final String attractionName;
    private final int day;
    private LocalTime slot;
    private final UUID userId;
    private boolean isConfirmed;
    private final LocalTime bookingTime;

    public ServerBooking(BookRequest bookRequest) {
        this.attractionName = bookRequest.getAttractionName();
        this.day = bookRequest.getDay();
        this.slot = LocalTime.parse(bookRequest.getTimeSlot(), CommonUtils.formatter);
        this.userId = UUID.fromString(bookRequest.getUUID());
        this.isConfirmed = false;
        this.bookingTime = LocalTime.now();
        CommonUtils.validateDay(this.day);
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
    public UUID getUserId() {
        return userId;
    }
    public LocalTime getBookingTime() {
        return bookingTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ServerBooking other = (ServerBooking) o;
        return this.attractionName.equals(other.attractionName) && this.day == other.day && this.slot.equals(other.slot) && this.userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attractionName, day, slot, userId);
    }
}
