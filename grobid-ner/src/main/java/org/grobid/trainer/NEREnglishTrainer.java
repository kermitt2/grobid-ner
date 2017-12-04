package org.grobid.trainer;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Paragraph;
import org.grobid.core.data.Sentence;
import org.grobid.core.data.TrainingDocument;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeaturesVectorNER;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.trainer.stax.CustomEMEXFormatStaxHandler;
import org.grobid.trainer.stax.StaxUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;


/**
 * Create the English language tagging model for named entities
 *
 * @author Patrice Lopez
 */
public class NEREnglishTrainer extends AbstractTrainer {

    private WstxInputFactory inputFactory = new WstxInputFactory();
    protected Lexicon lexicon = Lexicon.getInstance();

    private String nerCorpusPath = null;

    public NEREnglishTrainer() {
        super(GrobidModels.ENTITIES_NER);

        // adjusting CRF training parameters for this model
        epsilon = 0.000001;
        window = 20;

        GrobidProperties.setNBThreads("7");

        // read additional properties for this sub-project to get the paths to the resources
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = this.getClass().getResourceAsStream("/grobid-ner.properties");
            prop.load(input);
            nerCorpusPath = prop.getProperty("grobid.ner.corpus.path");
            if (!Files.exists(Paths.get(nerCorpusPath))) {
                throw new GrobidResourceException("Corpus path " + nerCorpusPath + " doesn't exists.");
            }
        } catch (IOException ex) {
            throw new GrobidResourceException("An exception occurred when accessing/reading the grobid-ner property file.", ex);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }


