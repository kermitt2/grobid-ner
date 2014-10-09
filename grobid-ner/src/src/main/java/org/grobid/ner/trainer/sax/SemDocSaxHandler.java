package org.grobid.trainer.sax;

import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.grobid.core.data.Entity;
import org.grobid.core.data.Sense;
import org.grobid.core.lexicon.NERLexicon;

import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SAX parser for SemDoc.
 *
 * @author Patrice Lopez
 */
public class SemDocSaxHandler extends DefaultHandler {

	private static Logger LOGGER = LoggerFactory.getLogger(SemDocSaxHandler.class);

    private StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text

    private String currentTag = null;
	private String sentence = null;
	
	private String currentToken = null;
	private String currentSenseKey = null;
	private String currentCoarseSenseKey = null;

	private String currentNEType = null;
	private String currentNESubType = null;
	private String currentRef = null;

	private String currentDM = null;

	private boolean covered = false;
	private boolean hasConstituent = false;
	private boolean isNE = false;
	private boolean modeWSD = false; 
	
	private String currentFineConfidence = null;
	private String currentCoarseConfidence = null;
	
	// working objects
	private Entity currentEntity = null;
	private Sense currentSense = null;
	
	//private Map<String,String> descriptions = null;
	private Map<String,Entity> neInfos = null;
	private Map<String,Sense> senseInfos = null;
	private Map<String,List<String>> constituents = null;
	private Map<String,String> coarses = null;

	private List<List<String>> textVector = null;
	private List<String> annotatedTextVector = null;
	private int currentVectorPos = 0;
	private int currentTokenPos = 0;
	private int currentTokenLength = 0;

	// for realignment
	private List<String> tokensFrag = null;
	private List<Pair<String,String>> accumulatedSenseKeys = null;
	private int currentParagraphLength = 0;
	private String srcString = null;
	
	// store the WordNet definitions corresponding to word senses
	private Map<String,String> descriptions = null; 
	
    public SemDocSaxHandler(List<List<String>> text) {
		//descriptions = new HashMap<String,String>();
		neInfos = new HashMap<String,Entity>();
		senseInfos = new HashMap<String,Sense>();
		//extRefs = new HashMap<String,List<String>>();
		coarses = new HashMap<String,String>();
		constituents = new HashMap<String,List<String>>();
		textVector = text;
		annotatedTextVector = new ArrayList<String>();
    }
	
	public void setModeWSD(boolean mode) {
		this.modeWSD = mode;
	}
	
	public void setDescriptions(Map<String,String> descriptions) {
		this.descriptions = descriptions;
	}
	
	public Map<String,String> getDescriptions() {
		return descriptions;
	}
	
    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

	public List<String> getAnnotatedTextVector() {
		return annotatedTextVector;
	}

