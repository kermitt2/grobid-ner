package org.grobid.trainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeaturesVectorNER;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.trainer.stax.*;
import org.grobid.trainer.evaluation.EvaluationUtilities;

import org.codehaus.stax2.XMLStreamReader2;
import com.ctc.wstx.stax.WstxInputFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.*;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Create the French NER tagging model 
 *
 * @author Patrice Lopez
 */
public class NERFrenchTrainer extends AbstractTrainer {

	private static Logger LOGGER = LoggerFactory.getLogger(NERFrenchTrainer.class);

	private NERLexicon nerLexicon = NERLexicon.getInstance();
	protected Lexicon lexicon = Lexicon.getInstance();

	private String leMondeCorpusPath = null;

    public NERFrenchTrainer() {
        super(GrobidModels.ENTITIES_NERFR);

		// read additional properties for this sub-project to get the paths to the resources
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("src/main/resources/grobid-ner.properties");

			// load the properties file
			prop.load(input);

			// get the property value
			leMondeCorpusPath = prop.getProperty("grobid.ner.leMondeCorpus.path");
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

	@Override
	/**
     * Add the selected features to the training data for the NER model
     */
    public int createCRFPPData(File sourcePathLabel,
                               File outputPath) {
		return createCRFPPData(sourcePathLabel, outputPath, null, 1.0);
	}
	
	/**
	 * Add the selected features to a NER example set 
	 * 
	 * @param corpusDir
	 *            a path where corpus files are located
	 * @param trainingOutputPath
	 *            path where to store the temporary training data
	 * @param evalOutputPath
	 *            path where to store the temporary evaluation data
	 * @param splitRatio
	 *            ratio to consider for separating training and evaluation data, e.g. 0.8 for 80% 
	 * @return the total number of used corpus items 
	 */
	@Override
	public int createCRFPPData(final File corpusDir, 
							final File trainingOutputPath, 
							final File evalOutputPath, 
							double splitRatio) {
        int totalExamples = 0;
        try {
            System.out.println("sourcePathLabel: " + corpusDir);
			if (trainingOutputPath != null)
				System.out.println("outputPath for training data: " + trainingOutputPath);
			if (evalOutputPath != null)
				System.out.println("outputPath for evaluation data: " + evalOutputPath);

			// the file for writing the training data
			OutputStream os2 = null;
			Writer writer2 = null;
			if (trainingOutputPath != null) {
				os2 = new FileOutputStream(trainingOutputPath);
				writer2 = new OutputStreamWriter(os2, "UTF8");
			}

			// the file for writing the evaluation data
			OutputStream os3 = null;
			Writer writer3 = null;
			if (evalOutputPath != null) {
				os3 = new FileOutputStream(evalOutputPath);
				writer3 = new OutputStreamWriter(os3, "UTF8");
			}

			File corpusLeMondeDir = new File(leMondeCorpusPath);
			if (!corpusLeMondeDir.exists()) {
				LOGGER.warn("Directory does not exist: " + leMondeCorpusPath);
			}
			File[] files = corpusLeMondeDir.listFiles();
			if ((files == null) || (files.length == 0)) {
				LOGGER.warn("No files in directory: " + corpusLeMondeDir);
			}

			// process the core trainig set corresponding to LeMonde corpus first
			for (int i=0; i<files.length; i++) {
				System.out.println(files[i].getName());
				if (files[i].getName().indexOf(".xml") != -1)
					totalExamples += processLeMonde(files[i], writer2, writer3, splitRatio);
			}
			
			if (writer2 != null) {
				writer2.close();
			}
			if (os2 != null) {
				os2.close();
			}
			
			if (writer3 != null) {
				writer3.close();
			}
			if (os3 != null) {
				os3.close();
			}
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return totalExamples;
    }


	private int processLeMonde(final File corpusFile, Writer writerTraining, Writer writerEvaluation, double splitRatio) {
		int res = 0;
		try {
			// the "core" set is a the set of files corresponding to Lemonde corpus, but with 
			// the default class annotations manually corrected
			System.out.println("Path to French corpus CoNLL training: " + corpusFile.getPath());
			if (!corpusFile.exists()) {
				throw new 
					GrobidException("Cannot start training, because corpus resource file is not correctly set : " 
					+ corpusFile.getPath());
			}
			
			WstxInputFactory inputFactory = new WstxInputFactory();
			Writer writer = new StringWriter();
			INRIALeMondeCorpusStaxHandler target = new INRIALeMondeCorpusStaxHandler(writer);

			InputStream is = new FileInputStream(corpusFile);
			XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);
	        StaxUtils.traverse(reader, target);

	        String output = writer.toString();
	        String[] lines = output.split("\n");
			writer = null;

			// to store unit term positions
            List<List<OffsetPosition>> locationPositions = new ArrayList<List<OffsetPosition>>();
            List<List<OffsetPosition>> personTitlePositions = new ArrayList<List<OffsetPosition>>();
            List<List<OffsetPosition>> organisationPositions = new ArrayList<List<OffsetPosition>>();		
			List<List<OffsetPosition>> orgFormPositions = new ArrayList<List<OffsetPosition>>();
			List<String> labeled = new ArrayList<String>();
			String line = null;
			for(int i = 0; i<lines.length; i++) {
				line = lines[i].trim();

				// note that we work at sentence level
				if (line.startsWith("-DOCSTART-") || line.startsWith("-X-")) {
					// the balance of data between training and evaluation is realised
					// at document level 
					if ( (writerTraining == null) && (writerEvaluation != null) )
						writer = writerEvaluation;
					if ( (writerTraining != null) && (writerEvaluation == null) )
						writer = writerTraining;
					else {		
						if (Math.random() <= splitRatio)
							writer = writerTraining;
						else 
							writer = writerEvaluation;
					}

					continue;
				}

				// in the this line, we only keep what we are interested in for this model 
				int ind = line.indexOf("\t");
				if (ind != -1) {
					ind = line.indexOf("\t", ind+1);
					if (ind != -1)
						line = line.substring(0,ind);
				}
				//System.out.println(line);

				if ((line.length() == 0) && (labeled.size() > 0)) {
					// sentence is complete
					locationPositions.add(lexicon.inLocationNames(labeled));
		            personTitlePositions.add(lexicon.inPersonTitleNames(labeled));
		            organisationPositions.add(lexicon.inOrganisationNames(labeled));
					orgFormPositions.add(lexicon.inOrgFormNames(labeled));	
				
					// this is mandatory for the correct setting of features
					labeled.add("@newline");
				
					addFeatures(labeled, writer, 
						locationPositions, personTitlePositions, organisationPositions, orgFormPositions);
					writer.write("\n");
				
					locationPositions = new ArrayList<List<OffsetPosition>>();
		            personTitlePositions = new ArrayList<List<OffsetPosition>>();
		            organisationPositions = new ArrayList<List<OffsetPosition>>();		
					orgFormPositions = new ArrayList<List<OffsetPosition>>();
					
					labeled = new ArrayList<String>();
					res++;
				}		
				else 
					labeled.add(line);	
			}
		}
		catch (Exception ex) {
			throw new GrobidResourceException(
				"An exception occured when accessing/reading Le Monde corpus files.", ex);
		} 
		finally {
		}
		return res;
	}
	

    @SuppressWarnings({"UnusedParameters"})
    static public void addFeatures(List<String> texts,
                            Writer writer,
                            List<List<OffsetPosition>> locationPositions,
							List<List<OffsetPosition>> personTitlePositions,
							List<List<OffsetPosition>> organisationPositions,
							List<List<OffsetPosition>> orgFormPositions) {
        int totalLine = texts.size();
        int posit = 0;
		int sentence = 0;
		int currentLocationIndex = 0;
		int currentPersonTitleIndex = 0;
		int currentOrganisationIndex = 0;
		int currentOrgFormIndex = 0;
		List<OffsetPosition> localLocationPositions = null;
		List<OffsetPosition> localPersonTitlePositions = null;
		List<OffsetPosition> localOrganisationPositions = null;
		List<OffsetPosition> localOrgFormPositions = null;
		if (locationPositions.size() > sentence)
			localLocationPositions = locationPositions.get(sentence);
		if (personTitlePositions.size() > sentence)	
			localPersonTitlePositions = personTitlePositions.get(sentence);
		if (organisationPositions.size() > sentence)			
			localOrganisationPositions = organisationPositions.get(sentence);
		if (orgFormPositions.size() > sentence)			
			localOrgFormPositions = orgFormPositions.get(sentence);	
        boolean isLocationToken = false;
		boolean isPersonTitleToken = false;
		boolean isOrganisationToken = false;
		boolean isOrgFormToken = false;
        try {
			String previousLabel = null;
            for (String line : texts) {
				if (line.trim().equals("@newline")) {
					writer.write("\n");
	                writer.flush();	
					sentence++;
					previousLabel= null;
					if (locationPositions.size() > sentence)
						localLocationPositions = locationPositions.get(sentence);
					if (personTitlePositions.size() > sentence)		
						localPersonTitlePositions = personTitlePositions.get(sentence);
					if (organisationPositions.size() > sentence)	
						localOrganisationPositions = organisationPositions.get(sentence);
					if (orgFormPositions.size() > sentence)	
						localOrgFormPositions = orgFormPositions.get(sentence);
				}
				
				/*int ind = line.indexOf("\t");
				if (ind == -1) 
				 	ind = line.indexOf(" ");
				if (ind != -1) {		
				}*/
				
				// do we have a unit term at position posit?
				if ( (localLocationPositions != null) && (localLocationPositions.size() > 0) ) {
					for(int mm = currentLocationIndex; mm < localLocationPositions.size(); mm++) {
						if ( (posit >= localLocationPositions.get(mm).start) && 
							 (posit <= localLocationPositions.get(mm).end) ) {
							isLocationToken = true;
							currentLocationIndex = mm;
							break;
						}
						else if (posit < localLocationPositions.get(mm).start) {
							isLocationToken = false;
							break;
						}
						else if (posit > localLocationPositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (localPersonTitlePositions != null) && (localPersonTitlePositions.size() > 0) ) {
					for(int mm = currentPersonTitleIndex; mm < localPersonTitlePositions.size(); mm++) {
						if ( (posit >= localPersonTitlePositions.get(mm).start) && 
							 (posit <= localPersonTitlePositions.get(mm).end) ) {
							isPersonTitleToken = true;
							currentPersonTitleIndex = mm;
							break;
						}
						else if (posit < localPersonTitlePositions.get(mm).start) {
							isPersonTitleToken = false;
							break;
						}
						else if (posit > localPersonTitlePositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (localOrganisationPositions != null) && (localOrganisationPositions.size() > 0) ) {
					for(int mm = currentOrganisationIndex; mm < localOrganisationPositions.size(); mm++) {
						if ( (posit >= localOrganisationPositions.get(mm).start) && 
							 (posit <= localOrganisationPositions.get(mm).end) ) {
							isOrganisationToken = true;
							currentOrganisationIndex = mm;
							break;
						}
						else if (posit < localOrganisationPositions.get(mm).start) {
							isOrganisationToken = false;
							break;
						}
						else if (posit > localOrganisationPositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (localOrgFormPositions != null) && (localOrgFormPositions.size() > 0) ) {
					for(int mm = currentOrgFormIndex; mm < localOrgFormPositions.size(); mm++) {
						if ( (posit >= localOrgFormPositions.get(mm).start) && 
							 (posit <= localOrgFormPositions.get(mm).end) ) {
							isOrgFormToken = true;
							currentOrgFormIndex = mm;
							break;
						}
						else if (posit < localOrgFormPositions.get(mm).start) {
							isOrgFormToken = false;
							break;
						}
						else if (posit > localOrgFormPositions.get(mm).end) {
							continue;
						}
					}
				}

				// the "line" expected by the method FeaturesVectorNER.addFeaturesNER is the token
				// followed by the label, separated by a tab, and nothing else
				// in addition, the B- prefix for the labels must be injected here if not 
				// present
				String cleanLine = "";
				String[] pieces = line.split("\t");
				
				if (pieces.length > 0) {
					// first string is always the token
					cleanLine += pieces[0];
				}
				if (pieces.length > 1) {
					// second piece is the NER label, which could a single label or a list of labels in brackets 
					// separated by a comma
					if (pieces[1].startsWith("[")) {
						String rawlab = pieces[1].substring(1,pieces[1].length()-1);
						int ind = rawlab.indexOf(",");
						if (ind != -1) {
							rawlab = rawlab.substring(0,ind);
							if (rawlab.startsWith("B-") || rawlab.equals("O") || rawlab.equals("<other>") || 
								rawlab.equals("0")) {
								cleanLine += "\t" + rawlab;
							}
							else {
								if ( (previousLabel == null) || (!previousLabel.equals(rawlab)) || 
									(previousLabel.equals("O")) || (previousLabel.equals("0")) || (previousLabel.equals("<other>"))) {
									cleanLine += "\tB-" + rawlab;
								}
								else 
									cleanLine += "\t" + rawlab;
							}
						}
						else {
							//cleanLine += "\t" + labels;
							System.out.println("WARNING, format error:" + rawlab);
						}
						previousLabel = rawlab;
					}
					else {
						String rawlab = pieces[1];
						if (rawlab.startsWith("B-") || rawlab.equals("O") ||  rawlab.equals("<other>") || 
							rawlab.equals("0")) {
							cleanLine += "\t" + rawlab;
						}
						else {
							if ( (previousLabel == null) || (!previousLabel.equals(rawlab)) || 
								(previousLabel.equals("O")) || (previousLabel.equals("0")) || (previousLabel.equals("<other>")) ) {
								cleanLine += "\tB-" + rawlab;
							}
							else 
								cleanLine += "\t" + rawlab;
						}
						previousLabel = rawlab;
					}
				}
                FeaturesVectorNER featuresVector =
                        FeaturesVectorNER.addFeaturesNER(cleanLine, isLocationToken, isPersonTitleToken, 
							isOrganisationToken, isOrgFormToken);
                if (featuresVector.label == null)
                    continue;
                writer.write(featuresVector.printVector()+"\n");
                writer.flush();
                posit++;
				isLocationToken = false;
				isPersonTitleToken = false;
				isOrganisationToken = false;
            }
        } 
		catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

	/**
	 *  Standard evaluation via the the usual Grobid evaluation framework.
	 */
	public String evaluate() {
		File evalDataF = GrobidProperties.getInstance().getEvalCorpusPath(
			new File(new File("resources").getAbsolutePath()), model);
		
		File tmpEvalPath = getTempEvaluationDataPath();		
		createCRFPPData(evalDataF, tmpEvalPath);

        return EvaluationUtilities.evaluateStandard(tmpEvalPath.getAbsolutePath(), getTagger());
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

	        NERFrenchTrainer trainer = new NERFrenchTrainer();
	
	        AbstractTrainer.runTraining(trainer);
	        //AbstractTrainer.runEvaluation(trainer);
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