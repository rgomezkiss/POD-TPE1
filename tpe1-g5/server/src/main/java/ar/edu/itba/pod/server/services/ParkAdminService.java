package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.grpc.park_admin.*;
import ar.edu.itba.pod.server.ParkData;
import ar.edu.itba.pod.server.models.ServerAttraction;
import ar.edu.itba.pod.server.models.ServerTicket;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class ParkAdminService extends ParkAdminServiceGrpc.ParkAdminServiceImplBase {
    private final ParkData parkData;

    public ParkAdminService(ParkData parkData) {
        this.parkData = parkData;
    }

    // TODO: podría no devolverse nada y manejar solo casos de error
    @Override
    public void addAttraction(AddAttractionRequest attraction, StreamObserver<com.google.protobuf.Empty> responseObserver) {
        parkData.addAttraction(new ServerAttraction(attraction));

        Empty response = Empty.newBuilder().build();
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
