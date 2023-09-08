package ar.edu.itba.pod.client.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerAddress {

    private final String host;
    private final int port;
    private final static Pattern PATTERN = Pattern.compile("^(?<host>localhost|\\d?\\d?\\d(?:\\.\\d{1,3}){3}):(?<port>\\d{1,5})$");

    public ServerAddress(String serverAddress) {
        Matcher matcher = PATTERN.matcher(serverAddress);
        if (matcher.matches()) {
            this.host = matcher.group("host");
            this.port = Integer.parseInt(matcher.group("port"));
        } else {
            this.host = null;
            this.port = 0;
        }
    }

    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public boolean isValid(){
        return host != null && port > 0;
    }

}
