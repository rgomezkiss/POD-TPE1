package ar.edu.itba.pod.client.admin;

import ar.edu.itba.pod.client.abstract_classes.AbstractParams;
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

        AbstractParams params = new AdminParser().parse(args);

        if (params == null) {
            return;
        }

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

        try {
            // TODO
            // Me comunico con el servidor e imprimo la respuesta
            // Acá llamamos a la clase Action (enum o abstracta), y ejecutamos
            // action.connectToServer(params)
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
