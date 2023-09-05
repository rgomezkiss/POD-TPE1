package ar.edu.itba.pod.client.booking.actions;

import ar.edu.itba.pod.client.booking.utils.BookingParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.booking.BookRequest;
import ar.edu.itba.pod.grpc.booking.BookResponse;
import ar.edu.itba.pod.grpc.booking.BookingServiceGrpc;
import io.grpc.ManagedChannel;

public class BookAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        BookingParams bookingParams = (BookingParams) params;

        BookingServiceGrpc.BookingServiceBlockingStub blockingStub = BookingServiceGrpc.newBlockingStub(channel);
        BookResponse bookResponse = blockingStub.book(BookRequest.newBuilder()
                .setUUID(bookingParams.getVisitorId())
                .setAttractionName(bookingParams.getRide())
                .setDay(bookingParams.getDay())
                .setTimeSlot(bookingParams.getSlot())
                .build());

        //Book response podría ya tener un message...
    }
}