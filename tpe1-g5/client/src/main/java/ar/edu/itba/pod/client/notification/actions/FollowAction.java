package ar.edu.itba.pod.client.notification.actions;

import ar.edu.itba.pod.client.notification.utils.NotificationParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.notification.NotificationRequest;
import ar.edu.itba.pod.grpc.notification.NotificationServiceGrpc;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class FollowAction implements Action<NotificationParams> {

    private final static Logger logger = LoggerFactory.getLogger(FollowAction.class);

    @Override
    public void execute(final NotificationParams params, final ManagedChannel channel) {
        final NotificationServiceGrpc.NotificationServiceStub stub = NotificationServiceGrpc.newStub(channel);
        final AtomicBoolean completed = new AtomicBoolean(false);
        final NotificationObserver responseObserver = new NotificationObserver(completed);

        try {
            stub.follow(NotificationRequest.newBuilder()
                    .setAttractionName(params.getRideName())
                    .setDay(params.getDay())
                    .setUUID(params.getVisitorId())
                    .build(), responseObserver
            );
        } catch (StatusRuntimeException e) {
            logger.error("{}: {}", e.getStatus().getCode().toString(), e.getMessage());
        } finally {
            while (!responseObserver.getCompletedSignal()) {
            }
            // Cierra el canal una vez que se ha recibido onCompleted o después de esperar un tiempo razonable.
            channel.shutdown();
            logger.info("Communication finished");
        }
    }

    private static class NotificationObserver implements StreamObserver<StringValue> {
        private final Logger logger = LoggerFactory.getLogger(NotificationObserver.class);
        private final AtomicBoolean completedSignal;

        public NotificationObserver(AtomicBoolean completedSignal) {
            this.completedSignal = completedSignal;
        }

        @Override
        public void onNext(StringValue response) {
            String notificationMessage = response.getValue();
            System.out.println(notificationMessage);
        }

        @Override
        public void onError(Throwable t) {
            completedSignal.set(true);
        }

        @Override
        public void onCompleted() {
            completedSignal.set(true);
            logger.info("Notification stream completed.");
        }

        public boolean getCompletedSignal() {
            return completedSignal.get();
        }
    }
}
