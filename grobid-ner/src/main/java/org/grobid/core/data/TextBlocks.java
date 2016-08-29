package org.grobid.core.data;

import org.grobid.core.utilities.TextUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TextBlocks {
    public static final String SUFFIX_NER = "\t<ner>";
    private List<String> textBlocks = new ArrayList<String>();
    private List<String> tokens = new ArrayList<String>();
    private List<Integer> textBlocksPositions = new ArrayList<Integer>();


    public List<String> getTextBlocks() {
        return textBlocks;
    }

    public void setTextBlocks(List<String> textBlocks) {
        this.textBlocks = textBlocks;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<Integer> getTextBlocksPositions() {
        return textBlocksPositions;
    }

    public void setTextBlocksPositions(List<Integer> textBlocksPositions) {
        this.textBlocksPositions = textBlocksPositions;
    }

    public static TextBlocks getTextBlocks(String text) {

        TextBlocks blocks = new TextBlocks();

        StringTokenizer st = new StringTokenizer(text, TextUtilities.fullPunctuations, true);
        if (st.countTokens() == 0) {
            return null;
        }

        int pos = 0; // current offset
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            blocks.getTokens().add(tok);
            if (!tok.equals(" ")) {
                blocks.getTextBlocks().add(tok + SUFFIX_NER);
                blocks.getTextBlocksPositions().add(pos);
            }
            pos += tok.length();
        }
        return blocks;
    }
}
