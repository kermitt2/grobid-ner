package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Sense;
import org.grobid.core.data.TextBlocks;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorNER;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.LexiconPositionsIndexes;
import org.grobid.core.utilities.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * NER
 *
 * @author Patrice Lopez
 */
public class NERParser extends AbstractParser {

    private static Logger LOGGER = LoggerFactory.getLogger(NERParser.class);

    protected Lexicon lexicon = Lexicon.getInstance();
    protected SenseTagger senseTagger = null;

    public NERParser() {
        super(GrobidModels.ENTITIES_NER);
        senseTagger = new SenseTagger();
    }

    /**
     * Extract all occurrences of named entity from a simple piece of text.
     */
    public List<Entity> extractNE(String text) {
        if (isEmpty(text))
            return null;

        text = text.replace("\n", " ");

        TextBlocks blocks = TextBlocks.getTextBlocks(text);
        LexiconPositionsIndexes positionsIndexes = new LexiconPositionsIndexes(lexicon);
        positionsIndexes.computeIndexes(text);

        String res = toFeatureVector(blocks, positionsIndexes);
        String result = label(res);
        List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(result);

        List<Entity> entities = resultExtraction(text, labeled, blocks.getTokens());

        // we use now the sense tagger for the recognized named entity
        List<Sense> senses = senseTagger.extractSenses(text, labeled, blocks.getTokens(), positionsIndexes);

        merge(entities, senses);

        return entities;
    }

    private String toFeatureVector(TextBlocks blocks, LexiconPositionsIndexes positionsIndexes) {
        StringBuffer ress = new StringBuffer();
        int posit = 0; // keep track of the position index in the list of positions

        int currentPosition = 0;

        for (String block : blocks.getTextBlocks()) {
            currentPosition += blocks.getTextBlocksPositions().get(posit);

            // check if the token is a known NE
            // do we have a NE at position posit?
            boolean isLocationToken = LexiconPositionsIndexes
                    .isTokenInLexicon(positionsIndexes.getLocalLocationPositions(), posit);
            boolean isPersonTitleToken = LexiconPositionsIndexes
                    .isTokenInLexicon(positionsIndexes.getLocalPersonTitlePositions(), posit);
            boolean isOrganisationToken = LexiconPositionsIndexes
                    .isTokenInLexicon(positionsIndexes.getLocalOrganisationPositions(), posit);
            boolean isOrgFormToken = LexiconPositionsIndexes
                    .isTokenInLexicon(positionsIndexes.getLocalOrgFormPositions(), posit);

            ress.append(FeaturesVectorNER
                    .addFeaturesNER(block,
                            isLocationToken, isPersonTitleToken, isOrganisationToken, isOrgFormToken)
                    .printVector());
            ress.append("\n");
            posit++;
        }
        ress.append("\n");
        return ress.toString();
    }

    private void merge(List<Entity> entities, List<Sense> senses) {
        Sense theSense = null;
        int sensePos = 0;
        for (Entity entity : entities) {
            int start = entity.getOffsetStart();
            int end = entity.getOffsetEnd();

            if (senses != null) {
                for (int i = sensePos; i < senses.size(); i++) {
                    Sense sense = senses.get(i);
                    if ((sense.getOffsetStart() >= start) && (sense.getOffsetEnd() <= end)) {
                        theSense = sense;
                        sensePos = i;
                        break;
                    }
                }
            }
            entity.setSense(theSense);
        }
    }

    /**
     * Extract the named entities from a labelled text.
     */
    public List<Entity> resultExtraction(String text,
                                         List<Pair<String, String>> labeled,
                                         List<String> tokenizations) {

        List<Entity> entities = new ArrayList<Entity>();
        String label = null; // label
        String actual = null; // token
        int offset = 0;
        int addedOffset = 0;
        int p = 0; // iterator for the tokenizations for restauring the original tokenization with
        // respect to spaces
        Entity currentEntity = null;
        for (Pair<String, String> l : labeled) {
            actual = l.a;
            label = l.b;

            boolean stop = false;
            while ((!stop) && (p < tokenizations.size())) {
                String tokOriginal = tokenizations.get(p);
                addedOffset += tokOriginal.length();
                if (tokOriginal.equals(actual)) {
                    stop = true;
                }
                p++;
            }

            if (label == null) {
                offset += addedOffset;
                addedOffset = 0;
                continue;
            }

            if (actual != null) {
                if (label.startsWith("B-")) {
                    if (currentEntity != null) {
                        int localPos = currentEntity.getOffsetEnd();
                        if (label.length() > 1) {
                            String subtag = label.substring(2, label.length()).toLowerCase();
                            if ((currentEntity != null) &&
                                    (currentEntity.getType() != null) &&
                                    (currentEntity.getType().getName().toLowerCase().equals(subtag)) &&
                                    (localPos == offset)) {
                                currentEntity.setOffsetEnd(offset + addedOffset);
                                offset += addedOffset;
                                addedOffset = 0;
                                continue;
                            }
                            currentEntity.setRawName(
                                    text.substring(currentEntity.getOffsetStart(), currentEntity.getOffsetEnd()));
                            entities.add(currentEntity);
                        }
                    }
                    if (label.length() > 1) {
                        String subtag = label.substring(2, label.length()).toLowerCase();
                        currentEntity = new Entity();
                        currentEntity.setTypeFromString(subtag);
                        if ((text.length() > offset) && (text.charAt(offset) == ' ')) {
                            currentEntity.setOffsetStart(offset + 1);
                        } else
                            currentEntity.setOffsetStart(offset);
                        currentEntity.setOffsetEnd(offset + addedOffset);
                    }
                }
                //else if (label.startsWith("I-")) {
                else if (!label.equals("O") && !label.equals("other")) {
                    if (label.length() > 1) {
                        //String subtag = label.substring(2,label.length()).toLowerCase();
                        String subtag = label.toLowerCase();
                        if ((currentEntity != null) &&
                                (currentEntity.getType() != null) &&
                                (currentEntity.getType().getName().toLowerCase().equals(subtag))) {
                            currentEntity.setOffsetEnd(offset + addedOffset);
                        } else {
                            // should not be the case, but we add the new entity, for robustness
                            if (currentEntity != null) {
                                currentEntity.setRawName(
                                        text.substring(currentEntity.getOffsetStart(), currentEntity.getOffsetEnd()));
                                entities.add(currentEntity);
                            }
                            currentEntity = new Entity();
                            currentEntity.setTypeFromString(subtag);
                            currentEntity.setOffsetStart(offset);
                            currentEntity.setOffsetEnd(offset + addedOffset);
                        }
                    }
                }

                offset += addedOffset;
                addedOffset = 0;
            }
        }

        if (currentEntity != null) {
            currentEntity.setRawName(
                    text.substring(currentEntity.getOffsetStart(), currentEntity.getOffsetEnd()));
            entities.add(currentEntity);
        }

        return entities;
    }


