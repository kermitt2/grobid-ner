package org.grobid.trainer;

import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidNerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.apache.commons.lang3.StringUtils;

/**
 * Training application for training a target model.
 *
 * @author Patrice Lopez
 */
public class NERTrainerRunner {
    private static Logger LOGGER = LoggerFactory.getLogger(NERTrainerRunner.class);

    private enum RunType {
        TRAIN, EVAL, SPLIT, EVAL_N_FOLD;

        public static RunType getRunType(int i) {
            for (RunType t : values()) {
                if (t.ordinal() == i) {
                    return t;
                }
            }

            throw new IllegalStateException("Unsupported RunType with ordinal " + i);
        }
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalStateException(
                    "Usage: {0 - train, 1 - evaluate, 2 - split, train and evaluate, 3 - n-fold evaluation} {ner,nerfr,nersense} -gH /path/to/Grobid/home -s { [0.0 - 1.0] - split ratio, optional} -n {[int, num folds for n-fold evaluation, optional]");
        }

        RunType mode = RunType.getRunType(Integer.parseInt(args[0]));
        if ((mode == RunType.SPLIT || mode == RunType.EVAL_N_FOLD) && (args.length < 6)) {
            throw new IllegalStateException(
                    "Usage: {0 - train, 1 - evaluate, 2 - split, train and evaluate, 3 - n-fold evaluation} {ner,nerfr,nersense} -gH /path/to/Grobid/home -s { [0.0 - 1.0] - split ratio, optional} -n {[int, num folds for n-fold evaluation, optional]");
        }

        String path2GbdHome = null;
        Double split = 0.0;
        int numFolds = 0;
        GrobidHomeFinder grobidHomeFinder = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-gH")) {
                path2GbdHome = args[i + 1];
                grobidHomeFinder = new GrobidHomeFinder(Arrays.asList(path2GbdHome));
                grobidHomeFinder.findGrobidHomeOrFail();
            } else if (args[i].equals("-s")) {
                if (i + 1 == args.length) {
                    throw new IllegalStateException("Missing split ratio value. ");
                }
                String splitRatio = args[i + 1];
                try {
                    split = Double.parseDouble(args[i + 1]);
                } catch (Exception e) {
                    throw new IllegalStateException("Invalid split value: " + args[i + 1]);
                }

            } else if (args[i].equals("-n")) {
                if (i + 1 == args.length) {
                    throw new IllegalStateException("Missing number of folds value. ");
                }
                try {
                    numFolds = Integer.parseInt(args[i + 1]);
                } catch (Exception e) {
                    throw new IllegalStateException("Invalid number of folds value: " + args[i + 1]);
                }
            }
        }

        if (path2GbdHome == null) {
            throw new IllegalStateException(
                    "Usage: {0 - train, 1 - evaluate, 2 - split, train and evaluate} {ner,nerfr,nersense} -gH /path/to/Grobid/home -s { [0.0 - 1.0] - split ratio, optional} -n {[int, num folds for n-fold evaluation, optional]");
        }

        System.out.println(grobidHomeFinder);
        GrobidProperties.getInstance(grobidHomeFinder);

        String model = args[1];

        AbstractTrainer trainer;

        if (model.equals("ner")) {
            trainer = new NERTrainer();
        } else if (model.equals("nerfr")) {
            trainer = new NERFrenchTrainer();
        } else if (model.equals("nersense")) {
            trainer = new SenseTrainer();
        } else {
            throw new IllegalStateException("The model " + model + " is unknown.");
        }

        switch (mode) {
            case TRAIN:
                AbstractTrainer.runTraining(trainer);
                break;
            case EVAL:
                System.out.println(AbstractTrainer.runEvaluation(trainer));
                break;
            case SPLIT:
                System.out.println(AbstractTrainer.runSplitTrainingEvaluation(trainer, split));
                break;
            case EVAL_N_FOLD:
                if(numFolds == 0) {
                    throw new IllegalArgumentException("N should be > 0");
                }
                String results = AbstractTrainer.runNFoldEvaluation(trainer, numFolds);
                System.out.println(results);
                break;
            default:
                throw new IllegalStateException("Invalid RunType: " + mode.name());
        }
    }
}