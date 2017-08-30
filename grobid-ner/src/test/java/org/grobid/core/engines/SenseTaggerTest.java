package org.grobid.core.engines;

import org.apache.commons.io.IOUtils;
import org.grobid.core.EngineMockTest;
import org.grobid.core.data.Sense;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Patrice Lopez
 */
public class SenseTaggerTest extends EngineMockTest {

    @Test
    public void testSenseTagger() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/test.en.txt");

        String text = IOUtils.toString(is, UTF_8);

        SenseTagger tagger = new SenseTagger();

        List<Sense> senses = tagger.extractSenses(text);
        assertNotNull(senses);
        assertThat(senses, hasSize(8));
        final Sense sense0 = senses.get(0);
        assertThat(text.substring(sense0.getOffsetStart(), sense0.getOffsetEnd()), is("James Capel"));
        assertThat(sense0.getFineSense(), is("contestant/N1"));

        final Sense sense1 = senses.get(1);
        assertThat(text.substring(sense1.getOffsetStart(), sense1.getOffsetEnd()), is("Mexico"));
        assertThat(sense1.getFineSense(), is("country/N1"));

        final Sense sense6 = senses.get(6);
        assertThat(text.substring(sense6.getOffsetStart(), sense6.getOffsetEnd()), is("Russian"));
        assertThat(sense6.getFineSense(), is("jurisdictional_cultural_adjective/J1"));
    }

}