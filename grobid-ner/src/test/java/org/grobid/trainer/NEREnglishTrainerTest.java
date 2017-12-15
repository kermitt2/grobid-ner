package org.grobid.trainer;

import org.grobid.core.EngineMockTest;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Paragraph;
import org.grobid.core.data.Sentence;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.utilities.OffsetPosition;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 17/07/2017.
 */
public class NEREnglishTrainerTest extends EngineMockTest {

    NEREnglishTrainer target;

    @Before
    public void setUp() {
        target = new NEREnglishTrainer();

    }

    @Test
    public void testOffsetToIndex_shouldWork() throws Exception {
        Sentence sentence = new Sentence();
        final String rawSentence = "This is a sentence. A nice one.";
        sentence.setRawValue(rawSentence);
        sentence.setTokenisedValue(GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(rawSentence));

        List<OffsetPosition> positions = new ArrayList<>();
        positions.add(new OffsetPosition(4, 6));
        positions.add(new OffsetPosition(4, 4));
        positions.add(new OffsetPosition(11, 12));

        List<Integer> indexes = target.offsetToIndex(sentence.getTokenisedValue(), positions);

        assertThat(indexes, hasSize(5));
        assertThat(indexes, hasItems(4, 5, 6, 11, 12));
    }

    @Test
    public void testOffsetToIndex_emptyOffsets_shouldReturnEmptyList() throws Exception {
        Sentence sentence = new Sentence();
        final String rawSentence = "This is a sentence. A nice one.";
        sentence.setRawValue(rawSentence);
        sentence.setTokenisedValue(GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(rawSentence));

        List<OffsetPosition> positions = new ArrayList<>();

        List<Integer> indexes = target.offsetToIndex(sentence.getTokenisedValue(), positions);

        assertThat(indexes, hasSize(0));
    }

    @Test
    public void testOffsetToIndex_emptySentence_shouldReturnEmptyList() throws Exception {
        Sentence sentence = new Sentence();
        final String rawSentence = "";
        sentence.setRawValue(rawSentence);
        sentence.setTokenisedValue(GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(rawSentence));

        List<OffsetPosition> positions = new ArrayList<>();
        positions.add(new OffsetPosition(4, 6));
        positions.add(new OffsetPosition(4, 4));
        positions.add(new OffsetPosition(11, 12));

        List<Integer> indexes = target.offsetToIndex(sentence.getTokenisedValue(), positions);

        assertThat(indexes, hasSize(0));
    }

    @Test
    public void testBalancingExamples_lowClassPresent_shouldReturnTrue() throws Exception {
        Paragraph paragraph = new Paragraph();
        final Sentence sentence1 = new Sentence();
        sentence1.addEntity(new Entity("bao", NERLexicon.NER_Type.AWARD));
        sentence1.addEntity(new Entity("miao", NERLexicon.NER_Type.AWARD));
        sentence1.addEntity(new Entity("ciao", NERLexicon.NER_Type.PERSON_TYPE));
        paragraph.addSentence(sentence1);
        final Sentence sentence2 = new Sentence();
        sentence2.addEntity(new Entity("222__bao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence2.addEntity(new Entity("222__miao", NERLexicon.NER_Type.EVENT));
        sentence2.addEntity(new Entity("222__ciao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence2.addEntity(new Entity("223__ciao", NERLexicon.NER_Type.EVENT));
        paragraph.addSentence(sentence2);

        final Map<String, Long> entitiesFrequencies = paragraph.getEntitiesFrequencies();
        assertThat(target.balanceExamples(entitiesFrequencies), is(true));
    }

    @Test
    public void testBalancingExamples_noLowFrequencyClassPresent_noThreshold_shouldReturnTrue() throws Exception {
        Paragraph paragraph = new Paragraph();
        final Sentence sentence1_P2 = new Sentence();
        sentence1_P2.addEntity(new Entity("bao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence1_P2.addEntity(new Entity("miao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence1_P2.addEntity(new Entity("ciao", NERLexicon.NER_Type.PERSON_TYPE));
        paragraph.addSentence(sentence1_P2);
        final Sentence sentence2_P2 = new Sentence();
        sentence2_P2.addEntity(new Entity("222__bao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence2_P2.addEntity(new Entity("222__miao", NERLexicon.NER_Type.EVENT));
        sentence2_P2.addEntity(new Entity("222__ciao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence2_P2.addEntity(new Entity("223__ciao", NERLexicon.NER_Type.EVENT));
        paragraph.addSentence(sentence2_P2);

        final Map<String, Long> entitiesFrequencies = paragraph.getEntitiesFrequencies();
        assertThat(target.balanceExamples(entitiesFrequencies), is(true));
    }

    @Test
    public void testBalancingExamples_noLowFrequencyClassPresent_YesThreshold_shouldReturnFalse() throws Exception {
        target.setMaxFrequencyNEClass(2);
        Paragraph paragraph = new Paragraph();
        final Sentence sentence1_P2 = new Sentence();
        sentence1_P2.addEntity(new Entity("bao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence1_P2.addEntity(new Entity("miao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence1_P2.addEntity(new Entity("ciao", NERLexicon.NER_Type.PERSON_TYPE));
        paragraph.addSentence(sentence1_P2);
        final Sentence sentence2_P2 = new Sentence();
        sentence2_P2.addEntity(new Entity("222__bao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence2_P2.addEntity(new Entity("222__miao", NERLexicon.NER_Type.EVENT));
        sentence2_P2.addEntity(new Entity("222__ciao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence2_P2.addEntity(new Entity("223__ciao", NERLexicon.NER_Type.EVENT));
        paragraph.addSentence(sentence2_P2);

        final Map<String, Long> entitiesFrequencies = paragraph.getEntitiesFrequencies();
        assertThat(target.balanceExamples(entitiesFrequencies), is(false));
    }

    @Test
    public void testBalancingExamples_lowClassPresent_AndThreshold_shouldReturnTrue() throws Exception {
        target.setMaxFrequencyNEClass(2);

        Paragraph paragraph = new Paragraph();
        final Sentence sentence1 = new Sentence();
        sentence1.addEntity(new Entity("bao", NERLexicon.NER_Type.AWARD));
        sentence1.addEntity(new Entity("miao", NERLexicon.NER_Type.AWARD));
        sentence1.addEntity(new Entity("ciao", NERLexicon.NER_Type.PERSON_TYPE));
        paragraph.addSentence(sentence1);
        final Sentence sentence2 = new Sentence();
        sentence2.addEntity(new Entity("222__bao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence2.addEntity(new Entity("222__miao", NERLexicon.NER_Type.EVENT));
        sentence2.addEntity(new Entity("222__ciao", NERLexicon.NER_Type.PERSON_TYPE));
        sentence2.addEntity(new Entity("223__ciao", NERLexicon.NER_Type.EVENT));
        paragraph.addSentence(sentence2);

        final Map<String, Long> entitiesFrequencies = paragraph.getEntitiesFrequencies();
        assertThat(target.balanceExamples(entitiesFrequencies), is(true));
    }

}