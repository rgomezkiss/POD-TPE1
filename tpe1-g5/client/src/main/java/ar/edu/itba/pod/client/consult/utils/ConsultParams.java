package ar.edu.itba.pod.client.consult.utils;

import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.ServerAddress;

import java.util.UUID;

public class ConsultParams extends AbstractParams {
    private final String outPath;

    public ConsultParams(ServerAddress serverAddress, String action, Integer day, String outPath) {
        super(serverAddress, action, day);
        this.outPath = outPath;
    }

    public String getOutPath() {
        return outPath;
    }
}
