package ar.edu.itba.pod.client.booking.utils;

import ar.edu.itba.pod.client.booking.actions.BookingActions;
import ar.edu.itba.pod.client.utils.ServerAddress;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookingParser {

    private final Logger logger = LoggerFactory.getLogger(BookingParser.class);
    private final CommandLineParser parser = new DefaultParser();
    private final Options options = new Options();
    private final static String SERVER_ADDRESS = "DserverAddress";
    private final static String ACTION = "Daction";
    private final static String DAY = "Dday";
    private final static String RIDE = "Dride";
    private final static String VISITOR = "Dvisitor";
    private final static String SLOT = "Dslot";
    private final static String SLOT_TO = "DslotTo";

    public BookingParser() {
        options.addRequiredOption(SERVER_ADDRESS, SERVER_ADDRESS, true, "Admin server address");
        options.addRequiredOption(ACTION, ACTION, true, "Action to perform");
        options.addOption(DAY, DAY, true, "Day of the year");
        options.addOption(RIDE, RIDE, true, "Ride name");
        options.addOption(VISITOR, VISITOR, true, "Visitor ID");
        options.addOption(SLOT, SLOT, true, "Start slot");
        options.addOption(SLOT_TO, SLOT_TO, true, "End slot");
    }

    // -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -Dday=dayOfYear -Dride=rideName -Dvisitor=visitorId -Dslot=bookingSlot -DslotTo=bookingSlotTo ]
    public BookingParams parse(String[] args) {
        try {
            final CommandLine cmd = parser.parse(options, args);
            final ServerAddress serverAddress = new ServerAddress(cmd.getOptionValue(SERVER_ADDRESS));
            final BookingActions action;

            if (!serverAddress.isValid()) {
                System.out.println("Invalid server address");
                logger.error("Invalid server address");
                return null;
            }

            try {
                action = BookingActions.valueOf(cmd.getOptionValue(ACTION).toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid action");
                logger.error("Invalid action");
                return null;
            }

            switch (action) {
                case ATTRACTIONS -> {
                    return new BookingParams(serverAddress, action.name());
                }
                case AVAILABILITY -> {
                    int day;
                    String slot;
                    String slotTo = null;
                    String ride = null;

                    if (cmd.hasOption(DAY)) {
                        day = Integer.parseInt(cmd.getOptionValue(DAY));
                    } else {
                        System.out.println("Day is required for this action");
                        logger.error("Day is required for this action");
                        return null;
                    }

                    if (cmd.hasOption(SLOT)) {
                        slot = cmd.getOptionValue(SLOT);
                    } else {
                        System.out.println("Start slot is required for this action");
                        logger.error("Start slot is required for this action");
                        return null;
                    }

                    if (cmd.hasOption(RIDE)) {
                        ride = cmd.getOptionValue(RIDE);
                    }
                    if (cmd.hasOption(SLOT_TO)) {
                        slotTo = cmd.getOptionValue(SLOT_TO);
                    }

                    return new BookingParams(serverAddress, String.valueOf(action), day, ride, slot, slotTo, null);
                }
                case BOOK, CONFIRM, CANCEL -> {
                    final int day;
                    final String slot;
                    final String ride;
                    final String visitorId;

                    if (cmd.hasOption(DAY)) {
                        day = Integer.parseInt(cmd.getOptionValue(DAY));
                    } else {
                        System.out.println("Day is required for this action");
                        logger.error("Day is required for this action");
                        return null;
                    }

                    if (cmd.hasOption(SLOT)) {
                        slot = cmd.getOptionValue(SLOT);
                    } else {
                        System.out.println("Start slot is required for this action");
                        logger.error("Start slot is required for this action");
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
                        System.out.println("Visitor ID is required for this action");
                        logger.error("Visitor ID is required for this action");
                        return null;
                    }

                    return new BookingParams(serverAddress, String.valueOf(action), day, ride, slot, visitorId);
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
