package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.server.exceptions.InvalidDayException;

// Clase para modelar la capacidad para cierto día, aún sin finalizar
public class DayCapacity {
    private final Integer day;
    private Integer capacity;

    public DayCapacity(Integer day) {
        if(day < 1 || day > 365){
            throw new InvalidDayException();
        }
        this.day = day;
        this.capacity = null;
    }

    public Integer getCapacity() {
        return capacity;
    }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DayCapacity other = (DayCapacity) o;
        return this.day.equals(other.day);
    }

    @Override
    public int hashCode() {
        return day.hashCode();
    }
}
