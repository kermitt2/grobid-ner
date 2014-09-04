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

	private double coarseSenseConfidence = 0.0;
	private double fineSenseConfidence = 0.0;

	// optional information
	private String description = null;
	private String wiktionaryExternalRef = null;
	private String wikipediaExternalRef = null;

    public Sense() {
    }

	public Sense(String coarse, String fine) {
        this.coarseSense = coarse;
		this.fineSense = fine;
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
	
	public double getCoarseSenseConfidence(double conf) {
		return coarseSenseConfidence;
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

    public String toString() {
        StringBuffer buffer = new StringBuffer();
		if (fineSense != null)
        	buffer.append(fineSense + "\t");
		if (coarseSense != null)
			buffer.append(coarseSense + "\t");	
		if (description != null)
			buffer.append("[" + description + "]\t");	
		if (wiktionaryExternalRef != null) {
			buffer.append(wiktionaryExternalRef + "\t");	
		} 
		if (wikipediaExternalRef != null) {
			buffer.append(wikipediaExternalRef + "\t");	
		}	
        return buffer.toString();
    }

}