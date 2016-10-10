package org.grobid.core.data;

import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.layout.BoundingBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Common representation of an unresolved entity mention for the NER components.
 * 
 * @author Patrice Lopez
 *
 */
public class Entity implements Comparable<Entity> {   
	
	// name of the entity = entity type                   
	private String rawName = null;
	
	// normalised name of the entity
    private String normalisedName = null;
	
	// type of the entity (person, location, etc.)
	private NERLexicon.NER_Type type = null;
	
	// subtypes of the entity when available - the first one is the main one, the others secondary subtypes
	private List<String> subTypes = null;
	
	// relative offset positions in context, if defined
	private OffsetPosition offsets = null;
	
	// probability of the entity in context, if defined
	private double prob = 1.0;
	
	// confidence score of the entity in context, if defined
	private double conf = 0.8;
	
	// all the sense information related to the entity
	private Sense sense = null;
	
	// optional bounding box in the source document
	private BoundingBox box = null;
		
	// orign of the entity definition
	public static int GROBID = 0;
	public static int USER = 1;
	private int origin = 0;
	
    public Entity() {
		this.offsets = new OffsetPosition();
    }
	
	public Entity(String raw) {
        this.rawName = raw;
		this.offsets = new OffsetPosition();
    }

	public Entity(Entity ent) {
		rawName = ent.rawName;
		normalisedName = ent.normalisedName;
		type = ent.type;
		subTypes = ent.subTypes;
		offsets = ent.offsets;
		prob = ent.prob;
		conf = ent.conf;
		sense = ent.sense;
		origin = ent.origin;
	}

    public String getRawName() {
        return rawName;
    }
	
	public void setRawName(String raw) {
        this.rawName = raw;
    }

	public String getNormalisedName() {
        return normalisedName;
    }
	
	public void setNormalisedName(String raw) {
        this.normalisedName = raw;
    }

	public NERLexicon.NER_Type getType() {
		return type;
	}
	
	public void setType(NERLexicon.NER_Type theType) {
		type = theType;
	}
	
	public void setTypeFromString(String theType) {
		if (theType.toUpperCase().equals("SPORT_TEAM"))
			theType = "ATHLETIC_TEAM";
		type = NERLexicon.NER_Type.valueOf(theType.toUpperCase());
	}
	
	public List<String> getSubTypes() {
		return subTypes;
	} 

	public void setSubTypes(List<String> theSubTypes) {
		subTypes = theSubTypes;
	}

	public void addSubType(String subType) {
		if (subTypes == null)
			subTypes = new ArrayList<String>();
		subTypes.add(subType);
	}
	
	public OffsetPosition getOffsets() {
		return offsets;
	}
	
	public void setOffsets(OffsetPosition offsets) {
		this.offsets = offsets;
	}
	
	public void setOffsetStart(int start) {
        offsets.start = start;
    }

    public int getOffsetStart() {
        return offsets.start;
    }

    public void setOffsetEnd(int end) {
        offsets.end = end;
    }

    public int getOffsetEnd() {
        return offsets.end;
    }
	
	public double getProb() {
		return this.prob;
	}
	
	public void setProb(double prob) {
		this.prob = prob;
	}
	
	public double getConf() {
		return this.conf;
	}
	
	public void setConf(double conf) {
		this.conf = conf;
	}
	
	public Sense getSense() {
		return sense;
	}
	
	public void setSense(Sense sense) {
		this.sense = sense;
	}
	
	public int getOrigin() {
		return origin;
	}
	
	public void setOrigin(int origin) {
		this.origin = origin;
	}
	
	public void normalise() {
		// TBD
	}
	
	@Override
	public boolean equals(Object object) {
		boolean result = false;
		if ( (object != null) && object instanceof Entity) {
			int start = ((Entity)object).getOffsetStart();
			int end = ((Entity)object).getOffsetEnd();
			if ( (start == offsets.start) && (end == offsets.end) ) {
				result = true;
			}
		}
		return result;
	}

	@Override
	public int compareTo(Entity theEntity) {
		int start = theEntity.getOffsetStart();
		int end = theEntity.getOffsetEnd();
		
		if (offsets.start != start) 
			return offsets.start - start;
		else 
			return offsets.end - end;
	}
	
	public String toJson() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{ ");
		buffer.append("\"rawName\" : \"" + rawName + "\"");
		if (normalisedName != null)
			buffer.append(", \"normalisedName\" : \"" + normalisedName + "\"");
		if (type != null)
			buffer.append(", \"type\" : \"" + type.getName() + "\"");	
		
		if (subTypes != null) {
			buffer.append(", \"subtype\" : [ ");
			boolean begin = true;
			for(String subtype : subTypes) {
				if (begin) {
					buffer.append("\"" + subtype + "\"");
					begin = false;
				}
				else {
					buffer.append(", \"" + subtype + "\"");
				}
			}
			buffer.append(" ] \"");
		}
			
		buffer.append(", \"offsetStart\" : " + offsets.start);
		buffer.append(", \"offsetEnd\" : " + offsets.end);	
		
		buffer.append(", \"conf\" : \"" + conf + "\"");
		buffer.append(", \"prob\" : \"" + prob + "\"");
		
		if (sense != null) {
			buffer.append(", \"sense\" : { "); 
			if (sense.getFineSense() != null) {
				buffer.append("\"fineSense\" : \"" + sense.getFineSense() + "\"");
				buffer.append(", \"conf\" : \"" + sense.getFineSenseConfidence() + "\"");
			}
		
			if (sense.getCoarseSense() != null) {
				if ( (sense.getFineSense() == null) ||
				     ( (sense.getFineSense() != null) && !sense.getCoarseSense().equals(sense.getFineSense())) ) {
					buffer.append(", \"coarseSense\" : \"" + sense.getCoarseSense() + "\"");
				}
			}
			buffer.append(" }");
		}
		
		buffer.append(" }");
		return buffer.toString();
	}
	
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (rawName != null) {
			buffer.append(rawName + "\t");
		}
		if (normalisedName != null) {
			buffer.append(normalisedName + "\t");
		}
		if (type != null) {
			buffer.append(type + "\t");	
		}
		if (subTypes != null) {
			for(String subType : subTypes)
				buffer.append(subType + "\t");	
		}
		if (offsets != null) {
			buffer.append(offsets.toString() + "\t");
		}
		if (sense != null) {
			if (sense.getFineSense() != null) {
				buffer.append(sense.getFineSense() + "\t");
			}
		
			if (sense.getCoarseSense() != null) {
				if ( (sense.getFineSense() == null) ||
				     ( (sense.getFineSense() != null) && !sense.getCoarseSense().equals(sense.getFineSense())) ) {
					buffer.append(sense.getCoarseSense() + "\t");
				}
			}
		}
		
        return buffer.toString();
    }
	
	/** 
	 * Export of entity annotation in TEI standoff format 
	 */	 
	public String toTEI(String id, int n) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<stf xml:id=\"" + "ner-" + n + "\" type=\"ne\" who=\"nerd\" when=\"\">");
		buffer.append("<ptr target=\"id," + offsets.start + "," + offsets.end + "\" />");
		if (type == NERLexicon.NER_Type.PERSON) {
			buffer.append("<person>" + rawName + "</person>");
		}
		buffer.append("</stf>");
		return buffer.toString();
	}
}