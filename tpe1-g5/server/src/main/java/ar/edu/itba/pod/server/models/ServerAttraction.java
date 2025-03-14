package ar.edu.itba.pod.server.models;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import ar.edu.itba.pod.grpc.park_admin.*;
import ar.edu.itba.pod.server.exceptions.InvalidException;
import ar.edu.itba.pod.server.utils.CommonUtils;

public class ServerAttraction {
    private final String attractionName;
    private final LocalTime openingTime;
    private final LocalTime closingTime;
    private final int slotSize;
    private final Set<LocalTime> timeSlots = new HashSet<>();

    public ServerAttraction(AddAttractionRequest attractionRequest) {
        this.attractionName = attractionRequest.getAttractionName();
        this.openingTime = CommonUtils.parseTime(attractionRequest.getOpeningTime());
        this.closingTime = CommonUtils.parseTime(attractionRequest.getClosingTime());
        this.slotSize = attractionRequest.getSlotSize();
        validateParameters();
        addTimeSlots();
    }

    private void validateParameters(){
        CommonUtils.validateTimeRange(this.openingTime, this.closingTime);
        if(this.slotSize <= 0){
            throw new InvalidException(CommonUtils.NEGATIVE_SLOT);
        }
        if(this.openingTime.plusMinutes(this.slotSize).isAfter(this.closingTime)){
            throw new InvalidException(CommonUtils.SLOT_SIZE_NOT_ENOUGH);
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

    public LocalTime getSlotsInRange(LocalTime startTime) {
        LocalTime currentTime = openingTime;

        while (!currentTime.isAfter(startTime)) {
            currentTime = currentTime.plusMinutes(this.slotSize);
        }

        return currentTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ServerAttraction other = (ServerAttraction) o;
        return this.attractionName.equals(other.attractionName);
    }

    @Override
    public int hashCode() {
        return this.attractionName.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s %s-%s with slot of %d min",
                this.attractionName, this.openingTime.toString(), this.closingTime.toString(), this.slotSize
                );
    }
}
