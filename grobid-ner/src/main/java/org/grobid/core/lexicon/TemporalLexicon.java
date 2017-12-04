package org.grobid.core.lexicon;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.lowerCase;

public class TemporalLexicon {

    // Predefined values for loading months or days
    public static final Integer DAYS = 8;
    public static final Integer MONTHS = 13;

    private List<String> dictionaryDays = new ArrayList<>();
    private List<String> dictionaryMonths = new ArrayList<>();

    public TemporalLexicon() {

    }

    public TemporalLexicon(String lexiconLocation) {
        Map<String, List<String>> lexiconDays = load(lexiconLocation, DAYS);
        dictionaryDays = fillDictionaryList(lexiconDays);

        Map<String, List<String>> lexiconMonths = load(lexiconLocation, MONTHS);
        dictionaryMonths = fillDictionaryList(lexiconMonths);
    }

    protected List<String> fillDictionaryList(Map<String, List<String>> lexiconDays) {
        List<String> dictionaryList = new ArrayList<>();
        for (String key : lexiconDays.keySet()) {
            for (String value : lexiconDays.get(key)) {
                final String valueLowerCase = lowerCase(value);
                dictionaryList.add(valueLowerCase);

                //Remove accents
                final String unAccented = Normalizer
                        .normalize(valueLowerCase, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "");
                if(!unAccented.equalsIgnoreCase(valueLowerCase)) {
                    dictionaryList.add(unAccented);
                }

                if ("english".equalsIgnoreCase(key)) {
                    dictionaryList.add(valueLowerCase.substring(0, 3));
                }

            }
        }
        return dictionaryList;
    }

    public Map<String, List<String>> load(String location, int nbItems) {

        InputStream is = this.getClass().getResourceAsStream(location);

        return load(is, nbItems);
    }

    public Map<String, List<String>> load(InputStream is, int nbItems) {
        Map<String, List<String>> multilanguageDayNamesDictionary = new HashMap<>();

        try {
            String lexiconString = IOUtils.toString(is, UTF_8);

            CSVParser parser = CSVParser.parse(lexiconString, CSVFormat.DEFAULT);
            for (final CSVRecord record : parser) {
                String language = record.get(0);

                List<String> translations = new ArrayList<>();
                for (int i = 1; i < nbItems; i++) {
                    translations.add(record.get(i));
                }

                multilanguageDayNamesDictionary.put(language, translations);

            }

        } catch (IOException e) {

        }

        return multilanguageDayNamesDictionary;
    }

    public boolean isMonth(String potentialMonth) {
        return dictionaryMonths.contains(lowerCase(potentialMonth));
    }

    public boolean isDay(String potentialDay) {
        return dictionaryDays.contains(lowerCase(potentialDay));
    }

    public void setDictionaryDays(List<String> dictionaryDays) {
        this.dictionaryDays = dictionaryDays;
    }

    public void setDictionaryDays(Map<String, List<String>> dictionaryDays) {
        this.dictionaryDays = fillDictionaryList(dictionaryDays);
    }

    public void setDictionaryMonths(List<String> dictionaryMonths) {
        this.dictionaryMonths = dictionaryMonths;
    }

    public void setDictionaryMonths(Map<String, List<String>> dictionaryMonths) {
        this.dictionaryMonths = fillDictionaryList(dictionaryMonths);
    }
}
