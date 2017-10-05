package org.grobid.trainer.stax;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class IdilliaSemDocStaxHandlerTest {

    IdilliaSemDocStaxHandler target;
    WstxInputFactory inputFactory = new WstxInputFactory();

    @Before
    public void setUp() throws Exception {
        target = new IdilliaSemDocStaxHandler("9K11_Malyutka.semdoc.xml");
    }

    @Test
    public void testParseSample_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/9K11_Malyutka.semdoc.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);
    }

    @Test
    public void testParseSample_confidence1_shouldWork() throws Exception {
        target.setConfidenceThreshold(0.9);
        InputStream is = this.getClass().getResourceAsStream("/9K11_Malyutka.semdoc.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);
    }

}