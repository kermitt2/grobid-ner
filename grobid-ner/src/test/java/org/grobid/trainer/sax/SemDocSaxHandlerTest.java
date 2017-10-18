package org.grobid.trainer.sax;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.trainer.stax.IdilliaSemDocTextStaxHandler;
import org.grobid.trainer.stax.StaxUtils;
import org.junit.Before;
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
    private SAXParserFactory spf;
    WstxInputFactory inputFactory = new WstxInputFactory();

    @Before
    public void setUp() throws Exception {
        spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setFeature("http://xml.org/sax/features/namespaces", false);
        spf.setFeature("http://xml.org/sax/features/validation", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    }

    @Test
    public void testAssemblingReutersAndSemdoc_shouldWork() throws Exception {
        InputStream reutersFile = this.getClass().getResourceAsStream("/100100newsML.xml");
        InputStream semdocFile = this.getClass().getResourceAsStream("/100100newsML.semdoc.xml");

        ReutersSaxHandler reutersSax = new ReutersSaxHandler();

        //get a new instance of parser
        SAXParser p = spf.newSAXParser();
        p.parse(reutersFile, reutersSax);

        target = new SemDocSaxHandler(reutersSax.getTextVector());

        p = spf.newSAXParser();
        p.parse(semdocFile, target);

        assertThat(target.getAnnotatedTextVector().size(), is(243));
    }

    @Test
    public void testAssemblingIdilliaAndSemdoc_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/9K11_Malyutka.semdoc.sample.xml");

        //extract the text
        IdilliaSemDocTextStaxHandler idilliaTextParser = new IdilliaSemDocTextStaxHandler();
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, idilliaTextParser);

        target = new SemDocSaxHandler(idilliaTextParser.getTextVector());
        System.out.println(idilliaTextParser.getText());

        SAXParser p = spf.newSAXParser();
        
        is = this.getClass().getResourceAsStream("/9K11_Malyutka.semdoc.sample.xml");
        p.parse(is, target);

        System.out.println(target.getAnnotatedTextVector());
    }

}