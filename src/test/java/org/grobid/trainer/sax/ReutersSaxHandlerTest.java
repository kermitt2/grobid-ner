package org.grobid.trainer.sax;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 25/08/16.
 */
public class ReutersSaxHandlerTest {

    @Test
    public void testRetokenize_1() throws Exception {
        List<String> tokens = new ArrayList<String>();
        tokens.add("around");
        tokens.add(" ");
        tokens.add("10");
        tokens.add(",");
        tokens.add("000");

        List<String> tokens2 = ReutersSaxHandler.retokenize(tokens);

        assertThat(tokens2.size(), is(3));
        assertThat(tokens2.get(0), is("around"));
        assertThat(tokens2.get(2), is("10,000"));
    }

    @Test
    public void testRetokenize_2() throws Exception {

        List<String> tokens = new ArrayList<String>();
        tokens.add("10");
        tokens.add(",");
        tokens.add("000");
        tokens.add(",");
        tokens.add("000");
        tokens.add(".");
        tokens.add("00");
        tokens.add(" ");
        tokens.add("errors");

        List<String> tokens2 = ReutersSaxHandler.retokenize(tokens);

        assertThat(tokens2.size(), is(3));
        assertThat(tokens2.get(0), is("10,000,000.00"));
        assertThat(tokens2.get(2), is("errors"));
    }


}