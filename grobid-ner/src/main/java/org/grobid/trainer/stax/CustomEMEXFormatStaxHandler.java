package org.grobid.trainer.stax;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Sentence;
import org.grobid.core.lexicon.NERLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This parser extract the list of sentences and its respective list of entities.
 * The entities are defined with the OffsetPosition, pointing at the starting and ending character within each
 * sentence.
 *
 * Created by lfoppiano on 05/07/2017.
 */
public class CustomEMEXFormatStaxHandler implements StaxParserContentHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(CustomEMEXFormatStaxHandler.class);

    List<Sentence> sentences = new ArrayList<>();
    private boolean inSentence = false;
    private boolean inNamedEntity = false;
    private Entity currentEntity;
    private Sentence currentSentence;

    @Override
    public void onStartDocument(XMLStreamReader2 reader) {

    }

    @Override
    public void onEndDocument(XMLStreamReader2 reader) {

    }

    @Override
    public void onStartElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();
        if (localName.equals("sentence")) {
            inSentence = true;
            currentSentence = new Sentence();

            String sentenceId = getAttributeFiltered(reader, "id", "xml");
            currentSentence.setId(sentenceId);

        } else if (localName.equals("ENAMEX")) {
            inNamedEntity = true;
            currentEntity = new Entity();

            final String typeValue = getAttributeFiltered(reader, "type");
            final NERLexicon.NER_Type type = NERLexicon.NER_Type.getTypeByValue(typeValue);
            if (type != null) {
                currentEntity.setType(type);
            } else {
                LOGGER.warn("Cannot extract entity type, extracted type: " + typeValue);
            }
            currentEntity.setOffsetStart(currentSentence.getRawValue().length());
        }

    }

    @Override
    public void onEndElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();

        if (reader.getName().getLocalPart().equals("sentence")) {
            sentences.add(currentSentence);
            inSentence = false;

        } else if (reader.getName().getLocalPart().equals("ENAMEX")) {
            inNamedEntity = false;
            
            currentSentence.addEntity(currentEntity);
            currentEntity = null;

        }
    }

    @Override
    public void onCharacter(XMLStreamReader2 reader) {
        String text = reader.getText();
        if (inNamedEntity) {
            currentEntity.setOrigin(Entity.Origin.USER);
            currentEntity.setRawName(text);
            currentEntity.setConf(1.0);
            currentEntity.setOffsetEnd(currentEntity.getOffsetStart() + text.length());
        }

        if(inSentence) {
            currentSentence.setRawValue(currentSentence.getRawValue() + text);
        }
    }

    @Override
    public void onAttribute(XMLStreamReader2 reader) {

    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    private String getAttributeFiltered(XMLStreamReader2 reader, String name) {
        return getAttributeFiltered(reader, name, null);
    }
    
    private String getAttributeFiltered(XMLStreamReader2 reader, String name, String namespace) {
        String ns = isBlank(namespace) ? "" : namespace;
        return StringUtils.equals(reader.getAttributeValue(ns, name), "null") ? null : reader.getAttributeValue(ns, name);
    }
}
