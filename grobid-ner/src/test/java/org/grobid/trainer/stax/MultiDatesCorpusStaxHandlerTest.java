package org.grobid.trainer.stax;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.data.dates.Period;
import org.grobid.core.utilities.Pair;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class MultiDatesCorpusStaxHandlerTest {
    WstxInputFactory inputFactory = new WstxInputFactory();
    MultiDatesCorpusStaxHandler target;


    @Before
    public void setUp() throws Exception {
        target = new MultiDatesCorpusStaxHandler();

    }

    @Test
    public void testParse_checkModel() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("multi.dates.test.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        List<Period> output = target.getPeriods();

        assertThat(output, hasSize(5));
        assertThat(output.get(0).getType(), is(Period.Type.INTERVAL));
        assertThat(output.get(0).getFromDate().getRawDate(), is("May 1 1943"));
        assertThat(output.get(0).getToDate().getRawDate(), is("July 30 1944"));

        assertThat(output.get(1).getType(), is(Period.Type.VALUE));
        assertThat(output.get(1).getValue().getRawDate(), is("September 28 1943"));

        assertThat(output.get(2).getType(), is(Period.Type.LIST));
        assertThat(output.get(2).getList(), hasSize(2));
        assertThat(output.get(2).getList().get(0).getRawDate(), is("1942-07-27"));
        assertThat(output.get(2).getList().get(1).getRawDate(), is("1944-09-04"));

        assertThat(output.get(3).getType(), is(Period.Type.INTERVAL));
        assertThat(output.get(3).getToDate().getRawDate(), is("23/01/1946"));
        assertNull(output.get(3).getFromDate());

    }

    @Test
    public void testParse_checkOutput() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("multi.dates.test.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        final List<Pair<String, String>> data = target.getData();

        for (Pair<String, String> da : data) {
            System.out.println(da.a + " -> " + da.b);
        }

    }

}