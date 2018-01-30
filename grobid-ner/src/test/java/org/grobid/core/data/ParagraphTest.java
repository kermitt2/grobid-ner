package org.grobid.core.data;

import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.utilities.OffsetPosition;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class ParagraphTest {

    private Paragraph target;

    @Before
    public void setUP() throws Exception {
        target = new Paragraph();
    }

    @Test
    public void testGetEntities() throws Exception {
        final Sentence sentence1 = new Sentence();
        sentence1.addEntity(new Entity("bao"));
        sentence1.addEntity(new Entity("miao"));
        sentence1.addEntity(new Entity("ciao"));
        target.addSentence(sentence1);
        final Sentence sentence2 = new Sentence();
        sentence2.addEntity(new Entity("222__bao"));
        sentence2.addEntity(new Entity("222__miao"));
        sentence2.addEntity(new Entity("222__ciao"));
        target.addSentence(sentence2);

        assertThat(target.getEntities(), hasSize(6));
    }

    @Test
    public void testGetEntities_testSorting() throws Exception {
        final Sentence sentence1 = new Sentence();
        final Entity bao = new Entity("bao");
        bao.setOffsets(new OffsetPosition(16, 19));
        sentence1.addEntity(bao);
        final Entity miao = new Entity("miao");
        miao.setOffsets(new OffsetPosition(10, 14));
        sentence1.addEntity(miao);
        final Entity ciao = new Entity("ciao");
        ciao.setOffsets(new OffsetPosition(0, 4));
        sentence1.addEntity(ciao);

        target.addSentence(sentence1);

        final Map<Integer, List<Entity>> collect = Arrays.asList(bao, miao, ciao).stream()
                .collect(Collectors.groupingBy(Entity::getOffsetStart));

        assertThat(target.getEntities(), is(Arrays.asList(ciao, miao, bao)));
    }

    @Test
    public void sortingTest1() throws Exception {
        final Sentence sentence1 = new Sentence();
        final Entity bao = new Entity("bao");
        bao.setOffsets(new OffsetPosition(16, 19));
        sentence1.addEntity(bao);
        final Entity miao = new Entity("miao");
        miao.setOffsets(new OffsetPosition(10, 14));
        sentence1.addEntity(miao);
        final Entity ciao = new Entity("ciao");
        ciao.setOffsets(new OffsetPosition(0, 12));
        sentence1.addEntity(ciao);

        target.addSentence(sentence1);


        final List<Entity> sorted = target.getSentences().get(0).getEntities().stream()
                .sorted(Comparator
                        .comparingInt(Entity::getOffsetStart)
                        .thenComparing(Entity::getOffsetEnd))
                .collect(Collectors.toList());


        sorted.stream().forEach(System.out::println);

    }

    @Test
    public void testGetEntitiesFrequencies() throws Exception {
        final Sentence sentence1 = new Sentence();
        sentence1.addEntity(new Entity("bao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence1.addEntity(new Entity("miao", NERLexicon.NER_Type.LOCATION));
        sentence1.addEntity(new Entity("ciao", NERLexicon.NER_Type.PERSON_TYPE));
        target.addSentence(sentence1);
        final Sentence sentence2 = new Sentence();
        sentence2.addEntity(new Entity("222__bao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence2.addEntity(new Entity("222__miao", NERLexicon.NER_Type.EVENT));
        sentence2.addEntity(new Entity("222__ciao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence2.addEntity(new Entity("223__ciao", NERLexicon.NER_Type.EVENT));
        target.addSentence(sentence2);

        assertThat(target.getEntitiesFrequencies().keySet(), hasSize(3));
        assertThat(target.getEntitiesFrequencies().get(NERLexicon.NER_Type.LOCATION.getName()), is(1L));
        assertThat(target.getEntitiesFrequencies().get(NERLexicon.NER_Type.EVENT.getName()), is(2L));
        assertThat(target.getEntitiesFrequencies().get(NERLexicon.NER_Type.PERSON_TYPE.getName()), is(4L));
    }

}