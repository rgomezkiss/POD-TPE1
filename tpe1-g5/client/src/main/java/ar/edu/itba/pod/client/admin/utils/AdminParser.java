package ar.edu.itba.pod.client.admin.utils;

import ar.edu.itba.pod.client.abstract_classes.AbstractParams;
import org.apache.commons.cli.*;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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

    // TODO: CHANGE
    private final static String[] actions = {"rides", "tickets", "slots"};
    private final static int MAX_DAY = 365;
    private final static int MIN_DAY = 1;

    public AdminParser() {
        options.addRequiredOption(SERVER_ADDRESS, SERVER_ADDRESS, true, "Admin server address");
        options.addRequiredOption(ACTION, ACTION, true, "Action to perform");
        options.addOption(PATH, PATH, true, "Input file path");
        options.addOption(RIDE, RIDE, true, "Ride name");
        options.addOption(DAY, DAY, true, "Day of the year");
        options.addOption(CAPACITY, CAPACITY, true, "Capacity of the slots");
    }

    public AbstractParams parse(String[] args){
        try {
            CommandLine commandLine = parser.parse(options, args);

            String serverAddress = commandLine.getOptionValue(SERVER_ADDRESS);
            String action = commandLine.getOptionValue(ACTION);
            String path = commandLine.getOptionValue(PATH);
            String ride = commandLine.getOptionValue(RIDE);

            String dayString = commandLine.getOptionValue(DAY);

            Integer capacity = Integer.parseInt(commandLine.getOptionValue(CAPACITY));
            Integer day = Integer.parseInt(commandLine.getOptionValue(DAY));

            // TODO: Ver de hacer un AdminActionsEnum o... algo abstracto
            if (Arrays.stream(actions).noneMatch(a -> a.equals(action))) {
                System.out.println("Invalid action for admin-cli");
                return null;
            }

            if (action.equals(actions[2])) {
                if (day < MIN_DAY || day > MAX_DAY) {
                    System.out.println("Invalid day");
                    return null;
                }
                if (capacity < 0) {
                    System.out.println("Invalid capacity");
                    return null;
                }

                return new AdminParams(serverAddress, action, day, ride, capacity);
            }

            Path pathToCheck = Paths.get(path);

            if (pathToCheck.toFile().exists()) {
                // TODO
                // Parsear el archivo según el comando
                // Dividir en casos para acciones rides y passes...

                // retornar una instancia valida de params
                return new AdminParams(serverAddress, action, day, path);

            } else {
                System.out.println("Invalid filepath");
                return null;
            }
        } catch(ParseException e) {
            logger.error("Error parsing command line arguments");
            return null;
        }
    }
}
