package ar.edu.itba.pod.server;

import ar.edu.itba.pod.grpc.booking.AvailabilityResponse;
import ar.edu.itba.pod.grpc.booking.GetAvailabilityResponse;
import ar.edu.itba.pod.grpc.notification.NotificationRequest;
import ar.edu.itba.pod.grpc.booking.GetAvailabilityRequest;
import ar.edu.itba.pod.grpc.park_admin.AddSlotRequest;
import ar.edu.itba.pod.grpc.park_admin.AddSlotResponse;
import ar.edu.itba.pod.grpc.park_consult.BookingResponse;
import ar.edu.itba.pod.grpc.park_consult.SuggestedCapacity;
import ar.edu.itba.pod.server.exceptions.*;
//import ar.edu.itba.pod.server.models.DayCapacity;
import ar.edu.itba.pod.server.models.ServerAttraction;
import ar.edu.itba.pod.server.models.ServerBooking;
import ar.edu.itba.pod.server.models.ServerTicket;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ParkData {
    // Attraction -> Day -> TimeSlot -> List<Book>
    private final Map<ServerAttraction, Map<Integer, Map<LocalTime, List<ServerBooking>>>> bookings = new ConcurrentHashMap<>();
    // Attraction -> Day -> Capacity
    private final Map<ServerAttraction, Map<Integer, Integer>> capacities = new ConcurrentHashMap<>();
    //AttractionName -> ServerAttraction
    private final Map<String, ServerAttraction> attractions = new ConcurrentHashMap<>();
    //UserId -> Day -> Ticket
    private final Map<UUID, Map<Integer, ServerTicket>> tickets = new ConcurrentHashMap<>();
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    private final static String ATTRACTION_ALREADY_EXISTS = "Attraction already exist";
    private final static String TICKET_ALREADY_EXISTS = "Ticket already exist";
    private final static String ATTRACTION_NOT_FOUND = "Attraction not found";
    private final static String NEGATIVE_CAPACITY = "Capacity must be a positive number";
    private final static String CAPACITY_ALREADY_ASSIGNED = "Capacity already assigned";
    private final static String INVALID_SLOT = "Invalid slot";
    private final static String INVALID_TICKET_FOR_DAY = "Invalid ticket type for that day";
    private final static String BOOKING_ALREADY_EXISTS = "Booking already exists";
    private final static String CAPACITY_FULL_EXCEPTION = "Can not book, capacity is full";
    private final static String INVALID_BOOK_TYPE = "Can not book due to ticket type";
    private final static String CAPACITY_NOT_ASSIGNED = "Capacity has not been assigned";
    private final static String BOOKING_ALREADY_CONFIRMED = "Booking has been already confirmed";
    private final static String BOOKING_NOT_FOUND = "Booking not found";

    //TODO: change to proper notifications...
    private final Map<String, StreamObserver<StringValue>> observers = new ConcurrentHashMap<>();
    public Map<String, ServerAttraction> getAttractions() {
        return attractions;
    }


    /**
     * ParkAdminService methods
     **/
    public void addAttraction(final ServerAttraction attraction) {
        // Falla:
        //  si Duplicate name --
        //  si InvalidTime --
        //  si slotSize negative --
        //  si slotSize not enough --
        if (attractions.containsKey(attraction.getAttractionName())) {
            throw new AlreadyExistsException(ATTRACTION_ALREADY_EXISTS);
        }

        attractions.put(attraction.getAttractionName(), attraction);
        bookings.put(attraction, new ConcurrentHashMap<>());
        capacities.put(attraction, new ConcurrentHashMap<>());
    }

    public void addTicket(final ServerTicket ticket) {
        //Falla:
        // si type not valid --
        // si day not valid --
        // si already has ticket --
        final UUID userId = ticket.getUserId();
        final Map<Integer, ServerTicket> userTickets = tickets.get(userId);
        if (userTickets != null && userTickets.containsKey(ticket.getDay())) {
            throw new AlreadyExistsException(TICKET_ALREADY_EXISTS);
        }

        tickets.putIfAbsent(ticket.getUserId(), new ConcurrentHashMap<>());
        // Podría solo tener el ticket type al final tal vez
        tickets.get(ticket.getUserId()).put(ticket.getDay(), ticket);
    }

    public AddSlotResponse addSlot(final AddSlotRequest request) {
        //Falla:
        // si la atracción no existe --
        // si el día es inválido --
        // si la capacidad es negativa --
        // si ya se cargó una capacidad para esa atracción y día --
        final ServerAttraction attraction = attractions.get(request.getAttractionName());

        if (attraction == null) {
            throw new NotFoundException(ATTRACTION_NOT_FOUND);
        }
        if(request.getCapacity() < 0){
            throw new InvalidException(NEGATIVE_CAPACITY);
        }

        final Integer capacity = getDayCapacity(attraction, request.getDay());

        if (capacity != null) {
            throw new AlreadyExistsException(CAPACITY_ALREADY_ASSIGNED);
        }

        return reorganizeBookings(attraction, request.getDay(), request.getCapacity());
    }

    private AddSlotResponse reorganizeBookings(final ServerAttraction attraction, final Integer day, final Integer capacity) {
        final Map<Integer, Map<LocalTime, List<ServerBooking>>> attractionBookings = bookings.get(attraction);
        final Map<LocalTime, List<ServerBooking>> capacityBookings = attractionBookings.getOrDefault(day, new ConcurrentHashMap<>());
        final List<ServerBooking> toMove = new ArrayList<>();

        int confirmed = 0;
        int relocated = 0;
        int cancelled = 0;

        // Recorremos las reservas y los horarios disponibles
        for (Map.Entry<LocalTime, List<ServerBooking>> entry : capacityBookings.entrySet()) {
            final List<ServerBooking> reservations = entry.getValue();

            // Confirmamos todas las que se puedan
            for (int i = 0; i < capacity; i++) {
                reservations.get(i).setConfirmed(true);
                confirmed++;
                //TODO: notificar que se confirmo o cargo cap
            }

            // Agregamos las excedentes a la lista de reservas a mover
            for (int i = capacity; i < reservations.size(); i++) {
                //Cambiar por una queue para que se haga en una sola operación
                toMove.add(reservations.get(i));
                //reservations.remove(reservations.get(i));
            }
            reservations.subList(capacity, reservations.size()).clear();
        }

        // Reasignamos las reservas excedentes
        for (ServerBooking booking : toMove) {
            final LocalTime nextAvailableTime = getNextAvailableTime(capacityBookings, booking.getSlot(), attraction, capacity);

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

    private LocalTime getNextAvailableTime(final Map<LocalTime, List<ServerBooking>> capacityBookings, final LocalTime bookingTime,
                                           final ServerAttraction attraction, final Integer capacity) {
        LocalTime nextTime = bookingTime.plusMinutes(attraction.getSlotSize());
        while (capacityBookings.getOrDefault(nextTime, new ArrayList<>()).size() >= capacity && nextTime.isBefore(attraction.getClosingTime())) {
            nextTime = nextTime.plusMinutes(attraction.getSlotSize());
        }
        return nextTime.isBefore(attraction.getClosingTime()) ? nextTime : null;
    }


    /**
     * BookingService methods
     **/
    public void book(final ServerBooking booking) {
        //Falla:
        // si la reserva ya existe --
        // si no se puede reservar la atracción según las restricciones del tipo de pase --
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot es inválido --
        // si no cuenta con un pase válido para ese día --
        // si se intenta reservar y ya se alcanzó la capacidad --
        final ServerAttraction attraction = attractions.get(booking.getAttractionName());

        if (attraction == null) {
            throw new NotFoundException(ATTRACTION_NOT_FOUND);
        }

        if (!attraction.isTimeSlotValid(booking.getSlot())) {
            throw new InvalidException(INVALID_SLOT);
        }

        final ServerTicket ticket = tickets
                .getOrDefault(booking.getUserId(), new ConcurrentHashMap<>())
                .getOrDefault(booking.getDay(), null);

        if (ticket == null) {
            throw new InvalidException(INVALID_TICKET_FOR_DAY);
        }

        // Si el usuario tiene un ticket asociado al día, y puede reservar en ese momento
        if (ticket.canBook(booking.getSlot())) {
            // Guardo el día y su capacity asociada. Si no estuviera se devuelve un nuevo dayCapacity con capacity == null
            final int day = booking.getDay();
            final Integer capacity = getDayCapacity(attraction, day);
            // Agrego día del año si no estuviera. Se agrega con capacity == null
            bookings.get(attraction).putIfAbsent(day, new ConcurrentHashMap<>());
            // Agrego hora del año sabiendo que es válida
            final List<ServerBooking> books = bookings.get(attraction).get(day).getOrDefault(booking.getSlot(), new ArrayList<>());

            // Si la capacidad ya esta cargada, entonces agrego y confirmo si hay lugar, modificando el confirmed
            if (capacity != null) {
                if (books.size() < capacity) {
                    if (books.contains(booking)) {
                        throw new AlreadyExistsException(BOOKING_ALREADY_EXISTS);
                    }
                    books.add(booking);
                    booking.setConfirmed(true);
                    ticket.book();
                }
                throw new UnavailableException(CAPACITY_FULL_EXCEPTION);
            }
            // Sino, agrego sin modificar el confirmed
            else {
                books.add(booking);
                ticket.book();
            }
        }

        throw new InvalidException(INVALID_BOOK_TYPE);
    }

    public List<AvailabilityResponse> getAvailability(final GetAvailabilityRequest request) {
        //Falla
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot o rango de slot es inválido --
        final LocalTime startTime = LocalTime.parse(request.getTimeRangeStart(), formatter);
        final LocalTime endTime = LocalTime.parse(request.getTimeRangeEnd(), formatter);

        if (endTime.isBefore(startTime)) {
            throw new InvalidException("Invalid time");
        }

        final List<AvailabilityResponse> availabilityResponses = new LinkedList<>();

        if (request.getAttractionName().isEmpty()) {
            availabilityResponses.addAll(getAvailability(request.getDay(), startTime, endTime));
        } else if (request.getTimeRangeEnd().isEmpty()) {
            availabilityResponses.add(getAvailability(request.getAttractionName(), request.getDay(), startTime));
        } else {
            availabilityResponses.addAll(getAvailability(request.getAttractionName(), request.getDay(), startTime, endTime));
        }

        return availabilityResponses;
    }

    private List<AvailabilityResponse> getAvailability(final int day, final LocalTime startTime, final LocalTime endTime) {
        final List<ServerAttraction> serverAttractions = new ArrayList<>(attractions.values());
        final List<AvailabilityResponse> responses = new ArrayList<>();

        for (ServerAttraction attraction : serverAttractions) {
            responses.addAll(getAvailability(attraction.getAttractionName(), day, startTime, endTime));
        }

        return responses;
    }

    private List<AvailabilityResponse> getAvailability(final String attractionName, final int day, final LocalTime startTime, final LocalTime endTime) {
        final ServerAttraction attraction = attractions.get(attractionName);
        if (attraction == null) {
            throw new NotFoundException(ATTRACTION_NOT_FOUND);
        }

        final List<LocalTime> timeSlotsInRange = attraction.getSlotsInRange(startTime, endTime);
        final List<AvailabilityResponse> responses = new ArrayList<>();

        for (LocalTime slot : timeSlotsInRange) {
            responses.add(getAvailability(attractionName, day, slot));
        }

        return responses;
    }

    private AvailabilityResponse getAvailability(final String attractionName, final int day, final LocalTime slot){
        final ServerAttraction attraction = attractions.get(attractionName);
        if (attraction == null) {
            throw new NotFoundException(ATTRACTION_NOT_FOUND);
        }

        final Integer capacity = getDayCapacity(attraction, day);
        final AtomicInteger confirmed = new AtomicInteger(0);
        final AtomicInteger pending = new AtomicInteger(0);

        bookings.get(attraction).get(day).get(slot).forEach(booking -> {
            if (booking.isConfirmed()) {
                confirmed.getAndIncrement();
            } else {
                pending.getAndIncrement();
            }
        });

        return AvailabilityResponse.newBuilder()
                .setAttractionName(attractionName)
                .setConfirmed(confirmed.get())
                .setPending(pending.get())
                .setCapacity(capacity)
                .setSlot(slot.toString())
                .build();
    }

    public void confirmBooking(final ServerBooking booking) {
        //Falla
        // si no se cargó la capacidad de los slots de la atracción para ese día --
        // si la reserva ya está confirmada --
        // si no existe una reserva realizada para la atracción con ese pase --
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot es inválido TODO
        // si no cuenta con un pase válido para ese día --

        final ServerAttraction attraction = attractions.get(booking.getAttractionName());
        if (attraction == null) {
            throw new NotFoundException(ATTRACTION_NOT_FOUND);
        }

        final Integer capacity = getDayCapacity(attraction, booking.getDay());
        if (capacity == null) {
            throw new NotFoundException(CAPACITY_NOT_ASSIGNED);
        }

        // TODO CHECK
        final ServerTicket ticket = tickets.getOrDefault(booking.getUserId(), new HashMap<>()).getOrDefault(booking.getDay(), null);
        if (ticket == null) {
            throw new InvalidException(INVALID_TICKET_FOR_DAY);
        }

        Optional<ServerBooking> toConfirmBook = bookings
                .get(attraction)
                .get(booking.getDay())
                .getOrDefault(booking.getSlot(), new ArrayList<>())
                .stream()
                .filter(toFind -> toFind.equals(booking))
                .findFirst();

        if (toConfirmBook.isPresent()) {
            if (toConfirmBook.get().isConfirmed()) {
                throw new AlreadyExistsException(BOOKING_ALREADY_CONFIRMED);
            }
            toConfirmBook.get().setConfirmed(true);
        }
        // No existía la reserva
        throw new NotFoundException(BOOKING_NOT_FOUND);
    }

    public void cancelBooking(final ServerBooking booking) {
        //Falla:
        // si no existe una reserva realizada para la atracción con ese pase --
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot es inválido TODO
        // si no cuenta con un pase válido para ese día --
        final ServerAttraction attraction = attractions.get(booking.getAttractionName());

        if (attraction == null) {
            throw new NotFoundException(ATTRACTION_NOT_FOUND);
        }

        // TODO CHECK
        final ServerTicket ticket = tickets.getOrDefault(booking.getUserId(), new HashMap<>()).getOrDefault(booking.getDay(), null);
        if (ticket == null) {
            throw new InvalidException(INVALID_TICKET_FOR_DAY);
        }

        // Elimino la reserva si existía, y sino ya vuelvo
        if (bookings.get(attraction).get(booking.getDay()).getOrDefault(booking.getSlot(), new ArrayList<>()).remove(booking)) {
            ticket.cancelBook();
        }
        // No existía la reserva
        throw new NotFoundException(BOOKING_NOT_FOUND);
    }

    // Función que devuelve la clave dayCapacity asociada a una atracción en cierto día.
    // Si no existe la devuelve con una nueva instancia de DayCapacity con el valor capacity en null
    private Integer getDayCapacity(final ServerAttraction attraction, final int day) {
//        final DayCapacity dayCapacityAux = new DayCapacity(day);
//
//        return bookings.get(attraction)
//                .keySet().stream()
//                .filter(dayCapacityMap -> dayCapacityMap.equals(dayCapacityAux))
//                .findFirst().orElse(dayCapacityAux);
        return capacities.getOrDefault(attraction, new ConcurrentHashMap<>()).getOrDefault(day, null);
    }


    /**
     * ConsultService methods
     **/
    public List<SuggestedCapacity> getSuggestedCapacity(final int day) {
        // En orden descendente por la capacidad sugerida, a partir del día del año.
        // Falla si el día es inválido --
        // Si la atracción ya cuenta con una capacidad cargada entonces no debe listarse en la consulta

        if (day < 1 || day > 365) {
            throw new InvalidException("Invalid day");
        }

        final List<SuggestedCapacity> suggestedCapacities = new ArrayList<>();

        for (ServerAttraction attraction : attractions.values()) {
            final Integer capacity = getDayCapacity(attraction, day);
            if (capacity == null) {
                final  SuggestedCapacity aux = singleSuggestedCapacity(attraction, day);
                if (aux != null) {
                    suggestedCapacities.add(aux);
                }
            }
        }

        // TODO: ordenar
        return suggestedCapacities;
    }

    private SuggestedCapacity singleSuggestedCapacity(final ServerAttraction attraction, final int day) {
        final Integer capacity = getDayCapacity(attraction, day);

        if (capacity != null) {
            return null;
        }

        //TODO chekc aca q onda con dayCapacity
        return Optional.ofNullable(bookings.get(attraction))
                .map(reservationsByAttraction -> reservationsByAttraction.get(day))
                .flatMap(reservationsByDay -> reservationsByDay
                        .entrySet().stream()
                        .max(Comparator.comparingInt(entry -> entry.getValue().size()))
                        .map(entry -> SuggestedCapacity.newBuilder()
                                .setAttractionName(attraction.getAttractionName())
                                .setSuggestedCapacity(entry.getValue().size())
                                .setMaxCapSlot(entry.getKey().toString())
                                .build()
                        )
                )
                .orElse(null);
    }

    public List<BookingResponse> getBookings(final int day) {
        // En orden de confirmación de la reserva, a partir del día del año
        // Falla si el día es inválido

        if (day < 1 || day > 365) {
            throw new InvalidException("Invalid day");
        }

        final List<BookingResponse> bookingsByDay = new ArrayList<>();

        for (Map<Integer, Map<LocalTime, List<ServerBooking>>> reservationsByDay : bookings.values()) {
            if (reservationsByDay.containsKey(day)) {
                final Map<LocalTime, List<ServerBooking>> reservationsByTime = reservationsByDay.get(day);
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

        // TODO: ordenar
        return bookingsByDay;
    }

    /**
     * NotificationService methods
     **/
    public void follow(NotificationRequest request, StreamObserver<StringValue> observer) {
        //Falla:
        // si no existe una atracción con ese nombre
        // si el día es inválido
        // si no cuenta con un pase válido para ese día
        // si ya estaba registrado para ser notificado de esa atracción ese día

        observers.put(request.getAttractionName(), observer);
    }

    public void unfollow(NotificationRequest request) {
        //Falla:
        // si no existe una atracción con ese nombre
        // si el día es inválido
        // si no cuenta con un pase válido para ese día
        // si no estaba registrado para ser notificado de esa atracción ese día

        observers.get(request.getAttractionName()).onCompleted();
        observers.remove(request.getAttractionName());
    }
}
