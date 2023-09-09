package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.grpc.park_consult.*;
import ar.edu.itba.pod.server.ParkData;
import io.grpc.stub.StreamObserver;
import java.util.List;

public class ConsultService extends ParkConsultServiceGrpc.ParkConsultServiceImplBase {
    private final ParkData parkData;

    public ConsultService(final ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void getSuggestedCapacity(final GetSuggestedCapacityRequest request, final StreamObserver<GetSuggestedCapacityResponse> responseObserver) {
        final List<SuggestedCapacity> suggestedCapacityList = parkData.getSuggestedCapacity(request.getDay());
        final GetSuggestedCapacityResponse response = GetSuggestedCapacityResponse.newBuilder().addAllSuggestedCapacities(suggestedCapacityList).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getBookings(final GetBookingsRequest request, final StreamObserver<GetBookingsResponse> responseObserver) {
        final List<BookingResponse> bookingList = parkData.getBookings(request.getDay());
        final GetBookingsResponse response = GetBookingsResponse.newBuilder().addAllBookings(bookingList).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
