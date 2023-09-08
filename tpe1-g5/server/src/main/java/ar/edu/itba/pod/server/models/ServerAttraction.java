package ar.edu.itba.pod.server.models;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import ar.edu.itba.pod.grpc.park_admin.*;

public class ServerAttraction {
    private final String attractionName;
    private final LocalTime openingTime;
    private final LocalTime closingTime;
    private final int slotSize;
    private final Set<LocalTime> timeSlots = new HashSet<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public ServerAttraction(String attractionName, LocalTime openingTime, LocalTime closingTime, int slotSize) {
        this.attractionName = attractionName;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.slotSize = slotSize;
        addTimeSlots();
    }

    public ServerAttraction(AddAttractionRequest attractionRequest) {
        this.attractionName = attractionRequest.getAttractionName();
        this.openingTime = LocalTime.parse(attractionRequest.getOpeningTime(), formatter);
        this.closingTime = LocalTime.parse(attractionRequest.getClosingTime(), formatter);
        this.slotSize = attractionRequest.getSlotSize();
        addTimeSlots();
    }

    public String getAttractionName() {
        return attractionName;
    }
    public LocalTime getOpeningTime() {
        return openingTime;
    }
    public LocalTime getClosingTime() {
        return closingTime;
    }
    public int getSlotSize() {
        return slotSize;
    }
    public boolean isTimeSlotValid(LocalTime time) {
        return this.timeSlots.contains(time);
    }
    private void addTimeSlots() {
        LocalTime aux = this.openingTime;
        while (aux.isBefore(this.closingTime)) {
            timeSlots.add(aux);
            aux = aux.plusMinutes(this.slotSize);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerAttraction other = (ServerAttraction) o;
        return this.attractionName.equals(other.attractionName);
    }

    @Override
    public int hashCode() {
        return this.attractionName.hashCode();
    }
}
