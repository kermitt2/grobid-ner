package org.grobid.core.engines;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Sense;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.lang.Language;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.LexiconPositionsIndexes;
//import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.LayoutTokensUtil;

import org.grobid.core.layout.LayoutToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * NER for English
 *
 * @author Patrice Lopez
 */
public class NEREnParser extends AbstractParser implements NERParser {

    private static Logger LOGGER = LoggerFactory.getLogger(NEREnParser.class);
    private final NERParserCommon nerParserCommon;

    protected Lexicon lexicon = Lexicon.getInstance();
    //protected SenseTagger senseTagger = null;


    public NEREnParser(GrobidModel model) {
        super(model);
        nerParserCommon = new NERParserCommon();
        //senseTagger = new SenseTagger();
    }

    public NEREnParser() {
        this(GrobidModels.ENTITIES_NER);
    }

    /**
     * Extract all occurrences of named entities from a simple piece of text. 
     * The positions of the recognized entities are given as character offsets 
     * (following Java specification of characters).
     */
    public List<Entity> extractNE(String text) {
        List<LayoutToken> tokens = null;
        try {
            tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text, new Language(Language.EN, 1.0));
        } catch(Exception e) {
            LOGGER.error("Tokenization failed. ", e);
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
        List<Entity> entities = nerParserCommon.resultExtraction(GrobidModels.ENTITIES_NER, result, tokens);

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
            tokens =  GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text, new Language(Language.EN, 1.0));
        } catch(Exception e) {
            LOGGER.error("Tokenization failed", e);
            return null;
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

    /*public StringBuilder createXMLTrainingFromText(String text, StringBuilder sb) {
        if (isEmpty(text))
            return null;

        // lazy loading of the sentence segmenter, as it is used only for generating more readable 
        // training
        if (tokenizer == null) {
            String dictionaryFile = "data/clearNLP/dictionary-1.3.1.zip";
            tokenizer = EngineGetter.getTokenizer(language, new FileInputStream(dictionaryFile));
        }

        // let's segment in paragraphs, assuming we have one per paragraph per line
        String[] paragraphs = text.split("\n");

        for(int p=0; p<paragraphs.length; p++) {
            String theText = paragraphs[p];
            if (theText.trim().length() == 0)
                continue;

            sb.append("\t\t\t<p>\n");

            // we process NER at paragraph level (as it is trained at this level and because 
            // inter sentence features/template are used by the CFR)
            List<Entity> entitites = parser.extractNE(theText);

            // let's segment in sentences with ClearNLP (to be updated to the newest NLP4J !)
            // this is only outputed for readability
            List<Sentence> sentences = NERParserCommon.sentenceSegmentation(String text, AbstractReader.LANG_EN, tokenizer);
            for(Sentence sentence : sentences) {

                

                for (Entity entity : entities) {

                    // don't forget to encode the text for XML
                }
            }

            sb.append("\t\t\t</p>\n");
        }    
        return sb;
    }*/

}
