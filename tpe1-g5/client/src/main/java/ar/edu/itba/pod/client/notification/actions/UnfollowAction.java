package ar.edu.itba.pod.client.notification.actions;

import ar.edu.itba.pod.client.notification.utils.NotificationParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.notification.NotificationRequest;
import ar.edu.itba.pod.grpc.notification.NotificationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class UnfollowAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        NotificationParams notificationParams = (NotificationParams) params;

        try {
            NotificationServiceGrpc.NotificationServiceBlockingStub blockingStub = NotificationServiceGrpc.newBlockingStub(channel);

            System.out.println(blockingStub.unfollow(NotificationRequest.newBuilder()
                    .setAttractionName(notificationParams.getRideName())
                    .setDay(notificationParams.getDay())
                    .setUUID(notificationParams.getVisitorId())
                    .build()));
        } catch (StatusRuntimeException e) {

        }
    }
}