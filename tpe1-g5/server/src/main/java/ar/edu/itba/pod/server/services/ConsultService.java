package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.grpc.park_consult.*;
import ar.edu.itba.pod.server.ParkData;
import ar.edu.itba.pod.server.models.ServerBooking;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.stream.Collectors;

public class ConsultService extends ParkConsultServiceGrpc.ParkConsultServiceImplBase {
    private final ParkData parkData;

    public ConsultService(ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void getSuggestedCapacity(GetSuggestedCapacityRequest request, StreamObserver<GetSuggestedCapacityResponse> responseObserver) {
        List<SuggestedCapacity> suggestedCapacityList = parkData.getSuggestedCapacity(request.getDay());

        GetSuggestedCapacityResponse response = GetSuggestedCapacityResponse.newBuilder().addAllSuggestedCapacities(suggestedCapacityList).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getBookings(GetBookingsRequest request, StreamObserver<GetBookingsResponse> responseObserver) {
        // Método listo, falta convertir al otro tipo de dato
        List<ServerBooking> bookingList = parkData.getBookings(request.getDay());

        GetBookingsResponse response = GetBookingsResponse.newBuilder()
            .addAllBookings(bookingList.stream()
                    .map(booking -> BookingResponse.newBuilder()
                            .setAttractionName(booking.getAttractionName())
                            .setUUID(booking.getUserId().toString())
                            .setTimeSlot(booking.getSlot().toString())
                            .build()
                    )
                    .collect(Collectors.toList())).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
