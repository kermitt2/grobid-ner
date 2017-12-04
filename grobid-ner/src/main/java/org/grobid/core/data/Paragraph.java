package org.grobid.core.data;

import java.util.ArrayList;
import java.util.List;

public class Paragraph {
    private String id;
    private List<Sentence> sentences = new ArrayList<>();

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void addSentence(Sentence sentence) {
        this.sentences.add(sentence);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
