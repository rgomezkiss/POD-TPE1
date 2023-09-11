package ar.edu.itba.pod.client.consult.actions;

import ar.edu.itba.pod.client.booking.utils.BookingParams;
import ar.edu.itba.pod.client.consult.utils.ConsultParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.park_consult.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class ConfirmedAction implements Action<ConsultParams> {

    private final static Logger logger = LoggerFactory.getLogger(ConfirmedAction.class);

    @Override
    public void execute(final ConsultParams params, final ManagedChannel channel) {
        final ParkConsultServiceGrpc.ParkConsultServiceBlockingStub blockingStub = ParkConsultServiceGrpc.newBlockingStub(channel);

        try {
            final GetBookingsResponse bookingsResponse = blockingStub.
                    getBookings(GetBookingsRequest.newBuilder().setDay(params.getDay()).build());

            writeToFile(bookingsResponse.getBookingsList(), params.getOutPath());
        } catch (StatusRuntimeException e) {
            logger.error("{}: {}", e.getStatus().getCode().toString(), e.getMessage());
        }
    }

    private void writeToFile(final List<BookingResponse> bookingList, final String path) {
        try {
            final Path filePath = Paths.get(path);

            final List<String> lines = bookingList.stream()
                    .map(booking -> String.format("%s | %s | %s",
                            booking.getTimeSlot(),
                            booking.getUUID(),
                            booking.getAttractionName()
                     ))
                    .collect(Collectors.toList());

            lines.add(0, "Slot  | Visitor | Attraction");

            Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            logger.error("Error while writing in file: {}", e.getMessage());
        }
    }
}