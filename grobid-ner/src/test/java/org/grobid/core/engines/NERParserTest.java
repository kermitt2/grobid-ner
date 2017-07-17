package org.grobid.core.engines;

import org.apache.commons.io.IOUtils;
import org.grobid.core.EngineMockTest;
import org.grobid.core.data.Entity;
import org.grobid.core.exceptions.GrobidException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author Patrice Lopez
 */
@Ignore
public class NERParserTest extends EngineMockTest {

    NERParsers target;

    @Before
    public void setUp() throws Exception {
        target = new NERParsers();
    }

    @Test
    public void testExtractNE_en() throws Exception {
        String text = IOUtils.toString(this.getClass().getResourceAsStream("/test.en.txt"), UTF_8);
        List<Entity> entities = target.extractNE(text);

        assertThat(entities, hasSize(10));
    }

    @Test
    public void testExtractNE_fr() throws Exception {
        String text = IOUtils.toString(this.getClass().getResourceAsStream("/test.fr.txt"));
        List<Entity> entities = target.extractNE(text);

        System.out.println("\n" + text);
        if (entities != null) {
            for (Entity entity : entities) {
                System.out.print(text.substring(entity.getOffsetStart(), entity.getOffsetEnd()) + "\t");
                System.out.println(entity.toString());
            }
        } else {
            System.out.println("No entity found.");
        }
        System.out.println("\n");
    }

    @Test
    public void testCreateTrainingTest_simpleParagraph() throws Exception {
        String text = IOUtils.toString(this.getClass().getResourceAsStream("/test.en.txt"));
        NERParser englishNER =  target.getParser("en");
        String test = englishNER.createCONNLTrainingFromText(text);

        System.out.println("\n" + test);
    }

}