    /**
     * Process the content of the specified input file and format the result as training data.
     * <p>
     * Input file should be a text file. Each file is a paragraph entry that it's normally processed by NERD.
     */
    public void createTraining(String inputFile,
                               String outputPath) throws Exception {
        File file = new File(inputFile);
        if (!file.exists()) {
            throw new GrobidException("Cannot create training data because input file can not be accessed: " + inputFile);
        }

        String data = null;
        if (inputFile.endsWith(".txt") || inputFile.endsWith(".TXT")) {
            data = createTrainingText(file);
        }

        if (data != null) {
            try {
                FileUtils.writeStringToFile(new File(outputPath), data);
            } catch (IOException e) {
                throw new GrobidException("Cannot create training data because output file can not be accessed: " + outputPath, e);
            }
        }
    }

    protected String createTrainingText(File file) throws IOException {
        String text = FileUtils.readFileToString(file);

        return createTrainingFromText(text);
    }

    protected String createTrainingFromText(String text) {
        if (isEmpty(text))
            return null;

        text = text.replace("\n", " ");

        TextBlocks blocks = TextBlocks.getTextBlocks(text);
        LexiconPositionsIndexes positionsIndexes = new LexiconPositionsIndexes(lexicon);
        positionsIndexes.computeIndexes(text);

        String featuresVector = toFeatureVector(blocks, positionsIndexes);
        String res = label(featuresVector);

        List<Pair<String, String>> labeledEntries = GenericTaggerUtils.getTokensAndLabels(res);
//        List<Sense> senses = senseTagger.extractSenses(text, labeledEntries, blocks.getTokens(), positionsIndexes);

        StringBuilder sb = new StringBuilder();

//        int count = 0;
        for (Pair<String, String> labeledEntry : labeledEntries) {
            String value = labeledEntry.a;
            String label = labeledEntry.b;

            if (value != null) {
                sb.append(value).append("\t").append(label);
                /*if (!StringUtils.equals(label, "O")) {
                    int senseIdx = blocks.getTextBlocksPositions().get(count);

                    for (Sense sense : senses) {
                        if (senseIdx >= sense.getOffsetStart() && senseIdx <= sense.getOffsetEnd()) {
                            sb.append("\t").append(sense.getFineSense());
                            break;
                        }
                    }
                }*/
                sb.append("\n");
            }
//            count++;
        }
        return sb.toString();
    }

    public int createTrainingBatch(String inputDirectory,
                                   String outputDirectory) throws IOException {
        try {
            File path = new File(inputDirectory);
            if (!path.exists()) {
                throw new GrobidException("Cannot create training data because input directory can not be accessed: " + inputDirectory);
            }

            File pathOut = new File(outputDirectory);
            if (!pathOut.exists()) {
                throw new GrobidException("Cannot create training data because ouput directory can not be accessed: " + outputDirectory);
            }

            // we process all pdf files in the directory
            File[] refFiles = path.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt") || name.endsWith(".TXT");
                }
            });

            if (refFiles == null)
                return 0;

            LOGGER.info(refFiles.length + " files to be processed.");

            for (final File file : refFiles) {
                try {
                    String outputPath = outputDirectory + "/" + file.getName().substring(0, file.getName().length() - 4) + ".training.txt";
                    createTraining(file.getAbsolutePath(), outputPath);
                } catch (final Exception exp) {
                    LOGGER.error("An error occured while processing the following pdf: "
                            + file.getPath() + ": ", exp);
                }
            }

            return refFiles.length;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occured while running Grobid batch.", exp);
        }
    }
}
