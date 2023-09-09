package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.grpc.park_admin.AddTicketRequest;
import ar.edu.itba.pod.grpc.park_admin.TicketType;
import ar.edu.itba.pod.server.exceptions.InvalidException;
import ar.edu.itba.pod.server.utils.CommonUtils;

import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

public class ServerTicket {
    private final UUID userId;
    private final int day;
    private final TicketType ticketType;
    private int bookings;

    public ServerTicket(AddTicketRequest ticket) {
        this.userId = UUID.fromString(ticket.getUUID());
        this.day = ticket.getTicketDay();
        try {
            this.ticketType = TicketType.valueOf(ticket.getTicketType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidException("Invalid ticket type");
        }
        this.bookings = 0;
        CommonUtils.validateDay(this.day);

    }

    public boolean canBook(LocalTime timeSlot) {
        return switch (this.ticketType) {
            case UNLIMITED -> true;
            case THREE -> this.bookings < 3;
            case HALF_DAY -> timeSlot.isBefore(LocalTime.of(14, 0));
            default -> false;
        };
    }

    public int getBookings() {
        return bookings;
    }
    public void setBookings(int bookings) {
        this.bookings = bookings;
    }
    public UUID getUserId() {
        return userId;
    }
    public int getDay() {
        return day;
    }
    public void book() {
        this.bookings++;
    }
    public void cancelBook() {
        this.bookings--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ServerTicket other = (ServerTicket) o;
        return this.userId.equals(other.userId) && this.day == other.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, day);
    }
}

