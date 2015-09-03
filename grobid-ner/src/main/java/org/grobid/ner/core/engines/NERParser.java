package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Sense;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.engines.AbstractParser;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorNER;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.OffsetPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NER
 *
 * @author Patrice Lopez
 */
public class NERParser extends AbstractParser {

	private static Logger LOGGER = LoggerFactory.getLogger(NERParser.class);

	protected NERLexicon nerLexicon = NERLexicon.getInstance();
	protected Lexicon lexicon = Lexicon.getInstance();
	protected SenseTagger senseTagger = null;
	
    public NERParser() {
        super(GrobidModels.ENTITIES_NER);
		senseTagger = new SenseTagger();
    }

	/** this is a NER specific list of delimiters for tokenization */ 
	//public static final String NERFullPunctuations = "([ ,:;?.!/)-–\"“”‘’'`$]*\u2666\u2665\u2663\u2660";

    /**
     * Extract all occurences of named entity from a simple piece of text.
     */
    public List<Entity> extractNE(String text) throws Exception {
        if (text == null)
            return null;
        if (text.length() == 0)
            return null;
        List<Entity> entities = null;
		List<Sense> senses = null;
        try {
            text = text.replace("\n", " ");
			int sentence = 0;
			List<OffsetPosition> localLocationPositions = lexicon.inLocationNames(text);
			List<OffsetPosition> localPersonTitlePositions = lexicon.inPersonTitleNames(text);
			List<OffsetPosition> localOrganisationPositions = lexicon.inOrganisationNames(text);
			List<OffsetPosition> localOrgFormPositions = lexicon.inOrgFormNames(text);
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
                	textBlocks.add(tok + "\t<ner>");
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
				if ( (localLocationPositions != null) && (localLocationPositions.size() > 0) ) {
					for(int mm = currentLocationIndex; mm < localLocationPositions.size(); mm++) {
						if ( (posit >= localLocationPositions.get(mm).start) && 
							 (posit <= localLocationPositions.get(mm).end) ) {
							isLocationToken = true;
							currentLocationIndex = mm;
							break;
						}
						else if (posit < localLocationPositions.get(mm).start) {
							isLocationToken = false;
							break;
						}
						else if (posit > localLocationPositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (localPersonTitlePositions != null) && (localPersonTitlePositions.size() > 0) ) {
					for(int mm = currentPersonTitleIndex; mm < localPersonTitlePositions.size(); mm++) {
						if ( (posit >= localPersonTitlePositions.get(mm).start) && 
							 (posit <= localPersonTitlePositions.get(mm).end) ) {
							isPersonTitleToken = true;
							currentPersonTitleIndex = mm;
							break;
						}
						else if (posit < localPersonTitlePositions.get(mm).start) {
							isPersonTitleToken = false;
							break;
						}
						else if (posit > localPersonTitlePositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (localOrganisationPositions != null) && (localOrganisationPositions.size() > 0) ) {
					for(int mm = currentOrganisationIndex; mm < localOrganisationPositions.size(); mm++) {
						if ( (posit >= localOrganisationPositions.get(mm).start) && 
							 (posit <= localOrganisationPositions.get(mm).end) ) {
							isOrganisationToken = true;
							currentOrganisationIndex = mm;
							break;
						}
						else if (posit < localOrganisationPositions.get(mm).start) {
							isOrganisationToken = false;
							break;
						}
						else if (posit > localOrganisationPositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (localOrgFormPositions != null) && (localOrgFormPositions.size() > 0) ) {
					for(int mm = currentOrgFormIndex; mm < localOrgFormPositions.size(); mm++) {
						if ( (posit >= localOrgFormPositions.get(mm).start) && 
							 (posit <= localOrgFormPositions.get(mm).end) ) {
							isOrgFormToken = true;
							currentOrgFormIndex = mm;
							break;
						}
						else if (posit < localOrgFormPositions.get(mm).start) {
							isOrganisationToken = false;
							break;
						}
						else if (posit > localOrgFormPositions.get(mm).end) {
							continue;
						}
					}
				}
                ress.append(FeaturesVectorNER
                        .addFeaturesNER(block, 
								isLocationToken, isPersonTitleToken, isOrganisationToken, isOrgFormToken)
                        .printVector());
				ress.append("\n");
                posit++;
            }
            ress.append("\n");
			String res = label(ress.toString());
			//LOGGER.info(res);
//System.out.println(res);
			List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);

            entities = resultExtraction(text, labeled, tokenizations);

			// we use now the sense tagger for the recognized named entity		
			senses = senseTagger.extractSenses(text, labeled, tokenizations, 
											localLocationPositions, 
											localPersonTitlePositions,
											localOrganisationPositions,
											localOrgFormPositions);
			int sensePos = 0;
			for(Entity entity : entities) {
				int start = entity.getOffsetStart();
				int end = entity.getOffsetEnd();
				Sense theSense = null;
				if (senses != null) {
					for(int i=sensePos; i<senses.size(); i++) {
						Sense sense = senses.get(i);
						if ( (sense.getOffsetStart() >= start) && (sense.getOffsetEnd() <= end) ) {
							theSense = sense;
							sensePos = i;
							break;
						}
					}
				}
				entity.setSense(theSense);
			}
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return entities;
    }

    /**
     * Extract the named entities from a labelled text.
     */
    public List<Entity> resultExtraction(String text, 
                                    List<Pair<String, String>> labeled,       
									List<String> tokenizations) {

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
                continue;
            }

            if (actual != null) { 
				if (label.startsWith("B-")) {      
					if (currentEntity != null) {
						int localPos = currentEntity.getOffsetEnd();
						if (label.length() > 1) {  
							String subtag = label.substring(2,label.length()).toLowerCase();
							if ( (currentEntity != null) && 
								 (currentEntity.getType() != null) && 
								 (currentEntity.getType().getName().toLowerCase().equals(subtag)) && 
								 (localPos == offset) ) {
								currentEntity.setOffsetEnd(offset+addedOffset);
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
						String subtag = label.substring(2,label.length()).toLowerCase();
						currentEntity = new Entity();  
						currentEntity.setTypeFromString(subtag);
						if ( (text.length()>offset) && ( text.charAt(offset) == ' ') ) {	
							currentEntity.setOffsetStart(offset+1);
						}
						else
							currentEntity.setOffsetStart(offset);
						currentEntity.setOffsetEnd(offset+addedOffset);
					}  
				}
				//else if (label.startsWith("I-")) {  
				else if (!label.equals("O") && !label.equals("other")) {  	
					if (label.length() > 1) {  
						//String subtag = label.substring(2,label.length()).toLowerCase();
						String subtag = label.toLowerCase();
					    if ( (currentEntity != null) && 
							 (currentEntity.getType() != null) && 
							 (currentEntity.getType().getName().toLowerCase().equals(subtag)) ) {
							currentEntity.setOffsetEnd(offset+addedOffset);	
						}
						else {
							// should not be the case, but we add the new entity, for robustness      
							if (currentEntity != null) {
								currentEntity.setRawName(
									text.substring(currentEntity.getOffsetStart(), currentEntity.getOffsetEnd()));
								entities.add(currentEntity);
							}
							currentEntity = new Entity();  
							currentEntity.setTypeFromString(subtag); 
							currentEntity.setOffsetStart(offset);
							currentEntity.setOffsetEnd(offset+addedOffset);
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
	}
}
