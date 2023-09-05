package ar.edu.itba.pod.server;

import ar.edu.itba.pod.grpc.booking.BookRequest;
import ar.edu.itba.pod.grpc.park_admin.Ticket;
import ar.edu.itba.pod.server.models.ServerAttraction;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParkData {
    // Attraction -> Day/Capacity -> TimeSlot -> List<Book>
    // Preguntar si un mismo usuario puede reservar varias veces mismo día, mismo horario. Eso me va a cambiar el equals/hash de ServerBook...
    // TODO: ver si la lista puede ser una cola o similar para la reorganización
    private final Map<ServerAttraction, Map<Integer, Map<LocalTime, List<BookRequest>>>> bookings = new ConcurrentHashMap<>();
    //AttractionName -> ServerAttraction
    private final Map<String, ServerAttraction> attractions = new ConcurrentHashMap<>();

    //UserId -> Day -> Ticket. TODO: que la clase Ticket tenga la cantidad que hizo en el dia para validar
    private final Map<String, Map<Integer, Ticket>> tickets = new ConcurrentHashMap<>();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public Map<String, ServerAttraction> getAttractions() {
        return attractions;
    }

    public boolean addAttraction(ServerAttraction attraction) {
        // TODO: agregar validaciones para especificar errores:
        // Duplicate name.
        // InvalidTime,
        // slotSize negative,
        // slotSize not enough.
        // Recién ahí devolver
        return attractions.put(attraction.getAttractionName(), attraction) == null;
    }

    public boolean addTicket(Ticket ticket) {
        // TODO: agregar validaciones para especificar errores:
        //UUID no valido
        //type not valid

        //day not valid

        //already has ticket

        tickets.putIfAbsent(ticket.getUUID(), new ConcurrentHashMap<>());

        // Podría solo tener el ticket type al final tal vez
        return tickets.get(ticket.getUUID()).put(ticket.getTicketDay(), ticket) == null;
    }

    public boolean book(BookRequest bookRequest) {
        ServerAttraction attraction = attractions.get(bookRequest.getAttractionName());

        if (attraction == null) {
            return false;
        }

        LocalTime bookSlot =  LocalTime.parse(bookRequest.getTimeSlot(), formatter);

        if (!attraction.isTimeSlotValid(bookSlot)) {
            return false;
        }

        // Agrego atracción si no estuviera
        bookings.putIfAbsent(attraction, new ConcurrentHashMap<>());

        // Agrego día del año si no estuviera. Debería agregar la capacidad (tal vez en una clase Pair<K,V>, donde la K uso en hash)
        bookings.get(attraction).putIfAbsent(bookRequest.getDay(), new ConcurrentHashMap<>());

        // Agrego hora del año sabiendo que es válida
        bookings.get(attraction).get(bookRequest.getDay()).putIfAbsent(bookSlot, new ArrayList<>());

        // Agrego finalmente la reserva
        return bookings.get(attraction).get(bookRequest.getDay()).get(bookSlot).add(bookRequest);
    }

    public boolean confirmBooking(BookRequest bookRequest) {
        ServerAttraction attraction = attractions.get(bookRequest.getAttractionName());

        if (attraction == null) {
            return false;
        }

        LocalTime bookSlot =  LocalTime.parse(bookRequest.getTimeSlot(), formatter);

        Map<Integer, Map<LocalTime, List<BookRequest>>> dayMap = bookings.get(attraction);

        if (dayMap == null) {
            return false;
        }

        Map<LocalTime, List<BookRequest>> timeMap = bookings.get(attraction).get(bookRequest.getDay());

        if (timeMap == null) {
            return false;
        }

        if (bookings.get(attraction).get(bookRequest.getDay()).getOrDefault(bookSlot, new ArrayList<>()).contains(bookRequest)) {
            // Confirmo la reserva
            return true;
        }

        // No existía la reserva
        return false;
    }

    public boolean cancelBooking(BookRequest bookRequest) {
        ServerAttraction attraction = attractions.get(bookRequest.getAttractionName());

        if (attraction == null) {
            return false;
        }

        LocalTime bookSlot =  LocalTime.parse(bookRequest.getTimeSlot(), formatter);

        Map<Integer, Map<LocalTime, List<BookRequest>>> dayMap = bookings.get(attraction);

        if (dayMap == null) {
            return false;
        }

        Map<LocalTime, List<BookRequest>> timeMap = bookings.get(attraction).get(bookRequest.getDay());

        if (timeMap == null) {
            return false;
        }

        // Elimino la reserva si existía, y sino ya vuelvo
        return bookings.get(attraction).get(bookRequest.getDay()).getOrDefault(bookSlot, new ArrayList<>()).remove(bookRequest);
    }
}
