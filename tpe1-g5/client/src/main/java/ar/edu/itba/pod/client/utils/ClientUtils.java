package ar.edu.itba.pod.client.utils;

import java.util.List;


public class ClientUtils {

    public void printSchedule(List<String> slots) {
        // Imprimir encabezado
        System.out.println("Slot | Capacity | Pending | Confirmed | Attraction");

        // Imprimir cada franja horaria en el formato deseado
        for (String timeSlot : slots) {
            String formattedTimeSlot = String.format("%02d:%02d | %8d | %8d  | %8d  | %s",
                    timeSlot.getHour(), timeSlot.getMinute(), timeSlot.getCapacity(),
                    timeSlot.getPending(), timeSlot.getConfirmed(), attractionName);
            System.out.println(formattedTimeSlot);
        }
    }
}
