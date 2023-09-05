package ar.edu.itba.pod.client.booking.actions;

import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.booking.BookingServiceGrpc;
import ar.edu.itba.pod.grpc.booking.GetAttractionsRequest;
import ar.edu.itba.pod.grpc.booking.GetAttractionsResponse;
import io.grpc.ManagedChannel;

import java.util.List;

public class AttractionsAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        BookingServiceGrpc.BookingServiceBlockingStub blockingStub = BookingServiceGrpc.newBlockingStub(channel);

        GetAttractionsResponse attractionsResponse = blockingStub.getAttractions(GetAttractionsRequest.newBuilder().build());

        attractionsResponse.getAttractionsList().forEach((attractionResponse) -> System.out.println(attractionResponse.getAttractionName() + attractionResponse.getOpeningTime() + attractionResponse.getClosingTime()));
    }
}
