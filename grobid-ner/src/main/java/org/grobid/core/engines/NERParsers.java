package org.grobid.core.engines;

import org.grobid.core.data.Entity;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.lang.Language;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.layout.LayoutToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Extract all occurrences of named entity from list of LayoutToken of unknown language.
     * A language identifier is used to determine the language, and the token sequence is 
     * processed if the identified language is supported.
     */
    public List<Entity> extractNE(List<LayoutToken> tokens) throws GrobidResourceException {
        // run language identifier
        LanguageUtilities languageIdentifier = LanguageUtilities.getInstance();                     
        Language resultLang = null;
        synchronized (languageIdentifier) {       
            resultLang = languageIdentifier.runLanguageId(LayoutTokensUtil.toText(tokens), 2000); 
        }

        return extractNE(tokens, resultLang);
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

        NERParser parser = parsers.get(lang.getLang());
        if (parser == null) {
            throw new GrobidResourceException("The automatically identified language is currently not supported by grobid-ner: " +
                lang.getLang());
        }

        return parser.extractNE(text);
    }

    /**
     * Extract all occurrences of named entity from a list of LayoutToken and a given language.
     */
    public List<Entity> extractNE(List<LayoutToken> tokens, Language lang) throws GrobidResourceException {

        if ((tokens == null) || (tokens.size() == 0))
            return null;

        //text = text.replace("\n", " ");

        if (lang == null) {
            return extractNE(tokens);
        }

        NERParser parser = parsers.get(lang.getLang());
        if (parser == null) {
            throw new GrobidResourceException("The automatically identified language is currently not supported by grobid-ner: " +
                lang.getLang());
        }

        return parser.extractNE(tokens);
    }

    public int createTrainingBatch(String inputDirectory,
                                   String outputDirectory,
                                   String lang) throws Exception {
        NERParser parser = parsers.get(lang);
        if (parser == null) {
            throw new GrobidResourceException("The automatically identified labnguage is currently not supported by grobid-ner: " + 
                lang);
        }
        return NERParserCommon.createTrainingBatch(inputDirectory, outputDirectory, parser, lang);
    }

    public NERParser getParser(String lang) {
        return parsers.get(lang);
    }

 }
