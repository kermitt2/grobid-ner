package org.grobid.core.engines;

import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.segmentation.AbstractSegmenter;
import com.googlecode.clearnlp.tokenization.AbstractTokenizer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Sense;
import org.grobid.core.data.Sentence;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorNER;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lexicon.LexiconPositionsIndexes;
import org.grobid.core.tokenization.LabeledTokensContainer;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

    /*static public String toFeatureVector(List<String> tokens, LexiconPositionsIndexes positionsIndexes) {
        StringBuffer ress = new StringBuffer();
        int posit = 0; // keep track of the position index in the list of positions

        for (String token : tokens) {
            if (token.equals(" ") || 
                token.equals("\t") || 
                token.equals("\n") || 
                token.equals("\r") || 
                token.equals("\u00A0")) {
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
    }*/

    public static String toFeatureVectorLayout(List<LayoutToken> tokens, LexiconPositionsIndexes positionsIndexes) {
        StringBuffer ress = new StringBuffer();
        int posit = 0; // keep track of the position index in the list of positions

        for (LayoutToken token : tokens) {
            if ((token.getText() == null) ||
                    (token.getText().length() == 0) ||
                    token.getText().equals(" ") ||
                    token.getText().equals("\t") ||
                    token.getText().equals("\n") ||
                    token.getText().equals("\r") ||
                    token.getText().equals("\u00A0")) {
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
                    .addFeaturesNER(token.getText(),
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
     * Extract the named entities from a labelled sequence of LayoutToken.
     * This version use the new Clusteror class.
     */
    public List<Entity> resultExtraction(GrobidModels model, String result, List<LayoutToken> tokenizations) {

        // convert to usual Grobid label scheme to use TaggingTokenClusteror
        result = result.replace("\tB-", "\tI-");

        List<Entity> entities = new ArrayList<Entity>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(model, result, tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();
        Entity currentEntity = null;
        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            if (clusterLabel.getLabel().equals("O"))
                continue;

            Engine.getCntManager().i(clusterLabel);

            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));
            currentEntity = new Entity();
            currentEntity.setRawName(clusterContent);
            currentEntity.setTypeFromString(GenericTaggerUtils.getPlainIOBLabel(clusterLabel.getLabel()));
            currentEntity.setBoundingBoxes(BoundingBoxCalculator.calculate(cluster.concatTokens()));
            currentEntity.setOffsets(calculateOffsets(cluster));
            currentEntity.setLayoutTokens(cluster.concatTokens());
            entities.add(currentEntity);
        }

        return entities;
    }

    private OffsetPosition calculateOffsets(TaggingTokenCluster cluster) {
        final List<LabeledTokensContainer> labeledTokensContainers = cluster.getLabeledTokensContainers();
        if (CollectionUtils.isEmpty(labeledTokensContainers) || CollectionUtils.isEmpty(labeledTokensContainers.get(0).getLayoutTokens())) {
            return new OffsetPosition();
        }

        final LabeledTokensContainer labeledTokensContainer = labeledTokensContainers.get(0);
        final List<LayoutToken> layoutTokens = labeledTokensContainer.getLayoutTokens();

        int start = layoutTokens.get(0).getOffset();
        int end = start + LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens())).length();

        return new OffsetPosition(start, end);
    }

    /**
     * Extract the named entities from a labelled text.
     * Use the new method using the clusteror List
     * resultExtraction(GrobidModels model, String result, List<LayoutToken> tokenizations)
     */
    /*@Deprecated
    public static List<Entity> resultExtraction(String text,
                                                List<Pair<String, String>> labeled,
                                                List<LayoutToken> tokenizations) {

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
                String tokOriginal = tokenizations.get(p).getText();
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
    }*/


    /**
     * Process the content of the specified input file and format the result as training data.
     * <p>
     * Input file should be a text file. Each file is a paragraph entry that it's normally processed by NERD.
     */
    static public StringBuilder createTraining(String inputFile,
                                               String outputPath,
                                               String fileName,
                                               NERParser parser,
                                               String lang,
                                               AbstractTokenizer tokenizer) throws Exception {
        File file = new File(inputFile);
        if (!file.exists()) {
            throw new GrobidException("Cannot create training data because input file can not be accessed: " + inputFile);
        }

        StringBuilder sb = new StringBuilder();
        if (inputFile.endsWith(".txt") || inputFile.endsWith(".TXT")) {
            sb.append(xmlHeader);

            // we use the name of the file as document ID, removing spaces, 
            // note that it could lead to non wellformed XML for weird file names
            sb.append("\t\t<document name=\"" + fileName.replace(" ", "_") + "\">\n");
            createTrainingText(file, parser, lang, tokenizer, sb);
            sb.append("\t\t</document>\n");

            sb.append(xmlEnd);
        }

        if (sb.length() > 0) {
            try {
                FileUtils.writeStringToFile(new File(outputPath), sb.toString());
            } catch (IOException e) {
                throw new GrobidException("Cannot create training data because output file can not be accessed: " + outputPath, e);
            }
        }
        return sb;
    }

    public static StringBuilder createTrainingText(File file, NERParser parser, String lang, AbstractTokenizer tokenizer, StringBuilder sb) throws IOException {
        String text = FileUtils.readFileToString(file, "UTF-8");

        if (isEmpty(text))
            return null;

        // let's segment in paragraphs, assuming we have one per paragraph per line
        String[] paragraphs = text.split("\n");

        for (int p = 0; p < paragraphs.length; p++) {
            String theText = paragraphs[p];
            if (theText.trim().length() == 0)
                continue;

            sb.append("\t\t\t<p xml:lang=\"" + lang + "\" xml:id=\"P" + p + "\">\n");

            // we process NER at paragraph level (as it is trained at this level and because 
            // inter sentence features/template are used by the CFR)
            List<Entity> entities = parser.extractNE(theText);
            //int currentEntityIndex = 0;

            // let's segment in sentences with ClearNLP (to be updated to the newest NLP4J !)
            // this is only outputed for readability
            List<Sentence> sentences = NERParserCommon.sentenceSegmentation(theText, lang, tokenizer);
            int sentenceIndex = 0;
            for (int s = 0; s < sentences.size(); s++) {
                Sentence sentence = sentences.get(s);
                int sentenceStart = sentence.getOffsetStart();
                int sentenceEnd = sentence.getOffsetEnd();

                sb.append("\t\t\t\t<sentence xml:id=\"P" + p + "E" + sentenceIndex + "\">");

                if ((entities == null) || (entities.size() == 0)) {
                    // don't forget to encode the text for XML
                    sb.append(TextUtilities.HTMLEncode(theText.substring(sentenceStart, sentenceEnd)));
                } else {
                    int index = sentenceStart;
                    // smal adjustement to avoid sentence starting with a space
                    if (theText.charAt(index) == ' ')
                        index++;
                    for (Entity entity : entities) {
                        if (entity.getOffsetEnd() < sentenceStart)
                            continue;
                        if (entity.getOffsetStart() >= sentenceEnd)
                            break;

                        int entityStart = entity.getOffsetStart();
                        int entityEnd = entity.getOffsetEnd();

                        // don't forget to encode the text for XML
                        if (index < entityStart)
                            sb.append(TextUtilities.HTMLEncode(theText.substring(index, entityStart)));
                        sb.append("<ENAMEX type=\"" + entity.getType().getName() + "\">");
                        sb.append(TextUtilities.HTMLEncode(theText.substring(entityStart, entityEnd)));
                        sb.append("</ENAMEX>");

                        index = entityEnd;

                        while (index > sentenceEnd) {
                            // bad luck, the sentence segmentation or ner failed somehow and we have an 
                            // entity across 2 sentences, so we merge on the fly these 2 sentences, which is
                            // easier than it looks ;)
                            s++;
                            if (s >= sentences.size())
                                break;
                            sentence = sentences.get(s);
                            sentenceStart = sentence.getOffsetStart();
                            sentenceEnd = sentence.getOffsetEnd();
                        }
                    }

                    if (index < sentenceEnd)
                        sb.append(TextUtilities.HTMLEncode(theText.substring(index, sentenceEnd)));
                    //else if (index > sentenceEnd)
                    //System.out.println(theText.length() + " / / " + theText + "/ / " + index + " / / " + sentenceEnd);
                }

                sb.append("</sentence>\n");
                sentenceIndex++;
            }

            sb.append("\t\t\t</p>\n");
        }
        return sb;
    }

    static public int createTrainingBatch(String inputDirectory,
                                          String outputDirectory,
                                          NERParser parser,
                                          String lang) throws IOException {
        // note that at the stage, we have already selected the NERParser according to the language
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

            // ClearParser components for sentence segmentation
            // slow down a bit at launch, but it is used only for generating more readable training
            String dictionaryFile = "data/clearNLP/dictionary-1.3.1.zip";
            LOGGER.info("Loading dictionary file for sentence segmentation: " + dictionaryFile);
            AbstractTokenizer tokenizer = EngineGetter.getTokenizer(lang, new FileInputStream(dictionaryFile));
            LOGGER.info("End of loading dictionary file");

            for (final File file : refFiles) {
                try {
                    String fileName = file.getName().substring(0, file.getName().length() - 4);
                    String outputPath = outputDirectory + "/" + fileName + ".training.xml";
                    createTraining(file.getAbsolutePath(), outputPath, fileName, parser, lang, tokenizer);
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

    // some pieces of XML for generating training data
    public static String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<corpus>\n\t<subcorpus>\n";
    public static String xmlEnd = "\t</subcorpus>\n</corpus>\n";

    public static List<Sentence> sentenceSegmentation(String text, String language, AbstractTokenizer tokenizer) {
        AbstractSegmenter segmenter = EngineGetter.getSegmenter(language, tokenizer);
        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(text.getBytes());
        // read it with BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<List<String>> sentences = segmenter.getSentences(br);
        List<Sentence> results = new ArrayList<Sentence>();

        if ((sentences == null) || (sentences.size() == 0)) {
            // there is some text but not in a state so that a sentence at least can be
            // identified by the sentence segmenter, so we parse it as a single sentence
            Sentence sentence = new Sentence();
            OffsetPosition pos = new OffsetPosition();
            pos.start = 0;
            pos.end = text.length();
            sentence.setOffsets(pos);
            results.add(sentence);
            return results;
        }

        // we need to realign with the original sentences, so we have to match it from the text 
        // to be parsed based on the tokenization
        int offSetSentence = 0;
        //List<List<String>> trueSentences = new ArrayList<List<String>>();
        for (List<String> theSentence : sentences) {
            int next = offSetSentence;
            for (String token : theSentence) {
                next = text.indexOf(token, next);
                next = next + token.length();
            }
            List<String> dummy = new ArrayList<String>();
            //dummy.add(text.substring(offSetSentence, next));   
            //trueSentences.add(dummy);   
            Sentence sentence = new Sentence();
            OffsetPosition pos = new OffsetPosition();
            pos.start = offSetSentence;
            pos.end = next;
            sentence.setOffsets(pos);
            results.add(sentence);
            offSetSentence = next;
        }
        return results;
    }
}
