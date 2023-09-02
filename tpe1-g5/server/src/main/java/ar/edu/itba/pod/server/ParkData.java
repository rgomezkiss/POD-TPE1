package ar.edu.itba.pod.server;


import ar.edu.itba.pod.grpc.park_admin.Attraction;
import ar.edu.itba.pod.grpc.park_admin.Pass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ParkData {
    private final Map<String, Attraction> attractions = new ConcurrentHashMap<>();
    //UserId -> Day -> Pass
    private final Map<String, Map<Integer, Pass>> passes = new ConcurrentHashMap<>();

    public Map<String, Attraction> getAttractions() {
        return attractions;
    }

    public boolean addAttraction(String name, Attraction attraction) {
        return attractions.put(name, attraction) == null;
    }


}
