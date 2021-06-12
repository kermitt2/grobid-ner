package org.grobid.core.main.batch;

import org.grobid.core.engines.NERParsers;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidNerConfiguration;
import org.grobid.core.utilities.GrobidConfig.ModelParameters;
import org.grobid.trainer.AssembleNERCorpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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

    /**
     * Arguments of the batch.
     */
    private static GrobidNERMainArgs gbdArgs;

    /**
     * Build the path to grobid.properties from the path to grobid-home.
     *
     * @param pPath2GbdHome The path to Grobid home.
     * @return the path to grobid.properties.
     */
    protected final static String getPath2GbdProperties(final String pPath2GbdHome) {
        return pPath2GbdHome + File.separator + "config" + File.separator + "grobid.properties";
    }

    /**
     * Init process with the provided grobid-home or  default value of the grobid home
     *
     * @param grobidHome
     */
    protected static void initProcess(String grobidHome) {
        try {
            GrobidNerConfiguration grobidNerConfiguration = null;
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                grobidNerConfiguration = mapper.readValue(new File("resources/config/grobid-ner.yaml"), GrobidNerConfiguration.class);
            } catch(Exception e) {
                LOGGER.error("The config file does not appear valid, see resources/config/grobid-astro.yaml", e);
            }

            if (grobidHome != null) {
                final GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList(grobidHome));
                grobidHomeFinder.findGrobidHomeOrFail();
                GrobidProperties.getInstance(grobidHomeFinder);
            } else {
                final GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList(grobidNerConfiguration.getGrobidHome()));
                grobidHomeFinder.findGrobidHomeOrFail();
                GrobidProperties.getInstance(grobidHomeFinder);
            }

            for (ModelParameters theModel : grobidNerConfiguration.getModels())
                GrobidProperties.getInstance().addModel(theModel);

            LibraryLoader.load();
        } catch (final Exception exp) {
            System.err.println("Grobid initialisation failed: " + exp);
        }
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
                    gbdArgs.setPath2grobidHome(pArgs[i + 1]);
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setPath2grobidProperty(getPath2GbdProperties(pArgs[i + 1]));
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-dIn")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setPath2Input(pArgs[i + 1]);
                        gbdArgs.setPdf(true);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-s")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setInput(pArgs[i + 1]);
                        gbdArgs.setPdf(false);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-l")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setLang(pArgs[i + 1]);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-dOut")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setPath2Output(pArgs[i + 1]);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-exe")) {
                    final String command = pArgs[i + 1];
                    if (availableCommands.contains(command)) {
                        gbdArgs.setProcessMethodName(command);
                        i++;
                        continue;
                    } else {
                        System.err.println("-exe value should be one value from this list: " + availableCommands);
                        result = false;
                        break;
                    }
                }
                if (currArg.equals("-r")) {
                    gbdArgs.setRecursive(true);
                    continue;
                }
            }
        }
        return result;
    }

    public static void main(final String[] args) throws Exception {
        gbdArgs = new GrobidNERMainArgs();

        if (processArgs(args) && (gbdArgs.getProcessMethodName() != null)) {
            if (isNotEmpty(gbdArgs.getPath2grobidHome())) {
                initProcess(gbdArgs.getPath2grobidHome());
            } else {
                LOGGER.warn("Grobid home not provided, using default. ");
                initProcess(null);
            }
            int nb = 0;

            long time = System.currentTimeMillis();

            if (gbdArgs.getProcessMethodName().equals(COMMAND_CREATE_TRAINING_NER)) {
                NERParsers nerParsers = new NERParsers();
                nb = nerParsers.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), gbdArgs.getLang());
                LOGGER.info(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            } else if (gbdArgs.getProcessMethodName().equals(COMMAND_CREATE_TRAINING_SENSE)) {
                throw new RuntimeException("Not yet implemented. ");

            } else if (gbdArgs.getProcessMethodName().equals(COMMAND_CREATE_TRAINING_IDILIA)) {
                new AssembleNERCorpus().assembleWikipedia(gbdArgs.getPath2Output());
                LOGGER.info(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            } else {
                System.out.println("No command supplied.");
                System.out.println(getHelp());
            }
        }
    }
}
