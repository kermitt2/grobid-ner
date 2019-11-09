package org.grobid.trainer;

import org.apache.commons.io.IOUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.layout.LayoutToken;

import java.io.*;
import java.util.*;

/**
 * Train a CRF model with CoNLL 2003 NER data (usual eng.train, eng.testa, eng.testb, 
 * with only 1 token and class per line, tab-separated).
 * 
 * To simplify the evaluation, we use a local install of wapiti executable that should 
 * be indicated in the property file and the official CoNLL evaluation script (in perl)
 * that should be installed with the CoNLL 2003 NER data under /bin/conlleval
 *
 * @author Patrice Lopez
 */
public class CoNNLNERTrainer extends NERTrainer {

    private String conllPath = null;

    // this is for CoNLL eval only (for historical reasons... TBD: try to use the JNI Wapiti wrapper instead!)
    private String wapitiExecPath = null;

    public CoNNLNERTrainer() {
        super();

        // adjusting CRF training parameters for this model
        this.epsilon = 0.0001;
        this.window = 20;
        this.nbMaxIterations = 50;
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

            // get the property value
            wapitiExecPath = prop.getProperty("grobid.ner.wapiti.exec");
        } catch (IOException ex) {
            throw new GrobidResourceException(
                    "An exception occured when accessing/reading the grobid-ner property file.", ex);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * Train using the CoNLL-2003 shared task NER gold corpus, English set.
     * see http://www.cnts.ua.ac.be/conll2003/ner/.
     * The resulting model is fully supervised and limited to the four classes of the gold corpus.
     */
    public void trainCoNLL(boolean includeTesta) {
        loadAdditionalProperties();
        long start = System.currentTimeMillis();
        Writer writer = null;
        try {
            File trainFile = new File(conllPath + "/eng.train");

            if (!trainFile.exists()) {
                throw new GrobidException(
                        "Cannot start training, because corpus resource path for CoNLL file " +
                                " is not correctly set : " + conllPath + "/eng.train");
            }
            File trainingOutputFile = getTempTrainingDataPath();
            System.out.println("Temp. training data under: " + trainingOutputFile.getPath());

            // the file for writing the training data
            OutputStream os = null;
            if (trainingOutputFile != null) {
                os = new FileOutputStream(trainingOutputFile);
                writer = new OutputStreamWriter(os, "UTF8");
            }

            List<OffsetPosition> locationPositions = null;
            List<OffsetPosition> titleNamePositions = null;
            List<OffsetPosition> organisationPositions = null;
            List<OffsetPosition> orgFormPositions = null;

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new DataInputStream(new FileInputStream(trainFile))));
            String line;
            String previousLabel = "O";
            List<LayoutToken> tokens = new ArrayList<LayoutToken>();
            List<String> labels = new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                if (line.startsWith("-DOCSTART-")) {
                    previousLabel = "O";
                    continue;
                }

                if (line.trim().length() == 0) {
                    //LayoutToken token = new LayoutToken("\n");
                    //tokens.add(token);
                    //labels.add(null);
                    previousLabel = "O";
                    continue;
                }

                String[] toks = line.split("\t");

                if (toks.length != 2) {
                    System.err.println("Invalid number of tokens for CoNNL train set line: " + line);
                    continue;
                }

                // we take the standard Grobid tokenizer
                StringTokenizer st2 = new StringTokenizer(toks[0],
                        TextUtilities.fullPunctuations, true);
                while (st2.hasMoreTokens()) {
                    String tok = st2.nextToken();
                    if (tok.trim().length() == 0)
                        continue;
                    LayoutToken token = new LayoutToken(tok);
                    tokens.add(token);

                    String label = toks[1];
                    label = translate(label);

					if (!label.equals("O")) {
    					if (previousLabel.equals("O") || !previousLabel.equals(label)) {
                            previousLabel = label;
    						label = "B-" + label;
                        }
    					else {
                            previousLabel = label;
    						label = "I-" + label;
                        }
                    } else {
                        previousLabel = label;
                    }

                    labels.add(label);
                }
            }

            locationPositions = lexicon.tokenPositionsLocationNames(tokens);
            titleNamePositions = lexicon.tokenPositionsPersonTitle(tokens);
            organisationPositions = lexicon.tokenPositionsOrganisationNames(tokens);
            orgFormPositions = lexicon.tokenPositionsOrgForm(tokens);

            addFeatures(tokens, labels, writer,
                    locationPositions, titleNamePositions, organisationPositions, orgFormPositions, true);
            writer.write("\n");

