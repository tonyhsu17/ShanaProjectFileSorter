package org.tonyhsu17;

import org.tonyhsu17.utilities.commandline.Parameter;



/**
 * Command line options available for use.
 * 
 * @author Tony Hsu
 *
 */
public class Params {
    public static final Parameter S = new Parameter("s", true, "source path");
    public static final Parameter D = new Parameter("d", true, "destination path");
    public static final Parameter U = new Parameter("u", true, "shanaproject url");
    public static final Parameter T = new Parameter("t", true, "cron time");
    public static final Parameter VERBOSE = new Parameter("verbose", false, "output logging");
    public static final Parameter ONCE = new Parameter("once", false, "run single time");

    public static Parameter[] getParams() {
        return new Parameter[] {S, D, U, T, VERBOSE, ONCE};
    }
}
