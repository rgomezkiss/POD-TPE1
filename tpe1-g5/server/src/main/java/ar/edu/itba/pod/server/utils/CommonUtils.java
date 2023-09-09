package ar.edu.itba.pod.server.utils;

import ar.edu.itba.pod.server.exceptions.InvalidException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CommonUtils {

    private final static Integer MIN_DAY = 1;
    private final static Integer MAX_DAY = 365;
    public final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    public final static String ATTRACTION_ALREADY_EXISTS = "Attraction already exist";
    public final static String TICKET_ALREADY_EXISTS = "Ticket already exist";
    public final static String ATTRACTION_NOT_FOUND = "Attraction not found";
    public final static String NEGATIVE_CAPACITY = "Capacity must be a positive number";
    public final static String CAPACITY_ALREADY_ASSIGNED = "Capacity already assigned";
    public final static String INVALID_SLOT = "Invalid slot";
    public final static String INVALID_TICKET_FOR_DAY = "Invalid ticket type for that day";
    public final static String BOOKING_ALREADY_EXISTS = "Booking already exists";
    public final static String CAPACITY_FULL_EXCEPTION = "Can not book, capacity is full";
    public final static String INVALID_BOOK_TYPE = "Can not book due to ticket type";
    public final static String CAPACITY_NOT_ASSIGNED = "Capacity has not been assigned";
    public final static String BOOKING_ALREADY_CONFIRMED = "Booking has been already confirmed";
    public final static String BOOKING_NOT_FOUND = "Booking not found";
    public final static String INVALID_DAY = "Invalid day";
    public final static String INVALID_TIME = "Invalid time";
    public final static String ALREADY_FOLLOWING = "Already following";

    public static LocalTime parseTime(final String time){
        try {
            return LocalTime.parse(time, formatter);
        } catch (DateTimeParseException e){
            return null;
        }
    }

    public static void validateDay(final Integer day){
        if (day < MIN_DAY || day > MAX_DAY) {
            throw new InvalidException(INVALID_DAY);
        }
    }

    public static void validateTimeRange(final LocalTime startTime, final LocalTime endTime){
        if (endTime.isBefore(startTime)) {
            throw new InvalidException(INVALID_TIME);
        }
    }

}
