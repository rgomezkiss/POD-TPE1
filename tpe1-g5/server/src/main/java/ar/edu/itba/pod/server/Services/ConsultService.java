package ar.edu.itba.pod.server.Services;

import ar.edu.itba.pod.grpc.park_consult.*;
import ar.edu.itba.pod.server.ParkData;
import io.grpc.stub.StreamObserver;

public class ConsultService extends ParkConsultServiceGrpc.ParkConsultServiceImplBase {
    private final ParkData parkData;

    public ConsultService(ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void getSuggestedCapacity(GetSuggestedCapacityRequest request, StreamObserver<GetSuggestedCapacityResponse> responseObserver) {
    }

    @Override
    public void getBookings(GetBookingsRequest request, StreamObserver<GetBookingsResponse> responseObserver) {
    }
}
