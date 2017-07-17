package org.grobid.core.lexicon;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;


/**
 * Class for managing the lexical resources for NER.
 *
 * @author Patrice Lopez
 */
public class NERLexicon {

    // NER base types
    public enum NER_Type {
        UNKNOWN("UNKNOWN"),
        PERSON("PERSON"),
        LOCATION("LOCATION"),
        ORGANISATION("ORGANISATION"),
        ACRONYM("ACRONYM"),
        ANIMAL("ANIMAL"),
        ARTIFACT("ARTIFACT"),
        BUSINESS("BUSINESS"),
        INSTITUTION("INSTITUTION"),
        MEASURE("MEASURE"),
        AWARD("AWARD"),
        CONCEPT("CONCEPT"),
        CONCEPTUAL("CONCEPTUAL"),
        CREATION("CREATION"),
        EVENT("EVENT"),
        LEGAL("LEGAL"),
        IDENTIFIER("IDENTIFIER"),
        INSTALLATION("INSTALLATION"),
        MEDIA("MEDIA"),
        NATIONAL("NATIONAL"),
        SUBSTANCE("SUBSTANCE"),
        PLANT("PLANT"),
        PERIOD("PERIOD"),
        TITLE("TITLE"),
        PERSON_TYPE("PERSON_TYPE"),
        WEBSITE("WEBSITE"),
        ATHLETIC_TEAM("ATHLETIC_TEAM");

        private String name;

        private NER_Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        static public NER_Type mapIdilia(String type) {
            if (type.equals("person/N1")) {
                return NER_Type.PERSON;
            } else if (type.equals("location/N1")) {
                return NER_Type.LOCATION;
            } else if (type.equals("organizational_unit/N1")) {
                return NER_Type.ORGANISATION;
            } else if (type.equals("acronym/N1")) {
                return NER_Type.ACRONYM;
            } else if (type.equals("animal/N1")) {
                return NER_Type.ANIMAL;
            } else if (type.equals("artifact/N1")) {
                return NER_Type.ARTIFACT;
            } else if (type.equals("business/N1")) {
                return NER_Type.BUSINESS;
            } else if (type.equals("institution/N2")) {
                return NER_Type.INSTITUTION;
            } else if (type.equals("measure/N3")) {
                return NER_Type.MEASURE;
            } else if (type.equals("award/N2")) {
                return NER_Type.AWARD;
            } else if (type.equals("concept/N1")) {
                return NER_Type.CONCEPT;
            } else if (type.equals("conceptual/J1")) {
                return NER_Type.CONCEPTUAL;
            } else if (type.equals("creation/N2")) {
                return NER_Type.CREATION;
            } else if (type.equals("event/N1")) {
                return NER_Type.EVENT;
            } else if (type.equals("identifier/N1")) {
                return NER_Type.IDENTIFIER;
            } else if (type.equals("installation/N2")) {
                return NER_Type.INSTALLATION;
            } else if (type.equals("media/N1")) {
                return NER_Type.MEDIA;
            } else if (type.equals("national/J3")) {
                return NER_Type.NATIONAL;
            } else if (type.equals("naturally-occurring_substance/N1")) {
                return NER_Type.SUBSTANCE;
            } else if (type.equals("plant/N2")) {
                return NER_Type.PLANT;
            } else if (type.equals("time_period/N1")) {
                return NER_Type.PERIOD;
            } else if (type.equals("title/N6")) {
                return NER_Type.TITLE;
            } else if (type.equals("type_of_person/N1")) {
                return NER_Type.PERSON_TYPE;
            } else if (type.equals("website/N1")) {
                return NER_Type.WEBSITE;
            } else if (type.equals("athletic_team/N1")) {
                return NER_Type.ATHLETIC_TEAM;
            } else if (type.equals("NETYPE_UNKNOWN/N1")) {
                return NER_Type.UNKNOWN;
            } else {
                return NER_Type.UNKNOWN;
            }
        }

        public static NER_Type getTypeByValue(String value) {
            for (NER_Type type: NER_Type.values()) {
                if (StringUtils.equalsIgnoreCase(value, type.toString())) {
                    return type;
                }
            }
            return null;
        }
    }

    private static Logger LOGGER = LoggerFactory.getLogger(NERLexicon.class);

    private static volatile NERLexicon instance;

    private Map<String, String> descriptions = new TreeMap<String, String>();

    public static synchronized NERLexicon getInstance() {
        if (instance == null)
            instance = new NERLexicon();

        return instance;
    }

    private NERLexicon() {

        String pathSenseDescriptions = GrobidProperties.getGrobidHomePath() + "/lexicon/senses/descriptions.txt";
        // read the Wordnet descriptions
        try {
            BufferedReader bufReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(pathSenseDescriptions)));
            String line;
            while ((line = bufReader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                if (line.trim().startsWith("#")) {
                    // this is a comment
                    continue;
                }
                String[] parts = line.trim().split("\t");
                if (parts.length != 2) {
                    continue;
                }
                descriptions.put(parts[0], parts[1]);
            }
            bufReader.close();
        } catch (IOException e) {
            throw new GrobidException("Error reading word sense dfescriptions file.", e);
        }
    }

    public String getDescription(String label) {
        return descriptions.get(label);
    }

    public static final String START_ENTITY_LABEL_PREFIX = "B-";

    public static String getPlainLabel(String label) {
        if (label.startsWith(START_ENTITY_LABEL_PREFIX))
            return label.substring(2);
        else 
            return label;
    }

    public static boolean isBeginningOfEntity(String label) {
        return label.startsWith(START_ENTITY_LABEL_PREFIX);
    }
}
