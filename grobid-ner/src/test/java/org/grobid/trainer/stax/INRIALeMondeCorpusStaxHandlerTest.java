package org.grobid.trainer.stax;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 29/08/16.
 */
public class INRIALeMondeCorpusStaxHandlerTest {

    WstxInputFactory inputFactory = new WstxInputFactory();
    INRIALeMondeCorpusStaxHandler target;
    Writer writer;

    @Before
    public void setUp() throws Exception {
        writer = new StringWriter();
        target = new INRIALeMondeCorpusStaxHandler(writer);
    }

    @Test
    public void testSampleParsing_shouldWork() throws Exception {

        InputStream is = this.getClass().getResourceAsStream("/le.monde.corpus.sample.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        String output = writer.toString();
        String splitted[] = output.split("\n");

        assertThat(splitted[0], is("-DOCSTART- id248980"));
        assertThat(splitted[1], is("Certes\tO"));
        assertThat(splitted[2], is(",\tO"));
    }

}