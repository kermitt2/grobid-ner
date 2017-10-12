package org.grobid.trainer;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.engines.NEREnParser;
import org.grobid.core.engines.SenseTagger;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeaturesVectorNER;
import org.grobid.core.features.FeaturesVectorNERSense;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.trainer.sax.ReutersSaxHandler;
import org.grobid.trainer.sax.SemDocSaxHandler;
import org.grobid.trainer.sax.TextSaxHandler;
import org.grobid.trainer.stax.IdilliaSemDocStaxHandler;
import org.grobid.trainer.stax.StaxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Assemble the different corpus information spread in different source files into CoNNL files and TEI files.
 *
 * @author Patrice Lopez
 */
public class CorporaAssembler {

    private static Logger LOGGER = LoggerFactory.getLogger(CorporaAssembler.class);
    private final Path outputDirectory;

    private AbstractTrainer trainer;

    private String reutersPath = null;
    private String conllPath = null;
    private String idiliaPath = null;
    private String nerCorpusPath = null;
    private Lexicon lexicon = null;

    WstxInputFactory inputFactory = new WstxInputFactory();

    public CorporaAssembler(GrobidHomeFinder grobidHomeFinder, NEREnglishTrainer trainer, Path outputDirectory) {
        File grobidHome = grobidHomeFinder.findGrobidHomeOrFail();
        grobidHomeFinder.findGrobidPropertiesOrFail(grobidHome);

        GrobidProperties.getInstance(grobidHomeFinder);
        this.outputDirectory = outputDirectory;

        this.trainer = trainer;
        trainer.setNerCorpusPath(outputDirectory.toAbsolutePath().toString());
        // we read the module specific property file to get the paths to the resources
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = this.getClass().getResourceAsStream("/grobid-ner.properties");

            // load the properties file
            prop.load(input);

            // get the property value
//            reutersPath = prop.getProperty("grobid.ner.reuters.paths");
//            conllPath = prop.getProperty("grobid.ner.reuters.conll_path");
            idiliaPath = prop.getProperty("grobid.ner.wikipedia.idilia_path");
            nerCorpusPath = prop.getProperty("grobid.ner.corpus.path");

            lexicon = Lexicon.getInstance();
        } catch (IOException ex) {

        } finally {
            IOUtils.closeQuietly(input);
        }

    }

    /**
     * Launch the creation of training files based on a selection of Wikipedia articles.
     */
    public void assembleAndTrain() {
        String wikipediaSelectionPath = "resources/wikipedia.txt";

        try {
            // Generate an temp directory where to store all the training data
            Files.createDirectory(this.outputDirectory);

            // IDILLIA WIKIPEDIA LIST
            LOGGER.info("Copying idillia wikipedia resources. ");
            List<String> list = new ArrayList<>();

            try (BufferedReader br = Files.newBufferedReader(Paths.get(wikipediaSelectionPath))) {

                //br returns as stream and convert it into a List
                list = br.lines().filter(l -> !l.startsWith("#")).collect(Collectors.toList());

            } catch (IOException e) {
                e.printStackTrace();
            }

            //Transform IDILLIA WIKIPEDIA
            List<String> absPathIdillia = list.stream().map(path -> idiliaPath + File.separator + path).collect(Collectors.toList());

            absPathIdillia.stream().forEach(stringPath -> {

                final Path path = Paths.get(stringPath);
                IdilliaSemDocStaxHandler parser = new IdilliaSemDocStaxHandler(stringPath);
                try {
                    XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(path.toFile());
                    StaxUtils.traverse(reader, parser);
                } catch (XMLStreamException e) {
                    LOGGER.error("Cannot open file " + stringPath, e);
                }

                String xmlOutput = parser.getConvertedText();
                final String outputAbsPath = outputDirectory.toString() + File.separator + path.getFileName().toString().replace("xml", "training.xml");
                try {
                    FileUtils.writeStringToFile(new File(outputAbsPath), xmlOutput, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    LOGGER.error("Cannot write file " + outputAbsPath, e);
                }
            });

            //Copy local resources
            LOGGER.info("Copying local resources. ");
            Collection<File> trainingFiles = FileUtils.listFiles(new File(nerCorpusPath),
                    new SuffixFileFilter("training.xml"), null);

            for (File trainingFile : trainingFiles) {
                String filename = Paths.get(trainingFile.getAbsolutePath()).getFileName().toString();
                final String outputAbsPath = outputDirectory.toString() + File.separator + filename;

                FileUtils.writeStringToFile(new File(outputAbsPath), FileUtils.readFileToString(trainingFile, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            }

            //Running training
            AbstractTrainer.runSplitTrainingEvaluation(trainer, 0.8);

        } catch (IOException e) {
            LOGGER.error("Something wrong when fiddling with files and directories here.", e);
        } finally {
            //Recursively delete dirctory and contained file
            try {
                FileUtils.deleteDirectory(outputDirectory.toFile());
            } catch (IOException e) {

            }
        }
    }


    public static void main(String... args) {

        GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList("../../grobid-home"));
        grobidHomeFinder.findGrobidHomeOrFail();
        GrobidProperties.getInstance(grobidHomeFinder);

        final NEREnglishTrainer trainer = new NEREnglishTrainer();
        Path tmpDirectory = Paths.get(GrobidProperties.getTempPath().getAbsolutePath() + File.separator + "NER");

        CorporaAssembler assembler = new CorporaAssembler(grobidHomeFinder, trainer, tmpDirectory);

        assembler.assembleAndTrain();
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }
}