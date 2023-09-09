package ar.edu.itba.pod.client.admin.actions;

import ar.edu.itba.pod.client.admin.AdminClient;
import ar.edu.itba.pod.client.admin.utils.AdminParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.grpc.park_admin.AddSlotRequest;
import ar.edu.itba.pod.grpc.park_admin.AddSlotResponse;
import ar.edu.itba.pod.grpc.park_admin.ParkAdminServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlotsAction implements Action {
    private final static Logger logger = LoggerFactory.getLogger(SlotsAction.class);

    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        AdminParams adminParams = (AdminParams) params;

        ParkAdminServiceGrpc.ParkAdminServiceBlockingStub blockingStub = ParkAdminServiceGrpc.newBlockingStub(channel);

        AddSlotRequest addSlotRequest = AddSlotRequest.newBuilder()
                .setAttractionName(adminParams.getRide())
                .setCapacity(adminParams.getCapacity())
                .setDay(adminParams.getDay())
                .build();

        try {
            AddSlotResponse addSlotResponse = blockingStub.addSlot(addSlotRequest);
            System.out.println(
                    String.format("Loaded capacity of %d for %s on day %d\n" +
                                    "%d bookings confirmed without changes\n" +
                                    "%d bookings relocated\n" +
                                    "%d bookings cancelled\n",
                            addSlotRequest.getCapacity(), addSlotRequest.getAttractionName(), addSlotRequest.getDay(),
                            addSlotResponse.getConfirmed(), addSlotResponse.getRelocated(), addSlotResponse.getCancelled())
            );
        } catch (StatusRuntimeException e) {
            logger.info(String.format("%s: %s", e.getStatus().getCode().toString(), e.getMessage()));
        }
    }
}
