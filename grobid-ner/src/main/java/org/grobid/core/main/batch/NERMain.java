package org.grobid.core.main.batch;

import org.grobid.core.engines.NERParsers;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.trainer.CorporaAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.grobid.trainer.CorporaAssembler.IDILLIA;

public class NERMain {
    private static Logger LOGGER = LoggerFactory.getLogger(NERMain.class);

    private static final String COMMAND_CREATE_TRAINING_NER = "createTrainingNER";
    private static final String COMMAND_CREATE_TRAINING_SENSE = "createTrainingSense";
    private static final String COMMAND_CREATE_TRAINING_IDILIA = "createTrainingIDILIA";

    private static List<String> availableCommands = Arrays.asList(
            COMMAND_CREATE_TRAINING_NER,
            COMMAND_CREATE_TRAINING_SENSE,
            COMMAND_CREATE_TRAINING_IDILIA
    );

    private static GrobidNERMainArgs grobidArguments;

    /**
     * Build the path to grobid.properties from the path to grobid-home.
     */
    protected final static String getPath2GbdProperties(final String pPath2GbdHome) {
        return pPath2GbdHome + File.separator + "config" + File.separator + "grobid.properties";
    }

    /**
     * Init process with the provided grobid-home
     */
    protected static void initProcess(String grobidHome) {
        try {
            final GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList(grobidHome));
            grobidHomeFinder.findGrobidHomeOrFail();
            GrobidProperties.getInstance(grobidHomeFinder);
            LibraryLoader.load();
        } catch (final Exception exp) {
            System.err.println("Grobid initialisation failed: " + exp);
        }
    }

    /**
     * Init process with the default value of the grobid home
     */
    protected static void initProcess() {
        try {
            LibraryLoader.load();
        } catch (final Exception exp) {
            System.err.println("Grobid initialisation failed: " + exp);
        }
        GrobidProperties.getInstance();
    }

    /**
     * @return String to display for help.
     */
    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP GROBID\n");
        help.append("-h: displays help\n");
        help.append("-gH: gives the path to grobid home directory.\n");
        help.append("-dIn: gives the path to the directory where the files to be processed are located, to be used only when the called method needs it.\n");
        help.append("-dOut: gives the path to the directory where the result files will be saved. The default output directory is the curent directory.\n");
        help.append("-s: is the parameter used for process using string as input and not file.\n");
        help.append("-r: recursive directory processing, default processing is not recursive.\n");
        help.append("-l: language to be used, as ISO code (e.g. [en, fr]).\n");
        help.append("-exe: gives the command to execute. The value should be one of these:\n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }

    /**
     * Process batch given the args.
     *
     * @param pArgs The arguments given to the batch.
     */
    protected static boolean processArgs(final String[] pArgs) {
        boolean result = true;
        if (pArgs.length == 0) {
            System.out.println(getHelp());
            result = false;
        } else {
            String currArg;
            for (int i = 0; i < pArgs.length; i++) {
                currArg = pArgs[i];
                if (currArg.equals("-h")) {
                    System.out.println(getHelp());
                    result = false;
                    break;
                }
                if (currArg.equals("-gH")) {
                    grobidArguments.setPath2grobidHome(pArgs[i + 1]);
                    if (pArgs[i + 1] != null) {
                        grobidArguments.setPath2grobidProperty(getPath2GbdProperties(pArgs[i + 1]));
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-dIn")) {
                    if (pArgs[i + 1] != null) {
                        grobidArguments.setPath2Input(pArgs[i + 1]);
                        grobidArguments.setPdf(true);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-s")) {
                    if (pArgs[i + 1] != null) {
                        grobidArguments.setInput(pArgs[i + 1]);
                        grobidArguments.setPdf(false);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-l")) {
                    if (pArgs[i + 1] != null) {
                        grobidArguments.setLang(pArgs[i + 1]);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-dOut")) {
                    if (pArgs[i + 1] != null) {
                        grobidArguments.setPath2Output(pArgs[i + 1]);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-exe")) {
                    final String command = pArgs[i + 1];
                    if (availableCommands.contains(command)) {
                        grobidArguments.setProcessMethodName(command);
                        i++;
                        continue;
                    } else {
                        System.err.println("-exe value should be one value from this list: " + availableCommands);
                        result = false;
                        break;
                    }
                }
                if (currArg.equals("-r")) {
                    grobidArguments.setRecursive(true);
                    continue;
                }
            }
        }
        return result;
    }

    public static void main(final String[] args) throws Exception {
        grobidArguments = new GrobidNERMainArgs();

        if (processArgs(args) && (grobidArguments.getProcessMethodName() != null)) {

            if (isNotEmpty(grobidArguments.getPath2grobidHome())) {
                initProcess(grobidArguments.getPath2grobidHome());
            } else {
                LOGGER.warn("Grobid home not provided, using default. ");
                initProcess();
            }

            int nb = 0;

            long time = System.currentTimeMillis();

            if (grobidArguments.getProcessMethodName().equals(COMMAND_CREATE_TRAINING_NER)) {
                NERParsers nerParsers = new NERParsers();
                nb = nerParsers.createTrainingBatch(grobidArguments.getPath2Input(), grobidArguments.getPath2Output(), grobidArguments.getLang());
                LOGGER.info(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            } else if (grobidArguments.getProcessMethodName().equals(COMMAND_CREATE_TRAINING_SENSE)) {
                throw new RuntimeException("Not yet implemented. ");

            } else if (grobidArguments.getProcessMethodName().equals(COMMAND_CREATE_TRAINING_IDILIA)) {
                String outputDirectory = grobidArguments.getPath2Output();
                if(isEmpty(outputDirectory)) {
                    LOGGER.warn("No output specified");
                    System.out.println(getHelp());
                    System.exit(-1);
                }

                CorporaAssembler assembler = new CorporaAssembler();
                assembler.assemble(IDILLIA, outputDirectory);
                LOGGER.info(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            } else {
                System.out.println("No command supplied.");
                System.out.println(getHelp());
            }
        }
    }
}
