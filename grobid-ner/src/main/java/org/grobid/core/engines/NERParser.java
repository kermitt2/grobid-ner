package org.grobid.core.engines;

import org.grobid.core.data.Entity;
import org.grobid.core.layout.LayoutToken;

import java.util.List;

/**
 * NER
 *
 * @author Patrice Lopez
 */
public interface NERParser {

	List<Entity> extractNE(String text);

	List<Entity> extractNE(List<LayoutToken> tokens);
	
	String label(String text);
}