            br.close();

            // if indicated, we include the development set (eng.testa) in the training
            if (includeTesta) {
                trainFile = new File(conllPath + "/eng.testa");

                if (!trainFile.exists()) {
                    throw new GrobidException(
                            "Cannot start training, because corpus resource path for CoNLL file " +
                                    " is not correctly set : " + conllPath + "/eng.testa");
                }

                tokens = new ArrayList<LayoutToken>();
                labels = new ArrayList<String>();
                br = new BufferedReader(new InputStreamReader(
                        new DataInputStream(new FileInputStream(trainFile))));
                previousLabel = "O";
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("-DOCSTART-")) {
                        previousLabel = "O";
                        continue;
                    }

                    if (line.trim().length() == 0) {
                        //LayoutToken token = new LayoutToken("\n");
                        //tokens.add(token);
                        //labels.add(null);
                        previousLabel = "O";
                        continue;
                    }

                    String[] toks = line.split("\t");

                    if (toks.length != 2) {
                        System.err.println("Invalid number of tokens for CoNNL testa line: " + line);
                        continue;
                    }
                    // we take the standard Grobid tokenizer
                    StringTokenizer st2 = new StringTokenizer(toks[0],
                            TextUtilities.fullPunctuations, true);
                    while (st2.hasMoreTokens()) {
                        String tok = st2.nextToken();
                        if (tok.trim().length() == 0)
                            continue;
                        LayoutToken token = new LayoutToken(tok);
                        tokens.add(token);

                        String label = toks[1];
                        label = translate(label);

                        if (!label.equals("O")) {
                            if (previousLabel.equals("O") || !previousLabel.equals(label)) {
                                previousLabel = label;
                                label = "B-" + label;
                            }
                            else {
                                previousLabel = label;
                                label = "I-" + label;
                            }
                        } else {
                            previousLabel = label;
                        }

                        labels.add(label);
                    }
                }

                locationPositions = lexicon.tokenPositionsLocationNames(tokens);
                titleNamePositions = lexicon.tokenPositionsPersonTitle(tokens);
                organisationPositions = lexicon.tokenPositionsOrganisationNames(tokens);
                orgFormPositions = lexicon.tokenPositionsOrgForm(tokens);

                addFeatures(tokens, labels, writer,
                        locationPositions, titleNamePositions, organisationPositions, orgFormPositions, true);
                writer.write("\n");

                br.close();
            }
            writer.close();

            // we can train now a model
            GenericTrainer trainer = TrainerFactory.getTrainer();
            trainer.setEpsilon(this.epsilon);
            trainer.setWindow(this.window);
            trainer.setNbMaxIterations(this.nbMaxIterations);
            final File tempModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath() + ".connl");

            System.out.println("Model file under: " + tempModelPath.getPath());
            trainer.train(getTemplatePath(),
                    trainingOutputFile,
                    tempModelPath, GrobidProperties.getNBThreads(), GrobidModels.ENTITIES_NER);

        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid CoNLL-2003 NER training.", e);
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (Exception e) {
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("training done in " + (end - start) / 1000 + " s.");
    }

    private String translate(String label) {
        if (label.equals("O")) {
            label = "O";
        } else if (label.endsWith("ORG")) {
            label = NERLexicon.NER_Type.ORGANISATION.getName();
        } else if (label.endsWith("PER")) {
            label = NERLexicon.NER_Type.PERSON.getName();
        } else if (label.endsWith("LOC")) {
            label = NERLexicon.NER_Type.LOCATION.getName();
        } else if (label.endsWith("MISC")) {
            label = NERLexicon.NER_Type.UNKNOWN.getName();
        }
        return label;
    }

    /**
     * Usual evalution of the 4-classes model NER for CoNNL-2003 Gold corpus using the official
     * CoNNL evaluation script, see http://www.cnts.ua.ac.be/conll2003/ner/
     */
    public void evalCoNLL(String set) {
        loadAdditionalProperties();
        long start = System.currentTimeMillis();
        Writer writer = null;
        File evalOutputFile2 = null;
        try {
            File eval = new File(conllPath + "/" + set);

            if (!eval.exists()) {
                throw new GrobidException(
                        "Cannot start evaluation, because corpus resource path for CoNLL file " +
                                " is not correctly set : " + conllPath + "/" + set);
            }

            System.out.println("\n\nEvaluation for data set: " + set);

            // we need to run the ner parser on the text stream
            // then recreate a CoNLL result file as expected by the CoNLL evaluation script
            // then call the evaluation script as command line

            File evalOutputFile = getTempEvaluationDataPath();
            System.out.println("Temp. evaluation data under: " + evalOutputFile.getPath());

            evalOutputFile2 = getTempEvaluationDataPath();
            System.out.println("Temp. processed evaluation data under: " + evalOutputFile2.getPath());

            // the file for writing the evaluation data
            OutputStream os = null;
            if (evalOutputFile != null) {
                os = new FileOutputStream(evalOutputFile);
                writer = new OutputStreamWriter(os, "UTF8");
            }

            List<OffsetPosition> locationPositions = null;
            List<OffsetPosition> titleNamePositions = null;
            List<OffsetPosition> organisationPositions = null;
            List<OffsetPosition> orgFormPositions = null;

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new DataInputStream(new FileInputStream(eval))));
            String line = null;
            List<LayoutToken> tokens = new ArrayList<LayoutToken>();
            List<String> labels = new ArrayList<String>();
            String previousLabel = "O";
            while ((line = br.readLine()) != null) {
                if (line.startsWith("-DOCSTART-") || line.startsWith("--")) {
                    previousLabel = "O";
                    continue;
                }

                if (line.trim().length() == 0) {
                    previousLabel = "O";
                    continue;
                }

                String[] toks = line.split("\t");
                if (toks.length != 2) {
                    System.err.println("Invalid number of tokens for CoNNL corpus line: " + line);
                    previousLabel = "O";
                    continue;
                }

                // we take the standard Grobid tokenizer
                StringTokenizer st2 = new StringTokenizer(toks[0],
                        TextUtilities.fullPunctuations, true);
                while (st2.hasMoreTokens()) {
                    String tok = st2.nextToken();
                    if (tok.trim().length() == 0)
                        continue;
                    LayoutToken token = new LayoutToken(tok);
                    tokens.add(token);

                    String label = toks[1];
                    label = translate(label);

                    if (!label.equals("O")) {
                        if (previousLabel.equals("O") || !previousLabel.equals(label)) {
                            previousLabel = label;
                            label = "B-" + label;
                        }
                        else {
                            previousLabel = label;
                            label = "I-" + label;
                        }
                    } else {
                        previousLabel = label;
                    }

                    labels.add(label);
                }
            }
            locationPositions = lexicon.tokenPositionsLocationNames(tokens);
            titleNamePositions = lexicon.tokenPositionsPersonTitle(tokens);
            organisationPositions = lexicon.tokenPositionsOrganisationNames(tokens);
            orgFormPositions = lexicon.tokenPositionsOrgForm(tokens);

            addFeatures(tokens, labels, writer,
                    locationPositions, titleNamePositions, organisationPositions, orgFormPositions, true);
            writer.write("\n");

            br.close();
            writer.close();

            // apply now the model, we use a simple command line as it is only evaluation
            String modelPath = GrobidProperties.getModelPath(model).getAbsolutePath() + ".connl";

            String[] command = {wapitiExecPath, "label", "-m", modelPath, evalOutputFile.getPath(), evalOutputFile2.getPath()};

            ProcessBuilder builder = new ProcessBuilder(command);
            //System.out.println("command: " + builder.command());
            Process process = builder.start();
            try {
                int exitValue = process.waitFor();
                //System.out.println("exit value for wapiti labeling: " + exitValue);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // we reformat the output for application of the CoNLL eval script
            br = new BufferedReader(new InputStreamReader(
                    new DataInputStream(new FileInputStream(evalOutputFile2))));

            // and finally apply the CoNLL evaluation script
            builder = new ProcessBuilder("/usr/bin/perl", conllPath + "/bin/conlleval");
            //System.out.println("command: " + builder.command());
            BufferedReader br2 = null;
            BufferedReader br3 = null;
            PrintWriter pw = null;
            try {
                process = builder.start();

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
            throw new GrobidException("An exception occured while running Grobid CoNLL-2003 NER evaluation.", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }

        long end = System.currentTimeMillis();
        System.out.println("evaluation done in " + (end - start) / 1000 + " s.");
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList("../grobid-home"));
        GrobidProperties.getInstance(grobidHomeFinder);

        CoNNLNERTrainer trainer = new CoNNLNERTrainer();

        trainer.trainCoNLL(true);
        //trainer.evalCoNLL("eng.testa");
        trainer.evalCoNLL("eng.testb");
    }


}