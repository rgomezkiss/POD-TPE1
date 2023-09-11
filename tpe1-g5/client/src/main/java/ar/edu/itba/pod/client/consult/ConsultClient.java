package ar.edu.itba.pod.client.consult;

import ar.edu.itba.pod.client.consult.actions.CapacityAction;
import ar.edu.itba.pod.client.consult.actions.ConfirmedAction;
import ar.edu.itba.pod.client.consult.actions.ConsultActions;
import ar.edu.itba.pod.client.consult.utils.ConsultParams;
import ar.edu.itba.pod.client.consult.utils.ConsultParser;
import ar.edu.itba.pod.client.utils.AbstractParams;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ConsultClient {
    private final static Logger logger = LoggerFactory.getLogger(ConsultClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("Consult Client Starting ...");

        final ConsultParams params = new ConsultParser().parse(args);

        if (params == null) {
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder
                .forAddress(params.getServerAddress().getHost(), params.getServerAddress().getPort())
                .usePlaintext()
                .build();

        try {
            switch (ConsultActions.valueOf(params.getAction())) {
                case CAPACITY -> new CapacityAction().execute(params, channel);
                case CONFIRMED -> new ConfirmedAction().execute(params, channel);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
