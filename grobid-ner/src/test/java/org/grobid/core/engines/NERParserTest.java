package org.grobid.core.engines;

import org.apache.commons.io.IOUtils;
import org.grobid.core.EngineMockTest;
import org.grobid.core.data.Entity;
import org.grobid.core.lexicon.NERLexicon;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
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

        assertThat(entities, hasSize(11));

        final Entity entity0 = entities.get(0);
        assertThat(entity0.getRawName(), is(text.substring(entity0.getOffsetStart(), entity0.getOffsetEnd())));
        assertThat(entity0.getType(), is(NERLexicon.NER_Type.PERSON));

        final Entity entity1 = entities.get(2);
        assertThat(entity1.getRawName(), is(text.substring(entity1.getOffsetStart(), entity1.getOffsetEnd())));
        assertThat(entity1.getType(), is(NERLexicon.NER_Type.LOCATION));

        final Entity entity2 = entities.get(3);
        assertThat(entity2.getRawName(), is(text.substring(entity2.getOffsetStart(), entity2.getOffsetEnd())));
        assertThat(entity2.getType(), is(NERLexicon.NER_Type.LOCATION));
    }

    @Test
    public void testExtractNE_fr() throws Exception {
        String text = IOUtils.toString(this.getClass().getResourceAsStream("/test.fr.txt"), UTF_8);
        List<Entity> entities = target.extractNE(text);

        assertThat(entities, hasSize(3));

        //checking the identify entities are correctly found 
        final Entity entity0 = entities.get(0);
        assertThat(entity0.getRawName(), is(text.substring(entity0.getOffsetStart(), entity0.getOffsetEnd())));
        assertThat(entity0.getType(), is(NERLexicon.NER_Type.LOCATION));

        final Entity entity1 = entities.get(1);
        assertThat(entity1.getRawName(), is(text.substring(entity1.getOffsetStart(), entity1.getOffsetEnd())));
        assertThat(entity1.getType(), is(NERLexicon.NER_Type.BUSINESS));

        final Entity entity2 = entities.get(2);
        assertThat(entity2.getRawName(), is(text.substring(entity2.getOffsetStart(), entity2.getOffsetEnd())));
        assertThat(entity2.getType(), is(NERLexicon.NER_Type.ORGANISATION));
    }

    public static void printTextAndEntities(String text, List<Entity> entities) {
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

}