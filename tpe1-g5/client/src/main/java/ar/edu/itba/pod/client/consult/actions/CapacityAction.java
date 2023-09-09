package ar.edu.itba.pod.client.consult.actions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import ar.edu.itba.pod.client.consult.utils.ConsultParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.park_consult.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class CapacityAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        ConsultParams consultParams = (ConsultParams) params;

        ParkConsultServiceGrpc.ParkConsultServiceBlockingStub blockingStub = ParkConsultServiceGrpc.newBlockingStub(channel);

        try {
            GetSuggestedCapacityResponse capacityResponse = blockingStub.
                    getSuggestedCapacity(GetSuggestedCapacityRequest.newBuilder().setDay(consultParams.getDay()).build());

            writeToFile(capacityResponse.getSuggestedCapacitiesList(), consultParams.getOutPath());
        } catch (StatusRuntimeException e) {

        }
    }

    private void writeToFile(List<SuggestedCapacity> suggestedCapacities, String path) {
        try {
            Path filePath = Paths.get(path);

            List<String> lines = suggestedCapacities.stream()
                    .map(capacity -> String.format("%s | %7d | %s",
                            capacity.getMaxCapSlot(),
                            capacity.getSuggestedCapacity(),
                            capacity.getAttractionName()))
                    .collect(Collectors.toList());

            lines.add(0, "Slot  | Capacity | Attraction");

            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}