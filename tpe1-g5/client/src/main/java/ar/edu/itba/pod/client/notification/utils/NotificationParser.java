package ar.edu.itba.pod.client.notification.utils;

import ar.edu.itba.pod.client.notification.actions.NotificationActions;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.ServerAddress;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationParser {

    private final Logger logger = LoggerFactory.getLogger(NotificationParser.class);
    private final CommandLineParser parser = new DefaultParser();
    private final Options options = new Options();
    private final static String SERVER_ADDRESS = "DserverAddress";
    private final static String ACTION = "Daction";
    private final static String DAY = "Dday";
    private final static String OUT_PATH = "DoutPath";
    private final static int MIN_DAY = 1;
    private final static int MAX_DAY = 365;

    public NotificationParser() {
        options.addRequiredOption(SERVER_ADDRESS, SERVER_ADDRESS, true, "Admin server address");
        options.addRequiredOption(ACTION, ACTION, true, "Action to perform");
        options.addRequiredOption(DAY, DAY, true, "Day of the year");
        options.addRequiredOption(OUT_PATH, OUT_PATH, true, "Out file path");
    }

    // -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName -Dday=dayOfYear -DoutPath=output.txt
    public AbstractParams parse(String[] args) {
        try {
            final CommandLine cmd = parser.parse(options, args);
            final ServerAddress serverAddress = new ServerAddress(cmd.getOptionValue(SERVER_ADDRESS));
            final NotificationActions action;
            final int day = Integer.parseInt(cmd.getOptionValue(DAY));
            final String outhPath = cmd.getOptionValue(OUT_PATH);

            if (!serverAddress.isValid()) {
                System.out.println("Invalid server address");
                logger.error("Invalid server address");
                return null;
            }

            try {
                action = NotificationActions.valueOf(cmd.getOptionValue(ACTION).toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid action");
                logger.error("Invalid action");
                return null;
            }

            if (day < MIN_DAY || day > MAX_DAY) {
                System.out.println("Invalid day");
                return null;
            }

            return new NotificationParams(serverAddress, String.valueOf(action), day, outhPath);

        } catch (ParseException e) {
            System.out.println("Error parsing command line arguments");
            logger.error("Error parsing command line arguments");
            return null;
        }
    }

}
