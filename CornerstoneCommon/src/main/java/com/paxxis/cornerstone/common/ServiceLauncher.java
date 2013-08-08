package com.paxxis.cornerstone.common;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The entry point for launching this service.
 * 
 * @author Rob Englander
 *
 */
public class ServiceLauncher {

    private static final Logger LOGGER = Logger.getLogger(ServiceLauncher.class);

    private CommandLine commandLine = null;
    private Options options = null;

    public static void main(String[] args) {
        new ServiceLauncher(args);
    }

    public ServiceLauncher(String[] args) {
        options = initialize(args);
        CommandLineParser parser = new PosixParser();
        try {
            commandLine = parser.parse(options, args, false);
            process();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            LOGGER.error(e);
			System.exit(1);
        }

    }

    private Options initialize(String[] args) {
        OptionGroup group = new OptionGroup();

        Option opt = new Option("cf", "contextfile", true, "the context file to load");
        opt.setArgs(1);
        group.addOption(opt);

        opt = new Option("pf", "pidfile", true, "the pid file to write");
        opt.setArgs(1);
        group.addOption(opt);

        Options options = new Options();
        options.addOptionGroup(group);
        return options;
    }

    protected void process() throws Exception {

        if (commandLine.getOptions().length == 0) {
            printHelp();
        } else {
            processContextFile(commandLine);
        }
    }

    private void doContextFile(String fileName) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fileName);
        ctx.registerShutdownHook();
    }

    private boolean processContextFile(CommandLine cmd) throws Exception {
        String[] vals = cmd.getOptionValues("cf");
        if (vals != null) {
            if (vals.length != 1) {
				throw new ParseException("Wrong number of argument for context file");
            }

            doContextFile(vals[0]);
            return true;
        }

        return false; 
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java " + this.getClass().getName(), options);
    }
}