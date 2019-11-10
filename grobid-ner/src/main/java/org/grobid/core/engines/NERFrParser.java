package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Entity;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.lang.Language;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.LexiconPositionsIndexes;
//import org.grobid.core.utilities.Pair;
import org.grobid.core.layout.LayoutToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * NER for French
 *
 * @author Patrice Lopez
 */
public class NERFrParser extends AbstractParser implements NERParser {

    private static Logger LOGGER = LoggerFactory.getLogger(NERFrParser.class);

    protected Lexicon lexicon = Lexicon.getInstance();
    private final NERParserCommon nerParserCommon;

    public NERFrParser() {
        super(GrobidModels.ENTITIES_NERFR);
        nerParserCommon = new NERParserCommon();
    }

    /**
     * Extract all occurrences of named entity from a simple piece of text.
     * The positions of the recognized entities are given as character offsets 
     * (following Java specification of characters).
     */
    public List<Entity> extractNE(String text) {
        List<LayoutToken> tokens = null;
        try {
            tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text, new Language(Language.FR, 1.0));
        } catch(Exception e) {
            LOGGER.error("Tokenization failed", e);
        }

        return extractNE(tokens);

    }

    /**
     * Extract all occurrences of named entities from a list of LayoutToken
     * coming from a document with fixed/preserved layout, e.g. PDF. 
     * The positions of the recognized entities are given with coordinates in 
     * the input document.
     */
    public List<Entity> extractNE(List<LayoutToken> tokens) {
        if (tokens == null)
            return null;

        LexiconPositionsIndexes positionsIndexes = new LexiconPositionsIndexes(lexicon);
        positionsIndexes.computeIndexes(tokens);

        String res = NERParserCommon.toFeatureVectorLayout(tokens, positionsIndexes);
        String result = label(res);
        //List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(result);

        //String text = LayoutTokensUtil.toText(tokens);
        List<Entity> entities = nerParserCommon.resultExtraction(GrobidModels.ENTITIES_NERFR, result, tokens);

        // we use now the sense tagger for the recognized named entity
        //List<Sense> senses = senseTagger.extractSenses(labeled, tokens, positionsIndexes);

        //NERParserCommon.merge(entities, senses);

        return entities;
    }

    public String createCONNLTrainingFromText(String text) {
        if (isEmpty(text))
            return null;

        text = text.replace("\n", " ");

        List<LayoutToken> tokens = null;
        try {
            tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text, new Language(Language.FR, 1.0));
        } catch(Exception e) {
            LOGGER.error("Tokenization failed", e);
        }
        LexiconPositionsIndexes positionsIndexes = new LexiconPositionsIndexes(lexicon);
        positionsIndexes.computeIndexes(tokens);

        String featuresVector = NERParserCommon.toFeatureVectorLayout(tokens, positionsIndexes);
        String res = label(featuresVector);

        List<Pair<String, String>> labeledEntries = GenericTaggerUtils.getTokensAndLabels(res);
//        List<Sense> senses = senseTagger.extractSenses(text, labeledEntries, blocks.getTokens(), positionsIndexes);

        StringBuilder sb = new StringBuilder();

//        int count = 0;
        for (Pair<String, String> labeledEntry : labeledEntries) {
            String value = labeledEntry.getLeft();
            String label = labeledEntry.getRight();

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
