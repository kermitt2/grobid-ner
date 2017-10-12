package org.grobid.trainer.stax;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Sentence;
import org.grobid.core.lexicon.NERLexicon;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public void testParseSmallSample_noThreshol_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/9K11_Malyutka.semdoc.sample.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        String text = target.getConvertedText();
        XMLStreamReader2 readerOutput = (XMLStreamReader2) inputFactory.createXMLStreamReader(new StringReader(text));

        CustomEMEXFormatStaxHandler staxHandler = new CustomEMEXFormatStaxHandler();
        StaxUtils.traverse(readerOutput, staxHandler);

        final List<Sentence> sentences = staxHandler.getSentences();

        assertThat(sentences, hasSize(38));

        final List<Entity> entities = sentences.stream().flatMap(s -> s.getEntities().stream()).collect(Collectors.toList());
        assertThat(entities.get(0).getType(), is(NERLexicon.NER_Type.MEASURE));
        assertThat(entities.get(0).getRawName(), is("13.7"));

        assertThat(entities.get(1).getType(), is(NERLexicon.NER_Type.ARTIFACT));
        assertThat(entities.get(1).getRawName(), is("HJ-73 Hongjian Red Arrow"));

        assertThat(entities.get(2).getType(), is(NERLexicon.NER_Type.ARTIFACT));
        assertThat(entities.get(2).getRawName(), is("HJ-73 MCLOS"));
    }

    @Test
    public void testParseSample_noThreshold_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/9K11_Malyutka.semdoc.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        String text = target.getConvertedText();
        XMLStreamReader2 readerOutput = (XMLStreamReader2) inputFactory.createXMLStreamReader(new StringReader(text));

        CustomEMEXFormatStaxHandler staxHandler = new CustomEMEXFormatStaxHandler();
        StaxUtils.traverse(readerOutput, staxHandler);

        final List<Sentence> sentences = staxHandler.getSentences();

        assertThat(sentences, hasSize(138));
        
        final List<Entity> entities = sentences.stream().flatMap(s -> s.getEntities().stream()).collect(Collectors.toList());
        assertThat(entities.size(), is(230));
    }

    @Test
    public void testParseSample_confidence1_shouldWork() throws Exception {
        target.setConfidenceThreshold(0.9);
        InputStream is = this.getClass().getResourceAsStream("/9K11_Malyutka.semdoc.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        String text = target.getConvertedText();
        XMLStreamReader2 readerOutput = (XMLStreamReader2) inputFactory.createXMLStreamReader(new StringReader(text));

        CustomEMEXFormatStaxHandler staxHandler = new CustomEMEXFormatStaxHandler();
        StaxUtils.traverse(readerOutput, staxHandler);

        final List<Sentence> sentences = staxHandler.getSentences();

        assertThat(sentences, hasSize(138));

        // 234
        final List<Entity> entities = sentences.stream().flatMap(s -> s.getEntities().stream()).collect(Collectors.toList());
        assertThat(entities.size(), is(170));
    }

    @Test
    public void testParserSample_SpecialChars_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/102.2_Jazz_FM.semdoc.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        String text = target.getConvertedText();
        XMLStreamReader2 readerOutput = (XMLStreamReader2) inputFactory.createXMLStreamReader(new StringReader(text));

        CustomEMEXFormatStaxHandler staxHandler = new CustomEMEXFormatStaxHandler();
        StaxUtils.traverse(readerOutput, staxHandler);

        final List<Sentence> sentences = staxHandler.getSentences();

        assertThat(sentences.get(0).getRawValue(), is("GMG made more changes to the playlist, shifting to more R&B, soul, easy listening and adult contemporary music during the daytime. "));
    }

}