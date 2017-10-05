package org.grobid.trainer.stax;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class IdilliaSemDocTextSaxHandlerTest {

    IdilliaSemDocTextStaxHandler target;
    WstxInputFactory inputFactory = new WstxInputFactory();

    @Before
    public void setUp() throws Exception {
        target = new IdilliaSemDocTextStaxHandler();
    }

    @Test
    public void testParseSample_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/wikipedia.semdoc.sample.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        assertThat(target.getTextVector(), hasSize(3));
        assertThat(target.getTextVector().toString(), is("[[World, War, I, -, Wikipedia, ,, the, free, encyclopedia], [World, War, I], [From, Wikipedia, ,, the, free, encyclopedia]]"));
        assertThat(target.getText(), is("World War I - Wikipedia, the free encyclopedia World War I From Wikipedia, the free encyclopedia"));
    }
}