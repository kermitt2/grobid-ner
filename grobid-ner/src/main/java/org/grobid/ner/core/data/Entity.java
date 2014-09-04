package org.grobid.core.data;

import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.lexicon.NERLexicon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import java.io.BufferedReader;
import java.util.List;    
     
import org.apache.commons.lang3.StringUtils;

/**
 * Common representation of an unresolved entity mention for the NER components.
 * 
 * @author Patrice Lopez
 *
 */
public class Entity {   
	   
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
	private double probability = 0.0;
	
	// confidence score of the entity in context, if defined
	private double confidence = 0.0;
	
    public Entity() {
		this.offsets = new OffsetPosition();
    }
	
	public Entity(String raw) {
        this.rawName = raw;
		this.offsets = new OffsetPosition();
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
	
	public double getProbability() {
		return this.probability;
	}
	
	public void setProbability(double prob) {
		this.probability = prob;
	}
	
	public double getConfidence() {
		return this.confidence;
	}
	
	public void setConfidence(double conf) {
		this.confidence = conf;
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
        return buffer.toString();
    }
}