    /**
     * Add the selected features to the training data for the NER model
     */
    @Override
    public int createCRFPPData(File sourcePathLabel, File outputPath) {
        return createCRFPPData(sourcePathLabel, outputPath, null, 1.0);
        //return createCRFPPData(sourcePathLabel, null, outputPath, 1.0);
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
    public int createCRFPPData(final File corpusDir, final File trainingOutputPath,
                               final File evalOutputPath, double splitRatio) {
        int totalExamples = 0;
        Writer trainingOutputWriter = null;
        Writer evaluationOutputWriter = null;
        try {
            System.out.println("sourcePathLabel: " + corpusDir);
            if (trainingOutputPath != null)
                System.out.println("outputPath for training data: " + trainingOutputPath);
            if (evalOutputPath != null)
                System.out.println("outputPath for evaluation data: " + evalOutputPath);

            // the file for writing the training data
            OutputStream os2 = null;

            if (trainingOutputPath != null) {
                os2 = new FileOutputStream(trainingOutputPath);
                trainingOutputWriter = new OutputStreamWriter(os2, "UTF8");
            }

            // the file for writing the evaluation data
            OutputStream os3 = null;

            if (evalOutputPath != null) {
                os3 = new FileOutputStream(evalOutputPath);
                evaluationOutputWriter = new OutputStreamWriter(os3, "UTF8");
            }

            // Override the default location
            totalExamples = processCorpus(nerCorpusPath, trainingOutputWriter, evaluationOutputWriter, splitRatio);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            IOUtils.closeQuietly(trainingOutputWriter, evaluationOutputWriter);
        }

        return totalExamples;
    }


    private int processCorpus(String corpusPath, Writer writerTraining, Writer writerEvaluation, double splitRatio) {

        int res = 0;
        Writer writer = null;
        try {
            Collection<File> trainingFiles = FileUtils.listFiles(new File(corpusPath),
                    new SuffixFileFilter("training.xml"), null);


            for (File trainingFile : trainingFiles) {
                System.out.println("Processing " + trainingFile.getAbsolutePath());

                writer = dispatchExample(writerTraining, writerEvaluation, splitRatio);

                InputStream resourceAsStream = new FileInputStream(trainingFile);
                XMLStreamReader2 reader = null;
                CustomEMEXFormatStaxHandler handler = new CustomEMEXFormatStaxHandler();
                try {
                    reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(resourceAsStream);

                    StaxUtils.traverse(reader, handler);

                    List<TrainingDocument> documents = handler.getDocuments();

                    for (TrainingDocument document : documents) {
                        for (Paragraph paragraph : document.getParagraphs()) {
                            for (Sentence sentence : paragraph.getSentences()) {
                                List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(sentence.getRawValue(), new Language(Language.EN, 1.0));
                                sentence.setTokenisedValue(tokens);

                                computeFeatures(sentence, writer);
                            }
                            writer.write("\n");
                        }
                    }

                } catch (XMLStreamException e) {
                    LOGGER.warn("The file " + trainingFile.getAbsolutePath() + " cannot be processed. Skipping it. ", e);
                }

            }
        } catch (IOException ex) {
            throw new GrobidResourceException("Error while running the training.", ex);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return res;
    }

    private void computeFeatures(Sentence sentence, Writer writer) {
        List<OffsetPosition> locationPositions = lexicon.charPositionsLocationNames(sentence.getTokenisedValue());
        List<OffsetPosition> personTitlePositions = lexicon.charPositionsPersonTitle(sentence.getTokenisedValue());
        List<OffsetPosition> organisationPositions = lexicon.charPositionsOrganisationNames(sentence.getTokenisedValue());
        List<OffsetPosition> orgFormPositions = lexicon.charPositionsOrgForm(sentence.getTokenisedValue());

        //Getting the flat indexes of the OffsetPositions for each dictionary
        List<Integer> locationIndexes = offsetToIndex(sentence.getTokenisedValue(), locationPositions);
        List<Integer> personTitleIndexes = offsetToIndex(sentence.getTokenisedValue(), personTitlePositions);
        List<Integer> organisationIndexes = offsetToIndex(sentence.getTokenisedValue(), organisationPositions);
        List<Integer> orgFormIndexes = offsetToIndex(sentence.getTokenisedValue(), orgFormPositions);

        Integer previousEntityIndexForThisToken = null;

        for (int i = 0; i < sentence.getTokenisedValue().size(); i++) {
            String label = "O";

            final String token = sentence.getTokenisedValue().get(i).getText();
            final Integer entityIndexForThisToken = sentence.getEntityIndexList().get(i);
            if (entityIndexForThisToken > -1) {
                label = sentence.getEntities().get(entityIndexForThisToken).getType().getName();
                if (!entityIndexForThisToken.equals(previousEntityIndexForThisToken)) {
                    label = "B-" + label;
                }
            }

            previousEntityIndexForThisToken = entityIndexForThisToken;

            if (isBlank(token) || isBlank(label)) {
                continue;
            }

            FeaturesVectorNER featuresVector = FeaturesVectorNER.addFeaturesNER(token, label,
                    locationIndexes.contains(i),
                    personTitleIndexes.contains(i),
                    organisationIndexes.contains(i),
                    orgFormIndexes.contains(i));

            final String vector = featuresVector.printVector();
            try {
                writer.write(vector + "\n");
                writer.flush();
            } catch (IOException e) {
                LOGGER.warn("Cannot append the feature vector \n" + vector);
            }

        }

    }

    /**
     * Given a tokenised sentence and a list of OffsetPositions representing indexes offset position
     * within the sentence, returns flat array of indexes:
     * <p>
     * e.g. the sentence is "I walk in the Bronx", which is, tokenised "I, ,walk, ,in, ,the, ,bronx"
     * with offsets [(2, 2), (6,8)] the result would be [2, 6, 7, 8]
     *
     * @param tokenisedSentence the sentence already tokenised
     * @param offsetPositions   the list of index position within the tokenised sentence
     * @return an index with explicit index definition
     */
    protected List<Integer> offsetToIndex(List<LayoutToken> tokenisedSentence, List<OffsetPosition> offsetPositions) {
        List<Integer> indexList = new ArrayList<>();

        int indexStart = 0;
        for (int i = 0; i < tokenisedSentence.size(); i++) {
            String token = tokenisedSentence.get(i).getText();

            out:
            for (int locationIdx = indexStart; locationIdx < offsetPositions.size(); locationIdx++) {
                OffsetPosition pos = offsetPositions.get(locationIdx);

                if (i >= pos.start && i <= pos.end) {
                    indexList.add(i);
                    if (pos.start == pos.end) {
                        indexStart = locationIdx + 1;
                    } else {
                        if (i == pos.end) {
                            indexStart = locationIdx + 1;
                        }
                    }

                    break out;
                }
            }

        }

        return indexList;
    }

    /**
     * Dispatch the example to the training or test data, based on the split ration and the drawing of
     * a random number
     */
    private Writer dispatchExample(Writer writerTraining, Writer writerEvaluation, double splitRatio) {
        Writer writer = null;
        if ((writerTraining == null) && (writerEvaluation != null)) {
            writer = writerEvaluation;
        } else if ((writerTraining != null) && (writerEvaluation == null)) {
            writer = writerTraining;
        } else {
            if (Math.random() <= splitRatio)
                writer = writerTraining;
            else
                writer = writerEvaluation;
        }
        return writer;
    }


    public static void main(String[] args) {
        GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList("../../grobid-home"));
        File grobidHome = grobidHomeFinder.findGrobidHomeOrFail();
        grobidHomeFinder.findGrobidPropertiesOrFail(grobidHome);

        GrobidProperties.getInstance(grobidHomeFinder);

        NEREnglishTrainer trainer = new NEREnglishTrainer();
//        AbstractTrainer.runTraining(trainer);
//        AbstractTrainer.runEvaluation(trainer);

        AbstractTrainer.runSplitTrainingEvaluation(trainer, 0.8);
    }

    public void setNerCorpusPath(String nerCorpusPath) {
        this.nerCorpusPath = nerCorpusPath;
    }
}