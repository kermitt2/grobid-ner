package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Entity;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.lang.Language;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.LexiconPositionsIndexes;
import org.grobid.core.utilities.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * NER for French
 *
 * @author Patrice Lopez
 */
public class NERFrParser extends AbstractParser implements NERParser {

    private static Logger LOGGER = LoggerFactory.getLogger(NERFrParser.class);

    public static String LANG_ID = "fr";

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
            tokens = GrobidAnalyzer.getInstance().tokenize(text, new Language(LANG_ID, 1.0));
        } catch (Exception e) {
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
            tokens = GrobidAnalyzer.getInstance().tokenize(text, new Language(LANG_ID, 1.0));
        } catch (Exception e) {
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
