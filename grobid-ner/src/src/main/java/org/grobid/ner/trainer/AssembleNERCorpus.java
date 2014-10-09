package org.grobid.trainer;

import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.trainer.sax.*;
import org.grobid.trainer.evaluation.EvaluationUtilities;
import org.grobid.core.data.Entity;
import org.grobid.core.lexicon.NERLexicon;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;

/**
 * Assemble the different corpus information spread in different files into a TEI file. 
 * 
 * @author Patrice Lopez
 */
public class AssembleNERCorpus {
	
	private static Logger LOGGER = LoggerFactory.getLogger(AssembleNERCorpus.class);

	private String reutersPath = null;
	private String conllPath = null;
	private String idiliaPath = null;

    public AssembleNERCorpus() {
		// we read the module specific property file to get the paths to the resources
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream("src/main/resources/grobid-ner.properties");

			// load the properties file
			prop.load(input);

			// get the property value
			reutersPath = prop.getProperty("grobid.ner.reuters.paths");
			conllPath = prop.getProperty("grobid.ner.reuters.conll_path");
			idiliaPath = prop.getProperty("grobid.ner.reuters.idilia_path");

		} 
		catch (IOException ex) {
			ex.printStackTrace();
		} 
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

    }

	/**
	 * Launch the creation of training files based on the combination 
	 * of the different corpus sources. Training files are written under the 
	 * usual training path of the model (resources/dataset/ner/corpus). 
	 */
	public void run() {
		
	}

	/**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
		try {
			String pGrobidHome = "../grobid-home";
			String pGrobidProperties = "../grobid-home/config/grobid.properties";

			MockContext.setInitialContext(pGrobidHome, pGrobidProperties);
		    GrobidProperties.getInstance();

	        AssembleNERCorpus assembler = new AssembleNERCorpus();
	        assembler.run();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		finally {
			try {
				MockContext.destroyInitialContext();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
    }

}