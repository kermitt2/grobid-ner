package org.grobid.ner.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.ner.core.lexicon.NERLexicon;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.trainer.GenericTrainer;
import org.grobid.trainer.TrainerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author Patrice Lopez
 */
public class CoNNLNERTrainer extends NERTrainer {
	
	private String conllPath = null;
	
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
	 * Train using the CoNLL-2003 shared task NER gold corpus, English set. 
	 *  see http://www.cnts.ua.ac.be/conll2003/ner/.
	 * The resulting model is fully supervised and limited to the four classes of the gold corpus. 
	 */
	public void trainCoNLL(boolean includeTesta) {
		loadAdditionalProperties();
		long start = System.currentTimeMillis();
		Writer writer = null;
		try {
			File trainFile = new File(conllPath + "/eng.train");
		
			if (!trainFile.exists()) {
				throw new GrobidException(
					"Cannot start training, because corpus resource path for CoNLL file " +
					" is not correctly set : " + conllPath + "/eng.train");
			}
			File trainingOutputFile = getTempTrainingDataPath();
			System.out.println("Temp. training data under: " + trainingOutputFile.getPath());
			
			// the file for writing the training data
			OutputStream os = null;
			if (trainingOutputFile != null) {
				os = new FileOutputStream(trainingOutputFile);
				writer = new OutputStreamWriter(os, "UTF8");
			}
			
			List<List<OffsetPosition>> locationPositions = new ArrayList<List<OffsetPosition>>();
            List<List<OffsetPosition>> titleNamePositions = new ArrayList<List<OffsetPosition>>();
            List<List<OffsetPosition>> organisationPositions = new ArrayList<List<OffsetPosition>>();
			List<List<OffsetPosition>> orgFormPositions = new ArrayList<List<OffsetPosition>>();
			
			List<String> labeled = new ArrayList<String>();
		  	BufferedReader br = new BufferedReader(new InputStreamReader(
				new DataInputStream(new FileInputStream(trainFile))));
		  	String line;
			String previousLabel = "O";
		  	while ((line = br.readLine()) != null)   {
				if (line.startsWith("-DOCSTART-")) {
					previousLabel = "O";
					continue;
				}
			
				if (line.trim().length() == 0) {
					labeled.add("@newline");
					
					locationPositions.add(lexicon.inLocationNames(labeled));
		            titleNamePositions.add(lexicon.inPersonTitleNames(labeled));
		            organisationPositions.add(lexicon.inOrganisationNames(labeled));
					orgFormPositions.add(lexicon.inOrgFormNames(labeled));			

					addFeatures(labeled, writer, 
						locationPositions, titleNamePositions, organisationPositions, orgFormPositions);
		            writer.write("\n");

					locationPositions = new ArrayList<List<OffsetPosition>>();
		            titleNamePositions = new ArrayList<List<OffsetPosition>>();
		            organisationPositions = new ArrayList<List<OffsetPosition>>();
					orgFormPositions = new ArrayList<List<OffsetPosition>>();
					labeled = new ArrayList<String>();
					previousLabel = "O";
					continue;
				}
				
				String[] tokens = line.split(" ");
				
				if (tokens.length != 4) {
					System.err.println("Invalid number of tokens for CoNNL corpus line: " + line);
					continue;
				}
				// we take the standard Grobid tokenizer
				StringTokenizer st2 = new StringTokenizer(tokens[0], 
					TextUtilities.fullPunctuations, true);
				while(st2.hasMoreTokens()) {
					String tok = st2.nextToken();
					if (tok.trim().length() == 0)
						continue;
					String label = tokens[3];
					label = translate(label);
					
					if (label.equals("O")) 
						labeled.add(tok + "\tO");
					else if (previousLabel.equals("O") || !previousLabel.equals(label))
						labeled.add(tok + "\tB-" + label);
					else
						labeled.add(tok + "\tI-" + label);
					previousLabel = label;
				}
			}
			writer.write("\n");
			br.close();
			
			// if indicated, we include the development set (eng.testa) in the training
			if (includeTesta) {
				trainFile = new File(conllPath + "/eng.testa");

				if (!trainFile.exists()) {
					throw new GrobidException(
						"Cannot start training, because corpus resource path for CoNLL file " +
						" is not correctly set : " + conllPath + "/eng.testa");
				}

				locationPositions = new ArrayList<List<OffsetPosition>>();
	            titleNamePositions = new ArrayList<List<OffsetPosition>>();
	            organisationPositions = new ArrayList<List<OffsetPosition>>();
				orgFormPositions = new ArrayList<List<OffsetPosition>>();

				labeled = new ArrayList<String>();
			  	br = new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(trainFile))));
				previousLabel = "O";
			  	while ((line = br.readLine()) != null)   {
					if (line.startsWith("-DOCSTART-")) {
						previousLabel = "O";
						continue;
					}

					if (line.trim().length() == 0) {
						labeled.add("@newline");

						locationPositions.add(lexicon.inLocationNames(labeled));
			            titleNamePositions.add(lexicon.inPersonTitleNames(labeled));
			            organisationPositions.add(lexicon.inOrganisationNames(labeled));
						orgFormPositions.add(lexicon.inOrgFormNames(labeled));			

						addFeatures(labeled, writer, 
							locationPositions, titleNamePositions, organisationPositions, orgFormPositions);
			            writer.write("\n");

						locationPositions = new ArrayList<List<OffsetPosition>>();
			            titleNamePositions = new ArrayList<List<OffsetPosition>>();
			            organisationPositions = new ArrayList<List<OffsetPosition>>();
						orgFormPositions = new ArrayList<List<OffsetPosition>>();
						labeled = new ArrayList<String>();
						previousLabel = "O";
						continue;
					}

					String[] tokens = line.split(" ");

					if (tokens.length != 4) {
						System.err.println("Invalid number of tokens for CoNNL corpus line: " + line);
						continue;
					}
					// we take the standard Grobid tokenizer
					StringTokenizer st2 = new StringTokenizer(tokens[0], 
						TextUtilities.fullPunctuations, true);
					while(st2.hasMoreTokens()) {
						String tok = st2.nextToken();
						if (tok.trim().length() == 0)
							continue;
						String label = tokens[3];
						label = translate(label);

						if (label.equals("O")) 
							labeled.add(tok + "\tO");
						else if (previousLabel.equals("O") || !previousLabel.equals(label))
							labeled.add(tok + "\tB-" + label);
						else
							labeled.add(tok + "\tI-" + label);
						previousLabel = label;
					}
				}
				writer.write("\n");
				br.close();				
			}
			writer.close();
			
			// we can train now a model
			GenericTrainer trainer = TrainerFactory.getTrainer();
	        final File tempModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath() + ".connl");
			
			System.out.println("Model file under: " + tempModelPath.getPath());	
	        trainer.train(getTemplatePath(), 
						  trainingOutputFile, 
						  tempModelPath, GrobidProperties.getNBThreads(), GrobidModels.ENTITIES_NER);
			
		}
		catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid CoNLL-2003 NER training.", e);
        }
		finally {
			try {
				if (writer != null)
					writer.close();
			}
			catch (Exception e) {
	        }
		}
		
		long end = System.currentTimeMillis();
		System.out.println("training done in " + (end - start)/1000 + " s.");
	}
	
	private String translate(String label) {
		if (label.equals("O")) {
			label = "O";
		}
		else if (label.endsWith("ORG")) {
			label = NERLexicon.NER_Type.ORGANISATION.getName();
		}
		else if (label.endsWith("PER")) {
			label = NERLexicon.NER_Type.PERSON.getName();
		}
		else if (label.endsWith("LOC")) {
			label = NERLexicon.NER_Type.LOCATION.getName();
		}
		else if (label.endsWith("MISC")) {
			label = NERLexicon.NER_Type.UNKNOWN.getName();;
		}
		return label;
	}
	
	/** 
	 *  Usual evalution of the 4-classes model NER for CoNNL-2003 Gold corpus using the official 
	 *  CoNNL evaluation script, see http://www.cnts.ua.ac.be/conll2003/ner/
	 */ 
	public void evalCoNLL(String set) {
		loadAdditionalProperties();
		long start = System.currentTimeMillis();
		Writer writer = null;
		File evalOutputFile2 = null;
		try {
			File eval = new File(conllPath + "/" + set);
			
			if (!eval.exists()) {
				throw new GrobidException(
					"Cannot start evaluation, because corpus resource path for CoNLL file " +
					" is not correctly set : " + conllPath + "/" + set);
			}
			
			System.out.println("\n\nEvaluation for data set: " + set);
			
			// we need to run the ner parser on the text stream
			// then recreate a CoNLL result file as expected by the CoNLL evaluation script
			// then call the evaluation script as command line
			
			File evalOutputFile = getTempEvaluationDataPath();
			System.out.println("Temp. evaluation data under: " + evalOutputFile.getPath());
			
			evalOutputFile2 = getTempEvaluationDataPath();
			System.out.println("Temp. processed evaluation data under: " + evalOutputFile2.getPath());
			
			// the file for writing the evaluation data
			OutputStream os = null;
			if (evalOutputFile != null) {
				os = new FileOutputStream(evalOutputFile);
				writer = new OutputStreamWriter(os, "UTF8");
			}
			
			List<List<OffsetPosition>> locationPositions = new ArrayList<List<OffsetPosition>>();
            List<List<OffsetPosition>> titleNamePositions = new ArrayList<List<OffsetPosition>>();
            List<List<OffsetPosition>> organisationPositions = new ArrayList<List<OffsetPosition>>();
			List<List<OffsetPosition>> orgFormPositions = new ArrayList<List<OffsetPosition>>();
						
			BufferedReader br = new BufferedReader(new InputStreamReader(
				new DataInputStream(new FileInputStream(eval))));
		  	String line = null;
			List<String> labeled = new ArrayList<String>();
			String previousLabel = "O";
		  	while ((line = br.readLine()) != null) {
				if (line.trim().length() == 0) {
					previousLabel = "O";
					if (labeled.size() > 0) {
						
						locationPositions.add(lexicon.inLocationNames(labeled));
			            titleNamePositions.add(lexicon.inPersonTitleNames(labeled));
			            organisationPositions.add(lexicon.inOrganisationNames(labeled));
						orgFormPositions.add(lexicon.inOrgFormNames(labeled));
						addFeatures(labeled, writer, 
							locationPositions, titleNamePositions, organisationPositions, orgFormPositions);
		            	writer.write("\n");
		
						locationPositions = new ArrayList<List<OffsetPosition>>();
			            titleNamePositions = new ArrayList<List<OffsetPosition>>();
			            organisationPositions = new ArrayList<List<OffsetPosition>>();
						orgFormPositions = new ArrayList<List<OffsetPosition>>();
						labeled = new ArrayList<String>();
						previousLabel = "O";
					}
					continue;
				}
				
				if (line.startsWith("-DOCSTART-") || line.startsWith("--")) {
					if (labeled.size() > 0) {
						addFeatures(labeled, writer, 
							locationPositions, titleNamePositions, organisationPositions, orgFormPositions);
		            	writer.write("\n");
						labeled = new ArrayList<String>();
					}
					previousLabel = "O";
					continue;
				}
			
				String[] tokens = line.split(" ");
				if (tokens.length != 4) {
					System.err.println("Invalid number of tokens for CoNNL corpus line: " + line);
					previousLabel = "O";
					continue;
				}
				
				String token = tokens[0];
				String label = tokens[3];
				label = translate(label);
				
				StringTokenizer st = new StringTokenizer(token, TextUtilities.fullPunctuations, true);
				while(st.hasMoreTokens()) { 
					if (label.equals("O"))
						labeled.add(st.nextToken() + " O");
					else if (previousLabel.equals(label))
						labeled.add(st.nextToken() + " I-" + label);
					else 
						labeled.add(st.nextToken() + " B-" + label);
					previousLabel = label;
				}
			}
			br.close();
			writer.close();	
			
			// apply now the model, we use a simple command line as it is only evaluation
			String modelPath = GrobidProperties.getModelPath(model).getAbsolutePath() + ".connl";
			
			String[] command = {"wapiti", "label", "-m", modelPath, evalOutputFile.getPath(), evalOutputFile2.getPath()};
		   	ProcessBuilder builder = new ProcessBuilder(command);
			//System.out.println("command: " + builder.command());
			Process process = builder.start();
		    try {
				int exitValue = process.waitFor();
				//System.out.println("exit value for wapiti labeling: " + exitValue);
			} 
			catch (InterruptedException e) {
			  	e.printStackTrace();
			}
			
			// we reformat the output for application of the CoNLL eval script
			br = new BufferedReader(new InputStreamReader(
				new DataInputStream(new FileInputStream(evalOutputFile2))));
			
			// and finally apply the CoNLL evaluation script
		    builder = new ProcessBuilder("/usr/bin/perl", conllPath + "/bin/conlleval");
			//System.out.println("command: " + builder.command());
			BufferedReader br2 = null;
			BufferedReader br3 = null;
			PrintWriter pw = null;
			try {
				process = builder.start();
				
				br2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
				br3 = new BufferedReader(new InputStreamReader(process.getErrorStream()));				
				pw = new PrintWriter(process.getOutputStream(), true);
				while ((line = br.readLine()) != null) {
					if (line.trim().length() == 0) {
						continue;
					}
					pw.write(line.replace("\t", " ") + "\n");
					pw.flush();
				}
				pw.close();
				
			    while ((line = br2.readLine()) != null) {
			      System.out.println(line);
			    }
				
				while ((line = br3.readLine()) != null) {
			      System.out.println(line);
			    }
				
				process.waitFor();
				int exitVal = process.exitValue();
			    //System.out.println("CoNLL eval script terminated, exit value: " + exitVal);
			}
			catch (Exception e) {
			  	e.printStackTrace();
			}
			finally {
				br.close();
				br2.close();
				br3.close();
				pw.close();
			}
		}
		catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid CoNLL-2003 NER evaluation.", e);
        }
		finally {
			try {
				if (writer != null)
					writer.close();
			   	//evalOutputFile2.delete();					
			}
			catch (Exception e) {
	        }		
		}
		
		long end = System.currentTimeMillis();
		System.out.println("evaluation done in " + (end - start)/1000 + " s.");	
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

	        CoNNLNERTrainer trainer = new CoNNLNERTrainer();
	
	        //trainer.trainCoNLL(true);
			trainer.evalCoNLL("eng.train");
			trainer.evalCoNLL("eng.testa");
			trainer.evalCoNLL("eng.testb");
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