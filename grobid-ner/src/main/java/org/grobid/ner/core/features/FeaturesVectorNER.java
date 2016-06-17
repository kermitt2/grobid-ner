package org.grobid.ner.core.features;

import org.grobid.core.features.FeatureFactory;
import org.grobid.core.utilities.TextUtilities;

import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * Class for features used for NER in raw texts.
 *
 * @author Patrice Lopez
 */
public class FeaturesVectorNER {

    public String string = null;     // lexical feature
    public String label = null;     // label if known

    public String capitalisation = null;// one of INITCAP, ALLCAPS, NOCAPS
    public String digit;                // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;

    public String punctType = null;
    // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)
    // OPENQUOTE, ENDQUOTE

	// lexical features
    public boolean lastName = false;
    public boolean commonName = false;
    public boolean firstName = false;
	public boolean year = false;
    public boolean month = false;
	public boolean cityName = false;
	public boolean countryName = false;

	public String shadowNumber = null; // Convert digits to “0” 
	
	public String wordShape = null; 
	// Convert upper-case letters to "X", lower- case letters to "x", digits to "d" and other to "c"  
	// there is also a trimmed variant where sequence of similar character shapes are reduced to one
	// converted character shape
	public String wordShapeTrimmed = null;

	public boolean isLocationToken = false;
	public boolean isPersonTitleToken = false;
	public boolean isOrganisationToken = false;
	public boolean isOrgFormToken = false;

    public FeaturesVectorNER() {
    }

    public String printVector() {
        if (string == null) return null;
        if (string.length() == 0) return null;
        StringBuffer res = new StringBuffer();

        // token string (1)
        res.append(string);

        // lowercase string
        res.append(" " + string.toLowerCase());

        // prefix (5)
        res.append(" " + string.substring(0, 1));

        if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

        if (string.length() > 2)
            res.append(" " + string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

        if (string.length() > 3)
            res.append(" " + string.substring(0, 4));
        else if (string.length() > 2)
            res.append(" " + string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

		if (string.length() > 4)
            res.append(" " + string.substring(0, 5));
		else if (string.length() > 3)
            res.append(" " + string.substring(0, 4));
        else if (string.length() > 2)
            res.append(" " + string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));
			
        // suffix (5)
        res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 2)
            res.append(" " + string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 3)
            res.append(" " + string.substring(string.length() - 4, string.length()));
        else if (string.length() > 2)
            res.append(" " + string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));

