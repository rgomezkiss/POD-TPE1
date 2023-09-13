package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.grpc.booking.BookRequest;
import ar.edu.itba.pod.grpc.notification.NotificationRequest;
import ar.edu.itba.pod.server.utils.CommonUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

public class ServerBooking {
    private final String attractionName;
    private final int day;
    private LocalTime slot;
    private final UUID userId;
    private boolean isConfirmed;
    private final LocalDateTime bookingTime;
    private LocalDateTime confirmedTime;

    public ServerBooking(BookRequest bookRequest) {
        this.attractionName = bookRequest.getAttractionName();
        this.day = CommonUtils.validateDay(bookRequest.getDay());
        this.slot = CommonUtils.parseTime(bookRequest.getTimeSlot());
        this.userId = CommonUtils.validateUserId(bookRequest.getUUID());
        this.isConfirmed = false;
        this.bookingTime = LocalDateTime.now();
        this.confirmedTime = null;
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
    public LocalDateTime getBookingTime() {
        return bookingTime;
    }
    public LocalDateTime getConfirmedTime() {
        return confirmedTime;
    }
    public void setConfirmedTime(LocalDateTime confirmedTime) {
        this.confirmedTime = confirmedTime;
    }

    public boolean equalsNotificationRequest(NotificationRequest request) {
        return this.attractionName.equals(request.getAttractionName()) && this.day == request.getDay()
                && this.userId.equals(CommonUtils.validateUserId(request.getUUID()));
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

    @Override
    public String toString() {
        return "ServerBooking{" + "attractionName='" + attractionName + '\'' + ", day=" + day + ", slot=" + slot + ", userId=" + userId + ", isConfirmed=" + isConfirmed + ", bookingTime=" + bookingTime + ", confirmedTime=" + confirmedTime + '}';
    }
}