	private String getText() {
        return accumulator.toString().trim();
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {
    	if (qName.equals("frag")) {
			while(currentTokenLength>0) {
				if (currentSenseKey != null) {
					accumulatedSenseKeys.add(new Pair<String,String>(currentSenseKey,srcString));
				}
				else {
					accumulatedSenseKeys.add(new Pair<String,String>(null,srcString));
				}
				currentTokenLength--;
			}
			
			currentSenseKey = null;
			currentCoarseSenseKey = null;
			covered = false;
			srcString = null;
		}
 		else if (qName.equals("cs") && !covered) {
			// sense info
			if (modeWSD) {
				if (currentSenseKey != null)
					currentSense = senseInfos.get(currentSenseKey);
				else
					currentSense = null;
			}
			{
				if (currentSenseKey != null)
					currentEntity = neInfos.get(currentSenseKey);
				else 
					currentEntity = null;
			}
			
			// confidence scores
			double confFine = 0.0;
			double confCoarse = 0.0;
			try {
				confFine = Double.parseDouble(currentFineConfidence);
			}
			catch(Exception e) {
				System.out.println("Warning - parsing of string confidence value failed:" + currentFineConfidence);
			}
			try {
				confCoarse = Double.parseDouble(currentCoarseConfidence);
			}
			catch(Exception e) {
				System.out.println("Warning - parsing of string confidence value failed:" + currentCoarseConfidence);
			}
			
			covered = true;
        }
		else if (qName.equals("neInfo")) {
			//if (!modeWSD) 
			{
				if ( (currentEntity.getType() == null) && (currentNEType != null) ) {
					// try to convert the entity type
					NERLexicon.NER_Type nerType = NERLexicon.NER_Type.mapIdilia(currentNEType);
					currentEntity.setType(nerType);
				}
				if (currentNESubType != null)
					currentEntity.addSubType(currentNESubType);
			}
		}
		else if (qName.equals("neT")) {
			currentNEType = getText();
		}
		else if (qName.equals("neST")) {
			currentNESubType = getText();
		}
		else if (qName.equals("sense")) {
			if (currentEntity != null) {
				neInfos.put(currentSenseKey, currentEntity);
				currentEntity = null;
			}
			if (currentSense != null) {
				senseInfos.put(currentSenseKey, currentSense);
				currentSense = null;
			}
			coarses.put(currentSenseKey, currentCoarseSenseKey);
			currentNEType = null;
			currentNESubType = null;
		}
		else if (qName.equals("constituent")) {
			List<String> consts = constituents.get(currentSenseKey);
			if (consts == null) {
				consts = new ArrayList<String>();
			}
			consts.add(getText());
			constituents.put(currentSenseKey, consts);
		}
		else if (qName.equals("desc")) {
			if ( (descriptions != null) && (descriptions.get(currentSenseKey) == null) ) {
				String desc = getText();
				descriptions.put(currentSenseKey, desc);
			}
		}
		else if (qName.equals("para")) {
				// we can use the identified dependency tokens to try to 
			// re-segment the local input text
//System.out.println(tokensFrag.toString());				
			List<String> newPara = new ArrayList<String>();
			
			if (currentVectorPos < textVector.size()) {
				for(int i=0; i<textVector.get(currentVectorPos).size(); i++) {
					String token = textVector.get(currentVectorPos).get(i);
					for(String tokFrag : tokensFrag) {
						if (tokFrag.startsWith(token)) {
							// we might have a new token, we look forward
							int j = 1;
							while(i+j<textVector.get(currentVectorPos).size()) {
								String nextToken = textVector.get(currentVectorPos).get(i+j);
								if (nextToken.equals("-")) {
									break;
								}
								if (tokFrag.startsWith(token+nextToken)) {
									token += nextToken;
									j++;
								}
								else {
									if (j>1) {
										i += j-1; 
									}
									break;
								}
							}
							if (i+j>=textVector.get(currentVectorPos).size()) {
								i = textVector.get(currentVectorPos).size();
							}
						}
					}
				
					newPara.add(token);
				}
			
				textVector.set(currentVectorPos, newPara);
				
	//System.out.println("para length: " + currentParagraphLength);
	//System.out.println("new vector lenght: " + textVector.get(currentVectorPos).size());
	//System.out.println(textVector.get(currentVectorPos));

				// we finally generate the annotated text, first in temporary vector
				currentTokenPos = 0;
				String previousType = null;
				List<String> tmpAnnotatedTextVector = null;
				for (Pair<String,String> sensekeySrc : accumulatedSenseKeys) {
					if (currentTokenPos >= textVector.get(currentVectorPos).size())
						break;
					String token = textVector.get(currentVectorPos).get(currentTokenPos);
					// get the type
					Entity entity = null;
					Sense sense = null; 
					String type = null;
					String senseType = null;
					if (sensekeySrc.getA() != null) {
					 	if (modeWSD) {
							sense = senseInfos.get(sensekeySrc.getA());
						}
						entity = neInfos.get(sensekeySrc.getA());
					}							
					if (entity == null) {
						type = "O";
					}
					else {
						type = entity.getType().toString();
					}
					if (type == null) {
						type = "O";
					}
					if ( (entity == null) && (sense == null) ) {
						senseType = "O";
					}
					else if (entity != null) {
						List<String> subTypes = entity.getSubTypes();
						if ( (subTypes != null) && (subTypes.size() > 0) )
							senseType = subTypes.get(0);
					}
					/*else if (sense != null) {
						senseType = sense.getFineSense();
					}*/
					if (senseType == null) {
						senseType = "O";
					}
					senseType = senseType.replace("NESUBTYPE_","");
	//System.out.println(token + "\t" + sensekeySrc.getB());		
				
					if (sensekeySrc.getB() != null) {
						if (!token.equals(sensekeySrc.getB()) && (sensekeySrc.getB().indexOf(token) == -1)) {
							// we have lost at some point the alignment, optionally print an error message 
							// for error analysis
							// and ignore this paragraph in the training/evaluation data							
System.out.println("Lost alignment for : " + textVector.get(currentVectorPos).toString());
							tmpAnnotatedTextVector = null;
							break;
						}
					}
					// re-establish the unusual "n't" Idilia tokenization (don't->do+n't)
					if (token.equals("n't")) {
						// we have to add the n to the previous token, so the last token of tmpAnnotatedTextVector
						String lastLine = tmpAnnotatedTextVector.get(tmpAnnotatedTextVector.size()-1);
						lastLine = lastLine.replace("\t","n\t");
						tmpAnnotatedTextVector.set(tmpAnnotatedTextVector.size()-1, lastLine);
						// and we replace the "n't" by simply "'t"
						token = "'t";
					}
					// retokenize for having something uniform
					StringTokenizer st = new StringTokenizer(token, TextUtilities.delimiters, true);
					while(st.hasMoreTokens()) {
						String subToken = st.nextToken();
						if (subToken.equals(" ") || subToken.equals("\t") )
							continue;
						if (modeWSD) {
							/*if ( ((previousType == null) || (!previousType.equals(senseType))) 
									&& (!senseType.equals("O")) ) {
								if (tmpAnnotatedTextVector == null) 
									tmpAnnotatedTextVector = new ArrayList<String>();	
								tmpAnnotatedTextVector.add(subToken + "\t" + senseType);
							}
							else {*/
								if (tmpAnnotatedTextVector == null) 
									tmpAnnotatedTextVector = new ArrayList<String>();	
								tmpAnnotatedTextVector.add(subToken + "\t" + senseType + "\t" + type);
							//}
						}
						else {	
							if ( ((previousType == null) || (!previousType.equals(type))) 
									&& (!type.equals("O")) ) {
								if (tmpAnnotatedTextVector == null) 
									tmpAnnotatedTextVector = new ArrayList<String>();	
								tmpAnnotatedTextVector.add(subToken + "\tB-" + type);
							}
							else {
								if (tmpAnnotatedTextVector == null) 
									tmpAnnotatedTextVector = new ArrayList<String>();	
								tmpAnnotatedTextVector.add(subToken + "\t" + type);
							}
						}
					}
					if (modeWSD)
						previousType = senseType;
					else
						previousType = type;
					currentTokenPos++;
				}
			
				if ( (tmpAnnotatedTextVector != null) && (tmpAnnotatedTextVector.size() > 0) ) {
					for(String line : tmpAnnotatedTextVector) {
						annotatedTextVector.add(line);
					}
					annotatedTextVector.add("@newline");
				}
			
				currentVectorPos++;
			}
		}
		
        accumulator.setLength(0);
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {

        accumulator.setLength(0);

		if (qName.equals("fs")) {
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("sk")) {
						// this is the same as fsk given in the sense element
                        currentSenseKey = value;
                    }
					else if (name.equals("pc")) {
						currentFineConfidence = value;
					}
                }
            }
        }
		else if (qName.equals("cs")) {
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("sk")) {
                        currentCoarseSenseKey = value;
                    }
					else if (name.equals("pc")) {
						currentCoarseConfidence = value;
					}
                }
            }
        }
		else if (qName.equals("sense")) {
			int length = atts.getLength();
			isNE = false;
			
            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("fsk")) {
                        currentSenseKey = value;
                    }
					else if (name.equals("isne")) {
						isNE = true;
					}
					else if (name.equals("csk")) {
                        currentCoarseSenseKey = value;
                    }
                }
            }

			if (isNE) {
				currentEntity = new Entity();
			}
			//else 
			{
				currentSense = new Sense();
				currentSense.setFineSense(currentSenseKey);
				currentSense.setCoarseSense(currentCoarseSenseKey);
			}
		}
		else if (qName.equals("frag")) {
			covered = false;
			int length = atts.getLength();
			currentTokenLength = 0;
			
            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("len")) {
						try {
                        	currentTokenLength = Integer.parseInt(value);
						}
						catch(Exception e) {
							LOGGER.debug("Invalid length string : " + value);
						}
                    }
                }
            }
		}
		else if (qName.equals("para")) {
			currentTokenPos = 0;
			int length = atts.getLength();
			currentParagraphLength = 0;
			// Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("len")) {
						try {
                        	currentParagraphLength = Integer.parseInt(value);
						}
						catch(Exception e) {
							LOGGER.debug("Invalid length string : " + value);
						}
                    }
                }
            }

			if (currentParagraphLength != 0) {
//System.out.println("-----------------");
//System.out.println("para length: " + currentParagraphLength);
//System.out.println("vector lenght: " + textVector.get(currentVectorPos).size());
//System.out.println(textVector.get(currentVectorPos));
				if (tokensFrag == null)
					tokensFrag = tokensF(); 
				accumulatedSenseKeys = new ArrayList<Pair<String,String>>();
			}
 			
		}
		else if (qName.equals("dep")) {
			int length = atts.getLength();
			
            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if ( (name != null) && (value != null) ) {
                    if (name.equals("dest")) {
						if (!tokensFrag.contains(value)) {
							tokensFrag.add(value);
						}
					}
					else if (name.equals("src")) {
						if (!tokensFrag.contains(value)) {
							tokensFrag.add(value);
						}
						srcString = value;
					}
				}
			}
		}
		else if (qName.equals("sent")) {
			
		}
		else if (qName.equals("docs")) {
		
		}
    }

	static private List<String> tokensF() {
		List<String> result = new ArrayList<String>();
		result.add("'s");
		result.add("'re");
		result.add("n't");
		result.add("corp.");
		result.add("Corp.");
		result.add("Co.");
		result.add("co.");
		result.add("Inc.");
		result.add("inc.");
		result.add("...");
		return result;
	}


 }