package ar.edu.itba.pod.server;

import ar.edu.itba.pod.grpc.booking.AttractionResponse;
import ar.edu.itba.pod.grpc.booking.AvailabilityResponse;
import ar.edu.itba.pod.grpc.notification.NotificationRequest;
import ar.edu.itba.pod.grpc.booking.GetAvailabilityRequest;
import ar.edu.itba.pod.grpc.park_admin.AddSlotRequest;
import ar.edu.itba.pod.grpc.park_admin.AddSlotResponse;
import ar.edu.itba.pod.grpc.park_consult.BookingResponse;
import ar.edu.itba.pod.grpc.park_consult.SuggestedCapacity;
import ar.edu.itba.pod.server.exceptions.*;
import ar.edu.itba.pod.server.models.ServerAttraction;
import ar.edu.itba.pod.server.models.ServerBooking;
import ar.edu.itba.pod.server.models.ServerTicket;
import ar.edu.itba.pod.server.utils.CommonUtils;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ParkData {

    private final static Logger logger = LoggerFactory.getLogger(ParkData.class);
    // Attraction -> Day -> TimeSlot -> List<Book>
    private final Map<ServerAttraction, Map<Integer, Map<LocalTime, List<ServerBooking>>>> bookings = new ConcurrentHashMap<>();
    // Attraction -> Day -> Capacity
    private final Map<ServerAttraction, Map<Integer, Integer>> capacities = new ConcurrentHashMap<>();
    // AttractionName -> ServerAttraction
    private final Map<String, ServerAttraction> attractions = new ConcurrentHashMap<>();
    // UserId -> Day -> Ticket
    private final Map<UUID, Map<Integer, ServerTicket>> tickets = new ConcurrentHashMap<>();
    // Attraction -> Dia -> Semaphore
    private final Map<ServerAttraction, Map<Integer, Semaphore>> semaphores = new ConcurrentHashMap<>();
    // Booking -> Observer
    private final Map<ServerBooking, StreamObserver<StringValue>> observers = new ConcurrentHashMap<>();

    /**
     * ParkAdminService methods
     **/
    public void addAttraction(final ServerAttraction attraction) {
        // Falla:
        //  si Duplicate name --
        //  si InvalidTime --
        //  si slotSize negative --
        //  si slotSize not enough --
        logger.debug("addAttraction() - Started!");

        if (attractions.containsKey(attraction.getAttractionName())) {
            throw new AlreadyExistsException(CommonUtils.ATTRACTION_ALREADY_EXISTS);
        }

        synchronized (attraction) {
            logger.debug("Added: " + attraction);
            attractions.put(attraction.getAttractionName(), attraction);
            bookings.put(attraction, new ConcurrentHashMap<>());
            capacities.put(attraction, new ConcurrentHashMap<>());
            semaphores.put(attraction, new ConcurrentHashMap<>());
        }

        logger.debug("addAttraction() - Done!");
    }

    public void addTicket(final ServerTicket ticket) {
        // Falla:
        // si type not valid --
        // si day not valid --
        // si already has ticket --
        logger.debug("addTicket() - Started!");

        final UUID userId = ticket.getUserId();
        final Map<Integer, ServerTicket> userTickets = tickets.get(userId);
        if (userTickets != null && userTickets.containsKey(ticket.getDay())) {
            throw new AlreadyExistsException(CommonUtils.TICKET_ALREADY_EXISTS);
        }

        synchronized (ticket) {
            logger.debug("Added: " + ticket);
            tickets.putIfAbsent(ticket.getUserId(), new ConcurrentHashMap<>());
            tickets.get(ticket.getUserId()).put(ticket.getDay(), ticket);
        }

        logger.debug("addTicket() - Done!");
    }

    public AddSlotResponse addSlot(final AddSlotRequest request) {
        // Falla:
        // si la atracción no existe --
        // si el día es inválido --
        // si la capacidad es negativa --
        // si ya se cargó una capacidad para esa atracción y día --
        logger.debug("addSlot() - Started!");

        final ServerAttraction attraction = validateAttractionExists(request.getAttractionName());

        if(request.getCapacity() <= 0){
            throw new InvalidException(CommonUtils.NEGATIVE_CAPACITY);
        }

        final Integer capacity = getDayCapacity(attraction, request.getDay());

        if (capacity != null) {
            throw new AlreadyExistsException(CommonUtils.CAPACITY_ALREADY_ASSIGNED);
        }

        capacities.get(attraction).put(request.getDay(), request.getCapacity());

        final Semaphore semaphore = getOrAddSemaphore(attraction, request.getDay());
        AddSlotResponse response = AddSlotResponse.newBuilder().build();

        try {
            semaphore.acquire();
            response = reorganizeBookings(attraction, request.getDay(), request.getCapacity());
        } catch (InterruptedException e) {
            logger.error("addSlot() - Interrupted semaphore");
            Thread.currentThread().interrupt();
        } finally {
            logger.debug("addSlot() - Semaphore released");
            semaphore.release();
        }

        logger.debug("addSlot() - Done!");
        return response;
    }

    //Para cada slot
    //del día (en orden cronológico) y en función de la capacidad indicada se deberán
    //primero confirmar todas las reservas posibles teniendo en cuenta el orden de la
    //realización de la reserva (priorizando a las primeras N reservas realizadas siendo N
    //la capacidad del slot). Luego, para cada slot del día (en orden cronológico), y de
    //existir todavía reservas pendientes, se deberán intentar reubicar esas reservas en
    //otros slots “cercanos” del mismo día, siempre considerando la capacidad de cada slot.
    private AddSlotResponse reorganizeBookings(final ServerAttraction attraction, final Integer day, final Integer capacity) {
        final Map<Integer, Map<LocalTime, List<ServerBooking>>> attractionBookings = bookings.get(attraction);
        final Map<LocalTime, List<ServerBooking>> capacityBookings = attractionBookings.getOrDefault(day, new HashMap<>());
        final List<ServerBooking> toMove = new ArrayList<>();

        int confirmed = 0;
        int relocated = 0;
        int cancelled = 0;

        // Recorremos las reservas y los horarios disponibles
        for (Map.Entry<LocalTime, List<ServerBooking>> entry : capacityBookings.entrySet()) {
            final List<ServerBooking> reservations = entry.getValue();
            ServerBooking currentBooking;

            // Confirmamos todas las que se puedan
            if (!reservations.isEmpty()) {
                for (int i = 0; i < capacity && i < reservations.size(); i++) {
                    currentBooking = reservations.get(i);
                    currentBooking.setConfirmed(true);
                    currentBooking.setConfirmedTime(LocalDateTime.now());

                    // Notificamos que se cargo la capacidad Y que se confirmo la reserva
                    notifyIfCapacityIsAnnounced(attraction, day, capacity, currentBooking, CommonUtils.CONFIRMED);
                    confirmed++;
                }
            }

            if (reservations.size() > capacity) {
                // Agregamos las excedentes a la lista de reservas a mover
                for (int i = capacity; i < reservations.size(); i++) {
                    currentBooking = reservations.get(i);
                    toMove.add(currentBooking);
                }
                reservations.subList(capacity, reservations.size()).clear();
            }

        }

        for (ServerBooking booking : toMove) {
            final LocalTime nextAvailableTime = getNextAvailableTime(capacityBookings, booking.getSlot(), attraction, capacity);

            if (nextAvailableTime != null) {
                notifyIfCapacityIsAnnounced(attraction, day, capacity, booking, nextAvailableTime);
                booking.setSlot(nextAvailableTime);

                // Caso límite 1
                // Al relocar la reserva, nos excedemos de la validez de HALF_DAY
                // Caso límite 2
                // Al relocar la reserva, caemos a un horario donde ya se tiene una reserva.
                // Aunque haya más horarios posteriores, se cancela para dar prioridad a otros cambios
                if (!validateTicketExists(booking.getUserId(), day).canRelocate(nextAvailableTime) || capacityBookings.getOrDefault(nextAvailableTime, new ArrayList<>()).contains(booking)) {
                    notifyBookStatus(booking, CommonUtils.CANCELLED);
                    cancelled++;
                }
                // Se puede relocar correctamente
                else {
                    capacityBookings.putIfAbsent(nextAvailableTime, new LinkedList<>());
                    capacityBookings.get(nextAvailableTime).add(booking);
                    relocated++;
                }

            } else {
                notifyIfCapacityIsAnnounced(attraction, day, capacity, booking, CommonUtils.CANCELLED);
                tickets.get(booking.getUserId()).get(booking.getDay()).cancelBook();
                cancelled++;
            }
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
        // Falla:
        // si la reserva ya existe --
        // si no se puede reservar la atracción según las restricciones del tipo de pase --
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot es inválido --
        // si no cuenta con un pase válido para ese día --
        // si se intenta reservar y ya se alcanzó la capacidad --
        logger.debug("book() - Started!");

        final ServerAttraction attraction = validateAttractionExists(booking.getAttractionName());
        final ServerTicket ticket = validateTicketExists(booking.getUserId(), booking.getDay());
        validateTimeSlot(attraction, booking.getSlot());

        if (ticket.canBook(booking.getSlot())) {
            final Semaphore semaphore = getOrAddSemaphore(attraction, booking.getDay());

            try {
                semaphore.acquire();
                final int day = booking.getDay();
                final Integer capacity = getDayCapacity(attraction, day);
                bookings.get(attraction).putIfAbsent(day, new ConcurrentHashMap<>());
                bookings.get(attraction).get(day).putIfAbsent(booking.getSlot(), new LinkedList<>());

                final List<ServerBooking> books = bookings.get(attraction).get(day).get(booking.getSlot());

                if (books.contains(booking)) {
                    semaphore.release();
                    throw new AlreadyExistsException(CommonUtils.BOOKING_ALREADY_EXISTS);
                }

                if (capacity != null) {
                    if (books.size() < capacity) {
                        books.add(booking);
                        booking.setConfirmed(true);
                        booking.setConfirmedTime(LocalDateTime.now());
                        ticket.book();
                    }
                    semaphore.release();
                    throw new UnavailableException(CommonUtils.CAPACITY_FULL_EXCEPTION);
                } else {
                    books.add(booking);
                    ticket.book();
                }

            } catch (InterruptedException e) {
                logger.error("book() - Interrupted semaphore");
                Thread.currentThread().interrupt();
            } finally {
                logger.debug("book() - Semaphore released");
                semaphore.release();
            }
        } else {
            throw new InvalidException(CommonUtils.INVALID_BOOK_TYPE);
        }

        logger.debug("book() - Done!");
    }

    public List<AttractionResponse> getAttractions() {
        return attractions.values()
                .stream()
                .map(attraction -> AttractionResponse.newBuilder()
                        .setAttractionName(attraction.getAttractionName())
                        .setOpeningTime(attraction.getOpeningTime().toString())
                        .setClosingTime(attraction.getClosingTime().toString())
                        .build()
                )
                .sorted(Comparator.comparing(AttractionResponse::getAttractionName))
                .collect(Collectors.toList());
    }

    public List<AvailabilityResponse> getAvailability(final GetAvailabilityRequest request) {
        // Falla
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot es inálido --
        // si el rango de slot es inválido --
        // El orden de impresión es cronológico y desempata alfabéticamente por el nombre de la atracción --
        logger.debug("getAvailability() - Started!");

        final LocalTime startTime = CommonUtils.parseTime(request.getTimeRangeStart());

        if (startTime == null){
            throw new InvalidException(CommonUtils.INVALID_TIME);
        }

        final LocalTime endTime = CommonUtils.parseTime(request.getTimeRangeEnd());
        final List<AvailabilityResponse> availabilityResponses = new ArrayList<>();

        if (endTime == null){
            availabilityResponses.add(getAvailability(request.getAttractionName(), request.getDay(), startTime));
        } else if (request.getAttractionName().isEmpty()) {
            availabilityResponses.addAll(getAvailability(request.getDay(), startTime, endTime));
        } else {
            availabilityResponses.addAll(getAvailability(request.getAttractionName(), request.getDay(), startTime, endTime));
        }

        availabilityResponses.sort((response1, response2) -> {
            final int slotComparison = response1.getSlot().compareTo(response2.getSlot());
            if (slotComparison == 0) {
                return response1.getAttractionName().compareTo(response2.getAttractionName());
            }
            return slotComparison;
        });

        logger.debug("getAvailability() - Done!");

        return availabilityResponses;
    }

    // GetAvailability de todas las atracciones en un cierto rango
    private List<AvailabilityResponse> getAvailability(final int day, final LocalTime startTime, final LocalTime endTime) {
        final List<ServerAttraction> serverAttractions = new ArrayList<>(attractions.values());
        final List<AvailabilityResponse> responses = new ArrayList<>();

        for (ServerAttraction attraction : serverAttractions) {
            responses.addAll(getAvailability(attraction.getAttractionName(), day, startTime, endTime));
        }

        return responses;
    }

    private List<AvailabilityResponse> getAvailability(final String attractionName, final int day, final LocalTime startTime, final LocalTime endTime) {
        final ServerAttraction attraction = validateAttractionExists(attractionName);
        LocalTime currentSlot = attraction.getSlotsInRange(startTime);
        final List<AvailabilityResponse> responses = new ArrayList<>();

        while (!currentSlot.isAfter(endTime)) {
            responses.add(getAvailability(attractionName, day, currentSlot));
            currentSlot = currentSlot.plusMinutes(attraction.getSlotSize());
        }

        return responses;
    }

    private AvailabilityResponse getAvailability(final String attractionName, final int day, final LocalTime slot){
        final ServerAttraction attraction = validateAttractionExists(attractionName);
        final Integer capacity = getDayCapacity(attraction, day);
        final AtomicInteger confirmed = new AtomicInteger(0);
        final AtomicInteger pending = new AtomicInteger(0);

        bookings.get(attraction).getOrDefault(day, new HashMap<>()).getOrDefault(slot, new ArrayList<>()).forEach(booking -> {
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
                .setCapacity(capacity == null ? 0 : capacity)
                .setSlot(slot.toString())
                .build();
    }

    public void confirmBooking(final ServerBooking booking) {
        // Falla
        // si no se cargó la capacidad de los slots de la atracción para ese día --
        // si la reserva ya está confirmada --
        // si no existe una reserva realizada para la atracción con ese pase --
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot es inválido --
        // si no cuenta con un pase válido para ese día --
        logger.debug("confirmBooking() - Started!");

        final ServerAttraction attraction = validateAttractionExists(booking.getAttractionName());
        validateTimeSlot(attraction,booking.getSlot());
        validateTicketExists(booking.getUserId(), booking.getDay());

        final Integer capacity = getDayCapacity(attraction, booking.getDay());
        if (capacity == null) {
            throw new NotFoundException(CommonUtils.CAPACITY_NOT_ASSIGNED);
        }

        final Semaphore semaphore = getOrAddSemaphore(attraction, booking.getDay());

        try {
            semaphore.acquire();
            final ServerBooking toConfirmBook = bookings
                    .get(attraction)
                    .get(booking.getDay())
                    .getOrDefault(booking.getSlot(), new ArrayList<>())
                    .stream()
                    .filter(toFind -> toFind.equals(booking))
                    .findFirst().orElseThrow(() -> new NotFoundException(CommonUtils.BOOKING_NOT_FOUND));

            if (toConfirmBook.isConfirmed()) {
                semaphore.release();
                throw new AlreadyExistsException(CommonUtils.BOOKING_ALREADY_CONFIRMED);
            }
            toConfirmBook.setConfirmed(true);
            toConfirmBook.setConfirmedTime(LocalDateTime.now());
            notifyBookStatus(toConfirmBook, CommonUtils.CONFIRMED);
        } catch (InterruptedException e) {
            logger.error("confirmBooking() - Semaphore interrupted!");
            Thread.currentThread().interrupt();
        } finally {
            logger.debug("confirmBooking() - Semaphore released");
            semaphore.release();
        }

        logger.debug("confirmBooking() - Done!");
    }

    public void cancelBooking(final ServerBooking booking) {
        // Falla:
        // si no existe una reserva realizada para la atracción con ese pase --
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si el slot es inválido --
        // si no cuenta con un pase válido para ese día --
        logger.debug("cancelBooking() - Started!");

        final ServerAttraction attraction = validateAttractionExists(booking.getAttractionName());
        validateTimeSlot(attraction,booking.getSlot());
        final ServerTicket ticket = validateTicketExists(booking.getUserId(), booking.getDay());

        final Semaphore semaphore = getOrAddSemaphore(attraction, booking.getDay());

        try {
            semaphore.acquire();
            if (bookings.get(attraction).getOrDefault(booking.getDay(), new HashMap<>()).getOrDefault(booking.getSlot(), new ArrayList<>()).remove(booking)) {
                notifyBookStatus(booking, CommonUtils.CANCELLED);
                ticket.cancelBook();
            }
            else {
                semaphore.release();
                throw new NotFoundException(CommonUtils.BOOKING_NOT_FOUND);
            }

        } catch (InterruptedException e) {
            logger.error("cancelBooking() - Semaphore interrupted");
            Thread.currentThread().interrupt();
        } finally {
            logger.debug("cancelBooking() - Semaphore released");
            semaphore.release();
        }

        logger.debug("cancelBooking() - Done!");
    }

    private Integer getDayCapacity(final ServerAttraction attraction, final int day) {
        return capacities.getOrDefault(attraction, new ConcurrentHashMap<>()).getOrDefault(day, null);
    }

    /**
     * ConsultService methods
     **/
    public List<SuggestedCapacity> getSuggestedCapacity(final int day) {
        // En orden descendente por la capacidad sugerida, a partir del día del año.
        // Falla si el día es inválido --
        // Si la atracción ya cuenta con una capacidad cargada entonces no debe listarse en la consulta
        logger.debug("SuggestedCapacity() - Started!");

        CommonUtils.validateDay(day);
        final List<SuggestedCapacity> suggestedCapacities = new ArrayList<>();

        for (ServerAttraction attraction : attractions.values()) {
            final Integer capacity = getDayCapacity(attraction, day);
            if (capacity == null) {
                final SuggestedCapacity aux = singleSuggestedCapacity(attraction, day);
                if (aux != null) {
                    suggestedCapacities.add(aux);
                }
            }
        }

        suggestedCapacities.sort((capacity1, capacity2) ->
                Integer.compare(capacity2.getSuggestedCapacity(), capacity1.getSuggestedCapacity())
        );

        logger.debug("SuggestedCapacity() - Done!");

        return suggestedCapacities;
    }

    private SuggestedCapacity singleSuggestedCapacity(final ServerAttraction attraction, final int day) {
        final Integer capacity = getDayCapacity(attraction, day);

        if (capacity != null) {
            return null;
        }

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
        logger.debug("getBookings() - Started!");

        CommonUtils.validateDay(day);
        final List<BookingResponse> bookingsByDay = new ArrayList<>();

        for (Map<Integer, Map<LocalTime, List<ServerBooking>>> reservationsByDay : bookings.values()) {
            if (reservationsByDay.containsKey(day)) {
                final Map<LocalTime, List<ServerBooking>> reservationsByTime = reservationsByDay.get(day);

                // Agregamos todas las reservas del día a una lista temporal para despues ordenarlas
                final List<ServerBooking> allBookingsAtDay = new ArrayList<>();

                reservationsByTime.values().forEach(list -> list.forEach(booking -> {
                    if (booking.isConfirmed()) {
                        allBookingsAtDay.add(booking);
                    }
                }));
                allBookingsAtDay.sort(Comparator.comparing(ServerBooking::getConfirmedTime));

                allBookingsAtDay.forEach(booking -> {
                    if (booking.isConfirmed()) {
                        bookingsByDay.add(BookingResponse.newBuilder()
                                .setAttractionName(booking.getAttractionName())
                                .setUUID(booking.getUserId().toString())
                                .setTimeSlot(booking.getSlot().toString())
                                .build()
                        );
                    }
                });
            }
        }

        logger.debug("getBookings() - Done!");

        return bookingsByDay;
    }

    /**
     * NotificationService methods
     **/
    public void follow(final NotificationRequest request, final StreamObserver<StringValue> observer) {
        // Falla:
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si no cuenta con un pase válido para ese día --
        // si ya estaba registrado para ser notificado de esa atracción ese día --
        // Adicional de diseño: Falla si no tiene ninguna reserva para esa atracción ese día
        logger.debug("follow() - Started!");

        CommonUtils.validateDay(request.getDay());
        final ServerAttraction attraction = validateAttractionExists(request.getAttractionName());
        final UUID userId = CommonUtils.validateUserId(request.getUUID());

        validateTicketExists(userId, request.getDay());

        if (isFollowing(request) != null) {
            throw new AlreadyExistsException(CommonUtils.ALREADY_FOLLOWING);
        }

        final ServerBooking booking = getMostRecentBooking(attraction, request.getDay(), userId);

        if (booking == null) {
            throw new NotFoundException(CommonUtils.BOOKING_NOT_FOUND);
        }

        if (booking.isConfirmed()) {
            observer.onNext(StringValue.newBuilder().setValue(
                    String.format(CommonUtils.BOOK_STATUS, booking.getAttractionName(),
                            booking.getSlot().toString(),booking.getDay(), CommonUtils.CONFIRMED)
            ).build());
            observer.onCompleted();
            return;
        }

        observer.onNext(StringValue.newBuilder().setValue(
                String.format(CommonUtils.BOOK_STATUS, booking.getAttractionName(),
                        booking.getSlot().toString(),booking.getDay(), CommonUtils.PENDING)
        ).build());
        observers.put(booking, observer);

        logger.debug("follow() - Done!");
    }

    public void unfollow(final NotificationRequest request) {
        // Falla:
        // si no existe una atracción con ese nombre --
        // si el día es inválido --
        // si no cuenta con un pase válido para ese día --
        // si no estaba registrado para ser notificado de esa atracción ese día --
        logger.debug("unfollow() - Started!");

        CommonUtils.validateDay(request.getDay());
        validateAttractionExists(request.getAttractionName());
        validateTicketExists(CommonUtils.validateUserId(request.getUUID()), request.getDay());

        final ServerBooking booking = isFollowing(request);

        if (booking == null) {
            throw new AlreadyExistsException(CommonUtils.ALREADY_UNFOLLOWING);
        }

        observers.get(booking).onCompleted();
        observers.remove(booking);
        logger.debug("unfollow() - Done!");
    }

    private ServerBooking getMostRecentBooking(final ServerAttraction attraction, final int day, final UUID userId) {
        if (bookings.containsKey(attraction) && bookings.get(attraction).containsKey(day)) {
            final Map<LocalTime, List<ServerBooking>> dayBookings = bookings.get(attraction).get(day);
            if (dayBookings != null) {
                final List<ServerBooking> userBookings = dayBookings.values().stream()
                        .flatMap(Collection::stream)
                        .filter(booking -> booking.getUserId().equals(userId))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

                if (!userBookings.isEmpty()) {
                    return Collections.max(userBookings, Comparator.comparing(ServerBooking::getBookingTime));
                }
            }
        }
        return null;
    }

    private ServerBooking isFollowing(NotificationRequest request) {
        for (ServerBooking booking: observers.keySet()) {
            if (booking.equalsNotificationRequest(request)) {
                return booking;
            }
        }
        return null;
    }

    // Notificar en caso de reserva cancelada o confirmada al anunciar capacidad
    // Se hace onCompleted y remove del observer
    private void notifyIfCapacityIsAnnounced(final ServerAttraction attraction, final Integer day, final Integer capacity,
                                             final ServerBooking booking, final String message
    ) {
        final StreamObserver<StringValue> currentObserver = observers.get(booking);

        if (currentObserver != null) {
            currentObserver.onNext(StringValue.newBuilder()
                    .setValue(String.format(CommonUtils.CAPACITY_ANNOUNCED, attraction.getAttractionName(), day, capacity))
                    .build());
            currentObserver.onNext(StringValue.newBuilder()
                    .setValue(String.format(CommonUtils.BOOK_STATUS, booking.getAttractionName(), booking.getSlot().toString(),
                            booking.getDay(), message))
                    .build());
            currentObserver.onCompleted();
            observers.remove(booking);
        }
    }

    // Notificar en caso de reserva movida
    private void notifyIfCapacityIsAnnounced(final ServerAttraction attraction, final Integer day, final Integer capacity,
                                             final ServerBooking booking, final LocalTime newSlot) {
        final StreamObserver<StringValue> currentObserver = observers.get(booking);

        if (currentObserver != null) {
            currentObserver.onNext(StringValue.newBuilder()
                    .setValue(String.format(CommonUtils.CAPACITY_ANNOUNCED, attraction.getAttractionName(), day, capacity))
                    .build());
            currentObserver.onNext(StringValue.newBuilder()
                    .setValue(String.format(CommonUtils.MOVED_BOOK, booking.getAttractionName(), booking.getSlot().toString(),
                            booking.getDay(), newSlot.toString()))
                    .build());
        }
    }

    private void notifyBookStatus(final ServerBooking booking, final String message) {
        final StreamObserver<StringValue> observer = observers.get(booking);

        // Notificamos que se cargo la capacidad Y que se confirmo la reserva
        if (observer != null) {
            observer.onNext(StringValue.newBuilder().setValue(
                    String.format(CommonUtils.BOOK_STATUS, booking.getAttractionName(), booking.getSlot().toString(), booking.getDay(), message)
            ).build());

            if (booking.isConfirmed()) {
                observer.onCompleted();
                observers.remove(booking);
            }
        }
    }


    /**
     * Validators
     * **/
    private ServerAttraction validateAttractionExists(final String attractionName) {
        final ServerAttraction attraction = attractions.get(attractionName);
        if (attraction == null) {
            throw new NotFoundException(CommonUtils.ATTRACTION_NOT_FOUND);
        }
        return attraction;
    }

    private ServerTicket validateTicketExists(final UUID userId, final Integer day) {
        final ServerTicket ticket = tickets.getOrDefault(userId, new HashMap<>()).getOrDefault(day, null);
        if (ticket == null) {
            throw new InvalidException(CommonUtils.INVALID_TICKET_FOR_DAY);
        }
        return ticket;
    }

    private void validateTimeSlot(final ServerAttraction attraction, final LocalTime timeSlot) {
        if (!attraction.isTimeSlotValid(timeSlot)) {
            throw new InvalidException(CommonUtils.INVALID_SLOT);
        }
    }

    private Semaphore getOrAddSemaphore(final ServerAttraction attraction, final Integer day) {
        semaphores.get(attraction).putIfAbsent(day, new Semaphore(1));
        return semaphores.get(attraction).get(day);
    }

}
