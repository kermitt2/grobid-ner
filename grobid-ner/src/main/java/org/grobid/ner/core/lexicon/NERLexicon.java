package org.grobid.core.lexicon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.lang.Language;
import org.grobid.core.sax.CountryCodeSaxParser;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.grobid.core.lexicon.FastMatcher;
import org.grobid.core.data.Entity;


/**
 * Class for managing the lexical resources for NER.
 *
 * @author Patrice Lopez
 */
public class NERLexicon {

	// NER base types
	public enum NER_Type {
		UNKNOWN				("UNKNOWN"),
		PERSON				("PERSON"),
		LOCATION			("LOCATION"),
		ORGANISATION		("ORGANISATION"),
		ACRONYM				("ACRONYM"),
		ANIMAL				("ANIMAL"),
		ARTIFACT			("ARTIFACT"),
		BUSINESS			("BUSINESS"),
		INSTITUTION			("INSTITUTION"),
		MEASURE				("MEASURE"),
		AWARD				("AWARD"),
		CONCEPT				("CONCEPT"),
		CONCEPTUAL			("CONCEPTUAL"),
		CREATION			("CREATION"),
		EVENT				("EVENT"),
		IDENTIFIER			("IDENTIFIER"),
		INSTALLATION		("INSTALLATION"),
		MEDIA				("MEDIA"),
		NATIONAL			("NATIONAL"),
		SUBSTANCE			("SUBSTANCE"),
		PLANT				("PLANT"),
		PERIOD				("PERIOD"),
		TITLE				("TITLE"),
		PERSON_TYPE			("PERSON_TYPE"),
		WEBSITE				("WEBSITE"),
		ATHLETIC_TEAM		("ATHLETIC_TEAM");
		
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
			}
			else if (type.equals("location/N1")) {
				return NER_Type.LOCATION;
			}
			else if (type.equals("organizational_unit/N1")) {
				return NER_Type.ORGANISATION;
			}
			else if (type.equals("acronym/N1")) {
				return NER_Type.ACRONYM;
			}
			else if (type.equals("animal/N1")) {
				return NER_Type.ANIMAL;
			}
			else if (type.equals("artifact/N1")) {
				return NER_Type.ARTIFACT;
			}
			else if (type.equals("business/N1")) {
				return NER_Type.BUSINESS;
			}
			else if (type.equals("institution/N2")) {
				return NER_Type.INSTITUTION;
			}
			else if (type.equals("measure/N3")) {
				return NER_Type.MEASURE;
			}
			else if (type.equals("award/N2")) {
				return NER_Type.AWARD;
			}
			else if (type.equals("concept/N1")) {
				return NER_Type.CONCEPT;
			}
			else if (type.equals("conceptual/J1")) {
				return NER_Type.CONCEPTUAL;
			}
			else if (type.equals("creation/N2")) {
				return NER_Type.CREATION;
			}
			else if (type.equals("event/N1")) {
				return NER_Type.EVENT;
			}
			else if (type.equals("identifier/N1")) {
				return NER_Type.IDENTIFIER;
			}
			else if (type.equals("installation/N2")) {
				return NER_Type.INSTALLATION;
			}
			else if (type.equals("media/N1")) {
				return NER_Type.MEDIA;
			}
			else if (type.equals("national/J3")) {
				return NER_Type.NATIONAL;
			}
			else if (type.equals("naturally-occurring_substance/N1")) {
				return NER_Type.SUBSTANCE;
			}
			else if (type.equals("plant/N2")) {
				return NER_Type.PLANT;
			}
			else if (type.equals("time_period/N1")) {
				return NER_Type.PERIOD;
			}
			else if (type.equals("title/N6")) {
				return NER_Type.TITLE;
			}
			else if (type.equals("type_of_person/N1")) {
				return NER_Type.PERSON_TYPE;
			}
			else if (type.equals("website/N1")) {
				return NER_Type.WEBSITE;
			}
			else if (type.equals("athletic_team/N1")) {
				return NER_Type.ATHLETIC_TEAM;
			}
			else if (type.equals("NETYPE_UNKNOWN/N1")) {
				return NER_Type.UNKNOWN;
			}
			else {
				return NER_Type.UNKNOWN;
			}
		}
	};
	
	private static Logger LOGGER = LoggerFactory.getLogger(NERLexicon.class);
	
	private static volatile NERLexicon instance;
	
	public static NERLexicon getInstance() {
        if (instance == null) {
            //double check idiom
            // synchronized (instanceController) {
                if (instance == null)
					getNewInstance();
            // }
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
	private static synchronized void getNewInstance() {
		instance = new NERLexicon();
	}
	
	
}
		