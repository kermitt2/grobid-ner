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
import org.grobid.core.analyzers.GrobidAnalyzer;
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
 * NER for French
 *
 * @author Patrice Lopez
 */
public class NERFrParser extends AbstractParser implements NERParser {

    private static Logger LOGGER = LoggerFactory.getLogger(NERFrParser.class);

    protected Lexicon lexicon = Lexicon.getInstance();

    public NERFrParser() {
        super(GrobidModels.ENTITIES_NERFR);
    }

    /**
     * Extract all occurrences of named entity from a simple piece of text.
     */
    public List<Entity> extractNE(String text) {
        List<String> tokens = null;
        try {
            tokens = GrobidAnalyzer.getInstance().tokenize(text, new Language(Language.FR, 1.0));
        } catch(Exception e) {
            LOGGER.error("Tokenization failed", e);
        }
        if (tokens == null)
            return null;
        
        LexiconPositionsIndexes positionsIndexes = new LexiconPositionsIndexes(lexicon);
        positionsIndexes.computeIndexes(text);

        String res = NERParserCommon.toFeatureVector(tokens, positionsIndexes);
        String result = label(res);
        List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(result);

        List<Entity> entities = NERParserCommon.resultExtraction(text, labeled, tokens);

        return entities;
    }

    public String createTrainingFromText(String text) {
        if (isEmpty(text))
            return null;

        text = text.replace("\n", " ");

        List<String> tokens = null;
        try {
            tokens = GrobidAnalyzer.getInstance().tokenize(text, new Language(Language.FR, 1.0));
        } catch(Exception e) {
            LOGGER.error("Tokenization failed", e);
        }
        LexiconPositionsIndexes positionsIndexes = new LexiconPositionsIndexes(lexicon);
        positionsIndexes.computeIndexes(text);

        String featuresVector = NERParserCommon.toFeatureVector(tokens, positionsIndexes);
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

}
