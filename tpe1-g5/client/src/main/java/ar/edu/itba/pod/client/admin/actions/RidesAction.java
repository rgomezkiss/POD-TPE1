package ar.edu.itba.pod.client.admin.actions;

import ar.edu.itba.pod.client.admin.utils.AdminParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.client.utils.AbstractParams;
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

public class RidesAction implements Action {
    private final static Logger logger = LoggerFactory.getLogger(RidesAction.class);

    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        AdminParams adminParams = (AdminParams) params;

        ParkAdminServiceGrpc.ParkAdminServiceBlockingStub blockingStub = ParkAdminServiceGrpc.newBlockingStub(channel);

        List<AddAttractionRequest> toAddAttractions = parseFile(adminParams.getInputPath());

        int added = 0;
        int notAdded = 0;

        for (AddAttractionRequest attraction : toAddAttractions) {
            try {
                blockingStub.addAttraction(attraction);
                added++;
            } catch (StatusRuntimeException e) {
                logger.info(String.format("%s: %s", e.getStatus().getCode().toString(), e.getMessage()));
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
        try {
            attractions = Files.lines(Paths.get(path))
                    .skip(1)
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
