package org.grobid.core.data;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;

public class TrainingDocumentTest {
    TrainingDocument target;

    @Before
    public void setUp() throws Exception {
        target = new TrainingDocument();
    }

    @Test
    public void testGetSentences() throws Exception {

        final Paragraph paragraph0 = new Paragraph();
        target.addParagraph(paragraph0);
        paragraph0.addSentence(new Sentence());
        paragraph0.addSentence(new Sentence());
        paragraph0.addSentence(new Sentence());
        paragraph0.addSentence(new Sentence());


        final Paragraph paragraph1 = new Paragraph();
        target.addParagraph(paragraph1);

        paragraph1.addSentence(new Sentence());
        paragraph1.addSentence(new Sentence());
        paragraph1.addSentence(new Sentence());
        paragraph1.addSentence(new Sentence());
        paragraph1.addSentence(new Sentence());
        paragraph1.addSentence(new Sentence());
        paragraph1.addSentence(new Sentence());

        assertThat(target.getSentences(), hasSize(11));

    }

}