		if (string.length() > 4)
            res.append(" " + string.substring(string.length() - 5, string.length()));
		else if (string.length() > 3)
            res.append(" " + string.substring(string.length() - 4, string.length()));
        else if (string.length() > 2)
            res.append(" " + string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));
		
        // capitalisation (1)
        if (digit.equals("ALLDIGIT"))
            res.append(" NOCAPS");
        else
            res.append(" " + capitalisation);

        // digit information (1)
        res.append(" " + digit);

        // character information (1)
        /*if (singleChar)
            res.append(" 1");
        else
            res.append(" 0"); */

        // punctuation information (1)
        //res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

		// lexical information (7)
        if (lastName)
            res.append(" 1");
        else
            res.append(" 0");

        if (commonName)
            res.append(" 1");
        else
            res.append(" 0");

        if (firstName)
            res.append(" 1");
        else
            res.append(" 0");

        if (cityName)
            res.append(" 1");
        else
            res.append(" 0");

		if (countryName)
            res.append(" 1");
        else
            res.append(" 0");

        if (year)
            res.append(" 1");
        else
            res.append(" 0");

        if (month)
            res.append(" 1");
        else
            res.append(" 0");

		
		// lexical feature: belongs to a known location (1)
		if (isLocationToken)
			res.append(" 1");
        else
            res.append(" 0");

		// lexical feature: belongs to a known person title (1)
		if (isPersonTitleToken)
			res.append(" 1");
        else
            res.append(" 0");

		// lexical feature: belongs to a known organisation (1)
		if (isOrganisationToken)
			res.append(" 1");
        else
            res.append(" 0");

		// lexical feature: belongs to a known organisation form (1)
		if (isOrgFormToken)
			res.append(" 1");
        else
            res.append(" 0");

        // token length (1)
        //res.append(" " + string.length()); // /

		// shadow number (1)
		//res.append(" " + shadowNumber); // /
		
		// word shape (1)
		res.append(" " + wordShape);
		
		// word shape trimmed (1)
		res.append(" " + wordShapeTrimmed);
		
        // label - for training data (1)
        if (label != null)
            res.append(" " + label + "");
        else
            res.append(" 0");

        return res.toString();
    }

    /**
     * Add the features for the NER model.
     */
    static public FeaturesVectorNER addFeaturesNER(String line,
                                                   boolean isLocationToken,
												   boolean isPersonTitleToken, 
												   boolean isOrganisationToken, 
												   boolean isOrgFormToken) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        FeaturesVectorNER featuresVector = new FeaturesVectorNER();
        StringTokenizer st = new StringTokenizer(line, "\t ");
        if (st.hasMoreTokens()) {
            String word = st.nextToken();
            String label = null;
            if (st.hasMoreTokens())
                label = st.nextToken();

            featuresVector.string = word;
            featuresVector.label = label;

            if (word.length() == 1) {
                featuresVector.singleChar = true;
            }

            if (featureFactory.test_all_capital(word))
                featuresVector.capitalisation = "ALLCAPS";
            else if (featureFactory.test_first_capital(word))
                featuresVector.capitalisation = "INITCAP";
            else
                featuresVector.capitalisation = "NOCAPS";

            if (featureFactory.test_number(word))
                featuresVector.digit = "ALLDIGIT";
            else if (featureFactory.test_digit(word))
                featuresVector.digit = "CONTAINDIGIT";
            else
                featuresVector.digit = "NODIGIT";

            Matcher m0 = featureFactory.isPunct.matcher(word);
            if (m0.find()) {
                featuresVector.punctType = "PUNCT";
            }
            if ((word.equals("(")) | (word.equals("["))) {
                featuresVector.punctType = "OPENBRACKET";
            } else if ((word.equals(")")) | (word.equals("]"))) {
                featuresVector.punctType = "ENDBRACKET";
            } else if (word.equals(".")) {
                featuresVector.punctType = "DOT";
            } else if (word.equals(",")) {
                featuresVector.punctType = "COMMA";
            } else if (word.equals("-")) {
                featuresVector.punctType = "HYPHEN";
            } else if (word.equals("\"") | word.equals("\'") | word.equals("`")) {
                featuresVector.punctType = "QUOTE";
            }

            if (featuresVector.capitalisation == null)
                featuresVector.capitalisation = "NOCAPS";

            if (featuresVector.digit == null)
                featuresVector.digit = "NODIGIT";

            if (featuresVector.punctType == null)
                featuresVector.punctType = "NOPUNCT";

			Matcher m2 = featureFactory.YEAR.matcher(word);
            if (m2.find()) {
                featuresVector.year = true;
            }

			if (featureFactory.test_common(word)) {
                featuresVector.commonName = true;
            }

			if (featureFactory.test_first_names(word)) {
                featuresVector.firstName = true;
            }

            if (featureFactory.test_last_names(word)) {
                featuresVector.lastName = true;
            }

            if (featureFactory.test_month(word)) {
                featuresVector.month = true;
            }

			if (featureFactory.test_city(word)) {
                featuresVector.cityName = true;
            }

			if (featureFactory.test_country(word)) {
                featuresVector.countryName = true;
            }

			featuresVector.isLocationToken = isLocationToken; 
			
			featuresVector.isPersonTitleToken = isPersonTitleToken;
			
			featuresVector.isOrganisationToken = isOrganisationToken;
			
			featuresVector.isOrgFormToken = isOrgFormToken;

			featuresVector.shadowNumber = TextUtilities.shadowNumbers(word);
			
			featuresVector.wordShape = TextUtilities.wordShape(word);
			
			featuresVector.wordShapeTrimmed = TextUtilities.wordShapeTrimmed(word);
        }

        return featuresVector;
    }

}
	
	
