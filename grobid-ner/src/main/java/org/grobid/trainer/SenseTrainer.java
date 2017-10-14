package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeaturesVectorNERSense;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.layout.LayoutToken;
import org.grobid.trainer.sax.ReutersSaxHandler;
import org.grobid.trainer.sax.SemDocSaxHandler;
import org.grobid.trainer.evaluation.EvaluationUtilities;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * Train a model for estimating the most likely synset for a recognized named entity. This sense 
 * enriches the NER results and can be exploited for improving the entity disambiguation resolution
 * in a further stage.
 * 
 * @author Patrice Lopez
 */
public class SenseTrainer extends AbstractTrainer {

	private NERLexicon nerLexicon = NERLexicon.getInstance();
	private Lexicon lexicon = Lexicon.getInstance();
	
	private String reutersPath = null;
	private String conllPath = null;
	private String idiliaPath = null;
	private String nerCorpusPath = null;
	
	private Map<String,String> descriptions = null;
	
	private static int LIMIT = 20000; // limit of files to process per zip archives, -1 if no limit
	private static int GLOBAL_LIMIT = 300000; // overall limit of files to process, -1 if no limit

    public SenseTrainer() {
        super(GrobidModels.ENTITIES_NERSense);
		descriptions = new TreeMap<String,String>();
		
		// we read first the module specific property file to get the paths to the resources
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
			nerCorpusPath = prop.getProperty("grobid.ner.extra_corpus");
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
     * Add the selected features to the training data for NER sense model
     */
    public int createCRFPPData(File sourcePathLabel,
                               File outputPath) {
		return createCRFPPData(sourcePathLabel, outputPath, null, 1.0);
	}

	/**
	 * Add the selected features to a NER sense example set 
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
			
			// the file for writing the relevant WordNet sense descriptions
			OutputStream osDesc = null;
			Writer writerDesc = null;
			String descOutputPath = GrobidProperties.getGrobidHomePath() + "/lexicon/senses/descriptions.txt";
			osDesc = new FileOutputStream(descOutputPath);
			writerDesc = new OutputStreamWriter(osDesc, "UTF8");

			// process the core trainig set first (set of Reuters files corresponding to the 
			// CoNLL set but with the 26 class annotations manually corrected)
			totalExamples = processReutersCore(writer2, writer3, splitRatio);

			// process the reuters corpus first
			totalExamples = processReutersCorpus(writer2, writer3, writerDesc, splitRatio);

			// we convert the tei files into the usual CRF label format
            // we process all tei files in the output directory
            File[] refFiles = corpusDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith("*.tei") || name.endsWith("*.tei.xml"));
                }
            });

            if (refFiles == null) {
                return 0;
            }

     		System.out.println(refFiles.length + " TEI files to process.");

            String name;
            int n = 0;
            for (File teifile : refFiles) {	
				Writer writer = null;
				if ( (writer2 == null) && (writer3 != null) )
					writer = writer3;
				if ( (writer2 != null) && (writer3 == null) )
					writer = writer2;
				else {
					if (Math.random() <= splitRatio)
						writer = writer2;
					else 
						writer = writer3;
				}

				// to store unit term positions
                List<OffsetPosition> locationPositions = null;	
                List<OffsetPosition> personTitlePositions = null;
                List<OffsetPosition> organisationPositions = null;
				List<OffsetPosition> orgFormPositions = null;
				
				// we simply need to retokenize the token following Grobid approach	
				List<LayoutToken> tokens = new ArrayList<LayoutToken>();
				List<String> labels = new ArrayList<String>();
			  	BufferedReader br = new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(teifile))));
			  	String line;
				List<String> input = new ArrayList<String>();
			  	while ((line = br.readLine()) != null)   {
					if (line.trim().length() == 0) {
						LayoutToken token = new LayoutToken("\n");
						tokens.add(token);
						labels.add(null);
						continue;
					}
					int ind = line.indexOf("\t");
					if (ind == -1) {
						continue;
					}
					// we take the standard Grobid tokenizer
					StringTokenizer st2 = new StringTokenizer(line.substring(0, ind), 
						TextUtilities.fullPunctuations, true);
					while(st2.hasMoreTokens()) {
						String tok = st2.nextToken();
						if (tok.trim().length() == 0)
							continue;
						LayoutToken token = new LayoutToken(tok);
						tokens.add(token);
						String label = line.substring(ind+1, line.length());

						if (label.startsWith("I-")) {
							label = label.substring(2,label.length());
						}
						else if (label.startsWith("B-")) {
							label = label;
						}
						labels.add(label);
					}					
				}
				//labeled.add("@newline");				                 				
				//nerTokenPositions.add(lexicon.inNERNames(input));

				locationPositions = lexicon.tokenPositionsLocationNames(tokens);
				personTitlePositions = lexicon.tokenPositionsPersonTitle(tokens);
				organisationPositions = lexicon.tokenPositionsOrganisationNames(tokens);
				orgFormPositions = lexicon.tokenPositionsOrgForm(tokens);

                addFeatures(tokens, labels, writer, 
					locationPositions, personTitlePositions, organisationPositions, orgFormPositions);
                writer.write("\n");
				br.close();
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
			if (writerDesc != null) {
				writerDesc.close();
			}
			if (os3 != null) {
				os3.close();
			}
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return totalExamples;
    }
	
	private int processReutersCore(Writer writerTraining, Writer writerEvaluation, double splitRatio) {
		int res = 0;
		try {
			// the "core" Reuters set is a the set of Reuters files corresponding to the 
			// CoNLL set, but with the 26 class annotations manually corrected
			// these annotated files are available in the corpus resource path of the model
			File corpusDir1 = new File(nerCorpusPath + "/reuters.ner26.train");
			System.out.println("Path to 26-classes Reuters corpus CoNLL train set: " + corpusDir1.getPath());
			if (!corpusDir1.exists()) {
				throw new GrobidException("Cannot start training, because corpus resource folder is not correctly set : " 
					+ corpusDir1.getPath());
			}
			
			File corpusDir2 = new File(nerCorpusPath + "/reuters.ner26.testa");
			System.out.println("Path to 26-classes Reuters corpus CoNLL testa set: " + corpusDir2.getPath());
			if (!corpusDir2.exists()) {
				throw new GrobidException("Cannot start training, because corpus resource folder is not correctly set : " 
					+ corpusDir2.getPath());
			}
		
			File corpusDir3 = new File(nerCorpusPath + "/reuters.ner26.testa");
			System.out.println("Path to 26-classes Reuters corpus CoNLL testb set: " + corpusDir3.getPath());
			if (!corpusDir3.exists()) {
				throw new GrobidException("Cannot start training, because corpus resource folder is not correctly set : " 
					+ corpusDir3.getPath());
			}
			
			List<File> corpora = new ArrayList<File>();
			corpora.add(corpusDir1);
			//corpora.add(corpusDir2);
			//corpora.add(corpusDir3);
			
			for(File corpusDir : corpora) {
				Writer writer = null;
				BufferedReader bufReader = new BufferedReader(
		                new InputStreamReader(new FileInputStream(corpusDir), "UTF-8"));
				// to store unit term positions
	            List<OffsetPosition> locationPositions = null;
	            List<OffsetPosition> personTitlePositions = null;
	            List<OffsetPosition> organisationPositions = null;		
				List<OffsetPosition> orgFormPositions = null;
				List<LayoutToken> tokens = new ArrayList<LayoutToken>();
				List<String> labels = new ArrayList<String>();
				String line = null;
				while ((line = bufReader.readLine()) != null) {
					line = line.trim();
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

					if (line.trim().length() == 0) {
						// sentence is complete
						LayoutToken token = new LayoutToken("\n");
						tokens.add(token);
						labels.add(null);
						continue;
					}		
					else {
						int ind = line.indexOf("\t");
						if (ind == -1) {
							continue;
						}
						// we take the standard Grobid tokenizer
						StringTokenizer st2 = new StringTokenizer(line.substring(0, ind), 
							TextUtilities.fullPunctuations, true);
						while(st2.hasMoreTokens()) {
							String tok = st2.nextToken();
							if (tok.trim().length() == 0)
								continue;
							LayoutToken token = new LayoutToken(tok);
							tokens.add(token);
							String label = line.substring(ind+1, line.length());

							if (label.startsWith("I-")) {
								label = label.substring(2,label.length());
							}
							else if (label.startsWith("B-")) {
								label = label;
							}
							labels.add(label);
						}
					}
				}
				locationPositions = lexicon.tokenPositionsLocationNames(tokens);
			    personTitlePositions = lexicon.tokenPositionsPersonTitle(tokens);
			    organisationPositions = lexicon.tokenPositionsOrganisationNames(tokens);
				orgFormPositions = lexicon.tokenPositionsOrgForm(tokens);	

				addFeatures(tokens, labels, writer, 
					locationPositions, personTitlePositions, organisationPositions, orgFormPositions);
				writer.write("\n");
			}
		}
		catch (IOException ex) {
			throw new GrobidResourceException(
				"An exception occured when accessing/reading the Reuters corpus files.", ex);
		} 
		finally {
		}
		return res;
	}
	

	private int processReutersCorpus(Writer writerTraining, 
									Writer writerEvaluation, 
									Writer writerDesc, 
									double splitRatio) {
		int res = 0;

		try {
			File corpusDir = new File(reutersPath);
			System.out.println("Path to Reuters corpus: " + reutersPath);
			if (!corpusDir.exists()) {
				throw new 
					GrobidException("Cannot start training, because corpus resource folder is not correctly set : " 
					+ reutersPath);
			}

			File[] refFiles = corpusDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".zip"));
                }
            });

			System.out.println(refFiles.length + " reuters zip files");

            if (refFiles == null) {
                return 0;
            }
			for (File thefile : refFiles) {	
				ZipFile zipFile = new ZipFile(thefile);

			    Enumeration<? extends ZipEntry> entries = zipFile.entries();
			    while(entries.hasMoreElements()) {
			        ZipEntry entry = entries.nextElement();
			        InputStream xmlStream = zipFile.getInputStream(entry);

					res += processReutersCorpus(xmlStream, entry, writerTraining, writerEvaluation, splitRatio);
					xmlStream.close();

					// as the number of files might be too important for development and debugging, 
					// we introduce an optional limit 
					if ( (LIMIT != -1) && (res > LIMIT) ) {
						break;
					}
			    }
				if ( (GLOBAL_LIMIT != -1) && (res > GLOBAL_LIMIT) ) {
					break;
				}
			}	
			
			// write the list of sense descriptions
			// the file for writing the evaluation data
			for (Map.Entry<String,String> entry : descriptions.entrySet()) {
				String desc = entry.getValue();
				String sense = entry.getKey();
				writerDesc.write(sense + "\t" + desc + "\n");
			}
			writerDesc.flush();
		}
		catch (IOException ex) {
			throw new GrobidResourceException(
				"An exception occured when accessing/reading the Reuters corpus zip files.", ex);
		} 
		finally {
			try {
				writerDesc.close();
			}
			catch(Exception e) {
			}
		}
		return res;
	}

	private int processReutersCorpus(InputStream currentStream, 
									 ZipEntry entry,
									 Writer writerTraining, 
									 Writer writerEvaluation, 
									 double splitRatio) {
		try {
			// try to open the corresponding semdoc file
			String fileName = entry.getName();
System.out.println(fileName);
			File semdocFile = 
				new File(idiliaPath+"/"+fileName.substring(0,3)+"/" + fileName.replace(".xml",".semdoc.xml"));
			if (!semdocFile.exists()) {
				throw new GrobidException("Cannot start training, because corpus resource folder for semdoc file " +
				" is not correctly set : " 
					+ idiliaPath+"/"+fileName.substring(0,3)+"/" + fileName.replace(".xml",".semdoc.xml"));
			}

			ReutersSaxHandler reutersSax = new ReutersSaxHandler();

			// get a factory
	        SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setValidating(false);
	        spf.setFeature("http://xml.org/sax/features/namespaces", false);
	        spf.setFeature("http://xml.org/sax/features/validation", false);
	        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
	        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			//get a new instance of parser
	        SAXParser p = spf.newSAXParser();
	        p.parse(currentStream, reutersSax);

			SemDocSaxHandler semdocSax = new SemDocSaxHandler(reutersSax.getTextVector());
			semdocSax.setModeWSD(true);
			semdocSax.setDescriptions(descriptions);
			
	        p = spf.newSAXParser();
	        p.parse(semdocFile, semdocSax);

			Writer writer = null;
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
			
			if (semdocSax.getAnnotatedTextVector() != null) {		
				// to store unit term positions
	            List<OffsetPosition> locationPositions = null;
	            List<OffsetPosition> personTitlePositions = null;
	            List<OffsetPosition> organisationPositions = null;		
				List<OffsetPosition> orgFormPositions = null;
				List<LayoutToken> tokens = new ArrayList<LayoutToken>();
				List<String> labels = new ArrayList<String>();

				for(String line : semdocSax.getAnnotatedTextVector()) {	
					if (line.trim().startsWith("@newline")) {
						// sentence is complete
						LayoutToken token = new LayoutToken("\n");
						tokens.add(token);
						labels.add(null);
						continue;
					}
					else {
						int ind = line.indexOf("\t");
						if (ind == -1) {
							continue;
						}

						// we take the standard Grobid tokenizer
						StringTokenizer st2 = new StringTokenizer(line.substring(0, ind), 
							TextUtilities.fullPunctuations, true);
						while(st2.hasMoreTokens()) {
							String tok = st2.nextToken();
							if (tok.trim().length() == 0)
								continue;
							LayoutToken token = new LayoutToken(tok);
							tokens.add(token);
							String label = line.substring(ind+1, line.length());

							if (label.startsWith("I-")) {
								label = label.substring(2,label.length());
							}
							else if (label.startsWith("B-")) {
								label = label;
							}
							labels.add(label);
						}
					}
				}

				locationPositions = lexicon.tokenPositionsLocationNames(tokens);
	            personTitlePositions = lexicon.tokenPositionsPersonTitle(tokens);
	            organisationPositions = lexicon.tokenPositionsOrganisationNames(tokens);
				orgFormPositions = lexicon.tokenPositionsOrgForm(tokens);

				addFeatures(tokens, labels, writer, 
					locationPositions, personTitlePositions, organisationPositions, orgFormPositions);
				writer.write("\n");	
			}
		}
		catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
		return 1;
	}

    @SuppressWarnings({"UnusedParameters"})
    static public void addFeatures(List<LayoutToken> tokens, 
    						List<String> labels,
                            Writer writer,
                            List<OffsetPosition> locationPositions,
							List<OffsetPosition> personTitlePositions,
							List<OffsetPosition> organisationPositions,
							List<OffsetPosition> orgFormPositions) {
        //int totalLine = texts.size();
        int posit = 0;
		//int sentence = 0;
		int currentLocationIndex = 0;
		int currentPersonTitleIndex = 0;
		int currentOrganisationIndex = 0;
		int currentOrgFormIndex = 0;
		/*List<OffsetPosition> localLocationPositions = null;
		List<OffsetPosition> localPersonTitlePositions = null;
		List<OffsetPosition> localOrganisationPositions = null;
		List<OffsetPosition> localOrgFormPositions = null;*/
		/*if (locationPositions.size() > sentence)
			localLocationPositions = locationPositions.get(sentence);
		if (personTitlePositions.size() > sentence)	
			localPersonTitlePositions = personTitlePositions.get(sentence);
		if (organisationPositions.size() > sentence)			
			localOrganisationPositions = organisationPositions.get(sentence);
		if (orgFormPositions.size() > sentence)			
			localOrgFormPositions = orgFormPositions.get(sentence);	*/
        boolean isLocationToken = false;
		boolean isPersonTitleToken = false;
		boolean isOrganisationToken = false;
		boolean isOrgFormToken = false;
        try {
			String previousLabel = null;
            for (int n=0; n<tokens.size(); n++) {
            	LayoutToken token = tokens.get(n);
				if (token.getText().startsWith("@newline") || (token.getText().equals("\n"))) {
					writer.write("\n");
	                writer.flush();	
					//sentence++;
					previousLabel = null;
					/*if (locationPositions.size() > sentence)
						localLocationPositions = locationPositions.get(sentence);
					if (personTitlePositions.size() > sentence)		
						localPersonTitlePositions = personTitlePositions.get(sentence);
					if (organisationPositions.size() > sentence)	
						localOrganisationPositions = organisationPositions.get(sentence);
					if (orgFormPositions.size() > sentence)	
						localOrgFormPositions = orgFormPositions.get(sentence);*/
					continue;
				}

				// do we have a unit term at position posit?
				if ( (locationPositions != null) && (locationPositions.size() > 0) ) {
					for(int mm = currentLocationIndex; mm < locationPositions.size(); mm++) {
						if ( (posit >= locationPositions.get(mm).start) && 
							 (posit <= locationPositions.get(mm).end) ) {
							isLocationToken = true;
							currentLocationIndex = mm;
							break;
						}
						else if (posit < locationPositions.get(mm).start) {
							isLocationToken = false;
							break;
						}
						else if (posit > locationPositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (personTitlePositions != null) && (personTitlePositions.size() > 0) ) {
					for(int mm = currentPersonTitleIndex; mm < personTitlePositions.size(); mm++) {
						if ( (posit >= personTitlePositions.get(mm).start) && 
							 (posit <= personTitlePositions.get(mm).end) ) {
							isPersonTitleToken = true;
							currentPersonTitleIndex = mm;
							break;
						}
						else if (posit < personTitlePositions.get(mm).start) {
							isPersonTitleToken = false;
							break;
						}
						else if (posit > personTitlePositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (organisationPositions != null) && (organisationPositions.size() > 0) ) {
					for(int mm = currentOrganisationIndex; mm < organisationPositions.size(); mm++) {
						if ( (posit >= organisationPositions.get(mm).start) && 
							 (posit <= organisationPositions.get(mm).end) ) {
							isOrganisationToken = true;
							currentOrganisationIndex = mm;
							break;
						}
						else if (posit < organisationPositions.get(mm).start) {
							isOrganisationToken = false;
							break;
						}
						else if (posit > organisationPositions.get(mm).end) {
							continue;
						}
					}
				}
				if ( (orgFormPositions != null) && (orgFormPositions.size() > 0) ) {
					for(int mm = currentOrgFormIndex; mm < orgFormPositions.size(); mm++) {
						if ( (posit >= orgFormPositions.get(mm).start) && 
							 (posit <= orgFormPositions.get(mm).end) ) {
							isOrgFormToken = true;
							currentOrgFormIndex = mm;
							break;
						}
						else if (posit < orgFormPositions.get(mm).start) {
							isOrgFormToken = false;
							break;
						}
						else if (posit > orgFormPositions.get(mm).end) {
							continue;
						}
					}
				}
				
				// the "line" expected by the method FeaturesVectorNER.addFeaturesNER is the token
				// followed by the label, separated by a tab, and nothing else
				// in addition, the B- prefix for the labels must be injected here if not 
				// present
				String cleanLine = token.getText();
				
				// second field is the NER label, the third one gives the sense label
				String label = labels.get(n);
				// second piece is the NER label, which could a single label or a list of labels in brackets 
				// separated by a comma
				if (label.startsWith("[")) {
					String rawlab = label.substring(1,label.length()-1);
					int ind = rawlab.indexOf(",");
					if (ind != -1) {
						rawlab = rawlab.substring(0,ind);
						if (rawlab.startsWith("B-") || rawlab.equals("O") || 
							rawlab.equals("0")) {
							cleanLine += "\t" + rawlab;
						}
						else {
							if ( (previousLabel == null) || (!previousLabel.equals(rawlab)) || 
								(previousLabel.equals("O")) || (previousLabel.equals("0")) ) {
								cleanLine += "\tB-" + rawlab;
							}
							else 
								cleanLine += "\t" + rawlab;
						}
					}
					else {
						//cleanLine += "\t" + labels;
						System.out.println("WARNING, format error: " + rawlab);
					}
					previousLabel = rawlab;
				}
				else {
					String rawlab = label;
					if (rawlab.startsWith("B-") || rawlab.equals("O") || 
						rawlab.equals("0")) {
						cleanLine += "\t" + rawlab;
					}
					else {
						if ( (previousLabel == null) || (!previousLabel.equals(rawlab)) || 
							(previousLabel.equals("O")) || (previousLabel.equals("0")) ) {
							cleanLine += "\tB-" + rawlab;
						}
						else 
							cleanLine += "\t" + rawlab;
					}
					previousLabel = rawlab;
				}
				
                FeaturesVectorNERSense featuresVector =
                        FeaturesVectorNERSense.addFeatures(cleanLine, isLocationToken, isPersonTitleToken,
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

	        Trainer trainer = new SenseTrainer();
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