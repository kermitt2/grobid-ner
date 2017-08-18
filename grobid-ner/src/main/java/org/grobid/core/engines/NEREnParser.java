package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Sense;
import org.grobid.core.data.Sentence;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.lang.Language;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.LexiconPositionsIndexes;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.LayoutTokensUtil;

import org.grobid.core.layout.LayoutToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * NER for English
 *
 * @author Patrice Lopez
 */
public class NEREnParser extends AbstractParser implements NERParser {

    private static Logger LOGGER = LoggerFactory.getLogger(NEREnParser.class);

    protected Lexicon lexicon = Lexicon.getInstance();
    //protected SenseTagger senseTagger = null;

    public NEREnParser() {
        super(GrobidModels.ENTITIES_NER);
        //senseTagger = new SenseTagger();
    }

    /**
     * Extract all occurrences of named entities from a simple piece of text. 
     * The positions of the recognized entities are given as character offsets 
     * (following Java specification of characters).
     */
    public List<Entity> extractNE(String text) {
        List<String> tokens = null;
        try {
            tokens = GrobidAnalyzer.getInstance().tokenize(text, new Language(Language.EN, 1.0));
        } catch(Exception e) {
            LOGGER.error("Tokenization failed. ", e);
        }
        if (tokens == null)
            return null;
        
        LexiconPositionsIndexes positionsIndexes = new LexiconPositionsIndexes(lexicon);
        positionsIndexes.computeIndexes(text);

        String res = NERParserCommon.toFeatureVector(tokens, positionsIndexes);
        String result = label(res);
        List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(result);

        List<Entity> entities = NERParserCommon.resultExtraction(text, labeled, tokens);

        // we use now the sense tagger for the recognized named entity
        //List<Sense> senses = senseTagger.extractSenses(text, labeled, tokens, positionsIndexes);

        //NERParserCommon.merge(entities, senses);

        return entities;
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
        List<Entity> entities = NERParserCommon.resultExtraction(GrobidModels.ENTITIES_NER, result, tokens);

        // we use now the sense tagger for the recognized named entity
        //List<Sense> senses = senseTagger.extractSenses(labeled, tokens, positionsIndexes);

        //NERParserCommon.merge(entities, senses);

        return entities;
    }
}
