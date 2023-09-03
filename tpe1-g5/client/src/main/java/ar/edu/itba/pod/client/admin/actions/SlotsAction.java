package ar.edu.itba.pod.client.admin.actions;

import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.client.utils.AbstractParams;
import io.grpc.ManagedChannel;

public class SlotsAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
//        // Crear canal para conectarse a server
//        ParkAdminServiceGrpc.ParkAdminServiceBlockingStub blockingStub = ParkAdminServiceGrpc.newBlockingStub(channel);
//        // Crear "dto" u objeto a enviar al server
//        AddAttractionRequest attractionRequest = AddAttractionRequest.newBuilder().build();
//        // Conectarse a server y esperar respuesta
//        GenericMessageResponse messageResponse = blockingStub.addAttraction(attractionRequest);
//
//        // Imprimo respuesta
//        System.out.println(messageResponse.getMessage());
    }
}
