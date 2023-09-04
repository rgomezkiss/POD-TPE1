package ar.edu.itba.pod.client.consult.utils;

import ar.edu.itba.pod.client.consult.actions.ConsultActions;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.ServerAddress;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ConsultParser {

    private final Logger logger = LoggerFactory.getLogger(ConsultParser.class);
    private final CommandLineParser parser = new DefaultParser();
    private final Options options = new Options();
    private final static String SERVER_ADDRESS = "DserverAddress";
    private final static String ACTION = "Daction";
    private final static String DAY = "Dday";
    private final static String RIDE = "Dride";
    private final static String VISITOR = "Dvisitor";
    private final static int MIN_DAY = 1;
    private final static int MAX_DAY = 365;

    public ConsultParser() {
        options.addRequiredOption(SERVER_ADDRESS, SERVER_ADDRESS, true, "Admin server address");
        options.addRequiredOption(ACTION, ACTION, true, "Action to perform");
        options.addRequiredOption(DAY, DAY, true, "Day of the year");
        options.addRequiredOption(RIDE, RIDE, true, "Ride name");
        options.addRequiredOption(VISITOR, VISITOR, true, "Visitor ID");
    }

    // -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName -Dday=dayOfYear -Dride=rideName -Dvisitor=visitorId
    public AbstractParams parse(String[] args) {
        try {
            final CommandLine cmd = parser.parse(options, args);
            final ServerAddress serverAddress = new ServerAddress(cmd.getOptionValue(SERVER_ADDRESS));
            final ConsultActions action;
            final int day = Integer.parseInt(cmd.getOptionValue(DAY));
            final String ride = cmd.getOptionValue(RIDE);
            final UUID visitorId = UUID.fromString(cmd.getOptionValue(VISITOR));

            if (!serverAddress.isValid()) {
                System.out.println("Invalid server address");
                logger.error("Invalid server address");
                return null;
            }

            try {
                action = ConsultActions.valueOf(cmd.getOptionValue(ACTION).toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid action");
                logger.error("Invalid action");
                return null;
            }

            if (day < MIN_DAY || day > MAX_DAY) {
                System.out.println("Invalid day");
                return null;
            }

            return new ConsultParams(serverAddress, String.valueOf(action), day, ride, visitorId);

        } catch (ParseException e) {
            System.out.println("Error parsing command line arguments");
            logger.error("Error parsing command line arguments");
            return null;
        }
    }

}

