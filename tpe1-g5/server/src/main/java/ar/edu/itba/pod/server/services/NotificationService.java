package ar.edu.itba.pod.server.services;

import ar.edu.itba.pod.grpc.notification.*;
import ar.edu.itba.pod.server.ParkData;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

public class NotificationService extends NotificationServiceGrpc.NotificationServiceImplBase{
    private final ParkData parkData;

    public NotificationService(ParkData parkData) {
        this.parkData = parkData;
    }

    @Override
    public void follow(NotificationRequest request, StreamObserver<StringValue> responseObserver) {
        parkData.follow(request, responseObserver);
    }

    @Override
    public void unfollow(NotificationRequest request, StreamObserver<StringValue> responseObserver) {
        parkData.unfollow(request);
        responseObserver.onNext(StringValue.newBuilder().setValue("Cancelled subscription").build());
        responseObserver.onCompleted();
    }
}
