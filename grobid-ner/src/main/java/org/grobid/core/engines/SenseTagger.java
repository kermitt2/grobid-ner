package org.grobid.core.engines;

import org.apache.commons.collections4.CollectionUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.Sense;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorNERSense;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.LexiconPositionsIndexes;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Sense tagging model for named entities
 *
 * @author Patrice Lopez
 */
public class SenseTagger extends AbstractParser {

	protected NERLexicon nerLexicon = NERLexicon.getInstance();
	protected Lexicon lexicon = Lexicon.getInstance();
	
    public SenseTagger() {
        super(GrobidModels.ENTITIES_NERSense);
    }

    /**
     * Extract all occurences of NER senses from a simple piece of text.
     */
    public List<Sense> extractSenses(String text) throws Exception {
        if (text == null)
            return null;
        if (text.length() == 0)
            return null;
        List<Sense> senses = null;
        try {
            text = text.replace("\n", " ");
			int sentence = 0;
			List<OffsetPosition> locationPositions = lexicon.tokenPositionsLocationNames(text);
			List<OffsetPosition> personTitlePositions = lexicon.tokenPositionsPersonTitle(text);
			List<OffsetPosition> organisationPositions = lexicon.tokenPositionsOrganisationNames(text);
			List<OffsetPosition> orgFormPositions = lexicon.tokenPositionsOrgForm(text);
			int currentPosition = 0;
            StringTokenizer st = new StringTokenizer(text, TextUtilities.fullPunctuations, true);
			
            if (st.countTokens() == 0)
                return null;
			
            List<String> textBlocks = new ArrayList<String>();
            List<String> tokenizations = new ArrayList<String>();
			int pos = 0; // current offset
			List<Integer> positions = new ArrayList<Integer>();
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                tokenizations.add(tok);
                if (!tok.equals(" ")) {
                	textBlocks.add(tok + "\t<sense>");
					positions.add(pos);
            	}
				pos += tok.length();
            }
            StringBuffer ress = new StringBuffer();
            int posit = 0; // keep track of the position index in the list of positions
			int currentLocationIndex = 0; // keep track of the position index in the list of unit match offsets 
			int currentPersonTitleIndex = 0; 
			int currentOrganisationIndex = 0; 
			int currentOrgFormIndex = 0; 
            for (String block : textBlocks) {
				currentPosition += positions.get(posit);
				
				// check if the token is a known NE
				// do we have a NE at position posit?
				boolean isLocationToken = false;
				boolean isPersonTitleToken = false;
				boolean isOrganisationToken = false;
				boolean isOrgFormToken = false;
				if ( (locationPositions != null) && (locationPositions.size() > 0) ) {
					for(int mm = currentLocationIndex; mm < locationPositions.size(); mm++) {
						if ( (posit >= locationPositions.get(mm).start) && 
							 (posit <= locationPositions.get(mm).end) ) {
							isLocationToken = true;
							currentLocationIndex = mm;
							break;
						}
						else if (posit < locationPositions.get(mm).start) {
							isLocationToken = false;
							break;
						}
						else if (posit > locationPositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (personTitlePositions != null) && (personTitlePositions.size() > 0) ) {
					for(int mm = currentPersonTitleIndex; mm < personTitlePositions.size(); mm++) {
						if ( (posit >= personTitlePositions.get(mm).start) && 
							 (posit <= personTitlePositions.get(mm).end) ) {
							isPersonTitleToken = true;
							currentPersonTitleIndex = mm;
							break;
						}
						else if (posit < personTitlePositions.get(mm).start) {
							isPersonTitleToken = false;
							break;
						}
						else if (posit > personTitlePositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (organisationPositions != null) && (organisationPositions.size() > 0) ) {
					for(int mm = currentOrganisationIndex; mm < organisationPositions.size(); mm++) {
						if ( (posit >= organisationPositions.get(mm).start) && 
							 (posit <= organisationPositions.get(mm).end) ) {
							isOrganisationToken = true;
							currentOrganisationIndex = mm;
							break;
						}
						else if (posit < organisationPositions.get(mm).start) {
							isOrganisationToken = false;
							break;
						}
						else if (posit > organisationPositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (orgFormPositions != null) && (orgFormPositions.size() > 0) ) {
					for(int mm = currentOrgFormIndex; mm < orgFormPositions.size(); mm++) {
						if ( (posit >= orgFormPositions.get(mm).start) && 
							 (posit <= orgFormPositions.get(mm).end) ) {
							isOrgFormToken = true;
							currentOrgFormIndex = mm;
							break;
						}
						else if (posit < orgFormPositions.get(mm).start) {
							isOrganisationToken = false;
							break;
						}
						else if (posit > orgFormPositions.get(mm).end) {
							continue;
						}
					}
				}
				// default value for named entity feature
				boolean isNER = false;
                ress.append(FeaturesVectorNERSense
                        .addFeatures(block, 
								isLocationToken, isPersonTitleToken, isOrganisationToken, isOrgFormToken)
                        .printVector());
				ress.append("\n");
                posit++;
            }
            ress.append("\n");
			String res = label(ress.toString());
//System.out.println(res);
			List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);

            senses = resultExtraction(text, labeled, tokenizations);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return senses;
    }


    /**
     * Extract all occurences of NER senses from a text tokenized and with already identified named entities.
     */
    public List<Sense> extractSenses(String text,
									 List<Pair<String, String>> taggedText,
									 List<String> tokenizations,
									 LexiconPositionsIndexes positionsIndexes) {

    	if (CollectionUtils.isEmpty(taggedText)) {
			return null;
		}

		List<Sense> senses = null;

		List<OffsetPosition> locationPositions = positionsIndexes.getLocalLocationPositions();
		List<OffsetPosition> personTitlePositions = positionsIndexes.getLocalPersonTitlePositions();
		List<OffsetPosition> organisationPositions = positionsIndexes.getLocalOrganisationPositions();
		List<OffsetPosition> orgFormPositions = positionsIndexes.getLocalOrgFormPositions();

        try {
			int sentence = 0;
			int currentPosition = 0;
			
            List<String> textBlocks = new ArrayList<String>();
			int pos = 0; // current offset
			int ind = 0; // current index in taggedText list
			List<Integer> positions = new ArrayList<Integer>();
			List<String> ners = new ArrayList<String>();
            for (Pair<String, String> tokPair : taggedText) {
				String tok = tokPair.getA();
				String tokLabel = tokPair.getB();
				positions.add(pos);
				if (tokLabel.equals("other") || tokLabel.equals("O"))
					tokLabel = "O";
				textBlocks.add(tok + "\t<sense>\t" + tokLabel);
				pos += tok.length();
				ind++;
            }
            StringBuffer ress = new StringBuffer();
            int posit = 0; // keep track of the position index in the list of positions
			int currentLocationIndex = 0; // keep track of the position index in the list of unit match offsets 
			int currentPersonTitleIndex = 0; 
			int currentOrganisationIndex = 0; 
			int currentOrgFormIndex = 0; 
            for (String block : textBlocks) {
				currentPosition += positions.get(posit);
				
				// check if the token is a known NE
				// do we have a NE at position posit?
				boolean isLocationToken = false;
				boolean isPersonTitleToken = false;
				boolean isOrganisationToken = false;
				boolean isOrgFormToken = false;
				if ( (locationPositions != null) && (locationPositions.size() > 0) ) {
					for(int mm = currentLocationIndex; mm < locationPositions.size(); mm++) {
						if ( (posit >= locationPositions.get(mm).start) && 
							 (posit <= locationPositions.get(mm).end) ) {
							isLocationToken = true;
							currentLocationIndex = mm;
							break;
						}
						else if (posit < locationPositions.get(mm).start) {
							isLocationToken = false;
							break;
						}
						else if (posit > locationPositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (personTitlePositions != null) && (personTitlePositions.size() > 0) ) {
					for(int mm = currentPersonTitleIndex; mm < personTitlePositions.size(); mm++) {
						if ( (posit >= personTitlePositions.get(mm).start) && 
							 (posit <= personTitlePositions.get(mm).end) ) {
							isPersonTitleToken = true;
							currentPersonTitleIndex = mm;
							break;
						}
						else if (posit < personTitlePositions.get(mm).start) {
							isPersonTitleToken = false;
							break;
						}
						else if (posit > personTitlePositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (organisationPositions != null) && (organisationPositions.size() > 0) ) {
					for(int mm = currentOrganisationIndex; mm < organisationPositions.size(); mm++) {
						if ( (posit >= organisationPositions.get(mm).start) && 
							 (posit <= organisationPositions.get(mm).end) ) {
							isOrganisationToken = true;
							currentOrganisationIndex = mm;
							break;
						}
						else if (posit < organisationPositions.get(mm).start) {
							isOrganisationToken = false;
							break;
						}
						else if (posit > organisationPositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (orgFormPositions != null) && (orgFormPositions.size() > 0) ) {
					for(int mm = currentOrgFormIndex; mm < orgFormPositions.size(); mm++) {
						if ( (posit >= orgFormPositions.get(mm).start) && 
							 (posit <= orgFormPositions.get(mm).end) ) {
							isOrgFormToken = true;
							currentOrgFormIndex = mm;
							break;
						}
						else if (posit < orgFormPositions.get(mm).start) {
							isOrganisationToken = false;
							break;
						}
						else if (posit > orgFormPositions.get(mm).end) {
							continue;
						}
					}
				}

                ress.append(FeaturesVectorNERSense
                        .addFeatures(block, 
								isLocationToken, isPersonTitleToken, isOrganisationToken, isOrgFormToken)
                        .printVector());
				ress.append("\n");
                posit++;
            }
            ress.append("\n");
			String res = label(ress.toString());
//System.out.println(res);
			List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);

			senses = resultExtraction(text, labeled, tokenizations);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return senses;
	}


    /**
     * Extract the named entities from a labelled text.
     */
    public List<Sense> resultExtraction(String text, 
                                    List<Pair<String, String>> labeled,       
									List<String> tokenizations) {
		List<Sense> senses = new ArrayList<Sense>();
		String label = null; // label without prefix like B-
		String originalLabel = null; // label as used by CRF
        String actual = null; // token
		int offset = 0;
		int addedOffset = 0;
		int p = 0; // iterator for the tokenizations for restauring the original tokenization with
        // respect to spaces
		Sense currentSense = null;
		String previousLabel = "O";
		for (Pair<String, String> l : labeled) {
            actual = l.a;
            originalLabel = l.b;  
			if (originalLabel.startsWith("B-")) 
				label = originalLabel.substring(2, originalLabel.length());
			else
				label = originalLabel;
			
			boolean strop = false;
           	while ((!strop) && (p < tokenizations.size())) {
           		String tokOriginal = tokenizations.get(p);
				addedOffset += tokOriginal.length();
				if (tokOriginal.equals(actual)) {
                  	strop = true;
               	}
                p++;
            }

            if (label == null) {
				offset += addedOffset;
				addedOffset = 0;
				previousLabel = "O";
                continue;
            }

            if (actual != null) {
				if (!label.equals("O") && !label.equals(previousLabel)) {      
					if (currentSense != null) {
						int localPos = currentSense.getOffsetEnd();
						if (label.length() > 1) {  
							if (currentSense.getFineSense().equals(label) && 
							   ( (localPos == offset) ) ) {
								currentSense.setOffsetEnd(offset+addedOffset);
								offset += addedOffset;
								addedOffset = 0;	
								continue;
							}														
							senses.add(currentSense);
						}
					}
					if (label.length() > 1) {  
						currentSense = new Sense(label, label);
						if (nerLexicon.getDescription(label) != null) {
							currentSense.setDescription(nerLexicon.getDescription(label));
						}   
						if ( (text.length()>offset) && (text.charAt(offset) == ' ') ) {	
							currentSense.setOffsetStart(offset+1);
						}
						else
							currentSense.setOffsetStart(offset);
						currentSense.setOffsetEnd(offset+addedOffset);
					}  
				}
				else if (!label.equals("O") && label.equals(previousLabel))	{
					if (label.length() > 1) {
					    if ( (currentSense != null) && (currentSense.getFineSense().equals(label)) ) {
							if (originalLabel.startsWith("B-")) {
								currentSense = new Sense(label, label);
								if (nerLexicon.getDescription(label) != null) {
									currentSense.setDescription(nerLexicon.getDescription(label));
								}   
								if ( (text.length()>offset) && (text.charAt(offset) == ' ') ) {	
									currentSense.setOffsetStart(offset+1);
								}
								else
									currentSense.setOffsetStart(offset);
								currentSense.setOffsetEnd(offset+addedOffset);
							}
							else
								currentSense.setOffsetEnd(offset+addedOffset);		
						}
						else {
							// should not be the case, but we add the new entity, for robustness      
							if (currentSense != null)
								senses.add(currentSense);
							currentSense = new Sense(label, label); 
							if (nerLexicon.getDescription(label) != null) {
								currentSense.setDescription(nerLexicon.getDescription(label));
							}
							currentSense.setOffsetStart(offset);
							currentSense.setOffsetEnd(offset+addedOffset);
						}
				   	}
				}
				
				offset += addedOffset;
				addedOffset = 0;
				previousLabel = label;
			}			
		}
		
		if (currentSense != null) {
			senses.add(currentSense);
		}
		
		return senses;
	}
}
