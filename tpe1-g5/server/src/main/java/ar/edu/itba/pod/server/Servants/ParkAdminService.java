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
    public void addAttraction(AddAttractionRequest attractionRequest, StreamObserver<GenericMessageResponse> responseObserver) {
        List<Attraction> list = attractionRequest.getAttractionListList();

        int added = 0;
        int not_added = 0;

        for (Attraction attraction: list) {
            if (parkData.addAttraction(attraction.getAttractionName(), attraction)) {
                added ++;
            }
            else {
                not_added ++;
            }
        }

        String returnMessage = "Cannot add " + not_added + " attractions\n" + added + " attractions added";
        GenericMessageResponse response = GenericMessageResponse.newBuilder().setMessage(returnMessage).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addPass(AddPassRequest request, StreamObserver<GenericMessageResponse> responseObserver) {

    }

    @Override
    public void addCapacity(AddCapacityRequest request, StreamObserver<GenericMessageResponse> responseObserver) {

    }
}
