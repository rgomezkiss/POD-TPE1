package ar.edu.itba.pod.client.consult.actions;

import ar.edu.itba.pod.client.consult.utils.ConsultParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.park_consult.*;
import io.grpc.ManagedChannel;

public class ConfirmedAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        ConsultParams consultParams = (ConsultParams) params;

        ParkConsultServiceGrpc.ParkConsultServiceBlockingStub blockingStub = ParkConsultServiceGrpc.newBlockingStub(channel);

        GetBookingsResponse capacityResponse = blockingStub.getBookings(GetBookingsRequest.newBuilder().setDay(consultParams.getDay()).build());

        // Imprimir con formato que corresponde en out.txt
    }
}