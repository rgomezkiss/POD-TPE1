package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.grpc.park_admin.*;
import ar.edu.itba.pod.server.ParkData;
import ar.edu.itba.pod.server.models.ServerAttraction;
import ar.edu.itba.pod.server.models.ServerTicket;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class ParkAdminService extends ParkAdminServiceGrpc.ParkAdminServiceImplBase {
    private final ParkData parkData;

    public ParkAdminService(final ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void addAttraction(final AddAttractionRequest attraction, final StreamObserver<Empty> responseObserver) {
        parkData.addAttraction(new ServerAttraction(attraction));
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void addTicket(final AddTicketRequest ticket, final StreamObserver<Empty> responseObserver) {
        parkData.addTicket(new ServerTicket(ticket));
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void addSlot(final AddSlotRequest request, final StreamObserver<AddSlotResponse> responseObserver) {
        final AddSlotResponse response = parkData.addSlot(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
