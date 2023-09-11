package ar.edu.itba.pod.client.admin;

import ar.edu.itba.pod.client.admin.actions.AdminActions;
import ar.edu.itba.pod.client.admin.actions.RidesAction;
import ar.edu.itba.pod.client.admin.actions.SlotsAction;
import ar.edu.itba.pod.client.admin.actions.TicketsAction;
import ar.edu.itba.pod.client.admin.utils.AdminParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.admin.utils.AdminParser;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AdminClient {
    private final static Logger logger = LoggerFactory.getLogger(AdminClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("Admin Client Starting ...");

        final AdminParams params = new AdminParser().parse(args);

        if (params == null) {
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder
                .forAddress(params.getServerAddress().getHost(), params.getServerAddress().getPort())
                .usePlaintext()
                .build();

        try {
            switch (AdminActions.valueOf(params.getAction())) {
                case RIDES -> new RidesAction().execute(params, channel);
                case TICKETS -> new TicketsAction().execute(params, channel);
                case SLOTS -> new SlotsAction().execute(params, channel);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
