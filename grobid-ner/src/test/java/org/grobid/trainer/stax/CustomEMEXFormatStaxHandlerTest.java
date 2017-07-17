package org.grobid.trainer.stax;

import com.ctc.wstx.stax.WstxInputFactory;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Sentence;
import org.grobid.core.lexicon.NERLexicon;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasValue;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 05/07/2017.
 */
public class CustomEMEXFormatStaxHandlerTest {

    WstxInputFactory inputFactory = new WstxInputFactory();
    CustomEMEXFormatStaxHandler target;

    
    @Before
    public void setUp() throws Exception {
        target = new CustomEMEXFormatStaxHandler();
    }

    @Test
    public void test1() throws Exception {

        InputStream resourceAsStream = this.getClass().getResourceAsStream("/sample.enamex.small.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(resourceAsStream);

        StaxUtils.traverse(reader, target);

        assertThat(target.getSentences(), hasSize(9));

        Sentence sentence = target.getSentences().get(0);
        assertThat(sentence.getEntities(), hasSize(4));

        Entity entity = sentence.getEntities().get(0);
        assertThat(entity.getConf(), is(1.0));
        assertThat(entity.getRawName(), is("National Archives of Belgium"));
        assertThat(entity.getOrigin(), is(Entity.Origin.USER));
        assertThat(entity.getType(), is(NERLexicon.NER_Type.INSTITUTION));
        assertThat(entity.getOffsetStart(), is(19));
        assertThat(entity.getOffsetEnd(), is(47));

        assertThat(sentence.getRawValue().substring(entity.getOffsetStart(), entity.getOffsetEnd()), is("National Archives of Belgium"));


        Sentence sentence2 = target.getSentences().get(1);
        assertThat(sentence2.getEntities(), hasSize(3));
        assertThat(sentence2.getRawValue(), is("In 1831, the archive depot in Brussels was officially named the National Archives of Belgium."));

        assertThat(target.getSentences().get(8).getEntities(), hasSize(0));
        assertThat(target.getSentences().get(8).getRawValue(), is("They ensure that public archives are transferred according to strict archival standards."));


    }

}