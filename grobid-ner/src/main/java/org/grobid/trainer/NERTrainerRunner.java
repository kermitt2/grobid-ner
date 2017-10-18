package org.grobid.trainer;

import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Training application for training a target model.
 * 
 * @author Patrice Lopez
 */
public class NERTrainerRunner {
	private static Logger LOGGER = LoggerFactory.getLogger(NERTrainerRunner.class);

	enum RunType {
		TRAIN, EVAL, SPLIT;

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
					"Usage: {0 - train, 1 - evaluate, 2 - split, train and evaluate} {ner,nerfr,nersense} -gH /path/to/Grobid/home -s { [0.0 - 1.0] - split ratio, optional}");
		}

		RunType mode = RunType.getRunType(Integer.parseInt(args[0]));
		if ( (mode == RunType.SPLIT) && (args.length < 6) ) {
			throw new IllegalStateException(
					"Usage: {0 - train, 1 - evaluate, 2 - split, train and evaluate} {ner,nerfr,nersense} -gH /path/to/Grobid/home -s { [0.0 - 1.0] - split ratio, optional}");
		}

		String path2GbdHome = null;
		Double split = 0.0;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-gH")) {
				if (i+1 == args.length) {
					throw new IllegalStateException("Missing path to Grobid home. ");
				}
				path2GbdHome = args[i + 1];
			}
			else if (args[i].equals("-s")) {
				if (i+1 == args.length) {
					throw new IllegalStateException("Missing split ratio value. ");
				}
				String splitRatio = args[i + 1];
				try {					
					split = Double.parseDouble(args[i + 1]);
				}
				catch(Exception e) {
					throw new IllegalStateException("Invalid split value: " + args[i + 1]);
				}
				
			}
		}

		if (path2GbdHome == null) {
			throw new IllegalStateException(
					"Usage: {0 - train, 1 - evaluate, 2 - split, train and evaluate} {ner,nerfr,nersense} -gH /path/to/Grobid/home -s { [0.0 - 1.0] - split ratio, optional}");
		}

		final String path2GbdProperties = path2GbdHome + File.separator + "config" + File.separator + "grobid.properties";

		System.out.println("path2GbdHome=" + path2GbdHome + "   path2GbdProperties=" + path2GbdProperties);

		GrobidProperties.getInstance();

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
			AbstractTrainer.runEvaluation(trainer);
			break;
		case SPLIT:
			AbstractTrainer.runSplitTrainingEvaluation(trainer, split);
			break;
		default:
			throw new IllegalStateException("Invalid RunType: " + mode.name());
		}
	}
}