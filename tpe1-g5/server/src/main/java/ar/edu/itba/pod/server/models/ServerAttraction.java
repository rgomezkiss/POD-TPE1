package ar.edu.itba.pod.server.models;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.itba.pod.grpc.park_admin.*;
import ar.edu.itba.pod.server.exceptions.InvalidException;

public class ServerAttraction {
    private final String attractionName;
    private final LocalTime openingTime;
    private final LocalTime closingTime;
    private final int slotSize;
    private final Set<LocalTime> timeSlots = new HashSet<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public ServerAttraction(AddAttractionRequest attractionRequest) {
        this.attractionName = attractionRequest.getAttractionName();
        this.openingTime = LocalTime.parse(attractionRequest.getOpeningTime(), formatter);
        this.closingTime = LocalTime.parse(attractionRequest.getClosingTime(), formatter);
        this.slotSize = attractionRequest.getSlotSize();
        validateParameters();
        addTimeSlots();
    }

    private void validateParameters(){
        if(this.closingTime.isBefore(this.openingTime)) {
            throw new InvalidException("Invalid time");
        }
        if(this.slotSize <= 0){
            throw new InvalidException("Slot size must be a positive number");
        }
        if(this.openingTime.plusMinutes(this.slotSize).isBefore(this.closingTime)){
            throw new InvalidException("Slot size not enough");

        }
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
            this.timeSlots.add(aux);
            aux = aux.plusMinutes(this.slotSize);
        }
    }

    public List<LocalTime> getSlotsInRange(LocalTime startTime, LocalTime endTime) {
        List<LocalTime> validTimesInRange = new ArrayList<>();
        LocalTime currentTime = openingTime;

        while (!currentTime.isAfter(closingTime)) {
            if (!currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)) {
                validTimesInRange.add(currentTime);
            }
            currentTime = currentTime.plusMinutes(this.slotSize);
        }

        return validTimesInRange;
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
