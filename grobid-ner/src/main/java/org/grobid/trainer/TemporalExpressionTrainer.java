package org.grobid.trainer;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeaturesVectorTemporalExpression;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Pair;
import org.grobid.trainer.stax.MultiDatesCorpusStaxHandler;
import org.grobid.trainer.stax.StaxUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.grobid.core.engines.Models.TEMPORAL_EXPRESSION;

public class TemporalExpressionTrainer extends AbstractTrainer {

    private WstxInputFactory inputFactory = new WstxInputFactory();

    private String corpusPath = null;

    public TemporalExpressionTrainer() {
        super(GrobidModels.modelFor(TEMPORAL_EXPRESSION.getModelName()));

        // adjusting CRF training parameters for this model
        epsilon = 0.000001;
        window = 20;

        // read additional properties for this sub-project to get the paths to the resources
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = this.getClass().getResourceAsStream("/grobid-ner.properties");
            prop.load(input);
            corpusPath = prop.getProperty("grobid.multiDate.corpus.path");
            if (!Files.exists(Paths.get(corpusPath))) {
                throw new GrobidResourceException("Corpus path " + Paths.get(corpusPath).toAbsolutePath() + " doesn't exists.");
            }
        } catch (IOException ex) {
            throw new GrobidResourceException("An exception occurred when accessing/reading the grobid-ner property file.", ex);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    @Override
    public int createCRFPPData(File sourcePathLabel, File outputPath) {
        return createCRFPPData(sourcePathLabel, outputPath, null, 1.0);
    }

    @Override
    public int createCRFPPData(final File corpusDirOrigin,
                               final File trainingOutputPath,
                               final File evalOutputPath,
                               double splitRatio) {
        int totalExamples = 0;

        Writer writerTraining = null;
        Writer writerEval = null;
        OutputStream streamTraining = null;
        OutputStream streamEval = null;

        try {
            final File corpusPathFile = new File(corpusPath);

            LOGGER.info("sourcePathLabel: " + corpusPathFile);
            if (trainingOutputPath != null) {
                LOGGER.info("outputPath for training data: " + trainingOutputPath);
            }

            if (evalOutputPath != null)
                LOGGER.info("outputPath for evaluation data: " + evalOutputPath);


            Collection<File> corpus = FileUtils.listFiles(corpusPathFile,
                    new SuffixFileFilter(".xml"), null);

            if (isEmpty(corpus)) {
                throw new IllegalStateException("Folder " + corpusPathFile.getAbsolutePath()
                        + " does not seem to contain training data. Please check");
            }

            LOGGER.info(corpus.size() + " tei files");

            // the file for writing the training data
            if (trainingOutputPath != null) {
                streamTraining = new FileOutputStream(trainingOutputPath);
                writerTraining = new OutputStreamWriter(streamTraining, StandardCharsets.UTF_8);
            }

            // the file for writing the evaluation data
            if (evalOutputPath != null) {
                streamEval = new FileOutputStream(evalOutputPath);
                writerEval = new OutputStreamWriter(streamEval, StandardCharsets.UTF_8);
            }

            for (File teifile : corpus) {
                InputStream is = new FileInputStream(teifile);
                XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

                MultiDatesCorpusStaxHandler staxHandler = new MultiDatesCorpusStaxHandler();
                StaxUtils.traverse(reader, staxHandler);

                final List<Pair<String, String>> labeledTokens = staxHandler.getData();

                List<List<String>> vectorsString = new ArrayList<>();
                List<String> example = new ArrayList<>();

                for (Pair<String, String> labeledToken : labeledTokens) {
                    if (StringUtils.equals(labeledToken.a, "@newline")) {
                        if (isNotEmpty(example)) {
                            vectorsString.add(example);
                        }
                        example = new ArrayList<>();
                    } else {
                        FeaturesVectorTemporalExpression vector = FeaturesVectorTemporalExpression.addFeatures(labeledToken.a, labeledToken.b);
                        String vectorAsString = vector.printVector();

                        example.add(vectorAsString);
                    }
                }

                for (List<String> exampleVectorString : vectorsString) {
                    StringBuilder sb = new StringBuilder();
                    for (String token : exampleVectorString) {
                        sb.append(token);
                    }

                    String exampleVector = sb.toString();
                    if ((writerTraining == null) && (writerEval != null))
                        writerEval.write(exampleVector + "\n");
                    else if ((writerTraining != null) && (writerEval == null))
                        writerTraining.write(exampleVector + "\n");
                    else {
                        if (Math.random() <= splitRatio) {
                            writerTraining.write(exampleVector + "\n");
                        } else {
                            writerEval.write(exampleVector + "\n");
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        } finally {
            IOUtils.closeQuietly(writerEval, writerTraining);
        }
        return totalExamples;
    }


    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList("../../grobid-home"));
        grobidHomeFinder.findGrobidHomeOrFail();
        GrobidProperties.getInstance(grobidHomeFinder);
        TemporalExpressionTrainer trainer = new TemporalExpressionTrainer();
        AbstractTrainer.runSplitTrainingEvaluation(trainer, 0.7);
    }
}
