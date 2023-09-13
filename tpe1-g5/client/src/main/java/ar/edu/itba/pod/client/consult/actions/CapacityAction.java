package ar.edu.itba.pod.client.consult.actions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import ar.edu.itba.pod.client.consult.utils.ConsultParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.park_consult.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CapacityAction implements Action<ConsultParams> {

    private final static Logger logger = LoggerFactory.getLogger(CapacityAction.class);

    @Override
    public void execute(final ConsultParams params, final ManagedChannel channel) {
        final ParkConsultServiceGrpc.ParkConsultServiceBlockingStub blockingStub = ParkConsultServiceGrpc.newBlockingStub(channel);

        try {
            final GetSuggestedCapacityResponse capacityResponse = blockingStub.getSuggestedCapacity(
                    GetSuggestedCapacityRequest.newBuilder().setDay(params.getDay()).build()
            );

            writeToFile(capacityResponse.getSuggestedCapacitiesList(), params.getOutPath());
        } catch (StatusRuntimeException e) {
            logger.error("{}: {}", e.getStatus().getCode().toString(), e.getMessage());
        }
    }

    private void writeToFile(final List<SuggestedCapacity> suggestedCapacities, final String path) {
        try {
            final Path filePath = Paths.get(path);

            final List<String> lines = suggestedCapacities.stream()
                    .map(capacity -> String.format("%s | %7d | %s",
                            capacity.getMaxCapSlot(),
                            capacity.getSuggestedCapacity(),
                            capacity.getAttractionName()))
                    .collect(Collectors.toList());

            lines.add(0, "Slot  | Capacity | Attraction");

            Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.error("Error while writing in file: {}", e.getMessage());
        }
    }
}