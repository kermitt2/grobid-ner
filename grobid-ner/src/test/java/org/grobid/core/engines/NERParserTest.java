package org.grobid.core.engines;

import org.apache.commons.io.IOUtils;
import org.grobid.core.EngineMockTest;
import org.grobid.core.data.Entity;
import org.grobid.core.exceptions.GrobidException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * @author Patrice Lopez
 */
public class NERParserTest extends EngineMockTest {

    NERParsers target;

    @Before
    public void setUp() throws Exception {
        target = new NERParsers();
    }

    public File getResourceDir(String resourceDir) {
        File file = new File(resourceDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
            }
        }
        return (file);
    }

    @Test
    public void testExtractNE_en() throws Exception {
        String text = IOUtils.toString(this.getClass().getResourceAsStream("/test.en.txt"));
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
        String test = englishNER.createTrainingFromText(text);

        System.out.println("\n" + test);
    }

}