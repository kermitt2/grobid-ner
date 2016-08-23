package org.grobid.core;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class EngineMockTest {
    protected static Engine engine;

    @AfterClass
    public static void destroyInitialContext() throws Exception {
        MockContext.destroyInitialContext();
    }

    @BeforeClass
    public static void initInitialContext() throws Exception {
        try {
            MockContext.setInitialContext("../../grobid-home");
        } catch (Exception e) {
        }

        GrobidProperties.set_GROBID_HOME_PATH("../../grobid-home");
        GrobidProperties.setGrobidPropertiesPath("../../grobid-home/config/grobid.properties");
        GrobidProperties.getInstance();
        engine = GrobidFactory.getInstance().createEngine();
    }
}