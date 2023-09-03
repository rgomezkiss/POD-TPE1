package ar.edu.itba.pod.server;


import ar.edu.itba.pod.grpc.park_admin.Attraction;
import ar.edu.itba.pod.grpc.park_admin.Ticket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ParkData {
    private final Map<String, Attraction> attractions = new ConcurrentHashMap<>();
    //UserId -> Day -> Pass
    private final Map<String, Map<Integer, Ticket>> passes = new ConcurrentHashMap<>();

    public Map<String, Attraction> getAttractions() {
        return attractions;
    }

    public boolean addAttraction(String name, Attraction attraction) {
        // TODO: agregar validaciones para especificar errores:
        // Duplicate name. invalidTime, slotSize negative, slotSize not enough. Agregar en logger...
        return attractions.put(name, attraction) == null;
    }

}
