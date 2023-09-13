package ar.edu.itba.pod.client.notification.utils;

import ar.edu.itba.pod.client.notification.actions.NotificationActions;
import ar.edu.itba.pod.client.utils.Parser;
import ar.edu.itba.pod.client.utils.ServerAddress;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationParser implements Parser<NotificationParams> {
    private final Logger logger = LoggerFactory.getLogger(NotificationParser.class);
    private final CommandLineParser parser = new DefaultParser();
    private final Options options = new Options();
    private final static String SERVER_ADDRESS = "DserverAddress";
    private final static String ACTION = "Daction";
    private final static String DAY = "Dday";
    private final static String RIDE = "Dride";
    private final static String VISITOR = "Dvisitor";

    public NotificationParser() {
        options.addRequiredOption(SERVER_ADDRESS, SERVER_ADDRESS, true, "Admin server address");
        options.addRequiredOption(ACTION, ACTION, true, "Action to perform");
        options.addRequiredOption(DAY, DAY, true, "Day of the year");
        options.addRequiredOption(RIDE, RIDE, true, "Ride name");
        options.addRequiredOption(VISITOR, VISITOR, true, "Visitor id");
    }

    // -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName -Dday=dayOfYear -DoutPath=output.txt
    @Override
    public NotificationParams parse(String[] args) {
        try {
            final CommandLine cmd = parser.parse(options, args);
            final ServerAddress serverAddress = new ServerAddress(cmd.getOptionValue(SERVER_ADDRESS));
            final NotificationActions action;
            int day;
            String ride;
            String visitorId;

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

            if (cmd.hasOption(DAY)) {
                day = Integer.parseInt(cmd.getOptionValue(DAY));
            } else {
                System.out.println("Day is required for this action");
                logger.error("Day is required for this action");
                return null;
            }

            if (cmd.hasOption(RIDE)) {
                ride = cmd.getOptionValue(RIDE);
            } else {
                System.out.println("Ride name is required for this action");
                logger.error("Ride name is required for this action");
                return null;
            }

            if (cmd.hasOption(VISITOR)) {
                visitorId = cmd.getOptionValue(VISITOR);
            } else {
                System.out.println("Visitor id is required for this action");
                logger.error("Visitor id is required for this action");
                return null;
            }

            return new NotificationParams(serverAddress, String.valueOf(action), day, ride, visitorId);

        } catch (ParseException e) {
            System.out.println("Error parsing command line arguments");
            logger.error("Error parsing command line arguments");
            return null;
        }
    }
}
