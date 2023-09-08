package ar.edu.itba.pod.client.consult.actions;

import ar.edu.itba.pod.client.consult.utils.ConsultParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.park_consult.*;
import io.grpc.ManagedChannel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ConfirmedAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        ConsultParams consultParams = (ConsultParams) params;

        ParkConsultServiceGrpc.ParkConsultServiceBlockingStub blockingStub = ParkConsultServiceGrpc.newBlockingStub(channel);

        GetBookingsResponse bookingsResponse = blockingStub.
                getBookings(GetBookingsRequest.newBuilder().setDay(consultParams.getDay()).build());

        writeToFile(bookingsResponse.getBookingsList(), consultParams.getOutPath());
    }

    private void writeToFile(List<BookingResponse> bookingList, String path) {
        try {
            Path filePath = Paths.get(path);

            List<String> lines = bookingList.stream()
                    .map(booking -> String.format("%s | %s | %s",
                            booking.getTimeSlot(),
                            booking.getUUID(),
                            booking.getAttractionName()
                     ))
                    .collect(Collectors.toList());

            lines.add(0, "Slot  | Visitor | Attraction");

            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}