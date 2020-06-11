package org.grobid.trainer.sax;

import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 31/08/16.
 */
public class TextSaxHandlerTest {

    TextSaxHandler target;
    SAXParser p;

    @Before
    public void setUp() throws Exception {
        target = new TextSaxHandler();

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setFeature("http://xml.org/sax/features/namespaces", false);
        spf.setFeature("http://xml.org/sax/features/validation", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        p = spf.newSAXParser();
    }


    @Test
    public void testSimpleParser_wikipediaSemdoc_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/wikipedia.semdoc.sample.xml");
        p.parse(is, target);

        assertThat(target.getTextVector().size(), is(3));
        assertThat(target.getTextVector().get(0).size(), is(9));
    }
}