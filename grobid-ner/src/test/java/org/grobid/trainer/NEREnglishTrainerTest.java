package org.grobid.trainer;

import org.grobid.core.EngineMockTest;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Sentence;
import org.grobid.core.utilities.OffsetPosition;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
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

}