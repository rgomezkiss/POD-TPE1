package ar.edu.itba.pod.server.Services;

import ar.edu.itba.pod.grpc.park_admin.*;
import ar.edu.itba.pod.server.ParkData;
import ar.edu.itba.pod.server.models.ServerAttraction;
import ar.edu.itba.pod.server.models.ServerTicket;
import io.grpc.stub.StreamObserver;

public class ParkAdminService extends ParkAdminServiceGrpc.ParkAdminServiceImplBase {
    private final ParkData parkData;

    public ParkAdminService(ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void addAttraction(AddAttractionRequest attraction, StreamObserver<BooleanResponse> responseObserver) {
        BooleanResponse response = BooleanResponse.newBuilder().setValue(parkData.addAttraction(new ServerAttraction(attraction))).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addTicket(AddTicketRequest ticket, StreamObserver<BooleanResponse> responseObserver) {
        BooleanResponse response = BooleanResponse.newBuilder().setValue(parkData.addTicket(new ServerTicket(ticket))).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addSlot(AddSlotRequest request, StreamObserver<GenericMessageResponse> responseObserver) {
        //
    }
}
