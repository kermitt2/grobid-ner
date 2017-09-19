package org.grobid.core.features;

import org.grobid.core.utilities.TextUtilities;

import java.util.regex.Matcher;

public class FeaturesVectorMultiDates {

    public String string = null;            // lexical feature
    public String label = null;             // label if known

    public String capitalisation = null;    // one of INITCAP, ALLCAPS, NOCAPS
    public String digit;                    // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;

    public String punctType = null;
    // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)
    // OPENQUOTE, ENDQUOTE

    // lexical features
    public boolean year = false;
    public boolean month = false;

    public String shadowNumber = null; // Convert digits to “0”

    public String wordShape = null;
    // Convert upper-case letters to "X", lower- case letters to "x", digits to "d" and other to "c"
    // there is also a trimmed variant where sequence of similar character shapes are reduced to one
    // converted character shape
    public String wordShapeTrimmed = null;


    static public FeaturesVectorMultiDates addFeatures(String word, String label) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        FeaturesVectorMultiDates featuresVector = new FeaturesVectorMultiDates();

        featuresVector.string = word;
        featuresVector.label = label;

        if (word.length() == 1) {
            featuresVector.singleChar = true;
        }

        if (featureFactory.test_all_capital(word)) {
            featuresVector.capitalisation = "ALLCAPS";
        }else if (featureFactory.test_first_capital(word)) {
            featuresVector.capitalisation = "INITCAP";
        }else {
            featuresVector.capitalisation = "NOCAPS";
        }

        if (featureFactory.test_number(word)) {
            featuresVector.digit = "ALLDIGIT";
        }else if (featureFactory.test_digit(word)) {
            featuresVector.digit = "CONTAINDIGIT";
        }else {
            featuresVector.digit = "NODIGIT";
        }

        Matcher m0 = featureFactory.isPunct.matcher(word);
        if (m0.find()) {
            featuresVector.punctType = "PUNCT";
        } else if ((word.equals("(")) || (word.equals("["))) {
            featuresVector.punctType = "OPENBRACKET";
        } else if ((word.equals(")")) | (word.equals("]"))) {
            featuresVector.punctType = "ENDBRACKET";
        } else if (word.equals(".")) {
            featuresVector.punctType = "DOT";
        } else if (word.equals(",")) {
            featuresVector.punctType = "COMMA";
        } else if (word.equals("-")) {
            featuresVector.punctType = "HYPHEN";
        } else if (word.equals("\"") || word.equals("\'") || word.equals("`")) {
            featuresVector.punctType = "QUOTE";
        }  else if(word.equals("/") || word.equals("\\")) {
            featuresVector.punctType = "SLASH";
        }

        if (featuresVector.capitalisation == null)
            featuresVector.capitalisation = "NOCAPS";

        if (featuresVector.digit == null)
            featuresVector.digit = "NODIGIT";

        if (featuresVector.punctType == null)
            featuresVector.punctType = "NOPUNCT";

        Matcher m2 = featureFactory.year.matcher(word);
        if (m2.find()) {
            featuresVector.year = true;
        }

        if (featureFactory.test_month(word)) {
            featuresVector.month = true;
        }

        featuresVector.shadowNumber = TextUtilities.shadowNumbers(word);

        featuresVector.wordShape = TextUtilities.wordShape(word);

        featuresVector.wordShapeTrimmed = TextUtilities.wordShapeTrimmed(word);

        return featuresVector;
    }

    public String printVector() {
        if (string == null) return null;
        if (string.length() == 0) return null;
        StringBuffer res = new StringBuffer();

        // token string (1)
        res.append(string);

        // lowercase string
        res.append(" " + string.toLowerCase());

        // prefix (4)
        res.append(" " + TextUtilities.prefix(string, 1));
        res.append(" " + TextUtilities.prefix(string, 2));
        res.append(" " + TextUtilities.prefix(string, 3));
        res.append(" " + TextUtilities.prefix(string, 4));

        // suffix (4)
        res.append(" " + TextUtilities.suffix(string, 1));
        res.append(" " + TextUtilities.suffix(string, 2));
        res.append(" " + TextUtilities.suffix(string, 3));
        res.append(" " + TextUtilities.suffix(string, 4));


        // capitalisation (1)
        if (digit.equals("ALLDIGIT"))
            res.append(" NOCAPS");
        else
            res.append(" " + capitalisation);

        // digit information (1)
        res.append(" " + digit);

        // character information (1)
        if (singleChar)
            res.append(" 1");
        else
            res.append(" 0");

        // lexical information (2)
        if (year)
            res.append(" 1");
        else
            res.append(" 0");

        if (month)
            res.append(" 1");
        else
            res.append(" 0");

        // punctuation information (2)
        res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

        res.append(" " + wordShape);

        // label - for training data (1)
        if (label != null)
            res.append(" " + label + "\n");
        else
            res.append(" 0\n");

        return res.toString();
    }

}
