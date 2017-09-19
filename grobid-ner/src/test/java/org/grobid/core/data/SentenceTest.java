package org.grobid.core.data;

import org.grobid.core.analyzers.GrobidAnalyzer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 13/07/2017.
 */
public class SentenceTest {
    Sentence target;

    @Before
    public void setUp() throws Exception {
        target = new Sentence();
    }

    @Test
    public void testRelinkEntitiesWithTokenisedList_0entities() throws Exception {
        target.setRawValue("This is a sentence.");

        target.setTokenisedValue(GrobidAnalyzer.getInstance().tokenizeWithLayoutToken("This is a sentence."));

        final List<Integer> indexList = target.getEntityIndexList();
        assertThat(indexList, hasSize(target.getTokenisedValue().size()));

        assertThat(indexList.get(0), is(-1));
        assertThat(indexList.get(1), is(-1));
        assertThat(indexList.get(2), is(-1));
        assertThat(indexList.get(3), is(-1));
        assertThat(indexList.get(4), is(-1));
        assertThat(indexList.get(5), is(-1));
        assertThat(indexList.get(6), is(-1));
        assertThat(indexList.get(7), is(-1));

    }

    @Test
    public void testRelinkEntitiesWithTokenisedList_1entity() throws Exception {
        target.setRawValue("This is a sentence.");

        Entity entity1 = new Entity();
        entity1.setRawName("a sentence");
        entity1.setOffsetStart(8);
        entity1.setOffsetEnd(18);

        target.setEntities(Arrays.asList(entity1));

        target.setTokenisedValue(GrobidAnalyzer.getInstance().tokenizeWithLayoutToken("This is a sentence."));

        final List<Integer> indexList = target.getEntityIndexList();
        assertThat(indexList, hasSize(target.getTokenisedValue().size()));

        assertThat(indexList.get(0), is(-1));
        assertThat(indexList.get(4), is(0));
        assertThat(indexList.get(5), is(0));
        assertThat(indexList.get(6), is(0));

    }

    @Test
    public void testRelinkEntitiesWithTokenisedList_2entities() throws Exception {
        target.setRawValue("This is a sentence.");

        Entity entity1 = new Entity();
        entity1.setRawName("a sentence");
        entity1.setOffsetStart(8);
        entity1.setOffsetEnd(18);

        Entity entity2 = new Entity();
        entity2.setRawName("This ");
        entity2.setOffsetStart(0);
        entity2.setOffsetEnd(5);

        target.setEntities(Arrays.asList(entity2, entity1));

        target.setTokenisedValue(GrobidAnalyzer.getInstance().tokenizeWithLayoutToken("This is a sentence."));

        final List<Integer> indexList = target.getEntityIndexList();
        assertThat(indexList, hasSize(target.getTokenisedValue().size()));

        assertThat(indexList.get(0), is(0));
        assertThat(indexList.get(1), is(0));
        assertThat(indexList.get(2), is(-1));
        assertThat(indexList.get(3), is(-1));
        assertThat(indexList.get(4), is(1));
        assertThat(indexList.get(5), is(1));
        assertThat(indexList.get(6), is(1));
        assertThat(indexList.get(7), is(-1));

    }

}