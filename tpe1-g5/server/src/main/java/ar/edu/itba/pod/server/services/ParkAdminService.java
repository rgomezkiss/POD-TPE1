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

    @Override
    public void addAttraction(AddAttractionRequest attraction, StreamObserver<Empty> responseObserver) {
        parkData.addAttraction(new ServerAttraction(attraction));
        Empty response = Empty.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addTicket(AddTicketRequest ticket, StreamObserver<Empty> responseObserver) {
        parkData.addTicket(new ServerTicket(ticket));
        Empty response = Empty.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addSlot(AddSlotRequest request, StreamObserver<AddSlotResponse> responseObserver) {
        AddSlotResponse response = parkData.addSlot(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
