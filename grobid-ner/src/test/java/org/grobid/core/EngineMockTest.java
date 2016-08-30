package org.grobid.core;

import org.grobid.core.engines.Engine;
import org.grobid.core.exceptions.GrobidPropertyException;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class EngineMockTest {
    private static Logger LOGGER = LoggerFactory.getLogger(EngineMockTest.class);
    protected static Engine engine;

    @AfterClass
    public static void destroyInitialContext() throws Exception {
        MockContext.destroyInitialContext();
    }

    @BeforeClass
    public static void initInitialContext() throws Exception {
        findGrobidHome();

        GrobidProperties.getInstance();
        MockContext.setInitialContext();
        engine = GrobidFactory.getInstance().createEngine();
    }

    /**
     * Try to get the GROBID_HOME from the environment variable or by using some default locations:
     *  - ../grobid-home
     *  - ../../grobid-home (in case the whole repository is cloned directly under the grobid project)
     */
    public static void findGrobidHome() {
        String grobidHome = System.getenv("GROBID_HOME");
        if (!isEmpty(grobidHome)) {
            GrobidProperties.set_GROBID_HOME_PATH(grobidHome);
            GrobidProperties.setGrobidPropertiesPath(grobidHome + "/config/grobid.properties");
        } else {
            try {
                LOGGER.trace("Trying grobid home from the usual location at ../grobid-home ");
                GrobidProperties.set_GROBID_HOME_PATH("../grobid-home");
                GrobidProperties.setGrobidPropertiesPath("../grobid-home/config/grobid.properties");
            } catch (GrobidPropertyException gpe) {
                LOGGER.error("Grobid HOME not found, trying to fish it from ../../grobid-home ");
                try {
                    GrobidProperties.set_GROBID_HOME_PATH("../../grobid-home");
                    GrobidProperties.setGrobidPropertiesPath("../../grobid-home/config/grobid.properties");
                } catch (GrobidPropertyException gpe2) {
                    LOGGER.error("Grobid HOME at ../../grobid-home not found, set the environment variable GROBID_HOME");
                }
            }
        }
    }
}