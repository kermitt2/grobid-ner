package org.grobid.core;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Arrays;

public abstract class EngineMockTest {
    protected static Engine engine;

    @AfterClass
    public static void destroyInitialContext() throws Exception {
    }

    @BeforeClass
    public static void initInitialContext() throws Exception {
        final GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList("../../grobid-home", "../grobid-home"));
        grobidHomeFinder.findGrobidHomeOrFail();

        GrobidProperties.getInstance(grobidHomeFinder);
        engine = GrobidFactory.getInstance().createEngine();
    }
}