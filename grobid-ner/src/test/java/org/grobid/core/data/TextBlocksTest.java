package org.grobid.core.data;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 29/08/16.
 */
public class TextBlocksTest {

    TextBlocks target;

    @Before
    public void setUp() throws Exception {
        target = new TextBlocks();
    }

    @Test
    public void testGetTextBlocks_checkPositions() throws Exception {
        TextBlocks blocks = target.getTextBlocks("this is a smaple text.");

        assertThat(blocks.getTextBlocksPositions().size(), is(6));
        assertThat(blocks.getTextBlocksPositions().get(0), is(0));

    }

    @Test
    public void testGetTextBlocks_checkBlocks() throws Exception {
        TextBlocks blocks = target.getTextBlocks("this is a smaple text.");

        assertThat(blocks.getTokens().size(), is(10));
        assertThat(blocks.getTokens().get(0), is("this"));
        assertThat(blocks.getTokens().get(1), is(" "));
    }

    @Test
    public void testGetTextBlocks_checkTextBlocks() throws Exception {
        TextBlocks blocks = target.getTextBlocks("this is a smaple text.");

        assertThat(blocks.getTextBlocks().size(), is(6));
        assertThat(blocks.getTextBlocks().get(0), is("this\t<ner>"));
        assertThat(blocks.getTextBlocks().get(1), is("is\t<ner>"));
    }

}