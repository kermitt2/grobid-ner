package org.grobid.trainer;

import org.apache.commons.io.IOUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.engines.NEREnParser;
import org.grobid.core.engines.NERParsers;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;


/**
 * Additional NER specific evaluation.
 *
 * @author Patrice Lopez
 */
public class NEREvaluation {
    private static Logger LOGGER = LoggerFactory.getLogger(NEREvaluation.class);

    private Lexicon lexicon = Lexicon.getInstance();
    private NERLexicon nerLexicon = NERLexicon.getInstance();

    private String conllPath = null;
    private GrobidModels model = null;

    public NEREvaluation() {
        GrobidProperties.getInstance();
        model = GrobidModels.ENTITIES_NER;
        loadAdditionalProperties();
    }

    private void loadAdditionalProperties() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("src/main/resources/grobid-ner.properties");

            // load the properties file
            prop.load(input);

            // get the property value
            conllPath = prop.getProperty("grobid.ner.reuters.conll_path");
        } catch (IOException ex) {
            throw new GrobidResourceException(
                    "An exception occured when accessing/reading the grobid-ner property file.", ex);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }


    /**
     * Evaluation based on the CoNLL-2003 shared task NER gold corpus, English set.
     * see http://www.cnts.ua.ac.be/conll2003/ner/.
     */
    public String evaluate_reuters() {
        long start = System.currentTimeMillis();
        StringBuilder report = new StringBuilder();
        try {
            GrobidFactory.getInstance();
            NERParsers parsers = new NERParsers();

            File evalDataF = GrobidProperties.getInstance().getEvalCorpusPath(
                    new File(new File("resources").getAbsolutePath()), model);

            File tmpEvalPath = getTempEvaluationDataPath();

            report.append("Eval. path: " + tmpEvalPath.getPath() + "\n");

            // There are three set that we can exploit testa, testb and the training sets.
            // However the training set should be used to reimforce the learning.
            File evalA = new File(conllPath + "/eng.testa");
            File evalB = new File(conllPath + "/eng.testb");
            File evalTrain = new File(conllPath + "/eng.train");

            if (!evalTrain.exists()) {
                throw new GrobidException(
                        "Cannot start evaluation, because corpus resource path for CoNLL file " +
                                " is not correctly set : " + evalDataF.getPath() + "/eng.train");
            }
            report.append(evaluate_reutersSet(parsers, evalTrain, tmpEvalPath));

            if (!evalA.exists()) {
                throw new GrobidException(
                        "Cannot start evaluation, because corpus resource path for CoNLL file " +
                                " is not correctly set : " + evalDataF.getPath() + "/eng.testa");
            }
            report.append(evaluate_reutersSet(parsers, evalA, tmpEvalPath));

            if (!evalB.exists()) {
                throw new GrobidException(
                        "Cannot start evaluation, because corpus resource path for CoNLL file " +
                                " is not correctly set : " + evalDataF.getPath() + "/eng.testb");
            }
            report.append(evaluate_reutersSet(parsers, evalB, tmpEvalPath));
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid Reuters evaluation.", e);
        }
        long end = System.currentTimeMillis();
        report.append("processed in " + (end - start) / 1000 + " s.");

        return report.toString();
    }

    private String evaluate_reutersSet(NERParsers parsers, File evalSet, File tmpEvalPath) {
        StringBuffer report = new StringBuffer();
        BufferedReader bufReader = null;
        Writer writer = null;
        try {
            NEREnParser parser = new NEREnParser();
            if (parser == null)
                throw new GrobidException("Instance of English NER not available.");
            report.append("\n" + evalSet.getPath());
            // the data need to be re-tokenized according to the Grobid NER tokenization level
            createCRFPPDataCoNLL(evalSet, tmpEvalPath);

            bufReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(tmpEvalPath), "UTF-8"));
            writer = new OutputStreamWriter(new FileOutputStream(tmpEvalPath + ".full"), "UTF8");

            // the parser needs to be applied on a sentence level as it has been trained as such
            String line;
            StringBuilder ress = new StringBuilder();
            List<String> results = new ArrayList<String>();
            while ((line = bufReader.readLine()) != null) {
                ress.append(line + "\n");
                if (line.trim().length() == 0) {
                    if (ress.toString().trim().length() != 0) {
                        String res = parser.label(ress.toString());
                        results.add(res);
                        ress = new StringBuilder();
                    }
                }
            }

            int tp = 0; // true positive
            int fp = 0; // false positive
            int tn = 0; // true negative
            int fn = 0; // false negative
            int nbToken = 0;

            // now evaluate the result, by comparing the label and pre-label
            for (String result : results) {
                StringTokenizer st = new StringTokenizer(result, "\n");
                String estimated = null;
                while (st.hasMoreTokens()) {
                    nbToken++;
                    line = st.nextToken();
                    String[] cells = line.split("\t");
                    String expected = cells[cells.length - 2];
                    estimated = translateLabel(cells[cells.length - 1]);

                    if (estimated.equals(expected) && !expected.equals("O")) {
                        tp++;
                    } else if (estimated.equals(expected) && expected.equals("O")) {
                        tn++;
                    } else if (!estimated.equals(expected) && expected.equals("O")) {
                        fp++;
                    } else {
                        fn++;
                    }
                    writer.write(line.replace("\t", " ") + " " + estimated + "\n");
                }
                writer.write("\n");
            }

            report.append("\n\nTotal sentences: " + results.size() + "\n");
            report.append("Total tokens: " + nbToken + "\n\n");

            report.append("True Positive: " + tp + "\n");
            report.append("False Positive: " + fp + "\n");
            report.append("True Negative: " + tn + "\n");
            report.append("False Negative: " + fn + "\n");

            report.append("\nToken level\n-----------\n");
            double precision = ((double) (tp)) / (tp + fp);
            double recall = ((double) (tp)) / (tp + fn);
            double f1 = ((double) (2 * tp)) / (2 * tp + fp + fn);

            report.append("Precision: " + TextUtilities.formatTwoDecimals(precision * 100) + "\n");
            report.append("Recall: " + TextUtilities.formatTwoDecimals(recall * 100) + "\n");
            report.append("f1: " + TextUtilities.formatTwoDecimals(f1 * 100) + "\n");

            // we also evaluate using official CoNLL 2003 evaluation script
            // we reformat the output for application of the CoNLL eval script
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new DataInputStream(new FileInputStream(tmpEvalPath))));

            // and finally apply the CoNLL evaluation script
            ProcessBuilder builder = new ProcessBuilder("/usr/bin/perl", conllPath + "/bin/conlleval");
			//System.out.println("command: " + builder.command());
            BufferedReader br2 = null;
            BufferedReader br3 = null;
            PrintWriter pw = null;
            try {
                Process process = builder.start();

                br2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
                br3 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                pw = new PrintWriter(process.getOutputStream(), true);
                while ((line = br.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    pw.write(line.replace("\t", " ") + "\n");
                    pw.flush();
                }
                pw.close();

                while ((line = br2.readLine()) != null) {
                    System.out.println(line);
                }

                while ((line = br3.readLine()) != null) {
                    System.out.println(line);
                }

                process.waitFor();
                int exitVal = process.exitValue();
                //System.out.println("CoNLL eval script terminated, exit value: " + exitVal);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(br, br2, br3, pw);
            }

        } catch (Exception e) {
            throw new GrobidException(
                    "An exception occured while running Grobid Reuters evaluation for the set "
                            + evalSet.getPath(), e);
        } finally {
            IOUtils.closeQuietly(bufReader, writer);

        }
        return report.toString();
    }

    /**
     * Translate labels into Conll NER class
     */
    public static String translateLabel(String label) {
        if (label.equals("O")) {
            return label;
        }

        if (label.startsWith("I-") || label.startsWith("B-")) {
            label = label.substring(2, label.length());
        }

        NERLexicon.NER_Type type = null;

        try {
            type = NERLexicon.NER_Type.valueOf(label);
        } catch (IllegalArgumentException iae) {
            LOGGER.warn("Forcing label " + label + " to 'O'");
            return "O";
        }
        switch (type) {
            case PERSON:
                label = "per";
                break;

            case LOCATION:
                label = "loc";
                break;
            case INSTALLATION:
                label = "loc";
                break;

            case ORGANISATION:
                label = "org";
                break;
            case INSTITUTION:
                label = "org";
                break;
            case BUSINESS:
                label = "org";
                break;
            case MEDIA:
                label = "org";
                break;

            case SPORT_TEAM:
                label = "misc";
                break;
            case NATIONAL:
                label = "misc";
                break;
            case AWARD:
                label = "misc";
                break;
            case PERSON_TYPE:
                label = "misc";
                break;

            case ANIMAL:
                label = "O";
                break;
            case ARTIFACT:
                label = "O";
                break;
            case ACRONYM:
                label = "O";
                break;
            case MEASURE:
                label = "O";
                break;
            case CONCEPT:
                label = "O";
                break;
            case CONCEPTUAL:
                label = "O";
                break;
            case CREATION:
                label = "O";
                break;
            case EVENT:
                label = "O";
                break;
            case IDENTIFIER:
                label = "O";
                break;
            case SUBSTANCE:
                label = "O";
                break;
            case PLANT:
                label = "O";
                break;
            case PERIOD:
                label = "O";
                break;
            case WEBSITE:
                label = "O";
                break;
            case UNKNOWN:
                label = "O";
                break;
            case TITLE:
                label = "O";
                break;

            default:
                label = "misc";
        }
        return label;
    }

    /**
     * Add the features to the CoNLL NER evaluation set
     */
    private int createCRFPPDataCoNLL(final File corpusDir, final File evalOutputPath) {
        BufferedReader bufReader = null;
        Writer writer = null;
        String line = null;
        int nbSentences = 0;
        try {
            bufReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(corpusDir), "UTF-8"));
            writer = new OutputStreamWriter(new FileOutputStream(evalOutputPath), "UTF8");
            List<String> labeled = new ArrayList<String>();
            // to store unit term positions
            List<OffsetPosition> locationPositions = null;
            List<OffsetPosition> peoplePositions = null;
            List<OffsetPosition> organisationPositions = null;
            List<OffsetPosition> orgFormPositions = null;
            List<LayoutToken> tokens = new ArrayList<LayoutToken>();
            List<String> labels = new ArrayList<String>();
            while ((line = bufReader.readLine()) != null) {
                line = line.trim();
                // note that we work at sentence level
                if (line.startsWith("-DOCSTART-") || line.startsWith("-X-")) {
                    continue;
                }

                if (line.length() == 0) {
                    // sentence is complete
                    LayoutToken token = new LayoutToken("\n");

                    tokens.add(token);
                    labels.add(null);

                    nbSentences++;
                    continue;
                }

                String[] pieces = line.split(" ");
                if (pieces.length == 4) {
                    // we retokenize the lexical string according to Grobid NER
                    String tok = pieces[0];
                    StringTokenizer st = new StringTokenizer(tok, TextUtilities.fullPunctuations);

                    String conllLabel = pieces[3];
                    String label = "O";
                    if (conllLabel.equals("I-ORG")) {
                        label = "org";
                    } else if (conllLabel.equals("I-LOC")) {
                        label = "loc";
                    } else if (conllLabel.equals("I-PER")) {
                        label = "per";
                    } else if (conllLabel.equals("I-MISC")) {
                        label = "misc";
                    }
                    boolean start = true;
                    while (st.hasMoreTokens()) {
                        LayoutToken token = new LayoutToken(st.nextToken());
                        tokens.add(token);
                        labels.add(label);
                    }
                }
            }

            locationPositions = lexicon.tokenPositionsLocationNames(tokens);
            peoplePositions = lexicon.tokenPositionsPersonTitle(tokens);
            organisationPositions = lexicon.tokenPositionsOrganisationNames(tokens);
            orgFormPositions = lexicon.tokenPositionsOrgForm(tokens);
            NERTrainer.addFeatures(tokens, labels, writer,
                    locationPositions, peoplePositions, organisationPositions, orgFormPositions);
            writer.write("\n");
            writer.close();
        } catch (Exception e) {
            throw new GrobidException(
                    "An exception occured while creating Grobid Reuters evaluation resource the set "
                            + corpusDir.getPath(), e);
        } finally {
            IOUtils.closeQuietly(writer, bufReader);
        }
        return nbSentences;
    }

    protected final File getTempEvaluationDataPath() {
        try {
            return File.createTempFile(model.getModelName(), ".test", GrobidProperties.getTempPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create a temporary evaluation file for model: " + model);
        }
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        final GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList("../../grobid-home", "../grobid-home"));
        GrobidProperties.getInstance(grobidHomeFinder);
        NEREvaluation eval = new NEREvaluation();

        // CoNLL evaluation
        System.out.println(eval.evaluate_reuters());
    }
}