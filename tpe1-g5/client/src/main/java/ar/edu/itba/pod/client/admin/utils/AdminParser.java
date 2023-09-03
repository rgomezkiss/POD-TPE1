package ar.edu.itba.pod.client.admin.utils;

import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.admin.actions.AdminActions;
import ar.edu.itba.pod.client.utils.ServerAddress;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AdminParser {

    private final Logger logger = LoggerFactory.getLogger(AdminParser.class);
    private final CommandLineParser parser = new DefaultParser();
    private final Options options = new Options();
    private final static String SERVER_ADDRESS = "DserverAddress";
    private final static String ACTION = "Daction";
    private final static String PATH = "DinPath";
    private final static String RIDE = "Dride";
    private final static String DAY = "Dday";
    private final static String CAPACITY = "Dcapacity";
    private final static int MIN_DAY = 1;
    private final static int MAX_DAY = 365;

    public AdminParser() {
        options.addRequiredOption(SERVER_ADDRESS, SERVER_ADDRESS, true, "Admin server address");
        options.addRequiredOption(ACTION, ACTION, true, "Action to perform");
        options.addOption(PATH, PATH, true, "Input file path");
        options.addOption(RIDE, RIDE, true, "Ride name");
        options.addOption(DAY, DAY, true, "Day of the year");
        options.addOption(CAPACITY, CAPACITY, true, "Capacity of the slots");
    }

    // -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -DinPath=filename | -Dride=rideName | -Dday=dayOfYear | -Dcapacity=amount ]
    public AbstractParams parse(String[] args) {
        try {
            final CommandLine cmd = parser.parse(options, args);
            final ServerAddress serverAddress = new ServerAddress(cmd.getOptionValue(SERVER_ADDRESS));
            final AdminActions action;

            if(!serverAddress.isValid()){
                System.out.println("Invalid server address");
                logger.error("Invalid server address");
                return null;
            }

            try {
                action = AdminActions.valueOf(cmd.getOptionValue(ACTION).toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid action");
                logger.error("Invalid action");
                return null;
            }

            switch (action) {
                case RIDES, TICKETS -> {
                    final String path;

                    if (cmd.hasOption(PATH)) {
                        path = cmd.getOptionValue(PATH);
                    } else {
                        System.out.println("Path is required for this action");
                        logger.error("Path is required for this action");
                        return null;
                    }

                    File file = new File(path);

                    if (!file.exists()) {
                        System.out.println("The file does not exist");
                        logger.error("The file does not exist");
                        return null;
                    }

                    if (!file.getName().endsWith(".csv")) {
                        System.out.println("The file is not a csv file");
                        logger.error("The file is not a csv file");
                        return null;
                    }

                    return new AdminParams(serverAddress, String.valueOf(action), path);

                }
                case SLOTS -> {
                    final String ride;
                    final int day;
                    final int capacity;

                    if (cmd.hasOption(DAY)) {
                        day = Integer.parseInt(cmd.getOptionValue(DAY));
                        if (day < MIN_DAY || day > MAX_DAY) {
                            System.out.println("Invalid day");
                            return null;
                        }
                    } else {
                        System.out.println("Day is required for this action");
                        logger.error("Day is required for this action");
                        return null;
                    }

                    if (cmd.hasOption(CAPACITY)) {
                        capacity = Integer.parseInt(cmd.getOptionValue(CAPACITY));
                        if (capacity < 0) {
                            System.out.println("Invalid capacity");
                            return null;
                        }
                    } else {
                        System.out.println("Capacity is required for this action");
                        logger.error("Capacity is required for this action");
                        return null;
                    }

                    if (cmd.hasOption(RIDE)) {
                        ride = cmd.getOptionValue(RIDE);
                    } else {
                        System.out.println("Ride name is required for this action");
                        logger.error("Ride name is required for this action");
                        return null;
                    }

                    return new AdminParams(serverAddress, String.valueOf(action), day, ride, capacity);
                }
            }
        } catch (ParseException e) {
            System.out.println("Error parsing command line arguments");
            logger.error("Error parsing command line arguments");
            return null;
        }
        return null;
    }
}
