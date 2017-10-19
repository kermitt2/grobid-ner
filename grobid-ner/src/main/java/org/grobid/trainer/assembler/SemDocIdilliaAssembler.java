package org.grobid.trainer.assembler;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.trainer.stax.IdilliaSemDocStaxHandler;
import org.grobid.trainer.stax.StaxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
public class SemDocIdilliaAssembler implements TrainingDataAssembler {
    private static Logger LOGGER = LoggerFactory.getLogger(SemDocIdilliaAssembler.class);

    private String idiliaPath;
    private WstxInputFactory inputFactory = new WstxInputFactory();

    public SemDocIdilliaAssembler() {
        // we read the module specific property file to get the paths to the resources
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = this.getClass().getResourceAsStream("/grobid-ner.properties");
            prop.load(input);
            idiliaPath = prop.getProperty("grobid.ner.wikipedia.idilia_path");
        } catch (IOException ex) {

        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public void assemble(final String outputDirectory) {
        String wikipediaSelectionPath = "resources/wikipedia.txt";

        // IDILLIA WIKIPEDIA LIST
        LOGGER.info("Read wikipedia resources: ");
        List<String> list = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(wikipediaSelectionPath))) {

            //br returns as stream and convert it into a List
            list = br.lines().filter(l -> !l.startsWith("#")).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Transform IDILLIA WIKIPEDIA
        final List<String> absPathIdillia = list.stream().map(path -> idiliaPath + File.separator + path).collect(Collectors.toList());

        absPathIdillia.stream().forEach(stringPath -> {

            final Path path = Paths.get(stringPath);
            IdilliaSemDocStaxHandler parser = new IdilliaSemDocStaxHandler(stringPath);
            try {
                XMLStreamReader2 reader = inputFactory.createXMLStreamReader(path.toFile());
                StaxUtils.traverse(reader, parser);
            } catch (XMLStreamException e) {
                LOGGER.error("Cannot open file " + stringPath + ". Ignoring it. ", e);
            }

            String xmlOutput = parser.getConvertedText();
            final String outputAbsPath = outputDirectory.toString() + File.separator + path.getFileName().toString().replace("xml", "training.xml");
            try {
                FileUtils.writeStringToFile(new File(outputAbsPath), xmlOutput, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error("Cannot write file " + outputAbsPath, e);
            }
        });
    }

    @Override
    public String getName() {
        return "SemDoc Idillia Assembler";
    }
}
