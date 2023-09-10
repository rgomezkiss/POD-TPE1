package ar.edu.itba.pod.client.notification.actions;

import ar.edu.itba.pod.client.notification.utils.NotificationParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.notification.NotificationRequest;
import ar.edu.itba.pod.grpc.notification.NotificationServiceGrpc;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class FollowAction implements Action {

    private final static Logger logger = LoggerFactory.getLogger(FollowAction.class);

    @Override
    public void execute(final AbstractParams params, final ManagedChannel channel) {
        final NotificationParams notificationParams = (NotificationParams) params;
        final NotificationServiceGrpc.NotificationServiceStub stub = NotificationServiceGrpc.newStub(channel);
        final StreamObserver<StringValue> responseObserver = new NotificationObserver();

        try {
            stub.follow(NotificationRequest.newBuilder()
                    .setAttractionName(notificationParams.getRideName())
                    .setDay(notificationParams.getDay())
                    .setUUID(notificationParams.getVisitorId())
                    .build(), responseObserver
            );
        } catch (StatusRuntimeException e) {
            logger.error("{}: {}", e.getStatus().getCode().toString(), e.getMessage());
        } finally {
            //TODO: check
            try {
                channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Error while shutting down channel: {}", e.getMessage());
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }
    }

    private static class NotificationObserver implements StreamObserver<StringValue> {
        private final Logger logger = LoggerFactory.getLogger(NotificationObserver.class);

        @Override
        public void onNext(StringValue response) {
            String notificationMessage = response.getValue();
            System.out.println(notificationMessage);
        }

        @Override
        public void onError(Throwable t) {
            logger.error("Error in communication: {}", t.getMessage());
        }

        @Override
        public void onCompleted() {
            logger.info("Notification stream completed.");
        }
    }
}
