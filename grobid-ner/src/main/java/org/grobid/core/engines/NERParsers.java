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
import java.util.Map;
import java.util.HashMap;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * NER
 *
 * @author Patrice Lopez
 */
public class NERParsers {

    private static Logger LOGGER = LoggerFactory.getLogger(NERParsers.class);

    private Map<String, NERParser> parsers = null;

    public NERParsers() {
        parsers = new HashMap<String, NERParser>();
        // supported languages
        parsers.put("en", new NEREnParser());
        parsers.put("fr", new NERFrParser());
    }

    /**
     * Extract all occurrences of named entity from a simple piece of text of unknown language.
     * A language identifier is used to determine the language, and the text is processed if 
     * the identified language is supported.
     */
    public List<Entity> extractNE(String text) throws GrobidResourceException {
        // run language identifier
        LanguageUtilities languageIdentifier = LanguageUtilities.getInstance();                     
        Language resultLang = null;
        synchronized (languageIdentifier) {       
            resultLang = languageIdentifier.runLanguageId(text);  
        }

        return extractNE(text, resultLang);
    }

    /**
     * Extract all occurrences of named entity from a simple piece of text and a given language.
     */
    public List<Entity> extractNE(String text, Language lang) throws GrobidResourceException {

        if (isEmpty(text))
            return null;

        text = text.replace("\n", " ");

        if (lang == null) {
            return extractNE(text);
        }

        NERParser parser = parsers.get(lang.getLangId());
        if (parser == null) {
            throw new GrobidResourceException("The automatically identified labnguage is currently not supported by grobid-ner: " + 
                lang.getLangId());
        }

        return parser.extractNE(text);
    }

    public int createTrainingBatch(String inputDirectory,
                                   String outputDirectory,
                                   String lang) throws Exception {
        NERParser parser = parsers.get(lang);
        if (parser == null) {
            throw new GrobidResourceException("The automatically identified labnguage is currently not supported by grobid-ner: " + 
                lang);
        }
        return NERParserCommon.createTrainingBatch(inputDirectory, outputDirectory, parser);
    }

    public NERParser getParser(String lang) {
        return parsers.get(lang);
    }

 }
