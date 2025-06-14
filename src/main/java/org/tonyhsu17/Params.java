package org.tonyhsu17;

import org.tonyhsu17.utilities.commandline.Parameter;



/**
 * Command line options available for use.
 *
 * @author Tony Hsu
 */
public class Params {
    public static final Parameter S = new Parameter("s", "src", true, "source path");
    public static final Parameter D = new Parameter("d", "des", true, "destination path");
    public static final Parameter U = new Parameter("u", "url", true, "https://www.shanaproject.com/USERNAME");
    public static final Parameter T = new Parameter("t", "time", true, "cron time in sec");
    public static final Parameter VERBOSE = new Parameter("v", "verbose", false, "output logging");
    public static final Parameter C = new Parameter("c", "cron", false, "activate cron");
    public static final Parameter SIZE = new Parameter("l", "logSize", true, "max history size");

    public static Parameter[] params = {S, D, U, T, VERBOSE, C, SIZE};
}
