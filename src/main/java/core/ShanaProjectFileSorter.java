package core;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.tonyhsu17.utilities.Logger;
import org.tonyhsu17.utilities.commandline.CommandLineArgs;



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
    
    public static void main(String[] args) throws NumberFormatException, IOException, ParseException {
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
    public ShanaProjectFileSorter(String[] args) throws NumberFormatException, IOException, ParseException {
        runOnce = false;
        cronTime = 0;
        CommandLine cmd = CommandLineArgs.getCommandLine(Params.getParams(), args);

        if(cmd.hasOption(Params.S.opt()) &&
           cmd.hasOption(Params.D.opt()) &&
           cmd.hasOption(Params.U.opt())) {
            headless = new RunHeadlessMode(cmd.getOptionValue(Params.S.opt()),
                cmd.getOptionValue(Params.D.opt()),
                cmd.getOptionValue(Params.U.opt()));
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
