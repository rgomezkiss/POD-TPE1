//package ar.edu.itba.pod.client.utils;
//
//import org.apache.commons.cli.CommandLineParser;
//import org.apache.commons.cli.DefaultParser;
//import org.apache.commons.cli.Options;
//
//public abstract class AbstractParser {
//
//    private final CommandLineParser parser = new DefaultParser();
//    private final Options options = new Options();
//    private final static String SERVER_ADDRESS = "DserverAddress";
//    private final static String ACTION = "Daction";
//    private final static int MIN_DAY = 1;
//    private final static int MAX_DAY = 365;
//
//    protected AbstractParser(){
//        options.addRequiredOption(SERVER_ADDRESS, SERVER_ADDRESS, true, "Admin server address");
//        options.addRequiredOption(ACTION, ACTION, true, "Action to perform");
//    }
//
//    public abstract AbstractParams parse(String[] args);
//}
