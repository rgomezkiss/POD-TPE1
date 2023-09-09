package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.grpc.park_consult.*;
import ar.edu.itba.pod.server.ParkData;
import io.grpc.stub.StreamObserver;
import java.util.List;

public class ConsultService extends ParkConsultServiceGrpc.ParkConsultServiceImplBase {
    private final ParkData parkData;

    public ConsultService(ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void getSuggestedCapacity(GetSuggestedCapacityRequest request, StreamObserver<GetSuggestedCapacityResponse> responseObserver) {
        List<SuggestedCapacity> suggestedCapacityList = parkData.getSuggestedCapacity(request.getDay());
        GetSuggestedCapacityResponse response = GetSuggestedCapacityResponse.newBuilder()
                .addAllSuggestedCapacities(suggestedCapacityList).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getBookings(GetBookingsRequest request, StreamObserver<GetBookingsResponse> responseObserver) {
        List<BookingResponse> bookingList = parkData.getBookings(request.getDay());
        GetBookingsResponse response = GetBookingsResponse.newBuilder().addAllBookings(bookingList).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
