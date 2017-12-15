package org.grobid.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public List<Entity> getEntities() {
        return sentences.parallelStream().flatMap(s -> s.getEntities().stream()).collect(Collectors.toList());
    }

    public Map<String, Long> getEntitiesFrequencies() {
        return getEntities()
                .parallelStream()
                .map(e -> e.getType().getName())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}
