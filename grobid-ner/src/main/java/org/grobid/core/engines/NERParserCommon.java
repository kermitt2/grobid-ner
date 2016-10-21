package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Sense;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorNER;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.LexiconPositionsIndexes;
import org.grobid.core.lang.Language;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.LanguageUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Common functionalities to be shared by the language-specific NER parsers
 *
 * @author Patrice Lopez
 */
public class NERParserCommon {

    private static Logger LOGGER = LoggerFactory.getLogger(NERParserCommon.class);

    static public String toFeatureVector(List<String> tokens, LexiconPositionsIndexes positionsIndexes) {
        StringBuffer ress = new StringBuffer();
        int posit = 0; // keep track of the position index in the list of positions

        for (String token : tokens) {
            if (token.equals(" ") || token.equals("\t") || token.equals("\n") || token.equals("\r")) {
                //posit++;
                continue;
            }

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
                    .addFeaturesNER(token,
                            isLocationToken, isPersonTitleToken, isOrganisationToken, isOrgFormToken)
                    .printVector());
            ress.append("\n");
            posit++;
        }
        ress.append("\n");
        return ress.toString();
    }

    public static void merge(List<Entity> entities, List<Sense> senses) {
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
    public static List<Entity> resultExtraction(String text,
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
                } else if (!label.equals("O") && !label.equals("other")) {
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
    static public void createTraining(String inputFile,
                               String outputPath,
                               NERParser parser) throws Exception {
        File file = new File(inputFile);
        if (!file.exists()) {
            throw new GrobidException("Cannot create training data because input file can not be accessed: " + inputFile);
        }

        String data = null;
        if (inputFile.endsWith(".txt") || inputFile.endsWith(".TXT")) {
            data = createTrainingText(file, parser);
        }

        if (data != null) {
            try {
                FileUtils.writeStringToFile(new File(outputPath), data);
            } catch (IOException e) {
                throw new GrobidException("Cannot create training data because output file can not be accessed: " + outputPath, e);
            }
        }
    }

    static public String createTrainingText(File file, NERParser parser) throws IOException {
        String text = FileUtils.readFileToString(file);

        return parser.createTrainingFromText(text);
    }

    static public int createTrainingBatch(String inputDirectory,
                                   String outputDirectory,
                                   NERParser parser) throws IOException {
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
                    createTraining(file.getAbsolutePath(), outputPath, parser);
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
