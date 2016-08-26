package org.grobid.trainer.sax;

import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 25/08/16.
 */
public class SemDocSaxHandlerTest {

    private SemDocSaxHandler target;

    @Test
    public void testAssembler() throws Exception {
        InputStream reutersFile = this.getClass().getResourceAsStream("/100100newsML.xml");
        InputStream semdocFile = this.getClass().getResourceAsStream("/100100newsML.semdoc.xml");

        ReutersSaxHandler reutersSax = new ReutersSaxHandler();

        // get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setFeature("http://xml.org/sax/features/namespaces", false);
        spf.setFeature("http://xml.org/sax/features/validation", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        //get a new instance of parser
        SAXParser p = spf.newSAXParser();
        p.parse(reutersFile, reutersSax);

        target = new SemDocSaxHandler(reutersSax.getTextVector());

        p = spf.newSAXParser();
        p.parse(semdocFile, target);

        assertThat(target.getAnnotatedTextVector().size(), is(243));
    }

}