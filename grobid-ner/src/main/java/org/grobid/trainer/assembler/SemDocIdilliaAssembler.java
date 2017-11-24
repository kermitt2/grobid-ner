package org.grobid.trainer.assembler;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.trainer.stax.IdilliaSemDocStaxHandler;
import org.grobid.trainer.stax.StaxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Class to assemble files written in SemDoc XML format used by IDILLIA for wikipedia annotations
 */
public class SemDocIdilliaAssembler {
    private static Logger LOGGER = LoggerFactory.getLogger(SemDocIdilliaAssembler.class);

    private String idiliaPath;
    private WstxInputFactory inputFactory = new WstxInputFactory();

    private String language;
    private double confidenceThreshold;

    public SemDocIdilliaAssembler() {
        // we read the module specific property file to get the paths to the resources
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = this.getClass().getResourceAsStream("/grobid-ner.properties");
            prop.load(input);
            idiliaPath = prop.getProperty("grobid.ner.wikipedia.idilia_path");

            if (!new File(idiliaPath).exists()) {
                throw new FileNotFoundException("Cannot read from the idillia path specified in option grobid.ner.wikipedia.idilia_path the grobid-ner.properties");
            }
        } catch (IOException ex) {
            throw new GrobidException("Cannot initialise the semdoc idillia assembler", ex);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public void assemble(String outputDirectory) {
        String wikipediaSelectionPath = "resources/wikipedia.txt";

        LOGGER.info("Language: " + language);
        LOGGER.info("Confidence Threshold: " + confidenceThreshold);

        // IDILLIA WIKIPEDIA LIST
        LOGGER.info("Read wikipedia resources: ");
        List<String> relativePaths = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(wikipediaSelectionPath))) {
            //br returns as stream and convert it into a List
            relativePaths = br.lines().filter(l -> !l.startsWith("#")).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Transform IDILLIA WIKIPEDIA
        relativePaths.parallelStream().forEach(relativeInputPath -> {
            String absoluteInputPath = idiliaPath + File.separator + relativeInputPath;
            String absoluteOutputPath = outputDirectory.toString() + File.separator + File.separator
                    + relativeInputPath.replace("xml", "training.xml");

            final Path path = Paths.get(absoluteInputPath);
            IdilliaSemDocStaxHandler parser = new IdilliaSemDocStaxHandler(absoluteInputPath, language, confidenceThreshold);
            try {
                XMLStreamReader2 reader = inputFactory.createXMLStreamReader(path.toFile());
                StaxUtils.traverse(reader, parser);
            } catch (XMLStreamException e) {
                LOGGER.error("Cannot open file " + absoluteInputPath + ". Ignoring it. ", e);
            }

            String xmlOutput = parser.getConvertedText();
            try {
                FileUtils.writeStringToFile(new File(absoluteOutputPath), xmlOutput, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error("Cannot write file " + absoluteOutputPath, e);
            }
        });
    }

    public String getName() {
        return "SemDoc Idillia Assembler";
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
