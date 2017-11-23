package org.grobid.trainer.assembler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;

/**
 * Class to assemble files written in ENAMEX XML, since this format is the desired output format
 * the files are just written in the output directory without further processing.
 */
public class ENAMEXAssembler implements TrainingDataAssembler {
    private static Logger LOGGER = LoggerFactory.getLogger(ENAMEXAssembler.class);

    private String nerCorpusPath;

    public ENAMEXAssembler() {
        // we read the module specific property file to get the paths to the resources
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = this.getClass().getResourceAsStream("/grobid-ner.properties");
            prop.load(input);
            nerCorpusPath = prop.getProperty("grobid.ner.corpus.path");
        } catch (IOException ex) {

        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public void assemble(final String outputDirectory) {
        //Copy local resources
        LOGGER.info("Copying local resources. ");
        Collection<File> trainingFiles = FileUtils.listFiles(new File(nerCorpusPath),
                new SuffixFileFilter("training.xml"), null);

        trainingFiles.stream().forEach(trainingFile -> {
            String filename = Paths.get(trainingFile.getAbsolutePath()).getFileName().toString();
            final String outputAbsPath = outputDirectory.toString() + File.separator + filename;

            try {
                FileUtils.writeStringToFile(new File(outputAbsPath), FileUtils.readFileToString(trainingFile, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.warn("Cannot write file " + outputAbsPath, e);
            }
        });

    }

    @Override
    public String getName() {
        return "ENAMEXAssembler";
    }
}
