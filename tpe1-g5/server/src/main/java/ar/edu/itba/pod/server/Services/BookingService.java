package ar.edu.itba.pod.server.Services;

import ar.edu.itba.pod.grpc.booking.*;
import ar.edu.itba.pod.grpc.park_admin.ParkAdminServiceGrpc;
import ar.edu.itba.pod.server.ParkData;
import io.grpc.stub.StreamObserver;

public class BookingService extends BookingServiceGrpc.BookingServiceImplBase {
    private final ParkData parkData;

    public BookingService(ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void getAttractions(GetAttractionsRequest request, StreamObserver<GetAttractionsResponse> responseObserver) {

    }

    @Override
    public void getAvailability(GetAvailabilityRequest request, StreamObserver<GetAvailabilityResponse> responseObserver) {
    }

    @Override
    public void book(BookRequest request, StreamObserver<BookResponse> responseObserver) {
    }

    @Override
    public void confirmBooking(BookRequest request, StreamObserver<BookResponse> responseObserver) {
    }

    @Override
    public void cancelBooking(BookRequest request, StreamObserver<BookResponse> responseObserver) {
    }
}
