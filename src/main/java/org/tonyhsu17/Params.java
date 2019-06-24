package org.tonyhsu17;

import org.tonyhsu17.utilities.commandline.Parameter;



/**
 * Command line options available for use.
 *
 * @author Tony Hsu
 */
public class Params {
    public static final Parameter S = new Parameter("s", true, "source path", true);
    public static final Parameter D = new Parameter("d", true, "destination path", true);
    public static final Parameter U = new Parameter("u", true, "shanaproject url", true);
    public static final Parameter T = new Parameter("t", true, "cron time");
    public static final Parameter VERBOSE = new Parameter("v", "verbose", false, "output logging");
    public static final Parameter ONCE = new Parameter("once", false, "run single time");
    public static final Parameter SIZE = new Parameter("l", "logSize", true, "max history size");

    public static Parameter[] params = {S, D, U, T, VERBOSE, ONCE, SIZE};
}
