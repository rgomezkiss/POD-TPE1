package ar.edu.itba.pod.client.notification;

import ar.edu.itba.pod.client.notification.actions.FollowAction;
import ar.edu.itba.pod.client.notification.actions.NotificationActions;
import ar.edu.itba.pod.client.notification.actions.UnfollowAction;
import ar.edu.itba.pod.client.notification.utils.NotificationParams;
import ar.edu.itba.pod.client.notification.utils.NotificationParser;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class NotificationClient {
    private final static Logger logger = LoggerFactory.getLogger(NotificationClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("Notification Client Starting ...");

        final NotificationParams params = new NotificationParser().parse(args);

        if (params == null) {
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder
                .forAddress(params.getServerAddress().getHost(), params.getServerAddress().getPort())
                .usePlaintext()
                .build();

        switch (NotificationActions.valueOf(params.getAction())) {
            case FOLLOW -> new FollowAction().execute(params, channel);
            case UNFOLLOW -> {
                new UnfollowAction().execute(params, channel);
                channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            }
        }

    }
}
