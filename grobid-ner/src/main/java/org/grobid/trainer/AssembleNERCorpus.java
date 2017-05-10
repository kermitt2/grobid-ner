package org.grobid.trainer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.grobid.core.engines.NEREnParser;
import org.grobid.core.engines.SenseTagger;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeaturesVectorNER;
import org.grobid.core.features.FeaturesVectorNERSense;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.trainer.sax.ReutersSaxHandler;
import org.grobid.trainer.sax.SemDocSaxHandler;
import org.grobid.trainer.sax.TextSaxHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Assemble the different corpus information spread in different source files into CoNNL files and TEI files.
 *
 * @author Patrice Lopez
 */
public class AssembleNERCorpus {

    private static Logger LOGGER = LoggerFactory.getLogger(AssembleNERCorpus.class);

    private String reutersPath = null;
    private String conllPath = null;
    private String idiliaPath = null;
    private String nerCorpusPath = null;
    private Lexicon lexicon = null;

    public AssembleNERCorpus() {
        // we read the module specific property file to get the paths to the resources
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = this.getClass().getResourceAsStream("/grobid-ner.properties");

            // load the properties file
            prop.load(input);

            // get the property value
            reutersPath = prop.getProperty("grobid.ner.reuters.paths");
            conllPath = prop.getProperty("grobid.ner.reuters.conll_path");
            idiliaPath = prop.getProperty("grobid.ner.reuters.idilia_path");
            nerCorpusPath = prop.getProperty("grobid.ner.extra_corpus");

            lexicon = Lexicon.getInstance();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
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
     * usual training path of the model (resources/dataset/ner/corpus) and
     * a CoNLL NER file format.
     */
    public void assembleCoNLL() {
        String reutersSelectionPath = "resources/dataset/ner/corpus/reuters.txt";
        NEREnParser parserNER = null;
        SenseTagger parserSense = null;
        String pGrobidHome = "../grobid-home";
        String pGrobidProperties = "../grobid-home/config/grobid.properties";
        try {
            GrobidProperties.getInstance();
            LibraryLoader.load();
            parserNER = new NEREnParser();
            parserSense = new SenseTagger();
        } catch (Exception e) {
            throw new GrobidException("Fail to initalise the grobid-ner component.", e);
        }

        Writer writerTrain = null;
        Writer writerTestA = null;
        Writer writerTestB = null;
        boolean testASet = false;
        try {
            // for the output
            writerTrain = new OutputStreamWriter(
                    new FileOutputStream(nerCorpusPath + "/reuters.ner26.train"), "UTF8");
            writerTestA = new OutputStreamWriter(
                    new FileOutputStream(nerCorpusPath + "/reuters.ner26.testa"), "UTF8");
            writerTestB = new OutputStreamWriter(
                    new FileOutputStream(nerCorpusPath + "/reuters.ner26.testb"), "UTF8");

            // prepare the CoNLL data
            // read CoNLL annotations
            int indexCoNLL = 0;
            File evalA = new File(conllPath + "/eng.testa");
            File evalB = new File(conllPath + "/eng.testb");
            File evalTrain = new File(conllPath + "/eng.train");
            List<File> conllFiles = new ArrayList<File>();
            try {
                if (!evalTrain.exists()) {
                    throw new GrobidException(
                            "Corpus resource path for CoNLL file " +
                                    " is not correctly set : " + conllPath + "/eng.train");
                }
                conllFiles.add(evalTrain);

                if (!evalA.exists()) {
                    throw new GrobidException(
                            "Corpus resource path for CoNLL file " +
                                    " is not correctly set : " + conllPath + "/eng.testa");
                }
                conllFiles.add(evalA);

                if (!evalB.exists()) {
                    throw new GrobidException(
                            "Corpus resource path for CoNLL file " +
                                    " is not correctly set : " + conllPath + "/eng.testb");
                }
                conllFiles.add(evalB);
            } catch (Exception e) {
                throw new GrobidException("An exception occured while running Grobid Reuters evaluation.", e);
            }

            BufferedReader bufReader = null;
            List<String> conllLabeled = new ArrayList<String>();
            String line = null;
            for (File conllFile : conllFiles) {
                File corpusDir = conllFile;
                try {
                    bufReader = new BufferedReader(
                            new InputStreamReader(new FileInputStream(corpusDir), "UTF-8"));

                    while ((line = bufReader.readLine()) != null) {
                        line = line.trim();
                        String[] pieces = line.split(" ");
                        if (pieces.length == 4) {
                            // we retokenize the lexical string according to Grobid NER
                            String token = pieces[0];
                            StringTokenizer st = new StringTokenizer(token, TextUtilities.fullPunctuations);

                            String conllLabel = pieces[3];
                            String label = "O";
                            if (conllLabel.equals("I-ORG")) {
                                label = "org";
                            } else if (conllLabel.equals("I-LOC")) {
                                label = "loc";
                            } else if (conllLabel.equals("I-PER")) {
                                label = "per";
                            } else if (conllLabel.equals("I-MISC")) {
                                label = "misc";
                            }
                            boolean start = true;
                            while (st.hasMoreTokens()) {
                                conllLabeled.add(st.nextToken() + "\t" + label);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new GrobidException(
                            "An exception occured while reading Grobid CoNLL resource the set "
                                    + corpusDir.getPath(), e);
                } finally {
                    try {
                        if (bufReader != null)
                            bufReader.close();
                    } catch (Exception e) {
                    }
                }
            }

            // read the list of files included in the CoNLL gold corpus
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new DataInputStream(new FileInputStream(reutersSelectionPath))));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                if (line.startsWith("#"))
                    continue;
                String fullReutersFileName = line;
                int ind = line.lastIndexOf("/");
                String reutersFileName = line.substring(ind + 1, line.length());
                // get the text of the file
                String reuPath = reutersPath.replace("/cd", "/RCV1_CD1") + "/" +
                        reutersFileName.substring(0, 3) + "/" + reutersFileName;
                File reutersFile = new File(reuPath);
                if (!reutersFile.exists()) {
                    LOGGER.error("Invalid file path: " + reutersPath);
                    continue;
                }

                // remove the tags with the conll script in the standard CoNNL NER 2003 distrib
                List<String> command = new ArrayList<String>();
                command.add(conllPath + "/bin/xml2txt.eng");
                command.add(reuPath);

                ProcessBuilder builder = new ProcessBuilder(command);
                final Process process = builder.start();
                process.waitFor();
                int exitVal = process.exitValue();

                String reutersPathTxt = reuPath.replace(".xml", ".txt");
                //System.out.println(reutersPathTxt);
                File reutersFileTxt = new File(reutersPathTxt);
                if (!reutersFileTxt.exists()) {
                    LOGGER.error("Invalid file path: " + reutersFileTxt);
                    continue;
                }
                String content = FileUtils.readFileToString(reutersFileTxt, "UTF-8");
                //System.out.println(content);

                try {
                    reutersFileTxt.delete();
                } catch (Exception e) {
                    LOGGER.error("Fail to delete text file: " + reutersFileTxt.getPath());
                    throw new GrobidResourceException(e);
                }

                String[] sentences = content.split("\n");
                List<List<Pair<String, String>>> allLabeled = new ArrayList<List<Pair<String, String>>>();
                List<List<Pair<String, String>>> allLabeledSense = new ArrayList<List<Pair<String, String>>>();
                for (int i = 0; i < sentences.length; i++) {
                    String text = sentences[i].trim();
                    if (text.length() == 0)
                        continue;
                    // ner model
                    // we process the file with the current model, which gives the grobid tokenisation
                    try {
                        //text = text.replace("\n", " ");
                        int sentence = 0;
                        List<OffsetPosition> localLocationPositions = lexicon.inLocationNames(text);
                        List<OffsetPosition> localPersonTitlePositions = lexicon.inPersonTitleNames(text);
                        List<OffsetPosition> localOrganisationPositions = lexicon.inOrganisationNames(text);
                        List<OffsetPosition> localOrgFormPositions = lexicon.inOrgFormNames(text);
                        int currentPosition = 0;
                        StringTokenizer st = new StringTokenizer(text, TextUtilities.fullPunctuations, true);

                        if (st.countTokens() == 0)
                            continue;

                        List<String> textBlocks = new ArrayList<String>();
                        List<String> tokenizations = new ArrayList<String>();
                        int pos = 0; // current offset
                        List<Integer> positions = new ArrayList<Integer>();
                        while (st.hasMoreTokens()) {
                            String tok = st.nextToken();
                            tokenizations.add(tok);
                            if (!tok.equals(" ")) {
                                textBlocks.add(tok + "\t<ner>");
                                positions.add(pos);
                            }
                            pos += tok.length();
                        }
                        StringBuffer ress = new StringBuffer();
                        StringBuffer ress2 = new StringBuffer();
                        int posit = 0; // keep track of the position index in the list of positions
                        int currentLocationIndex = 0; // keep track of the position index in the list of unit match offsets
                        int currentPersonTitleIndex = 0;
                        int currentOrganisationIndex = 0;
                        int currentOrgFormIndex = 0;
                        for (String block : textBlocks) {
                            currentPosition += positions.get(posit);

                            // check if the token is a known NE
                            // do we have a NE at position posit?
                            boolean isLocationToken = false;
                            boolean isPersonTitleToken = false;
                            boolean isOrganisationToken = false;
                            boolean isOrgFormToken = false;
                            if ((localLocationPositions != null) && (localLocationPositions.size() > 0)) {
                                for (int mm = currentLocationIndex; mm < localLocationPositions.size(); mm++) {
                                    if ((posit >= localLocationPositions.get(mm).start) &&
                                            (posit <= localLocationPositions.get(mm).end)) {
                                        isLocationToken = true;
                                        currentLocationIndex = mm;
                                        break;
                                    } else if (posit < localLocationPositions.get(mm).start) {
                                        isLocationToken = false;
                                        break;
                                    } else if (posit > localLocationPositions.get(mm).end) {
                                        continue;
                                    }
                                }
                            }
                            if ((localPersonTitlePositions != null) && (localPersonTitlePositions.size() > 0)) {
                                for (int mm = currentPersonTitleIndex; mm < localPersonTitlePositions.size(); mm++) {
                                    if ((posit >= localPersonTitlePositions.get(mm).start) &&
                                            (posit <= localPersonTitlePositions.get(mm).end)) {
                                        isPersonTitleToken = true;
                                        currentPersonTitleIndex = mm;
                                        break;
                                    } else if (posit < localPersonTitlePositions.get(mm).start) {
                                        isPersonTitleToken = false;
                                        break;
                                    } else if (posit > localPersonTitlePositions.get(mm).end) {
                                        continue;
                                    }
                                }
                            }
                            if ((localOrganisationPositions != null) && (localOrganisationPositions.size() > 0)) {
                                for (int mm = currentOrganisationIndex; mm < localOrganisationPositions.size(); mm++) {
                                    if ((posit >= localOrganisationPositions.get(mm).start) &&
                                            (posit <= localOrganisationPositions.get(mm).end)) {
                                        isOrganisationToken = true;
                                        currentOrganisationIndex = mm;
                                        break;
                                    } else if (posit < localOrganisationPositions.get(mm).start) {
                                        isOrganisationToken = false;
                                        break;
                                    } else if (posit > localOrganisationPositions.get(mm).end) {
                                        continue;
                                    }
                                }
                            }
                            if ((localOrgFormPositions != null) && (localOrgFormPositions.size() > 0)) {
                                for (int mm = currentOrgFormIndex; mm < localOrgFormPositions.size(); mm++) {
                                    if ((posit >= localOrgFormPositions.get(mm).start) &&
                                            (posit <= localOrgFormPositions.get(mm).end)) {
                                        isOrgFormToken = true;
                                        currentOrgFormIndex = mm;
                                        break;
                                    } else if (posit < localOrgFormPositions.get(mm).start) {
                                        isOrganisationToken = false;
                                        break;
                                    } else if (posit > localOrgFormPositions.get(mm).end) {
                                        continue;
                                    }
                                }
                            }
                            ress.append(FeaturesVectorNER
                                    .addFeaturesNER(block,
                                            isLocationToken, isPersonTitleToken, isOrganisationToken, isOrgFormToken)
                                    .printVector());
                            ress.append("\n");
                            ress2.append(FeaturesVectorNERSense
                                    .addFeatures(block,
                                            isLocationToken, isPersonTitleToken, isOrganisationToken, isOrgFormToken)
                                    .printVector());
                            ress2.append("\n");

                            posit++;
                        }
                        ress.append("\n");

                        String res = parserNER.label(ress.toString());
                        //LOGGER.info(res);
                        List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);
                        allLabeled.add(labeled);

                        res = parserSense.label(ress2.toString());
                        labeled = GenericTaggerUtils.getTokensAndLabels(res);
                        allLabeledSense.add(labeled);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // we indicate the idilia tag if no agreement
                File semdocFile =
                        new File(idiliaPath + "/reuters/" + reutersFileName.substring(0, 3) + "/" +
                                reutersFileName.replace(".xml", ".semdoc.xml"));
                System.out.println(idiliaPath + "/reuters/" + reutersFileName.substring(0, 3) + "/" +
                        reutersFileName.replace(".xml", ".semdoc.xml"));
                if (!semdocFile.exists()) {
                    throw new GrobidException("Cannot start training, because corpus resource folder for semdoc file " +
                            " is not correctly set : "
                            + idiliaPath + "/reuters/" + reutersFileName.substring(0, 3) + "/" + reutersFileName.replace(".xml", ".semdoc.xml"));
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
                p.parse(reutersFile, reutersSax);

                SemDocSaxHandler semdocSax = new SemDocSaxHandler(reutersSax.getTextVector());
                p = spf.newSAXParser();
                p.parse(semdocFile, semdocSax);

                // same for ner sense model
                SemDocSaxHandler semdocSax2 = new SemDocSaxHandler(reutersSax.getTextVector());
                semdocSax2.setModeWSD(true);
                //semdocSax2.setDescriptions(descriptions);

                p = spf.newSAXParser();
                p.parse(semdocFile, semdocSax2);

                int indexSemdoc = 0;
                int indexSemdocSense = 0;

                // buffer to write assembled training data
                StringBuffer buffer = new StringBuffer();
                // select the right output
                Writer writer = null;

                if ((fullReutersFileName.indexOf("19961206/") != -1) ||
                        (fullReutersFileName.indexOf("19961207/") != -1)) {
                    writer = writerTestB;
                    testASet = false;
                } else if ((fullReutersFileName.indexOf("19960830/23489newsML") != -1) || testASet) {
                    writer = writerTestA;
                    testASet = true;
                } else {
                    writer = writerTrain;
                }
                // file name
                buffer.append("-DOCSTART- -" + fullReutersFileName + "-" + "\n");

                int indexSense = 0;
                // we align semdoc annotations and current NER+sense tagger annotations
                for (List<Pair<String, String>> labeled : allLabeled) {
                    int indexTokenSense = 0;
                    List<Pair<String, String>> labeledSense = allLabeledSense.get(indexSense);
                    for (Pair<String, String> pair : labeled) {
                        // NER labels from the current NER model
                        String token = pair.getA();
                        String label = pair.getB();

                        // get the corresponding sense label (if we can)
                        Pair<String, String> pairSense = labeledSense.get(indexTokenSense);
                        String tokenSense = pairSense.getA();
                        String labelSense = pairSense.getB();

                        if (token.equals("<ner>"))
                            continue;
                        if (label.startsWith("B-") || label.startsWith("I-")) {
                            label = label.substring(2, label.length());
                        }
                        String lineLabel = label;
                        String conllLabel = "";
                        String lineLabelSense = labelSense;

                        // NER labels from the semdoc file
                        if (semdocSax.getAnnotatedTextVector() != null) {
                            for (int j = indexSemdoc; j < semdocSax.getAnnotatedTextVector().size(); j++) {
                                String linee = semdocSax.getAnnotatedTextVector().get(j);

                                if (linee.trim().length() == 0) {
                                    // new doc
                                    continue;
                                }
                                if (linee.trim().equals("@newline")) {
                                    // new sentence
                                    continue;
                                }
                                String[] pieces = linee.split("\t");
                                String lineToken = pieces[0];
                                String currentLabel = pieces[1];
                                if (currentLabel.startsWith("B-") || currentLabel.startsWith("I-")) {
                                    currentLabel = currentLabel.substring(2, currentLabel.length());
                                }

                                if (lineToken.equals(token)) {
                                    lineLabel = currentLabel;
                                    indexSemdoc = j;
                                    break;
                                }
                            }
                        }

                        // CoNLL gold labels
                        if ((conllLabeled != null) && (conllLabeled.size() != 0)) {
                            for (int j = indexCoNLL; j < conllLabeled.size(); j++) {
                                String linee = conllLabeled.get(j);
                                if (linee.trim().length() == 0) {
                                    continue;
                                }
                                if (linee.trim().equals("@newline")) {
                                    continue;
                                }
                                String[] pieces = linee.split("\t");
                                String lineToken = pieces[0];
                                String currentLabel = pieces[1];
                                if (currentLabel.startsWith("B-")) {
                                    currentLabel = currentLabel.substring(2, currentLabel.length());
                                }

                                if (lineToken.equals(token)) {
                                    conllLabel = currentLabel;
                                    indexCoNLL = j;
                                    break;
                                }
                            }
                        }

                        // sense labels from the semdoc file
                        if (semdocSax2.getAnnotatedTextVector() != null) {
                            for (int j = indexSemdocSense; j < semdocSax2.getAnnotatedTextVector().size(); j++) {
                                String linee = semdocSax2.getAnnotatedTextVector().get(j);

                                if (linee.trim().length() == 0) {
                                    // new doc
                                    continue;
                                }
                                if (linee.trim().equals("@newline")) {
                                    // new sentence
                                    continue;
                                }
                                String[] pieces = linee.split("\t");
                                String lineToken = pieces[0];
                                String currentLabel = pieces[1];
                                if (currentLabel.startsWith("B-") || currentLabel.startsWith("I-")) {
                                    currentLabel = currentLabel.substring(2, currentLabel.length());
                                }

                                if (lineToken.equals(token)) {
                                    lineLabelSense = currentLabel;
                                    indexSemdocSense = j;
                                    break;
                                }
                            }
                        }

                        if (!lineLabel.equals(label) ||
                                (!conllLabel.equals(NEREvaluation.translateLabel(label)) &&
                                        !conllLabel.equals(NEREvaluation.translateLabel(lineLabel)) && conllLabel.length() > 0)) {
                            buffer.append(token + "\t[" + label);
                            if (!lineLabel.equals(label)) {
                                buffer.append("," + lineLabel);
                            }
                            if (!conllLabel.equals(NEREvaluation.translateLabel(label)) &&
                                    !conllLabel.equals(NEREvaluation.translateLabel(lineLabel))) {
                                if (conllLabel.length() > 0)
                                    buffer.append("," + conllLabel);
                            }
                            buffer.append("]");
                        } else
                            buffer.append(token + "\t" + label);

                        if (!labelSense.equals(lineLabelSense)) {
                            buffer.append("\t[" + labelSense + "," + lineLabelSense + "]");
                        } else
                            buffer.append("\t" + labelSense);

                        buffer.append("\n");
                        indexTokenSense++;
                    }
                    buffer.append("\n");
                    indexTokenSense = 0;
                    indexSense++;
                }

                writer.write(buffer.toString());
                writer.write("\n");
                writer.flush();
            }
        } catch (Exception e) {
            throw new GrobidResourceException(e);
        } finally {
            try {
                writerTrain.close();
                writerTestA.close();
                writerTestB.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Launch the creation of training files based on a selection of Wikipedia articles.
     */
    public void assembleWikipedia(String outputDirectory) {
        String wikipediaSelectionPath = "resources/dataset/ner/corpus/wikipedia.txt";
        Writer writer = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new DataInputStream(new FileInputStream(wikipediaSelectionPath))));
            String line = null;
            while ((line = br.readLine()) != null) {
                line = trim(line);
                if (isBlank(line) || line.startsWith("#")) {
                    continue;
                }
                System.out.println("Processing " + line);

                String fullWikipediaPath = idiliaPath + "/wikipedia/" + line;
                File semdocFile = new File(fullWikipediaPath);
                if (!semdocFile.exists()) {
                    throw new GrobidException("Cannot start training, because corpus resource folder for semdoc file " +
                            " is not correctly set : " + semdocFile);
                }

                // for the output
                writer = new OutputStreamWriter(
                        new FileOutputStream(outputDirectory + File.separator + semdocFile.getName() + ".out"), "UTF8");

                // to get a text vector representation first
                TextSaxHandler textSax = new TextSaxHandler();

                // get a factory
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setValidating(false);
                spf.setFeature("http://xml.org/sax/features/namespaces", false);
                spf.setFeature("http://xml.org/sax/features/validation", false);
                spf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
                spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(semdocFile, textSax);
                System.out.println(textSax.getTextVector().toString());
                // NER labels
                SemDocSaxHandler semdocSax = new SemDocSaxHandler(textSax.getTextVector());
                semdocSax.setToRealign(false);
                p = spf.newSAXParser();
                p.parse(semdocFile, semdocSax);

                // same for ner sense labels
                SemDocSaxHandler semdocSax2 = new SemDocSaxHandler(textSax.getTextVector());
                semdocSax2.setModeWSD(true);
                semdocSax2.setToRealign(false);
                //semdocSax2.setDescriptions(descriptions);

                p = spf.newSAXParser();
                p.parse(semdocFile, semdocSax2);

                int indexSemdoc = 0;
                int indexSemdocSense = 0;

                String lineLabel = null;
                String lineLabelSense = null;
                String lineToken = null;

                // file name
                writer.write("-DOCSTART- -" + line + "-" + "\n");
                writer.flush();

                // NER labels
                while (indexSemdoc < semdocSax.getAnnotatedTextVector().size()) {

                    if (semdocSax.getAnnotatedTextVector() != null) {
                        for (; indexSemdoc < semdocSax.getAnnotatedTextVector().size(); indexSemdoc++) {
                            String line2 = semdocSax.getAnnotatedTextVector().get(indexSemdoc);

                            if (line2.trim().length() == 0) {
                                // new doc
                                continue;
                            }
                            if (line2.trim().equals("@newline")) {
                                // new sentence
                                writer.write("\n");
                                continue;
                            }
                            String[] pieces = line2.split("\t");
                            lineToken = pieces[0];
                            String currentLabel = pieces[1];
                            if (currentLabel.startsWith("B-") || currentLabel.startsWith("I-")) {
                                currentLabel = currentLabel.substring(2, currentLabel.length());
                            }

                            //if (lineToken.equals(token)) {
                            lineLabel = currentLabel;
                            indexSemdoc++;
                            break;
                            //}
                        }
                    }

                    // NER sense labels
                    if (semdocSax2.getAnnotatedTextVector() != null) {
                        for (; indexSemdocSense < semdocSax2.getAnnotatedTextVector().size(); indexSemdocSense++) {
                            String linee = semdocSax2.getAnnotatedTextVector().get(indexSemdocSense);

                            if (linee.trim().length() == 0) {
                                // new doc
                                continue;
                            }
                            if (linee.trim().equals("@newline")) {
                                // new sentence
                                continue;
                            }
                            String[] pieces = linee.split("\t");
                            String localLineToken = pieces[0];
                            String currentLabel = pieces[1];
                            if (currentLabel.startsWith("B-") || currentLabel.startsWith("I-")) {
                                currentLabel = currentLabel.substring(2, currentLabel.length());
                            }

                            //if (localLineToken.equals(lineToken)) {
                            lineLabelSense = currentLabel;
                            indexSemdocSense++;
                            break;
                            //}
                        }
                    }

                    writer.write(lineToken + "\t" + lineLabel + "\t" + lineLabelSense + "\n");
                    writer.flush();
                }
                writer.write("\n");
                writer.flush();
            }

        } catch (Exception e) {
            throw new GrobidResourceException(e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}