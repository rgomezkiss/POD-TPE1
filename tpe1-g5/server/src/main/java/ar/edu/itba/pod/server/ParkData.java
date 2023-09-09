package ar.edu.itba.pod.server;

import ar.edu.itba.pod.grpc.booking.AvailabilityResponse;
import ar.edu.itba.pod.grpc.booking.GetAvailabilityResponse;
import ar.edu.itba.pod.grpc.park_admin.AddSlotRequest;
import ar.edu.itba.pod.grpc.park_admin.AddSlotResponse;
import ar.edu.itba.pod.grpc.park_consult.BookingResponse;
import ar.edu.itba.pod.grpc.park_consult.SuggestedCapacity;
import ar.edu.itba.pod.server.exceptions.*;
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
    public void addAttraction(ServerAttraction attraction) {
        // TODO: agregar validaciones para especificar errores:
        // Duplicate name.
        // Las que siguen estan agregadas en el constructor
        // InvalidTime,
        // slotSize negative,
        // slotSize not enough.
        if (attractions.containsKey(attraction.getAttractionName())) {
            throw new AttractionAlreadyExistsException();
        }
        attractions.put(attraction.getAttractionName(), attraction);
        bookings.put(attraction, new ConcurrentHashMap<>());
    }

    public void addTicket(ServerTicket ticket) {
        // TODO: agregar validaciones para especificar errores:
        //UUID no valido, hace falta esto ??
        //type not valid
        //day not valid
        //already has ticket
        UUID userId = ticket.getUserId();
        Map<Integer, ServerTicket> userTickets = tickets.get(userId);
        if (userTickets != null && userTickets.containsKey(ticket.getDay())) {
            throw new TicketAlreadyExistsException();
        }

        tickets.putIfAbsent(ticket.getUserId(), new ConcurrentHashMap<>());
        // Podría solo tener el ticket type al final tal vez
        tickets.get(ticket.getUserId()).put(ticket.getDay(), ticket);
    }

    public AddSlotResponse addSlot(AddSlotRequest request) {
        //Falla:
        // si la atracción no existe
        // si el día es inválido
        // si la capacidad es negativa
        // si ya se cargó una capacidad para esa atracción y día.
        ServerAttraction attraction = attractions.get(request.getAttractionName());

        if (attraction == null) {
            throw new AttractionNotExistException();
        }
        if(request.getCapacity() < 0){
            throw new NegativeCapacityException();
        }

        DayCapacity dayCapacity = getDayCapacity(attraction, request.getDay());

        if (dayCapacity.getCapacity() != null) {
            throw new CapacityAlreadyAssignedException();
        }

        dayCapacity.setCapacity(request.getCapacity());

        return reorganizeBookings(attraction, dayCapacity);
    }

    private AddSlotResponse reorganizeBookings(ServerAttraction attraction, DayCapacity capacity) {
        Map<DayCapacity, Map<LocalTime, List<ServerBooking>>> attractionBookings = bookings.get(attraction);

        //TODO Esto no deberia pasar, pq ya chequeamos antes que la atraccion no sea null no?
        if (attractionBookings == null) {
            return null;
        }

        Map<LocalTime, List<ServerBooking>> capacityBookings = attractionBookings.get(capacity);
        //TODO Aca no pasa algo parecido? hace falta ver essto?
        if (capacityBookings == null) {
            return null;
        }

        List<ServerBooking> toMove = new ArrayList<>();
        Integer confirmed = 0;
        Integer relocated = 0;
        Integer cancelled = 0;

        // Recorremos las reservas y los horarios disponibles
        for (Map.Entry<LocalTime, List<ServerBooking>> entry : capacityBookings.entrySet()) {
            List<ServerBooking> reservations = entry.getValue();

            // Confirmamos todas las que se puedan
            for (int i = 0; i < capacity.getCapacity(); i++){
                reservations.get(i).setConfirmed(true);
                confirmed++;
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

            if (nextAvailableTime != null) {
                capacityBookings.putIfAbsent(nextAvailableTime, new ArrayList<>());
                capacityBookings.get(nextAvailableTime).add(booking);
                relocated++;
            }

            // Iterar sobre las que van a quedar canceladas
        }

        return AddSlotResponse.newBuilder()
                .setConfirmed(confirmed)
                .setRelocated(relocated)
                .setCancelled(cancelled)
                .build();
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
        //Falla:
        // si la reserva ya existe
        // si no se puede reservar la atracción según las restricciones del tipo de pase
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot es inválido --
        // si no cuenta con un pase válido para ese día
        ServerAttraction attraction = attractions.get(booking.getAttractionName());

        if (attraction == null) {
            throw new AttractionNotExistException();
        }

        if (!attraction.isTimeSlotValid(booking.getSlot())) {
            throw new InvalidSlotException();
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

    public List<AvailabilityResponse> getAvailability(int day, LocalTime startTime, LocalTime endTime) {
        List<ServerAttraction> serverAttractions = new ArrayList<>(attractions.values());
        List<AvailabilityResponse> responses = new ArrayList<>();

        for (ServerAttraction attraction: serverAttractions) {
            responses.addAll(getAvailability(attraction.getAttractionName(), day, startTime, endTime));
        }

        return responses;
    }

    public List<AvailabilityResponse> getAvailability(String attractionName, int day, LocalTime startTime, LocalTime endTime){
        ServerAttraction attraction = attractions.get(attractionName);
        if (attraction == null) {
            throw new AttractionNotExistException();
        }

        List<LocalTime> timeSlotsInRange = attraction.getSlotsInRange(startTime, endTime);
        List<AvailabilityResponse> responses = new ArrayList<>();

        for (LocalTime slot : timeSlotsInRange) {
            responses.add(getAvailability(attractionName, day, slot));
        }

        return responses;
    }

    public AvailabilityResponse getAvailability(String attractionName, int day, LocalTime slot){
        ServerAttraction attraction = attractions.get(attractionName);
        if (attraction == null) {
            throw new AttractionNotExistException();
        }

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
        //Falla
        // si no se cargó la capacidad de los slots de la atracción para ese día --
        // si la reserva ya está confirmada
        // si no existe una reserva realizada para la atracción con ese pase
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot es inválido
        // si no cuenta con un pase válido para ese día.

        ServerAttraction attraction = attractions.get(booking.getAttractionName());

        if (attraction == null) {
            throw new AttractionNotExistException();
        }

        DayCapacity dayCapacityAux = getDayCapacity(attraction, booking.getDay());

        if (dayCapacityAux.getCapacity() == null){
            throw new CapacityNotAssignedException();
        }

        Map<DayCapacity, Map<LocalTime, List<ServerBooking>>> dayMap = bookings.get(attraction);

        //TODO puede pasar esto ?
        if (dayMap == null) {
            return false;
        }

        Map<LocalTime, List<ServerBooking>> timeMap = bookings.get(attraction).get(dayCapacityAux);

        //TODO puede pasar esto ?
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
        //Falla:
        // si no existe una reserva realizada para la atracción con ese pase
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot es inválido
        // si no cuenta con un pase válido para ese día.
        ServerAttraction attraction = attractions.get(booking.getAttractionName());

        if (attraction == null) {
            throw new AttractionNotExistException();
        }

        DayCapacity dayCapacityAux = new DayCapacity(booking.getDay());

        Map<DayCapacity, Map<LocalTime, List<ServerBooking>>> dayMap = bookings.get(attraction);

        //TODO puede pasar esto ?
        if (dayMap == null) {
            return false;
        }

        Map<LocalTime, List<ServerBooking>> timeMap = bookings.get(attraction).get(dayCapacityAux);

        //TODO puede pasar esto ?
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

    // Función que devuelve la clave dayCapacity asociada a una atracción en cierto día.
    // Si no existe la devuelve con una nueva instancia de DayCapacity con el valor capacity en null
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

        for (ServerAttraction attraction : attractions.values()) {
            if (getDayCapacity(attraction, day).getCapacity() == null) {
                SuggestedCapacity aux = singleSuggestedCapacity(attraction, day);
                if (aux != null) {
                    suggestedCapacities.add(aux);
                }
            }
        }

        // TODO: ordenar en front según criterio que diga
        return suggestedCapacities;
    }

    private SuggestedCapacity singleSuggestedCapacity(ServerAttraction attraction, int day) {
        DayCapacity dayCapacity = getDayCapacity(attraction, day);

        if (dayCapacity != null) {
            return null;
        }

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

    public List<BookingResponse> getBookings(int day) {
        List<BookingResponse> bookingsByDay = new ArrayList<>();
        DayCapacity dayCapacity = new DayCapacity(day);

        for (Map<DayCapacity, Map<LocalTime, List<ServerBooking>>> reservationsByDay : bookings.values()) {
            if (reservationsByDay.containsKey(dayCapacity)) {
                Map<LocalTime, List<ServerBooking>> reservationsByTime = reservationsByDay.get(dayCapacity);

                reservationsByTime.values().forEach(bookingsAtTime ->
                        bookingsAtTime.forEach(booking -> {
                            if (booking.isConfirmed()) {
                                bookingsByDay.add(
                                    BookingResponse.newBuilder()
                                        .setAttractionName(booking.getAttractionName())
                                        .setUUID(booking.getUserId().toString())
                                        .setTimeSlot(booking.getSlot().toString())
                                        .build()
                                );
                            }
                        })
                );
            }
        }

        // TODO: ver como y cuando conviene ordenar las reservas...
        return bookingsByDay;
    }
}
