package org.grobid.trainer;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Paragraph;
import org.grobid.core.data.Sentence;
import org.grobid.core.data.TrainingDocument;
import org.grobid.core.engines.NEREnParser;
import org.grobid.core.engines.NERParser;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.trainer.stax.CustomEMEXFormatStaxHandler;
import org.grobid.trainer.stax.StaxUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class TwoLayerEntityCombinatorTest {

    WstxInputFactory inputFactory = new WstxInputFactory();

    TwoLayerEntityCombinator target;
    NERParser parserMock;

    @Before
    public void setUp() throws Exception {
        target = new TwoLayerEntityCombinator();

        parserMock = createMock(NERParser.class);
        target.setEnParser(parserMock);

    }

    @Test
    public void test1() throws Exception {
        List<TrainingDocument> docs = new ArrayList<>();

        final TrainingDocument document = new TrainingDocument();
        docs.add(document);
        document.setDocumentName("document1");
        final Paragraph paragraph1 = new Paragraph();
        paragraph1.setId("1");
        document.addParagraph(paragraph1);

        final Sentence sentence1 = new Sentence();
        final String rawValue = "The President of the United States was here in New York.";
        sentence1.setRawValue(rawValue);
        sentence1.setOffsetStart(0);
        sentence1.setOffsetEnd(56);

        paragraph1.addSentence(sentence1);

        //Larger entities already in the text
        final Entity potus = new Entity("The President of the United States");
        potus.setOffsets(new OffsetPosition(0, 34));
        potus.setType(NERLexicon.NER_Type.PERSON_TYPE);
        sentence1.addEntity(potus);

        final Entity ny = new Entity("New York");
        ny.setOffsets(new OffsetPosition(47, 55));
        ny.setType(NERLexicon.NER_Type.LOCATION);
        sentence1.addEntity(ny);


        //Entities found by the NER
        final Entity us = new Entity("United States");
        us.setOffsets(new OffsetPosition(21, 34));
        us.setType(NERLexicon.NER_Type.LOCATION);

        final Entity potus2 = new Entity("President of the United States");
        potus2.setOffsets(new OffsetPosition(4, 34));
        potus2.setType(NERLexicon.NER_Type.PERSON);

        final Entity president = new Entity("President");
        president.setOffsets(new OffsetPosition(4, 13));
        president.setType(NERLexicon.NER_Type.TITLE);

        expect(parserMock.extractNE(rawValue)).andReturn(Arrays.asList(potus2, president, us));

        replay(parserMock);
        String output = target.constructXML(docs);
        verify(parserMock);
        System.out.println(output);
    }

    @Ignore("Requires the model")
    @Test
    public void test2() throws Exception {
        GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList("../../grobid-home"));
        File grobidHome = grobidHomeFinder.findGrobidHomeOrFail();
        grobidHomeFinder.findGrobidPropertiesOrFail(grobidHome);

        GrobidProperties.getInstance(grobidHomeFinder);
        LibraryLoader.load();

        NERParser enParser = new NEREnParser();
        target.setEnParser(enParser);

        InputStream resourceAsStream = this.getClass().getResourceAsStream("strange.case.2.enamex.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(resourceAsStream);

        CustomEMEXFormatStaxHandler handler = new CustomEMEXFormatStaxHandler();
        StaxUtils.traverse(reader, handler);

        System.out.println(target.constructXML(handler.getDocuments()));

    }

}