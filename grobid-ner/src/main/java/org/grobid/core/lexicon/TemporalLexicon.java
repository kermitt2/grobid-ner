package org.grobid.core.lexicon;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.grobid.core.utilities.GrobidProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.lowerCase;

public class TemporalLexicon {

    // Predefined values for loading months or days
    public static final Integer DAYS_NB_ITEMS = 8;
    public static final String DAYS_CSV_FILENAME = "days_multilanguage.csv";
    public static final Integer MONTHS_NB_ITEMS = 13;
    public static final String MONTHS_CSV_FILENAME = "months_multilanguage.csv";

    private List<String> dictionaryDays = new ArrayList<>();
    private List<String> dictionaryMonths = new ArrayList<>();

    public Pattern year = Pattern.compile("[1,2][0-9][0-9][0-9]");

    public Pattern month = Pattern.compile("1|2|3|4|5|6|7|8|9|10|11|12");
    public Pattern day = Pattern.compile("1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31");

    private static TemporalLexicon instance;

    public static TemporalLexicon getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new TemporalLexicon("/temporalLexicon");
    }

    /**
     * Just for testing
     */
    TemporalLexicon(boolean testing) {
    }

    private TemporalLexicon(String lexiconLocation) {
        Map<String, List<String>> lexiconDays = load(lexiconLocation +
                File.separator + DAYS_CSV_FILENAME, DAYS_NB_ITEMS);
        dictionaryDays = fillDictionaryList(lexiconDays);

        Map<String, List<String>> lexiconMonths = load(lexiconLocation +
                File.separator + MONTHS_CSV_FILENAME, MONTHS_NB_ITEMS);
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
                if (!unAccented.equalsIgnoreCase(valueLowerCase)) {
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

    public boolean isMonthNameMatching(String potentialMonth) {
        return dictionaryMonths.contains(lowerCase(potentialMonth));
    }

    public boolean isDayNameMatching(String potentialDay) {
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
