package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.grpc.booking.*;
import ar.edu.itba.pod.server.ParkData;
import ar.edu.itba.pod.server.models.ServerAttraction;
import ar.edu.itba.pod.server.models.ServerBooking;
import io.grpc.stub.StreamObserver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BookingService extends BookingServiceGrpc.BookingServiceImplBase {
    private final ParkData parkData;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public BookingService(ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void getAttractions(GetAttractionsRequest request, StreamObserver<GetAttractionsResponse> responseObserver) {
        List<ServerAttraction> attractionList = new ArrayList<>(parkData.getAttractions().values());

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
        List<AvailabilityResponse> availabilityResponses = new LinkedList<>();

        LocalTime startTime = LocalTime.parse(request.getTimeRangeStart(), formatter);

        if (request.getAttractionName().isEmpty()){
            availabilityResponses.addAll(parkData.getAvailability(request.getDay(), startTime, LocalTime.parse(request.getTimeRangeEnd(), formatter)));
        }
        else if (request.getTimeRangeEnd().isEmpty()){
            availabilityResponses.add(parkData.getAvailability(request.getAttractionName(), request.getDay(), startTime));
        }
        else {
            availabilityResponses.addAll(parkData.getAvailability(request.getAttractionName(), request.getDay(), startTime, LocalTime.parse(request.getTimeRangeEnd())));
        }

        GetAvailabilityResponse response = GetAvailabilityResponse.newBuilder().addAllAvailabilityResponses(availabilityResponses).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // TODO: podría devolverse únicamente Empty y que los errores se catcheen por el interceptor
    @Override
    public void book(BookRequest request, StreamObserver<BookResponse> responseObserver) {
        // Ver que devolver...
        boolean toRet = parkData.book(new ServerBooking(request));

        BookResponse response = BookResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void confirmBooking(BookRequest request, StreamObserver<BookResponse> responseObserver) {
        boolean toRet = parkData.confirmBooking(new ServerBooking(request));

        BookResponse response = BookResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void cancelBooking(BookRequest request, StreamObserver<BookResponse> responseObserver) {
        boolean toRet = parkData.cancelBooking(new ServerBooking(request));

        BookResponse response = BookResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public ParkData getParkData() {
        return parkData;
    }
}
