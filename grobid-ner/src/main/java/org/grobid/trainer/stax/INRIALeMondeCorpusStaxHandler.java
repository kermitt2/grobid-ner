package org.grobid.trainer.stax;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.data.TextBlocks;
import org.grobid.core.lexicon.NERLexicon;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Created by lfoppiano on 29/08/16.
 */
public class INRIALeMondeCorpusStaxHandler implements StaxParserContentHandler {

    private Writer writer;
    private StringBuilder sb;

    private boolean inSentence = false;
    private boolean inDocument = false;
    private boolean inNamedEntity = false;

    private String entityType = null;
    private String disambiguatedName = null;
    private String entitySubType = null;
    private String uri = null;

    //Ignored for the moment as they are too specific
    private String comment = null;
    private String gender = null;

    TextBlocks blocks = new TextBlocks();

    public INRIALeMondeCorpusStaxHandler() {

        this.sb = new StringBuilder();
    }

    public INRIALeMondeCorpusStaxHandler(Writer writer) {
        this();
        this.writer = writer;
    }


    @Override
    public void onStartDocument(XMLStreamReader2 xmlStreamReader2) {
    }

    @Override
    public void onEndDocument(XMLStreamReader2 xmlStreamReader2) {

    }

    /**
     * -DOCSTART- followed by the document id is appended to define the start of a new document
     */
    @Override
    public void onStartElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();
        if (localName.equals("document")) {
            inDocument = true;
            sb.append("-DOCSTART- ").append(reader.getAttributeValue("", "id")).append("\n");
        } else if (localName.equals("sentence")) {
            inSentence = true;
        } else if (localName.equals("ENAMEX")) {
            inNamedEntity = true;
            readOtherAttributes(reader);
        }
    }

    private void readOtherAttributes(XMLStreamReader2 reader) {
        entityType = getAttributeFiltered(reader, "type");
        if (isNotBlank(entityType)) {
            uri = getAttributeFiltered(reader, "uri");
            entitySubType = getAttributeFiltered(reader, "sub_type");
            disambiguatedName = getAttributeFiltered(reader, "name");

//            if (StringUtils.equals(entityType, "Person")) {
//                gender = getAttributeFiltered(reader, "gender");
//            }
        }

    }

    private String getAttributeFiltered(XMLStreamReader2 reader, String name) {
        return StringUtils.equals(reader.getAttributeValue("", name), "null") ? null : reader.getAttributeValue("", name);
    }


    /**
     * When the sentence is closed, add an empty line as sentence separator.
     */
    @Override
    public void onEndElement(XMLStreamReader2 reader) {
        if (reader.getName().getLocalPart().equals("sentence")) {
            inSentence = false;
            try {
                writer.write(sb.append("\n").toString());
            } catch (IOException e) {
                throw new RuntimeException();
            }
            sb = new StringBuilder();
        } else if (reader.getName().getLocalPart().equals("ENAMEX")) {
            inNamedEntity = false;
            entityType = null;
            entitySubType = null;
        }
    }

    @Override
    public void onCharacter(XMLStreamReader2 reader) {
        if (inSentence || inNamedEntity) {
            String text = reader.getText();
            text = trim(text);
            if (isEmpty(text)) {
                return;
            }

            TextBlocks textBlocks = blocks.getTextBlocks(text);

            for (String textBlock : textBlocks.getTextBlocks()) {
                String textBlockCleaned = StringUtils.replace(textBlock, TextBlocks.SUFFIX_NER, "");

                if ((inNamedEntity) && (isNotEmpty(entityType))) {
                    sb.append(textBlockCleaned).append("\t").append(translate(entityType, entitySubType));
                    if (isNotEmpty(entitySubType)) {
                        sb.append("\t").append(entitySubType);
                    }

                    if (isNotBlank(disambiguatedName)) {
                        sb.append("\t").append(disambiguatedName);
                    }

                    if (isNotBlank(uri)) {
                        sb.append("\t").append(uri);
                    }

                    sb.append("\n");
                } else {
                    sb.append(textBlockCleaned).append("\t").append("O").append("\n");
                }
            }
        }


    }

    @Override
    public void onAttribute(XMLStreamReader2 reader) {
    }

    private String extractTagContent(XMLEventReader reader, XMLEventWriter writer) throws XMLStreamException {
        XMLEvent event = reader.nextEvent();
        String data = event.asCharacters().getData();
        data = data != null ? data.trim() : "";
        writer.add(event);
        return data;
    }

    protected String translate(String type, String subType) {
        //default
        String labelOutput = "O";
        String senseOutput = "";

        if (StringUtils.equalsIgnoreCase(type, "Company")) {
            labelOutput = NERLexicon.NER_Type.BUSINESS.getName();
            senseOutput = "business/N1";
        } else if (StringUtils.equalsIgnoreCase(type, "FictionCharacter")) {
            labelOutput = NERLexicon.NER_Type.PERSON.getName();
        } else if (StringUtils.equalsIgnoreCase(type, "organization")) {
            if (StringUtils.equalsIgnoreCase(subType, "InstitutionalOrganization")) {
                labelOutput = NERLexicon.NER_Type.INSTITUTION.getName();
            } else if (StringUtils.endsWithIgnoreCase(subType, "company")) {
                labelOutput = NERLexicon.NER_Type.BUSINESS.getName();
            } else {
                labelOutput = NERLexicon.NER_Type.ORGANISATION.getName();
            }

        } else if (StringUtils.equalsIgnoreCase(type, "Person")) {
            labelOutput = NERLexicon.NER_Type.PERSON.getName();
        } else if (StringUtils.equalsIgnoreCase(type, "Location")) {
            labelOutput = NERLexicon.NER_Type.LOCATION.getName();
        } else if (StringUtils.equalsIgnoreCase(type, "Poi")) {
            labelOutput = NERLexicon.NER_Type.LOCATION.getName();
        } else if (StringUtils.equalsIgnoreCase(type, "Product")) {
            labelOutput = NERLexicon.NER_Type.ARTIFACT.getName();
        }

        return labelOutput;
    }


    /**
     * How to use it
     * <p>
     * This class require a single parameter which is the input file containng the french
     * corpus from Le Monde manually annotated.
     * <p>
     * The class will output the cONLL 2013 format in a file having the same name as the input
     * suffixed with .output.
     */
    public static void main(String[] args) throws IOException, XMLStreamException {

        if (args.length == 0) {
            System.out.println("Missing input file. First parameter.");
            System.exit(-1);
        }

        WstxInputFactory inputFactory = new WstxInputFactory();

        Writer writer = new FileWriter(args[0] + ".output");
        INRIALeMondeCorpusStaxHandler inriaLeMondeCorpusStaxHandler = new INRIALeMondeCorpusStaxHandler(writer);

        InputStream is = new FileInputStream(args[0]);
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, inriaLeMondeCorpusStaxHandler);

        writer.close();
    }
}
