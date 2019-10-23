package org.grobid.trainer;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeaturesVectorNER;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.trainer.stax.INRIALeMondeCorpusStaxHandler;
import org.grobid.trainer.stax.StaxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Create the French NER tagging model
 *
 * @author Patrice Lopez
 */
public class NERFrenchTrainer extends AbstractTrainer {

    private static Logger LOGGER = LoggerFactory.getLogger(NERFrenchTrainer.class);

    private NERLexicon nerLexicon = NERLexicon.getInstance();
    protected Lexicon lexicon = Lexicon.getInstance();

    private String leMondeCorpusPath = null;

    public NERFrenchTrainer() {
        super(GrobidModels.ENTITIES_NERFR);

        // adjusting CRF training parameters for this model
        epsilon = 0.000001;
        window = 20;
        nbMaxIterations = 1000;

        // read additional properties for this sub-project to get the paths to the resources
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("src/main/resources/grobid-ner.properties");

            // load the properties file
            prop.load(input);

            // get the property value
            leMondeCorpusPath = prop.getProperty("grobid.ner.leMondeCorpus.path");
        } catch (IOException ex) {
            throw new GrobidResourceException(
                    "An exception occured when accessing/reading the grobid-ner property file.", ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    /**
     * Add the selected features to the training data for the NER model
     */
    public int createCRFPPData(File sourcePathLabel,
                               File outputPath) {
        return createCRFPPData(sourcePathLabel, outputPath, null, 1.0);
    }

    /**
     * Add the selected features to a NER example set
     *
     * @param corpusDir          a path where corpus files are located
     * @param trainingOutputPath path where to store the temporary training data
     * @param evalOutputPath     path where to store the temporary evaluation data
     * @param splitRatio         ratio to consider for separating training and evaluation data, e.g. 0.8 for 80%
     * @return the total number of used corpus items
     */
    @Override
    public int createCRFPPData(final File corpusDir,
                               final File trainingOutputPath,
                               final File evalOutputPath,
                               double splitRatio) {
        int totalExamples = 0;
        try {
            System.out.println("sourcePathLabel: " + corpusDir);
            if (trainingOutputPath != null)
                System.out.println("outputPath for training data: " + trainingOutputPath);
            if (evalOutputPath != null)
                System.out.println("outputPath for evaluation data: " + evalOutputPath);

            // the file for writing the training data
            OutputStream os2 = null;
            Writer writer2 = null;
            if (trainingOutputPath != null) {
                os2 = new FileOutputStream(trainingOutputPath);
                writer2 = new OutputStreamWriter(os2, "UTF8");
            }

            // the file for writing the evaluation data
            OutputStream os3 = null;
            Writer writer3 = null;
            if (evalOutputPath != null) {
                os3 = new FileOutputStream(evalOutputPath);
                writer3 = new OutputStreamWriter(os3, "UTF8");
            }

            File corpusLeMondeDir = new File(leMondeCorpusPath);
            if (!corpusLeMondeDir.exists()) {
                LOGGER.warn("Directory does not exist: " + leMondeCorpusPath);
            }
            File[] files = corpusLeMondeDir.listFiles();
            if ((files == null) || (files.length == 0)) {
                LOGGER.warn("No files in directory: " + corpusLeMondeDir);
            }

            // process the core trainig set corresponding to LeMonde corpus first
            for (int i = 0; i < files.length; i++) {
                System.out.println(files[i].getName());
                if (files[i].getName().indexOf(".xml") != -1)
                    totalExamples += processLeMonde(files[i], writer2, writer3, splitRatio);
            }

            if (writer2 != null) {
                writer2.close();
            }
            if (os2 != null) {
                os2.close();
            }

            if (writer3 != null) {
                writer3.close();
            }
            if (os3 != null) {
                os3.close();
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return totalExamples;
    }


    private int processLeMonde(final File corpusFile, Writer writerTraining, Writer writerEvaluation, double splitRatio) {
        int res = 0;
        try {
            // the "core" set is a the set of files corresponding to Lemonde corpus, but with
            // the default class annotations manually corrected
            System.out.println("Path to French corpus CoNLL training: " + corpusFile.getPath());
            if (!corpusFile.exists()) {
                throw new
                        GrobidException("Cannot start training, because corpus resource file is not correctly set : "
                        + corpusFile.getPath());
            }

            WstxInputFactory inputFactory = new WstxInputFactory();
            Writer writer = new StringWriter();
            INRIALeMondeCorpusStaxHandler target = new INRIALeMondeCorpusStaxHandler(writer);

            InputStream is = new FileInputStream(corpusFile);
            XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);
            StaxUtils.traverse(reader, target);

            String output = writer.toString();
            String[] lines = output.split("\n");
            writer = null;

			// to store unit term positions
            List<OffsetPosition> locationPositions = null;
            List<OffsetPosition> personTitlePositions = null;
            List<OffsetPosition> organisationPositions = null;
			List<OffsetPosition> orgFormPositions = null;

			List<LayoutToken> tokens =new ArrayList<LayoutToken>();
			List<String> labels = new ArrayList<String>();
			String line = null;
			for(int i = 0; i<lines.length; i++) {
				line = lines[i].trim();
//System.out.println(line);
                // note that we work at sentence level
                if (line.startsWith("-DOCSTART-") || line.startsWith("-X-")) {
                    // the balance of data between training and evaluation is realised
                    // at document level
                    if ((writerTraining == null) && (writerEvaluation != null))
                        writer = writerEvaluation;
                    if ((writerTraining != null) && (writerEvaluation == null))
                        writer = writerTraining;
                    else {
                        if (Math.random() <= splitRatio)
                            writer = writerTraining;
                        else
                            writer = writerEvaluation;
                    }
                    continue;
                }

                // in the this line, we only keep what we are interested in for this model
                int ind = line.indexOf("\t");
                if (ind != -1) {
                    ind = line.indexOf("\t", ind + 1);
                    if (ind != -1)
                        line = line.substring(0, ind);
                }
                //System.out.println(line);

				if (line.trim().length() == 0 && tokens.size() > 0)  {                 
					// sentence is complete
					/*LayoutToken token = new LayoutToken("\n");
					tokens.add(token);
					labels.add(null);*/

                    locationPositions = lexicon.tokenPositionsLocationNames(tokens);
                    personTitlePositions = lexicon.tokenPositionsPersonTitle(tokens);
                    organisationPositions = lexicon.tokenPositionsOrganisationNames(tokens);
                    orgFormPositions = lexicon.tokenPositionsOrgForm(tokens);
                    addFeatures(tokens, labels, writer,
                        locationPositions, personTitlePositions, organisationPositions, orgFormPositions);
                    writer.write("\n");

                    tokens = new ArrayList<LayoutToken>();
                    labels = new ArrayList<String>();
					res++;
				} else {
					String pieces[] = line.split("\t");
					if (pieces.length != 2)
						continue;
					// check if previous label is different from new line - if yes add a space
					/*if (!tokens.get(tokens.size()-1).getText().equals("\n")) {
						LayoutToken token = new LayoutToken(" ");
						tokens.add(token);
				    labels.add(null);
					}*/
					LayoutToken token = new LayoutToken(pieces[0]);
					tokens.add(token);
					labels.add(pieces[1]);
				}
			}
		}
		catch (Exception ex) {
			throw new GrobidResourceException(
				"An exception occured when accessing/reading Le Monde corpus files.", ex);

		}finally {
		}
		return res;
	}
	

    @SuppressWarnings({"UnusedParameters"})
    static public void addFeatures(List<LayoutToken> tokens,
    						List<String> labels,
                            Writer writer,
                            List<OffsetPosition> locationPositions,
							List<OffsetPosition> personTitlePositions,
							List<OffsetPosition> organisationPositions,
							List<OffsetPosition> orgFormPositions) {
        //int totalLine = texts.size();
        int posit = 0;
        //int sentence = 0;
        int currentLocationIndex = 0;
        int currentPersonTitleIndex = 0;
        int currentOrganisationIndex = 0;
        int currentOrgFormIndex = 0;
		/*List<OffsetPosition> localLocationPositions = null;
		List<OffsetPosition> localPersonTitlePositions = null;
		List<OffsetPosition> localOrganisationPositions = null;
		List<OffsetPosition> localOrgFormPositions = null;*/
		/*if (locationPositions.size() > sentence)
			localLocationPositions = locationPositions.get(sentence);
		if (personTitlePositions.size() > sentence)
			localPersonTitlePositions = personTitlePositions.get(sentence);
		if (organisationPositions.size() > sentence)
			localOrganisationPositions = organisationPositions.get(sentence);
		if (orgFormPositions.size() > sentence)
			localOrgFormPositions = orgFormPositions.get(sentence);	*/
        boolean isLocationToken = false;
        boolean isPersonTitleToken = false;
        boolean isOrganisationToken = false;
        boolean isOrgFormToken = false;
        try {
            String previousLabel = null;
            for (int n = 0; n < tokens.size(); n++) {
                LayoutToken token = tokens.get(n);
                if (token.getText().equals("@newline") || token.getText().equals("\n")) {
                    writer.write("\n");
                    writer.flush();

                    previousLabel = null;

                }

                // do we have a unit term at position posit?
                if ((locationPositions != null) && (locationPositions.size() > 0)) {
                    for (int mm = currentLocationIndex; mm < locationPositions.size(); mm++) {
                        if ((posit >= locationPositions.get(mm).start) &&
                                (posit <= locationPositions.get(mm).end)) {
                            isLocationToken = true;
                            currentLocationIndex = mm;
                            break;
                        } else if (posit < locationPositions.get(mm).start) {
                            isLocationToken = false;
                            break;
                        } else if (posit > locationPositions.get(mm).end) {
                            continue;
                        }
                    }
                }
                if ((personTitlePositions != null) && (personTitlePositions.size() > 0)) {
                    for (int mm = currentPersonTitleIndex; mm < personTitlePositions.size(); mm++) {
                        if ((posit >= personTitlePositions.get(mm).start) &&
                                (posit <= personTitlePositions.get(mm).end)) {
                            isPersonTitleToken = true;
                            currentPersonTitleIndex = mm;
                            break;
                        } else if (posit < personTitlePositions.get(mm).start) {
                            isPersonTitleToken = false;
                            break;
                        } else if (posit > personTitlePositions.get(mm).end) {
                            continue;
                        }
                    }
                }
                if ((organisationPositions != null) && (organisationPositions.size() > 0)) {
                    for (int mm = currentOrganisationIndex; mm < organisationPositions.size(); mm++) {
                        if ((posit >= organisationPositions.get(mm).start) &&
                                (posit <= organisationPositions.get(mm).end)) {
                            isOrganisationToken = true;
                            currentOrganisationIndex = mm;
                            break;
                        } else if (posit < organisationPositions.get(mm).start) {
                            isOrganisationToken = false;
                            break;
                        } else if (posit > organisationPositions.get(mm).end) {
                            continue;
                        }
                    }
                }
                if ((orgFormPositions != null) && (orgFormPositions.size() > 0)) {
                    for (int mm = currentOrgFormIndex; mm < orgFormPositions.size(); mm++) {
                        if ((posit >= orgFormPositions.get(mm).start) &&
                                (posit <= orgFormPositions.get(mm).end)) {
                            isOrgFormToken = true;
                            currentOrgFormIndex = mm;
                            break;
                        } else if (posit < orgFormPositions.get(mm).start) {
                            isOrgFormToken = false;
                            break;

                        } else if (posit > orgFormPositions.get(mm).end) {
                            continue;
                        }
                    }
                }

                // the "line" expected by the method FeaturesVectorNER.addFeaturesNER is the token
                // followed by the label, separated by a tab, and nothing else
                // in addition, the B- prefix for the labels must be injected here if not
                // present
                String cleanLine = token.getText();
                String label = labels.get(n);

                if (label != null) {
                    // second piece is the NER label, which could a single label or a list of labels in brackets
                    // separated by a comma
                    if (label.startsWith("[")) {
                        String rawlab = label.substring(1, label.length() - 1);
                        int ind = rawlab.indexOf(",");
                        if (ind != -1) {
                            rawlab = rawlab.substring(0, ind);
                            if (rawlab.startsWith("B-") || rawlab.equals("O") || rawlab.equals("<other>") ||
                                    rawlab.equals("0")) {
                                cleanLine += "\t" + rawlab;
                            } else {
                                if ((previousLabel == null) || (!previousLabel.equals(rawlab)) ||
                                        (previousLabel.equals("O")) || (previousLabel.equals("0")) || (previousLabel.equals("<other>"))) {
                                    cleanLine += "\tB-" + rawlab;
                                } else
                                    cleanLine += "\t" + rawlab;
                            }
                        } else {
                            //cleanLine += "\t" + labels;
                            System.out.println("WARNING, format error:" + rawlab);
                        }
                        previousLabel = rawlab;
                    } else {
                        String rawlab = label;
                        if (rawlab.startsWith("B-") || rawlab.equals("O") || rawlab.equals("<other>") ||
                                rawlab.equals("0")) {
                            cleanLine += "\t" + rawlab;
                        } else {
                            if ((previousLabel == null) || (!previousLabel.equals(rawlab)) ||
                                    (previousLabel.equals("O")) || (previousLabel.equals("0")) || (previousLabel.equals("<other>"))) {
                                cleanLine += "\tB-" + rawlab;

                            } else
                                cleanLine += "\t" + rawlab;
                        }
                        previousLabel = rawlab;
                    }
                }
                FeaturesVectorNER featuresVector =
                        FeaturesVectorNER.addFeaturesNER(cleanLine, isLocationToken, isPersonTitleToken,
                                isOrganisationToken, isOrgFormToken);
                if (featuresVector.label == null)
                    continue;
                writer.write(featuresVector.printVector() + "\n");
                writer.flush();
                posit++;
                isLocationToken = false;
                isPersonTitleToken = false;
                isOrganisationToken = false;
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        GrobidProperties.getInstance();
        NERFrenchTrainer trainer = new NERFrenchTrainer();

        AbstractTrainer.runTraining(trainer);
        System.out.println(AbstractTrainer.runEvaluation(trainer));

        System.exit(0);
    }
}