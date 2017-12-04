package org.grobid.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** This class represent the document from the training data of NER **/
public class TrainingDocument {
    private String documentName;
    private List<Paragraph> paragraphs = new ArrayList<>();

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public void addParagraph(Paragraph paragraph) {
        paragraphs.add(paragraph);
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public List<Sentence> getSentences() {
        return this.paragraphs.stream()
                .flatMap(paragraph -> paragraph.getSentences().stream())
                .collect(Collectors.toList());
    }
}
