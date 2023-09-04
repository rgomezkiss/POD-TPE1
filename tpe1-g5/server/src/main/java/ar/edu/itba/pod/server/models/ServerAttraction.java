package ar.edu.itba.pod.server.models;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import ar.edu.itba.pod.grpc.park_admin.*;

public class ServerAttraction {
    private final String attractionName;
    private final LocalTime openingTime;
    private final LocalTime closingTime;
    private final int slotSize;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");


    public ServerAttraction(String attractionName, LocalTime openingTime, LocalTime closingTime, int slotSize) {
        this.attractionName = attractionName;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.slotSize = slotSize;
    }

    public ServerAttraction(AddAttractionRequest attractionRequest) {
        this.attractionName = attractionRequest.getAttractionName();
        this.openingTime = LocalTime.parse(attractionRequest.getOpeningTime(), formatter);
        this.closingTime = LocalTime.parse(attractionRequest.getClosingTime(), formatter);
        this.slotSize = 0;
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

    @Override
    public int hashCode() {
        return this.attractionName.hashCode();
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
}
