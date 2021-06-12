package org.grobid.core;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidNerConfiguration;
import org.grobid.core.utilities.GrobidConfig.ModelParameters;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.main.LibraryLoader;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public abstract class EngineMockTest {
    protected static Engine engine;

    @AfterClass
    public static void destroyInitialContext() throws Exception {
    }

    @BeforeClass
    public static void initInitialContext() throws Exception {
        GrobidNerConfiguration grobidNerConfiguration = null;
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            grobidNerConfiguration = mapper.readValue(new File("resources/config/grobid-ner.yaml"), GrobidNerConfiguration.class);
        } catch (IOException ex) {
            throw new GrobidResourceException(
                    "An exception occured when accessing/reading the grobid-ner property file.", ex);
        } 

        final GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(
            Arrays.asList(grobidNerConfiguration.getGrobidHome(), "../../grobid-home", "../grobid-home"));
        grobidHomeFinder.findGrobidHomeOrFail();

        GrobidProperties.getInstance(grobidHomeFinder);

        for (ModelParameters theModel : grobidNerConfiguration.getModels())
            GrobidProperties.getInstance().addModel(theModel);

        LibraryLoader.load();

        engine = GrobidFactory.getInstance().createEngine();
    }
}