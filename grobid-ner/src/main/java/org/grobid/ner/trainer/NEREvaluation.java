package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.mock.MockContext;
import org.grobid.core.features.FeaturesVectorNER;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.trainer.evaluation.EvaluationUtilities;
import org.grobid.core.data.Entity;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.engines.NERParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Properties;

import org.apache.commons.io.FileUtils;


/**
 * Additional NER specific evaluation.
 * 
 *
 * @author Patrice Lopez
 */
public class NEREvaluation {

	private NERLexicon lexicon = NERLexicon.getInstance();

	private String conllPath = null;
	private GrobidModels model = null;

	public NEREvaluation() {
		model = GrobidModels.ENTITIES_NER;
		loadAdditionalProperties();
	}
	
	private void loadAdditionalProperties() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("src/main/resources/grobid-ner.properties");

			// load the properties file
			prop.load(input);

			// get the property value
			conllPath = prop.getProperty("grobid.ner.reuters.conll_path");
		} 
		catch (IOException ex) {
			throw new GrobidResourceException(
				"An exception occured when accessing/reading the grobid-ner property file.", ex);
		} 
		finally {
			if (input != null) {
				try {
					input.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }


	/**
	 *  Evaluation based on the CoNLL-2003 shared task NER gold corpus, English set. 
	 *  see http://www.cnts.ua.ac.be/conll2003/ner/. 
	 */
	public String evaluate_reuters() {
		StringBuffer report = new StringBuffer();
		try {
			NERParser parser = new NERParser();
			
			File evalDataF = GrobidProperties.getInstance().getEvalCorpusPath(
				new File(new File("resources").getAbsolutePath()), model);
		
			File tmpEvalPath = getTempEvaluationDataPath();	
			
			// There are three set that we can exploit testa, testb and the training sets.
			// However the training set should be used to reimforce the learning. 
			File evalA = new File(conllPath + "/eng.testa");
			File evalB = new File(conllPath + "/eng.testb");
			File evalTrain = new File(conllPath + "/eng.train");	
			
			if (!evalTrain.exists()) {
				throw new GrobidException(
					"Cannot start evaluation, because corpus resource path for CoNLL file " +
					" is not correctly set : " + evalDataF.getPath() + "/eng.train");
			}
			report.append(evaluate_reutersSet(parser, evalTrain, tmpEvalPath));
			
			if (!evalA.exists()) {
				throw new GrobidException(
					"Cannot start evaluation, because corpus resource path for CoNLL file " +
					" is not correctly set : " + evalDataF.getPath() + "/eng.testa");
			}
			report.append(evaluate_reutersSet(parser, evalA, tmpEvalPath));
			
			if (!evalB.exists()) {
				throw new GrobidException(
					"Cannot start evaluation, because corpus resource path for CoNLL file " +
					" is not correctly set : " + evalDataF.getPath() + "/eng.testb");
			}
			report.append(evaluate_reutersSet(parser, evalB, tmpEvalPath));
		}	
		catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid Reuters evaluation.", e);
        }
		return report.toString();
	}
	
	private String evaluate_reutersSet(NERParser parser, File evalSet, File tmpEvalPath) {
		StringBuffer report = new StringBuffer();
		try {
			// the data need to be re-tokenized according to the Grobid NER tokenization level
			createCRFPPDataCoNLL(evalSet, tmpEvalPath);			
	
			BufferedReader bufReader = new BufferedReader(
	                new InputStreamReader(new FileInputStream(tmpEvalPath), "UTF-8"));

			// the parser needs to be applied on a sentence level as it has been trained as such
	        String line;
	        StringBuffer ress = new StringBuffer();
			StringBuffer theResultBuffer = new StringBuffer();
	        while ((line = bufReader.readLine()) != null) {
	            ress.append(line+"\n");
				if (line.trim().length() == 0) {
					String res = parser.label(ress.toString());
					theResultBuffer.append(res + "\n");				
					ress = new StringBuffer();
				}
	        }
		
			// now evaluate the result
			//List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);
			
			
		}
		catch (Exception e) {
            throw new GrobidException(
				"An exception occured while running Grobid Reuters evaluation for the set "
				+ evalSet.getPath(), e);
        }
		return report.toString();
	}

	/**
	 * Add the features to the CoNLL NER evaluation set 
	 */
	private int createCRFPPDataCoNLL(final File corpusDir, final File evalOutputPath) {
		BufferedReader bufReader = null;
		Writer writer = null;
		String line = null;
		int nbSentences = 0;
		try {
			bufReader = new BufferedReader(
	                new InputStreamReader(new FileInputStream(corpusDir), "UTF-8"));
			writer = new OutputStreamWriter(new FileOutputStream(corpusDir), "UTF8");
			List<String> labeled = new ArrayList<String>();
			// to store unit term positions
            List<List<OffsetPosition>> nerTokenPositions = new ArrayList<List<OffsetPosition>>();
			while ((line = bufReader.readLine()) != null) {
				line = line.trim();
				// note that we work at sentence level
				if (line.startsWith("-DOCSTART-")) {
					continue;
				}
				
				if (line.length() == 0) {
					// sentence is complete
					writer.write("\n");
					NERTrainer.addFeatures(labeled, writer, nerTokenPositions);
					labeled = new ArrayList<String>();
					nerTokenPositions = new ArrayList<List<OffsetPosition>>();
					nbSentences++;
					continue;
				}
				
				String[] pieces = line.split(" ");
				if (pieces.length == 4) {
					// we retokenize the lexical string according to Grobid NER
					String token = pieces[0];
					StringTokenizer st = new StringTokenizer(token, TextUtilities.fullPunctuations);
					
					String conllLabel = pieces[3];
					String label = "other";
					if (conllLabel.equals("I-ORG")) {
						label = "org";
					} 
					else if (conllLabel.equals("I-LOC")) {
						label = "loc";
					}
					else if (conllLabel.equals("I-PER")) {
						label = "per";
					}
					else if (conllLabel.equals("I-MISC")) {
						label = "misc";
					}
					boolean start = true;
		            while(st.hasMoreTokens()) {
						labeled.add(st.nextToken() + "\t" + label);
					}
				}
			}
		}
		catch (Exception e) {
            throw new GrobidException(
				"An exception occured while creating Grobid Reuters evaluation resource the set "
				+ corpusDir.getPath(), e);
        }
		finally {
			try {
				if (writer != null)
					writer.close();
				if (bufReader != null)
					bufReader.close();
			}
			catch (Exception e) {
			}
		}
		return nbSentences;
	}
	
	protected final File getTempEvaluationDataPath() {
		try {
			return File.createTempFile(model.getModelName(), ".test", GrobidProperties.getTempPath());
		} catch (IOException e) {
			throw new RuntimeException("Unable to create a temporary evaluation file for model: " + model);
		}
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

	        NEREvaluation eval = new NEREvaluation();
	
			// CoNLL evaluation
			System.out.println(eval.evaluate_reuters());
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