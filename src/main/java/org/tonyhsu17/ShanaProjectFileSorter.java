package org.tonyhsu17;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.tonyhsu17.utilities.Logger;
import org.tonyhsu17.utilities.commandline.CommandLineArgs;
import org.tonyhsu17.utilities.commandline.Parameter;

import java.io.IOException;
import java.util.Optional;



/**
 * Sorts anime files into Season Year/Series based on your followed list on ShanaProject.
 * 
 * @author Tony Hsu
 *
 */
public class ShanaProjectFileSorter implements Logger {
    private RunHeadlessMode headless;
    private int cronInterval;
    private boolean hasCron;
    
    public static void main(String[] args) throws IOException, ParseException {
        new ShanaProjectFileSorter(args).run(); 
    }

    /**
     * Sorts anime files into Season Year/Series based on your followed list on ShanaProject.
     * 
     * @param args Arguments to pass in
     * @throws NumberFormatException
     * @throws IOException
     * @throws ParseException
     */
    public ShanaProjectFileSorter(String[] args) throws IOException, ParseException {
        try {
            CommandLine cmd = CommandLineArgs.getCommandLine(Params.params, args);
            String url = getOptionValue(cmd, Params.U, "SP_URL", "");
            String src = getOptionValue(cmd, Params.S, "SP_SRC", "");
            String dest = getOptionValue(cmd, Params.D, "SP_DES", "");
            hasCron = !cmd.hasOption(Params.ONCE.opt()) || Boolean.parseBoolean(getOptionValue(cmd, Params.VERBOSE, "RSS_USE_CRON", "false"));
            cronInterval = Integer.parseInt(getOptionValue(cmd, Params.T, "SP_CRON_INTERVAL", "10"));
            int logSize = Integer.parseInt(getOptionValue(cmd, Params.SIZE, "SP_LOG_SIZE", "1000000"));

            if(!url.isEmpty() && !src.isEmpty() && !dest.isEmpty()) {
                headless = new RunHeadlessMode(src, dest, url, logSize);
            }
        } catch (ParseException | NumberFormatException e) {
            CommandLineArgs.printHelp("ShanaProjectFileSorter.java", Params.params);
            System.exit(0);
        }
    }

    public static String getOptionValue(CommandLine cmd, Parameter param, String sysEnvKey, String defaultValue) {
        return Optional.ofNullable(cmd.getOptionValue(param.opt())).
            orElse(Optional.ofNullable(cmd.getOptionValue(param.longOpt()))
                .orElse(Optional.ofNullable(System.getenv(sysEnvKey)).orElse(defaultValue)));
    }

    /**
     * Starts the file sorting.
     * 
     * @throws IOException
     */
    public void run() throws IOException {
        if(!hasCron) {
            headless.run();
        }
        else {
            while(true) {
                headless.run();
                try {
                    Thread.sleep(cronInterval);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
