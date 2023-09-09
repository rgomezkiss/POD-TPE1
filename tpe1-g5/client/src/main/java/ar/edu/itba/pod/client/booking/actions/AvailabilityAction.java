package ar.edu.itba.pod.client.booking.actions;

import ar.edu.itba.pod.client.booking.utils.BookingParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.booking.BookingServiceGrpc;
import ar.edu.itba.pod.grpc.booking.GetAvailabilityRequest;
import ar.edu.itba.pod.grpc.booking.GetAvailabilityResponse;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class AvailabilityAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        BookingParams bookingParams = (BookingParams) params;

        BookingServiceGrpc.BookingServiceBlockingStub blockingStub = BookingServiceGrpc.newBlockingStub(channel);

        try {
            GetAvailabilityResponse availabilityResponse = blockingStub.getAvailability(
                    GetAvailabilityRequest.newBuilder()
                            .setDay(bookingParams.getDay())
                            .setAttractionName(bookingParams.getRide())
                            .setTimeRangeStart(bookingParams.getSlot())
                            .setTimeRangeEnd(bookingParams.getSlotTo())
                            .build()
            );

            //  Slot  | Capacity  | Pending   | Confirmed | Attraction
            //  15:30 |       30  |        0  |        30 | SpaceMountain

            System.out.println("Slot | Capacity | Pending | Confirmed | Attraction");
            availabilityResponse.getAvailabilityResponsesList().forEach((availability) ->
                    System.out.printf("%s | %s | %d | %d | %s%n",
                            availability.getSlot(),
                            availability.getCapacity() == 0 ? "X" : String.valueOf(availability.getCapacity()),
                            availability.getPending(),
                            availability.getConfirmed(),
                            availability.getAttractionName())
            );

        } catch (StatusRuntimeException e) {

        }
   }
}