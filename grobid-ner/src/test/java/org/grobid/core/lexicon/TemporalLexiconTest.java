package org.grobid.core.lexicon;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;

public class TemporalLexiconTest {

    TemporalLexicon target;

    @Before
    public void setUp() throws Exception {
        target = new TemporalLexicon(true);
    }

    @Test
    public void testLoading_Days() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("days_multilanguage.sample.csv");

        final Map<String, List<String>> load = target.load(is, TemporalLexicon.DAYS_NB_ITEMS);

        assertThat(load.keySet(), hasSize(8));
        
        assertThat(load.get("English"), hasSize(7));
        assertThat(load.get("Albanian"), hasSize(7));
    }

    @Test
    public void testLoading_Months() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("months_multilanguage.sample.csv");

        final Map<String, List<String>> load = target.load(is, TemporalLexicon.MONTHS_NB_ITEMS);

        assertThat(load.keySet(), hasSize(11));

        assertThat(load.get("English"), hasSize(12));
        assertThat(load.get("Albanian"), hasSize(12));
    }


    @Test
    public void testIsMonth() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("months_multilanguage.sample.csv");

        final Map<String, List<String>> data = target.load(is, TemporalLexicon.MONTHS_NB_ITEMS);

        target.setDictionaryMonths(data);

        assertThat(target.isMonthNameMatching("jan"), is(true));
        assertThat(target.isMonthNameMatching("january"), is(true));
        assertThat(target.isMonthNameMatching("January"), is(true));
        assertThat(target.isMonthNameMatching("janvier"), is(true));
        assertThat(target.isMonthNameMatching("fevrier"), is(true));
        assertThat(target.isMonthNameMatching("Fevrier"), is(true));
        assertThat(target.isMonthNameMatching("février"), is(true));
    }

    @Test
    public void testIsDay() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("days_multilanguage.sample.csv");

        final Map<String, List<String>> data = target.load(is, TemporalLexicon.DAYS_NB_ITEMS);

        target.setDictionaryDays(data);

        assertThat(target.isDayNameMatching("mon"), is(true));
        assertThat(target.isDayNameMatching("monday"), is(true));
        assertThat(target.isDayNameMatching("Monday"), is(true));
        assertThat(target.isDayNameMatching("miércoles"), is(true));
        assertThat(target.isDayNameMatching("miercoles"), is(true));
    }


}