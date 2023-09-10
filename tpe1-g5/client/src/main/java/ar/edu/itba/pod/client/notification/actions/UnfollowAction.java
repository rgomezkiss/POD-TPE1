package ar.edu.itba.pod.client.notification.actions;

import ar.edu.itba.pod.client.notification.utils.NotificationParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.notification.NotificationRequest;
import ar.edu.itba.pod.grpc.notification.NotificationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnfollowAction implements Action {

    private final static Logger logger = LoggerFactory.getLogger(UnfollowAction.class);

    @Override
    public void execute(final AbstractParams params, final ManagedChannel channel) {
        final NotificationParams notificationParams = (NotificationParams) params;

        try {
            final NotificationServiceGrpc.NotificationServiceBlockingStub blockingStub = NotificationServiceGrpc.newBlockingStub(channel);

            System.out.println(blockingStub.unfollow(NotificationRequest.newBuilder()
                    .setAttractionName(notificationParams.getRideName())
                    .setDay(notificationParams.getDay())
                    .setUUID(notificationParams.getVisitorId())
                    .build()));
        } catch (StatusRuntimeException e) {
            logger.error("{}: {}", e.getStatus().getCode().toString(), e.getMessage());
        }
    }
}