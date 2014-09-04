package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Entity;
import org.grobid.core.lexicon.NERLexicon;
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

/**
 * NER
 *
 * @author Patrice Lopez
 */
public class NERParser extends AbstractParser {

	private NERLexicon lexicon = NERLexicon.getInstance();

    public NERParser() {
        super(GrobidModels.ENTITIES_NER);
    }

    /**
     * Extract all occurences of named entity from a simple piece of text.
     */
    public List<Entity> extractNE(String text) throws Exception {
        if (text == null)
            return null;
        if (text.length() == 0)
            return null;
        List<Entity> entities = null;
        try {
            text = text.replace("\n", " ");
			List<OffsetPosition> positionNERNames = null;//lexicon.inNERNames(text);
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
			int currentNERIndex = 0; // keep track of the position index in the list of unit match offsets 
            for (String block : textBlocks) {
				currentPosition += positions.get(posit);
				
				// check if the token is a known NE
				// do we have a NE at position posit?
				boolean isNERToken = false;
				if ( (positionNERNames != null) && (positionNERNames.size() > 0) ) {
					for(int mm = currentNERIndex; mm < positionNERNames.size(); mm++) {
						if ( (posit >= positionNERNames.get(mm).start) && (posit <= positionNERNames.get(mm).end) ) {
							isNERToken = true;
							currentNERIndex = mm;
							break;
						}
						else if (posit < positionNERNames.get(mm).start) {
							isNERToken = false;
							break;
						}
						else if (posit > positionNERNames.get(mm).end) {
							continue;
						}
					}
				}
                ress.append(FeaturesVectorNER
                        .addFeaturesNER(block, isNERToken)
                        .printVector());
				ress.append("\n");
                posit++;
            }
            ress.append("\n");
			String res = label(ress.toString());
			
			List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);

            entities = resultExtraction(text, labeled, tokenizations);
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
							if (currentEntity.getRawName().equals(subtag) && 
							   ( (localPos == offset) ) ) {
								currentEntity.setOffsetEnd(offset+addedOffset);
								offset += addedOffset;
								addedOffset = 0;	
								continue;
							}														
							entities.add(currentEntity);
						}
					}
					if (label.length() > 1) {  
						String subtag = label.substring(2,label.length()).toLowerCase();
						currentEntity = new Entity(subtag);   
						if ( text.charAt(offset) == ' ') {	
							currentEntity.setOffsetStart(offset+1);
						}
						else
							currentEntity.setOffsetStart(offset);
						currentEntity.setOffsetEnd(offset+addedOffset);
					}  
				}
				else if (label.startsWith("I-")) {  
					if (label.length() > 1) {  
						String subtag = label.substring(2,label.length()).toLowerCase();

					    if ( (currentEntity != null) && (currentEntity.getRawName().equals(subtag)) ) {
							currentEntity.setOffsetEnd(offset+addedOffset);		
						}
						else {
							// should not be the case, but we add the new entity, for robustness      
							if (currentEntity != null)
								entities.add(currentEntity);
							currentEntity = new Entity(subtag);   
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
			entities.add(currentEntity);
		}
		
		return entities;
	}
}
