package ar.edu.itba.pod.server;

import ar.edu.itba.pod.grpc.booking.AvailabilityResponse;
import ar.edu.itba.pod.grpc.booking.GetAvailabilityResponse;
import ar.edu.itba.pod.grpc.park_admin.AddSlotRequest;
import ar.edu.itba.pod.grpc.park_consult.SuggestedCapacity;
import ar.edu.itba.pod.server.exceptions.AttractionAlreadyExistsException;
import ar.edu.itba.pod.server.models.DayCapacity;
import ar.edu.itba.pod.server.models.ServerAttraction;
import ar.edu.itba.pod.server.models.ServerBooking;
import ar.edu.itba.pod.server.models.ServerTicket;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ParkData {
    // Attraction -> Day/Capacity -> TimeSlot -> List<Book>
    // Preguntar si un mismo usuario puede reservar varias veces mismo día, mismo horario. Eso me va a cambiar el equals/hash de ServerBook...
    // TODO: ver si la lista puede ser una cola o similar para la reorganización
    private final Map<ServerAttraction, Map<DayCapacity, Map<LocalTime, List<ServerBooking>>>> bookings = new ConcurrentHashMap<>();
    //AttractionName -> ServerAttraction
    private final Map<String, ServerAttraction> attractions = new ConcurrentHashMap<>();

    //UserId -> Day -> Ticket
    private final Map<UUID, Map<Integer, ServerTicket>> tickets = new ConcurrentHashMap<>();

    public Map<String, ServerAttraction> getAttractions() {
        return attractions;
    }

    /**
     * ParkAdminService methods
     **/
    public boolean addAttraction(ServerAttraction attraction) {
        // TODO: agregar validaciones para especificar errores:
        // Duplicate name.
        // InvalidTime,
        // slotSize negative,
        // slotSize not enough.
        // Recién ahí devolver

        if (!attractions.containsKey(attraction.getAttractionName())) {
            attractions.put(attraction.getAttractionName(), attraction);
            bookings.put(attraction, new ConcurrentHashMap<>());
        }
        else {
            throw new AttractionAlreadyExistsException();
        }
        // También agrego en bookings
        return true;
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

    public void addSlot(AddSlotRequest request) {
        ServerAttraction attraction = attractions.get(request.getAttractionName());

        if (attraction == null) {
            return;
        }

        DayCapacity dayCapacity = getDayCapacity(attraction, request.getDay());

        if (dayCapacity.getCapacity() == null) {
            // Ya agregamos la capacidad
            return;
        }

        dayCapacity.setCapacity(request.getCapacity());

        reorganizeBookings(attraction, dayCapacity);
    }

    private void reorganizeBookings(ServerAttraction attraction, DayCapacity capacity) {
        Map<DayCapacity, Map<LocalTime, List<ServerBooking>>> attractionBookings = bookings.get(attraction);

        if (attractionBookings == null) {
            return;
        }

        Map<LocalTime, List<ServerBooking>> capacityBookings = attractionBookings.get(capacity);
        if (capacityBookings == null) {
            return;
        }

        List<ServerBooking> toMove = new ArrayList<>();

        // Recorremos las reservas y los horarios disponibles
        for (Map.Entry<LocalTime, List<ServerBooking>> entry : capacityBookings.entrySet()) {
            List<ServerBooking> reservations = entry.getValue();

            // Confirmamos todas las que se puedan
            for (int i = 0; i < capacity.getCapacity(); i++){
                reservations.get(i).setConfirmed(true);
                //TODO: notificar que se confirmo o cargo cap
            }

            // Agregamos las excedentes a la lista de reservas a mover
            for (int i = capacity.getCapacity(); i < reservations.size(); i++) {
                //Cambiar por una queue para que se haga en una sola operación
                toMove.add(reservations.get(i));
                //reservations.remove(reservations.get(i));
            }
            reservations.subList(capacity.getCapacity(), reservations.size()).clear();
        }

        // Reasignamos las reservas excedentes
        for (ServerBooking booking : toMove) {
            LocalTime nextAvailableTime = getNextAvailableTime(capacityBookings, booking.getSlot(), attraction, capacity.getCapacity());

            if (nextAvailableTime == null) {
                // Iterar sobre las que sobren y eliminar y avisar...
                return;
            }

            capacityBookings.putIfAbsent(nextAvailableTime, new ArrayList<>());
            capacityBookings.get(nextAvailableTime).add(booking);
        }
    }

    private LocalTime getNextAvailableTime(Map<LocalTime, List<ServerBooking>> capacityBookings, LocalTime bookingTime, ServerAttraction attraction, Integer capacity) {
        LocalTime nextTime = bookingTime.plusMinutes(attraction.getSlotSize());
        while (capacityBookings.getOrDefault(nextTime, new ArrayList<>()).size() >= capacity && nextTime.isBefore(attraction.getClosingTime())) {
            nextTime = nextTime.plusMinutes(attraction.getSlotSize());
        }
        return nextTime.isBefore(attraction.getClosingTime()) ? nextTime : null;
    }


    /**
     * BookingService methods
     **/
    public boolean book(ServerBooking booking) {
        ServerAttraction attraction = attractions.get(booking.getAttractionName());

        if (attraction == null) {
            return false;
        }

        if (!attraction.isTimeSlotValid(booking.getSlot())) {
            return false;
        }

        ServerTicket ticket = tickets.getOrDefault(booking.getUserId(), new HashMap<>()).getOrDefault(booking.getDay(), null);

        // Si el usuario tiene un ticket asociado al día, y puede reservar en ese momento
        if (ticket != null && ticket.canBook(booking.getSlot())) {
            // Guardo el día y su capacity asociada. Si no estuviera se devuelve un nuevo dayCapacity con capacity == null
            DayCapacity dayCapacity = getDayCapacity(attraction, booking.getDay());

            // Agrego día del año si no estuviera. Se agrega con capacity == null
            bookings.get(attraction).putIfAbsent(dayCapacity, new ConcurrentHashMap<>());

            // Agrego hora del año sabiendo que es válida
            List<ServerBooking> serverBookingList = bookings.get(attraction).get(dayCapacity).putIfAbsent(booking.getSlot(), new ArrayList<>());

            // Si la capacidad ya esta cargada, entonces agrego y confirmo si hay lugar, modificando el confirmed
            if (dayCapacity.getCapacity() != null) {
                if (serverBookingList.size() < dayCapacity.getCapacity()) {
                    bookings.get(attraction).get(dayCapacity).get(booking.getSlot()).add(booking);
                    booking.setConfirmed(true);
                    ticket.book();
                }
                return false;
            }
            // Sino, agrego sin modificar el confirmed
            else {
                bookings.get(attraction).get(dayCapacity).get(booking.getSlot()).add(booking);
                ticket.book();
            }
            return true;
        }

        return false;
    }

    public AvailabilityResponse getAvailability(String attractionName, int day, LocalTime slot){
        ServerAttraction attraction = attractions.get(attractionName);

        DayCapacity dayCapacity = getDayCapacity(attraction, day);

        AtomicInteger confirmed = new AtomicInteger(0);
        AtomicInteger pending = new AtomicInteger(0);


        bookings.get(attraction).get(dayCapacity).get(slot)
                .forEach(booking -> {
                    if (booking.isConfirmed()) {
                        confirmed.getAndIncrement();
                    }
                    else {
                        pending.getAndIncrement();
                    }
                });

        return AvailabilityResponse.newBuilder()
                .setAttractionName(attractionName)
                .setConfirmed(confirmed.get())
                .setPending(pending.get())
                .setCapacity(dayCapacity.getCapacity())
                .setSlot(slot.toString())
                .build();
    }

    public boolean confirmBooking(ServerBooking booking) {
        ServerAttraction attraction = attractions.get(booking.getAttractionName());

        if (attraction == null) {
            return false;
        }

        DayCapacity dayCapacityAux = getDayCapacity(attraction, booking.getDay());

        if (dayCapacityAux.getCapacity() == null){
            return false;
        }

        Map<DayCapacity, Map<LocalTime, List<ServerBooking>>> dayMap = bookings.get(attraction);

        if (dayMap == null) {
            return false;
        }

        Map<LocalTime, List<ServerBooking>> timeMap = bookings.get(attraction).get(dayCapacityAux);

        if (timeMap == null) {
            return false;
        }

        Optional<ServerBooking> toConfirmBook = bookings.get(attraction).get(dayCapacityAux).getOrDefault(booking.getSlot(), new ArrayList<>()).stream().filter(toFind -> toFind.equals(booking)).findFirst();

        if (toConfirmBook.isPresent()) {
            // Confirmo la reserva
            toConfirmBook.get().setConfirmed(true);
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

        ServerTicket ticket = tickets.getOrDefault(booking.getUserId(), new HashMap<>()).getOrDefault(booking.getDay(), null);

        // Elimino la reserva si existía, y sino ya vuelvo
        if (bookings.get(attraction).get(dayCapacityAux).getOrDefault(booking.getSlot(), new ArrayList<>()).remove(booking)) {
            ticket.cancelBook();
            return true;
        }
        return false;
    }

    // Función que devuelve la clave dayCapacity asociada a una atracción en cierto día
    private DayCapacity getDayCapacity(ServerAttraction attraction, int day) {
        DayCapacity dayCapacityAux = new DayCapacity(day);

        return bookings.get(attraction).keySet().stream()
                .filter(dayCapacityMap -> dayCapacityMap.equals(dayCapacityAux))
                .findFirst().orElse(dayCapacityAux);
    }


    /**
     * ConsultService methods
     **/
    public List<SuggestedCapacity> getSuggestedCapacity(int day) {
        List<SuggestedCapacity> suggestedCapacities = new ArrayList<>();

        for (Map.Entry<ServerAttraction, Map<DayCapacity, Map<LocalTime, List<ServerBooking>>>> attractionEntry : bookings.entrySet()) {
            ServerAttraction attraction = attractionEntry.getKey();

            if (getDayCapacity(attraction, day).getCapacity() == null) {
                SuggestedCapacity aux = singleSuggestedCapacity(attraction, day);
                if (aux != null) {
                    suggestedCapacities.add(aux);
                }
            }

        }

        return suggestedCapacities;
    }

    private SuggestedCapacity singleSuggestedCapacity(ServerAttraction attraction, int day) {
        DayCapacity dayCapacity = new DayCapacity(day);

        return Optional.ofNullable(bookings.get(attraction))
                .map(reservationsByAttraction -> reservationsByAttraction.get(dayCapacity))
                .map(reservationsByDay -> reservationsByDay.entrySet().stream()
                        .max(Comparator.comparingInt(entry -> entry.getValue().size()))
                        .map(entry -> SuggestedCapacity.newBuilder()
                                .setAttractionName(attraction.getAttractionName())
                                .setSuggestedCapacity(entry.getValue().size())
                                .setMaxCapSlot(entry.getKey().toString())
                                .build()).orElse(null)).orElse(null);
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
