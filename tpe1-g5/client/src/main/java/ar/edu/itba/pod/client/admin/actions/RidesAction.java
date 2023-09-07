package ar.edu.itba.pod.client.admin.actions;

import ar.edu.itba.pod.client.admin.utils.AdminParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.grpc.park_admin.AddAttractionRequest;
import ar.edu.itba.pod.grpc.park_admin.BooleanResponse;
import ar.edu.itba.pod.grpc.park_admin.ParkAdminServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class RidesAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        AdminParams adminParams = (AdminParams) params;
        // Crear canal para conectarse a server
        ParkAdminServiceGrpc.ParkAdminServiceBlockingStub blockingStub = ParkAdminServiceGrpc.newBlockingStub(channel);

        // Crear "dto" u objeto a enviar al server
        List<AddAttractionRequest> toAddAttractions = parseFile(adminParams.getInputPath());

        int added = 0;
        int notAdded = 0;

        for (AddAttractionRequest attraction : toAddAttractions) {
            try {
                blockingStub.addAttraction(attraction);
                added++;
            }
            catch (StatusRuntimeException e) {
                System.out.println(e.getStatus() + e.getMessage());
                notAdded++;
            }
        }

        // Imprimo respuesta
        if (notAdded > 0) {
            System.out.println("Cannot add " + notAdded + " attractions");
        }
        if (added > 0) {
            System.out.println(added + " attractions added");
        }
    }

    private static List<AddAttractionRequest> parseFile(String path) {
        List<AddAttractionRequest> attractions = null;
        try {
            attractions = Files.lines(Paths.get(path))
                    .skip(1) // Saltar la primera línea (encabezados)
                    .map(line -> line.split(";"))
                    .map(data -> AddAttractionRequest.newBuilder()
                            .setAttractionName(data[0])
                            .setOpeningTime(data[1])
                            .setClosingTime(data[2])
                            .setSlotSize(Integer.parseInt(data[3]))
                            .build())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return attractions;
    }
}
