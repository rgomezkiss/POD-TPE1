package ar.edu.itba.pod.server.Servants;

import ar.edu.itba.pod.grpc.park_admin.*;
import ar.edu.itba.pod.server.ParkData;
import io.grpc.stub.StreamObserver;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ParkAdminService extends ParkAdminServiceGrpc.ParkAdminServiceImplBase{
    private final ParkData parkData;
    public ParkAdminService(ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void addAttraction(Attraction attraction, StreamObserver<BooleanResponse> responseObserver) {
        BooleanResponse response = BooleanResponse.newBuilder().setValue(parkData.addAttraction(attraction)).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addTicket(Ticket ticket, StreamObserver<BooleanResponse> responseObserver) {
        BooleanResponse response = BooleanResponse.newBuilder().setValue(parkData.addTicket(ticket)).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addSlot(AddSlotRequest request, StreamObserver<GenericMessageResponse> responseObserver) {

    }
}
