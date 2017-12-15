package org.grobid.core.data;

import org.grobid.core.lexicon.NERLexicon;
import org.junit.Before;
import org.junit.Test;

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