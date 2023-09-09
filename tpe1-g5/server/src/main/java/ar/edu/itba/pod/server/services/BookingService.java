package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.grpc.booking.*;
import ar.edu.itba.pod.server.ParkData;
import ar.edu.itba.pod.server.models.ServerAttraction;
import ar.edu.itba.pod.server.models.ServerBooking;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookingService extends BookingServiceGrpc.BookingServiceImplBase {
    private final ParkData parkData;

    public BookingService(final ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void getAttractions(final Empty request, final StreamObserver<GetAttractionsResponse> responseObserver) {
        final List<ServerAttraction> attractionList = new ArrayList<>(parkData.getAttractions().values());

        // TODO: ver si debería ordenarse
        final GetAttractionsResponse response = GetAttractionsResponse.newBuilder()
                .addAllAttractions(attractionList.stream()
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
    public void getAvailability(final GetAvailabilityRequest request, final StreamObserver<GetAvailabilityResponse> responseObserver) {
        final List<AvailabilityResponse> attractionList = parkData.getAvailability(request);
        final GetAvailabilityResponse response = GetAvailabilityResponse.newBuilder().addAllAvailabilityResponses(attractionList).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void book(final BookRequest request, final StreamObserver<Empty> responseObserver) {
        parkData.book(new ServerBooking(request));
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void confirmBooking(final BookRequest request, final StreamObserver<Empty> responseObserver) {
        parkData.confirmBooking(new ServerBooking(request));
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void cancelBooking(final BookRequest request, final StreamObserver<Empty> responseObserver) {
        parkData.cancelBooking(new ServerBooking(request));
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
