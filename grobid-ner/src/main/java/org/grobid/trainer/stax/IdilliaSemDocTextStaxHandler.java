package org.grobid.trainer.stax;

import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This parser extracts the text from the wikipedia text annotated by Idillia
 */
public class IdilliaSemDocTextStaxHandler implements StaxParserContentHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(IdilliaSemDocTextStaxHandler.class);

    private StringBuilder accumulatorSentence = new StringBuilder();
    private StringBuilder accumulatorText = new StringBuilder();
    private List<List<String>> textVector = new ArrayList<>();
    private List<String> currentVector = null;

    private boolean insideText = false;


    public String getText() {
        return accumulatorText.toString().trim();
    }

    private String getTextSentence() {
        return accumulatorSentence.toString().trim();
    }


    @Override
    public void onStartDocument(XMLStreamReader2 reader) {

    }

    @Override
    public void onEndDocument(XMLStreamReader2 reader) {

    }

    @Override
    public void onStartElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();

        if ("txt".equals(localName)) {
            insideText = true;
        }
    }

    @Override
    public void onEndElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();

        if ("txt".equals(localName)) {
            insideText = false;
            String token = getTextSentence();
            // Idillia tokenisation glitches
            token = token.replace("n't", " n't");

            List<String> currentTmpVector = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(token, TextUtilities.delimiters, true);
            while (st.hasMoreTokens()) {
                currentTmpVector.add(st.nextToken());
            }

            // finally remove spaces
            for (String tok : currentTmpVector) {
                if (!tok.equals(" ")) {
                    if (currentVector == null) {
                        currentVector = new ArrayList<>();
                    }
                    currentVector.add(tok);
                }
            }
        } else if ("sent".equals(localName)) {
            textVector.add(currentVector);
            currentVector = null;
        }
        accumulatorSentence.setLength(0);

    }

    @Override
    public void onCharacter(XMLStreamReader2 reader) {
        if (insideText) {
            final String text = reader.getText();
            accumulatorSentence.append(text);
            accumulatorText.append(text);
        }
    }

    @Override
    public void onAttribute(XMLStreamReader2 reader) {

    }

    public List<List<String>> getTextVector() {
        return textVector;
    }
}
