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
    private int cronTime;
    private boolean runOnce;
    
    public static void main(String[] args) throws IOException {
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
    public ShanaProjectFileSorter(String[] args) throws IOException  {
        runOnce = false;
        cronTime = 0;
        CommandLine cmd;
        try {
            cmd = CommandLineArgs.getCommandLine(Params.params, args);
            if(hasOption(cmd, Params.S) &&
               hasOption(cmd, Params.D) &&
               hasOption(cmd, Params.U)) {
                headless = new RunHeadlessMode(cmd.getOptionValue(Params.S.opt()),
                    cmd.getOptionValue(Params.D.opt()),
                    cmd.getOptionValue(Params.U.opt()),
                    hasOption(cmd, Params.SIZE) ? Optional.ofNullable(Integer.valueOf(getOptionValue(cmd, Params.SIZE))).orElse(-1) : -1);
            }

            info("Running...");
            if(cmd.hasOption(Params.ONCE.opt())) {
                runOnce = true;
            }
            else if(cmd.hasOption(Params.T.opt())) {
                cronTime = Integer.parseInt(cmd.getOptionValue(Params.T.opt())) * 1000 * 60;
            }
            else {
                error("Failed to provide -t or -once");

                System.exit(0);
            }
        } catch (ParseException | NumberFormatException e) {
            CommandLineArgs.printHelp("ShanaProjectFileSorter.java", Params.params);
        }
    }

    public static boolean hasOption(CommandLine line, Parameter param) {
        return line.hasOption(param.opt()) || line.hasOption(param.longOpt());
    }

    public static String getOptionValue(CommandLine line, Parameter param) {
        String value = line.getOptionValue(param.opt());
        if(value != null) {
            return value;
        } else {
            return line.getOptionValue(param.longOpt());
        }
    }


    /**
     * Starts the file sorting.
     * 
     * @throws IOException
     */
    public void run() throws IOException {
        if(runOnce) {
            headless.run();
        }
        else {
            while(true) {
                headless.run();
                try {
                    Thread.sleep(cronTime);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
