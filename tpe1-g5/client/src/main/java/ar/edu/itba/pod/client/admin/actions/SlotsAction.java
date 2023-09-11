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

public class SlotsAction implements Action<AdminParams>  {
    private final static Logger logger = LoggerFactory.getLogger(SlotsAction.class);

    @Override
    public void execute(final AdminParams params, final ManagedChannel channel) {
        final ParkAdminServiceGrpc.ParkAdminServiceBlockingStub blockingStub = ParkAdminServiceGrpc.newBlockingStub(channel);

        final AddSlotRequest addSlotRequest = AddSlotRequest.newBuilder()
                .setAttractionName(params.getRide())
                .setCapacity(params.getCapacity())
                .setDay(params.getDay())
                .build();

        try {
            final AddSlotResponse addSlotResponse = blockingStub.addSlot(addSlotRequest);
            System.out.printf("""
                    Loaded capacity of %d for %s on day %d
                    %d bookings confirmed without changes
                    %d bookings relocated
                    %d bookings cancelled
                    %n""",
                    addSlotRequest.getCapacity(), addSlotRequest.getAttractionName(),
                    addSlotRequest.getDay(), addSlotResponse.getConfirmed(),
                    addSlotResponse.getRelocated(), addSlotResponse.getCancelled()
            );
        } catch (StatusRuntimeException e) {
            logger.error("{}: {}", e.getStatus().getCode().toString(), e.getMessage());
        }
    }
}
