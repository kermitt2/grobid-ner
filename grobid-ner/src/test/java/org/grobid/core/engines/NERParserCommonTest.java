package org.grobid.core.engines;

import edu.emory.mathcs.nlp.component.tokenizer.EnglishTokenizer;
import edu.emory.mathcs.nlp.component.tokenizer.token.Token;
import org.grobid.core.data.Sentence;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;

public class NERParserCommonTest {

    NERParserCommon target;

    @Before
    public void setUp() {
        target = new NERParserCommon();
    }

    @Test
    public void testSentenceSegmentation_singleSentence() throws Exception {

        final String input = "This is a single sentence.";
        List<Sentence> sentences = target.sentenceSegmentation(input, new EnglishTokenizer());

        assertThat(sentences, hasSize(1));
        assertThat(sentences.get(0).getOffsetStart(), is(0));
        assertThat(sentences.get(0).getOffsetEnd(), is(26));
    }

    @Test
    public void testSentenceSegmentation_doubleSentence() throws Exception {

        final String input = "This is a single sentence. This is a second sentence...";
        List<Sentence> sentences = target.sentenceSegmentation(input, new EnglishTokenizer());

        assertThat(sentences, hasSize(2));
        assertThat(sentences.get(0).getOffsetStart(), is(0));
        assertThat(sentences.get(0).getOffsetEnd(), is(26));
        assertThat(sentences.get(1).getOffsetStart(), is(27));
        assertThat(sentences.get(1).getOffsetEnd(), is(55));
    }

}