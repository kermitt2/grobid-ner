package org.grobid.core.engines;

import org.grobid.core.data.Entity;

import java.util.List;

/**
 * NER
 *
 * @author Patrice Lopez
 */
public interface NERParser {

	List<Entity> extractNE(String text);

	String createTrainingFromText(String text);

	String label(String text);
}