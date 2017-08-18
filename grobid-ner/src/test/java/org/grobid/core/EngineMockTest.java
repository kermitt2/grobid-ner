package org.grobid.core;

import org.grobid.core.engines.Engine;
import org.grobid.core.exceptions.GrobidPropertyException;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidHome;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class EngineMockTest {
    protected static Engine engine;

    @AfterClass
    public static void destroyInitialContext() throws Exception {
        MockContext.destroyInitialContext();
    }

    @BeforeClass
    public static void initInitialContext() throws Exception {
        GrobidHome.findGrobidHome();

        GrobidProperties.getInstance();
        MockContext.setInitialContext();
        engine = GrobidFactory.getInstance().createEngine();
    }
}