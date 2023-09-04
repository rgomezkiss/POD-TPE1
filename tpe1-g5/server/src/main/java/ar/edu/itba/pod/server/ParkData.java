package ar.edu.itba.pod.server;

import ar.edu.itba.pod.grpc.park_admin.Ticket;
import ar.edu.itba.pod.server.models.ServerAttraction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParkData {
    private final Map<ServerAttraction, String> attractions = new ConcurrentHashMap<>();
    //UserId -> Day -> Pass
    private final Map<String, Map<Integer, Ticket>> tickets = new ConcurrentHashMap<>();

    public Map<ServerAttraction, String> getAttractions() {
        return attractions;
    }

    public boolean addAttraction(ServerAttraction attraction) {
        // TODO: agregar validaciones para especificar errores:
        // Duplicate name.
        // InvalidTime,
        // slotSize negative,
        // slotSize not enough.
        // Recién ahí devolver
        return attractions.put(attraction, attraction.getAttractionName()) == null;
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

}
