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
 * Common representation of a sense.
 * 
 * @author Patrice Lopez
 *
 */
public class Sense {   
	// name of the entity                   
	private String coarseSense = null;
    private String fineSense = null;

	// confidence score
	private double coarseSenseConfidence = 0.7;
	private double fineSenseConfidence = 0.7;
	private double conf = 0.7; // global conf

	// probability of the entity in context, if defined
	private double prob = 1.0;

	// optional information
	private String description = null;
	private String wiktionaryExternalRef = null;
	private String wikipediaExternalRef = null;

	// relative offset positions in context, if defined
	private OffsetPosition offsets = null;

    public Sense() {
		this.offsets = new OffsetPosition();
    }

	public Sense(String coarse, String fine) {
        this.coarseSense = coarse;
		this.fineSense = fine;
		this.offsets = new OffsetPosition();
    }

    public String getFineSense() {
        return fineSense;
    }

	public String getCoarseSense() {
        return coarseSense;
    }

	public void setFineSense(String fine) {
        this.fineSense = fine;
    }

	public void setCoarseSense(String coarse) {
        this.coarseSense = coarse;
    }

	public double getFineSenseConfidence() {
		return coarseSenseConfidence;
	}
	
	public double getCoarseSenseConfidence() {
		return coarseSenseConfidence;
	}
	
	public double getConf() {
		return conf;
	}
	
	public void setFineSenseConfidence(double conf) {
		fineSenseConfidence = conf;
	}
	
	public void setCoarseSenseConfidence(double conf) {
		coarseSenseConfidence = conf;
	}
	
	public void setConf(double conf) {
		this.conf = conf;
	}
	
	public void setWiktionaryExternalRef(String ref) {
        this.wiktionaryExternalRef = ref;
    }

	public void setWikipediaExternalRef(String ref) {
        this.wikipediaExternalRef = ref;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		description = desc;
	}

	public String getWiktionaryExternalRef() {
		return wiktionaryExternalRef;
	}

	public String getWikipediaExternalRef() {
		return wikipediaExternalRef;
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
		return this.prob;
	}
	
	public void setProbability(double prob) {
		this.prob = prob;
	}

    public String toString() {
        StringBuffer buffer = new StringBuffer();
		if (fineSense != null)
        	buffer.append(fineSense + "\t");
		if (coarseSense != null) {
			if ( (fineSense == null) || (!fineSense.equals(coarseSense)) )
				buffer.append(coarseSense + "\t");
		}	
		if (description != null)
			buffer.append("[" + description + "]\t");	
		if (wiktionaryExternalRef != null) {
			buffer.append(wiktionaryExternalRef + "\t");	
		} 
		if (wikipediaExternalRef != null) {
			buffer.append(wikipediaExternalRef + "\t");	
		}	
		if (offsets != null) {
			buffer.append(offsets.toString() + "\t");
		}
		if (description != null) {
			buffer.append(description + "\t");
		}
        return buffer.toString();
    }

}