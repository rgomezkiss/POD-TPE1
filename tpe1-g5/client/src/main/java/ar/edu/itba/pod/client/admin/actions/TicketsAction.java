package ar.edu.itba.pod.client.admin.actions;

import ar.edu.itba.pod.client.admin.utils.AdminParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.grpc.park_admin.*;
import io.grpc.ManagedChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TicketsAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        AdminParams adminParams = (AdminParams) params;
        // Crear canal para conectarse a server
        ParkAdminServiceGrpc.ParkAdminServiceBlockingStub blockingStub = ParkAdminServiceGrpc.newBlockingStub(channel);

        // Crear "dto" u objeto a enviar al server
        List<AddTicketRequest> toAddTickets = parseFile(adminParams.getInputPath());

        int added = 0;
        int notAdded = 0;

        for (AddTicketRequest ticket : toAddTickets) {
            BooleanResponse booleanResponse = blockingStub.addTicket(ticket);
            if (booleanResponse.getValue()) {
                added++;
            } else {
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

    private static List<AddTicketRequest> parseFile(String path) {
        List<AddTicketRequest> passes = null;
        try {
            passes = Files.lines(Paths.get(path))
                    .skip(1) // Saltar la primera línea (encabezados)
                    .map(line -> line.split(";"))
                    .map(data -> AddTicketRequest.newBuilder()
                            .setUUID(data[0])
                            .setTicketType(TicketType.valueOf(data[1]))
                            .setTicketDay(Integer.parseInt(data[2]))
                            .build())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return passes;
    }
}
