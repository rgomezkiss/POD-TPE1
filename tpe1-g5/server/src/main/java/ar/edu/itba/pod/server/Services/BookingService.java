package ar.edu.itba.pod.server.Services;

import ar.edu.itba.pod.grpc.booking.*;
import ar.edu.itba.pod.server.ParkData;
import ar.edu.itba.pod.server.models.ServerAttraction;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookingService extends BookingServiceGrpc.BookingServiceImplBase {
    private final ParkData parkData;

    public BookingService(ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void getAttractions(GetAttractionsRequest request, StreamObserver<GetAttractionsResponse> responseObserver) {
        List<ServerAttraction> attractionList = new ArrayList<>(parkData.getAttractions().keySet());

        GetAttractionsResponse response = GetAttractionsResponse.newBuilder().
                addAllAttractions(attractionList.stream()
                .map(attraction -> AttractionResponse.newBuilder()
                        .setAttractionName(attraction.getAttractionName())
                        .setOpeningTime(attraction.getOpeningTime().toString())
                        .setClosingTime(attraction.getClosingTime().toString())
                        .build()
                )
                .collect(Collectors.toList())).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
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

    public ParkData getParkData() {
        return parkData;
    }
}
