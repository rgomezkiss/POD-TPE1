package ar.edu.itba.pod.client.admin.actions;

import ar.edu.itba.pod.client.admin.utils.AdminParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.park_admin.AddAttractionRequest;
import ar.edu.itba.pod.grpc.park_admin.ParkAdminServiceGrpc;
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

public class RidesAction implements Action<AdminParams> {
    private final static Logger logger = LoggerFactory.getLogger(RidesAction.class);

    @Override
    public void execute(final AdminParams params, final ManagedChannel channel) {
        final ParkAdminServiceGrpc.ParkAdminServiceBlockingStub blockingStub = ParkAdminServiceGrpc.newBlockingStub(channel);
        final List<AddAttractionRequest> toAddAttractions = parseFile(params.getInputPath());

        int added = 0;
        int notAdded = 0;

        for (AddAttractionRequest attraction : toAddAttractions) {
            try {
                blockingStub.addAttraction(attraction);
                added++;
            } catch (StatusRuntimeException e) {
                logger.error("{}: {}", e.getStatus().getCode().toString(), e.getMessage());
                notAdded++;
            }
        }

        if (notAdded > 0) {
            System.out.println("Cannot add " + notAdded + " attractions");
        }
        if (added > 0) {
            System.out.println(added + " attractions added");
        }
    }

    private static List<AddAttractionRequest> parseFile(String path) {
        List<AddAttractionRequest> attractions = null;
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            attractions = lines
                    .skip(1) // Salteamos los encabezados
                    .map(line -> line.split(";"))
                    .map(data -> AddAttractionRequest.newBuilder()
                            .setAttractionName(data[0])
                            .setOpeningTime(data[1])
                            .setClosingTime(data[2])
                            .setSlotSize(Integer.parseInt(data[3]))
                            .build())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error while reading file: {}", e.getMessage());
        }

        return attractions;
    }
}
