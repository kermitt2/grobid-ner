package org.grobid.core.utilities;

import org.grobid.core.exceptions.GrobidPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by lfoppiano on 31/08/16.
 */
public class GrobidHome {

    private static Logger LOGGER = LoggerFactory.getLogger(GrobidHome.class);

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
