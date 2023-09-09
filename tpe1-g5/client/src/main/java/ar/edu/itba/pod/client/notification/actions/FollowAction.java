package ar.edu.itba.pod.client.notification.actions;

import ar.edu.itba.pod.client.notification.utils.NotificationParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.notification.NotificationRequest;
import ar.edu.itba.pod.grpc.notification.NotificationServiceGrpc;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class FollowAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        NotificationParams notificationParams = (NotificationParams) params;

        NotificationServiceGrpc.NotificationServiceStub stub = NotificationServiceGrpc.newStub(channel);
        StreamObserver<StringValue> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(StringValue response) {
                String notificationMessage = response.getValue();
                System.out.println("Received notification: " + notificationMessage);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error in communication: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Notification stream completed.");
            }
        };

        stub.follow(NotificationRequest.newBuilder()
                .setAttractionName(notificationParams.getRideName())
                .setDay(notificationParams.getDay())
                .setUUID(notificationParams.getVisitorId())
                .build(), responseObserver);
    }
}