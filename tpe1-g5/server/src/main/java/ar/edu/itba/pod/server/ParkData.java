package ar.edu.itba.pod.server;

import ar.edu.itba.pod.server.models.DayCapacity;
import ar.edu.itba.pod.server.models.ServerAttraction;
import ar.edu.itba.pod.server.models.ServerBooking;
import ar.edu.itba.pod.server.models.ServerTicket;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParkData {
    // Attraction -> Day/Capacity -> TimeSlot -> List<Book>
    // Preguntar si un mismo usuario puede reservar varias veces mismo día, mismo horario. Eso me va a cambiar el equals/hash de ServerBook...
    // TODO: ver si la lista puede ser una cola o similar para la reorganización
    private final Map<ServerAttraction, Map<DayCapacity, Map<LocalTime, List<ServerBooking>>>> bookings = new ConcurrentHashMap<>();
    //AttractionName -> ServerAttraction
    private final Map<String, ServerAttraction> attractions = new ConcurrentHashMap<>();

    //UserId -> Day -> Ticket. TODO: que la clase Ticket tenga la cantidad que hizo en el dia para validar
    private final Map<UUID, Map<Integer, ServerTicket>> tickets = new ConcurrentHashMap<>();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public Map<String, ServerAttraction> getAttractions() {
        return attractions;
    }

    /** ParkAdminService methods **/
    public boolean addAttraction(ServerAttraction attraction) {
        // TODO: agregar validaciones para especificar errores:
        // Duplicate name.
        // InvalidTime,
        // slotSize negative,
        // slotSize not enough.
        // Recién ahí devolver
        return attractions.put(attraction.getAttractionName(), attraction) == null;
    }

    public boolean addTicket(ServerTicket ticket) {
        // TODO: agregar validaciones para especificar errores:
        //UUID no valido
        //type not valid

        //day not valid

        //already has ticket

        tickets.putIfAbsent(ticket.getUserId(), new ConcurrentHashMap<>());

        // Podría solo tener el ticket type al final tal vez
        return tickets.get(ticket.getUserId()).put(ticket.getDay(), ticket) == null;
    }


    /** BookingService methods **/
    public boolean book(ServerBooking booking) {
        ServerAttraction attraction = attractions.get(booking.getAttractionName());

        if (attraction == null) {
            return false;
        }

        if (!attraction.isTimeSlotValid(booking.getSlot())) {
            return false;
        }

        // Agrego atracción si no estuviera
        bookings.putIfAbsent(attraction, new ConcurrentHashMap<>());

        DayCapacity dayCapacityAux = new DayCapacity(booking.getDay());

        // Agrego día del año si no estuviera. Debería agregar la capacidad (tal vez en una clase Pair<K,V>, donde la K uso en hash)
        bookings.get(attraction).putIfAbsent(dayCapacityAux, new ConcurrentHashMap<>());

        // Agrego hora del año sabiendo que es válida
        bookings.get(attraction).get(dayCapacityAux).putIfAbsent(booking.getSlot(), new ArrayList<>());

        // Agrego finalmente la reserva. Incrementar la cantidad de bookings del usuario en ese día
        return bookings.get(attraction).get(dayCapacityAux).get(booking.getSlot()).add(booking);
    }

    public boolean confirmBooking(ServerBooking booking) {
        ServerAttraction attraction = attractions.get(booking.getAttractionName());

        if (attraction == null) {
            return false;
        }

        DayCapacity dayCapacityAux = new DayCapacity(booking.getDay());

        Map<DayCapacity, Map<LocalTime, List<ServerBooking>>> dayMap = bookings.get(attraction);

        if (dayMap == null) {
            return false;
        }

        Map<LocalTime, List<ServerBooking>> timeMap = bookings.get(attraction).get(dayCapacityAux);

        if (timeMap == null) {
            return false;
        }

        if (bookings.get(attraction).get(dayCapacityAux).getOrDefault(booking.getSlot(), new ArrayList<>()).contains(booking)) {
            // Confirmo la reserva
            return true;
        }

        // No existía la reserva
        return false;
    }

    public boolean cancelBooking(ServerBooking booking) {
        ServerAttraction attraction = attractions.get(booking.getAttractionName());

        if (attraction == null) {
            return false;
        }

        DayCapacity dayCapacityAux = new DayCapacity(booking.getDay());

        Map<DayCapacity, Map<LocalTime, List<ServerBooking>>> dayMap = bookings.get(attraction);

        if (dayMap == null) {
            return false;
        }

        Map<LocalTime, List<ServerBooking>> timeMap = bookings.get(attraction).get(dayCapacityAux);

        if (timeMap == null) {
            return false;
        }

        // Elimino la reserva si existía, y sino ya vuelvo
        return bookings.get(attraction).get(dayCapacityAux).getOrDefault(booking.getSlot(), new ArrayList<>()).remove(booking);
    }


    /** ConsultService methods **/
    public List<ServerAttraction> getSuggestedCapacity(int day) {
        return null;
    }

    public List<ServerBooking> getBookings(int day) {
        List<ServerBooking> bookingsByDay = new ArrayList<>();
        DayCapacity dayCapacity = new DayCapacity(day);


        for (Map.Entry<ServerAttraction, Map<DayCapacity, Map<LocalTime, List<ServerBooking>>>> attractionEntry : bookings.entrySet()) {
            Map<DayCapacity, Map<LocalTime, List<ServerBooking>>> reservationsByDay = attractionEntry.getValue();

            if (reservationsByDay.containsKey(dayCapacity)) {
                Map<LocalTime, List<ServerBooking>> reservationsByTime = reservationsByDay.get(dayCapacity);

                reservationsByTime.values().forEach(bookingsAtTime ->
                        bookingsAtTime.forEach(booking -> {
                            if (booking.isConfirmed()) {
                                bookingsByDay.add(booking);
                            }
                        })
                );
            }
        }

        // TODO: ver como y cuando conviene ordenar las reservas...
        return bookingsByDay;
    }
}
