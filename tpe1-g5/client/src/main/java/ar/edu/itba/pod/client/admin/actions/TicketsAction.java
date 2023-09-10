package ar.edu.itba.pod.client.admin.actions;

import ar.edu.itba.pod.client.admin.utils.AdminParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.grpc.park_admin.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TicketsAction implements Action {
    private final static Logger logger = LoggerFactory.getLogger(TicketsAction.class);

    @Override
    public void execute(final AbstractParams params, final ManagedChannel channel) {
        final AdminParams adminParams = (AdminParams) params;
        final ParkAdminServiceGrpc.ParkAdminServiceBlockingStub blockingStub = ParkAdminServiceGrpc.newBlockingStub(channel);
        final List<AddTicketRequest> toAddTickets = parseFile(adminParams.getInputPath());

        int added = 0;
        int notAdded = 0;

        for (AddTicketRequest ticket : toAddTickets) {
            try {
                blockingStub.addTicket(ticket);
                added++;
            } catch (StatusRuntimeException e) {
                logger.error("{}: {}", e.getStatus().getCode().toString(), e.getMessage());
                notAdded++;
            }
        }

        // Imprimo respuesta
        if (notAdded > 0) {
            System.out.println("Cannot add " + notAdded + " tickets");
        }
        if (added > 0) {
            System.out.println(added + " tickets added");
        }
    }

    private static List<AddTicketRequest> parseFile(final String path) {
        List<AddTicketRequest> passes = null;

        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            passes = lines
                    .skip(1)    // Salteamos los encabezados
                    .map(line -> line.split(";"))
                    .map(data -> AddTicketRequest.newBuilder()
                            .setUUID(data[0])
                            .setTicketType(data[1])
                            .setTicketDay(Integer.parseInt(data[2]))
                            .build())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error while reading file: {}", e.getMessage());
        }

        return passes;
    }
}
