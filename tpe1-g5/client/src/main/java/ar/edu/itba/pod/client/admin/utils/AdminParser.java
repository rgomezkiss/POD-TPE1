package ar.edu.itba.pod.client.admin.utils;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public AdminParser() {
        options.addRequiredOption(SERVER_ADDRESS, SERVER_ADDRESS, true, "Admin server address");
        options.addRequiredOption(ACTION, ACTION, true, "Action to perform");
        options.addOption(PATH, PATH, true, "Input file path");
        options.addOption(RIDE, RIDE, true, "Ride name");
        options.addOption(DAY, DAY, true, "Day of the year");
        options.addOption(CAPACITY, CAPACITY, true, "Capacity of the slots");
    }

    //TODO ver como parsear

    private CommandLine parse(String[] args){
        try {
            return parser.parse(options, args);
        } catch(ParseException e) {
            logger.error("Error parsing command line arguments");
            return null;
        }
    }

}
