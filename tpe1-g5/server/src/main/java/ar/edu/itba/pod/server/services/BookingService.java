package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.grpc.booking.*;
import ar.edu.itba.pod.server.ParkData;
import ar.edu.itba.pod.server.exceptions.InvalidException;
import ar.edu.itba.pod.server.models.ServerAttraction;
import ar.edu.itba.pod.server.models.ServerBooking;
import com.google.protobuf.Empty;
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
    public void getAttractions(Empty request, StreamObserver<GetAttractionsResponse> responseObserver) {
        List<ServerAttraction> attractionList = new ArrayList<>(parkData.getAttractions().values());

        // TODO: ver si debería ordenarse
        GetAttractionsResponse response = GetAttractionsResponse.newBuilder()
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
    public void getAvailability(GetAvailabilityRequest request, StreamObserver<GetAvailabilityResponse> responseObserver) {
        GetAvailabilityResponse response = GetAvailabilityResponse.newBuilder()
                .addAllAvailabilityResponses(parkData.getAvailability(request))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // TODO: podría devolverse únicamente Empty y que los errores se catcheen por el interceptor
    @Override
    public void book(BookRequest request, StreamObserver<Empty> responseObserver) {
        parkData.book(new ServerBooking(request));
        Empty response = Empty.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void confirmBooking(BookRequest request, StreamObserver<Empty> responseObserver) {
        parkData.confirmBooking(new ServerBooking(request));
        Empty response = Empty.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void cancelBooking(BookRequest request, StreamObserver<Empty> responseObserver) {
        parkData.cancelBooking(new ServerBooking(request));
        Empty response = Empty.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public ParkData getParkData() {
        return parkData;
    }
}
