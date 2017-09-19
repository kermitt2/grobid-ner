package org.grobid.core.data;

import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.OffsetPosition;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * This class represents a sentence with stand-off position to mark its boundaries in a text.
 *
 * @author Patrice Lopez
 */
public class Sentence {

    private String rawValue = "";
    private String id;
    private List<Entity> entities = new ArrayList<>();

    // relative offset positions in context
    private OffsetPosition offsets = null;
    private List<LayoutToken> tokenisedValue;
    private List<Integer> entityIndexList = new ArrayList<>();

    public Sentence() {
        this.offsets = new OffsetPosition();
    }

    public OffsetPosition getOffsets() {
        return this.offsets;
    }

    public void setOffsets(OffsetPosition offsets) {
        this.offsets = offsets;
    }

    public void setOffsetStart(int start) {
        offsets.start = start;
    }

    public int getOffsetStart() {
        return offsets.start;
    }

    public void setOffsetEnd(int end) {
        offsets.end = end;
    }

    public int getOffsetEnd() {
        return offsets.end;
    }

    public String toJSON() {
        return "{ \"offsetStart\" : " + offsets.start + ", \"offsetEnd\" : " + offsets.end + " }";
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public void addEntity(Entity currentEntity) {
        this.entities.add(currentEntity);
    }

    /**
     * Set the tokenised version of the sentence, the responsibility is delegated to the caller.
     * When the tokens are set, if the entities are present (the list is not empty), a reversed index
     * (to simplify the identification of entities starting from the tokens) is calculated.
     */
    public void setTokenisedValue(List<LayoutToken> tokenisedValue) {
        this.tokenisedValue = tokenisedValue;
        List<Integer> entityIndexList = new ArrayList<>();
        if (isNotEmpty(getEntities())) {
            int checkIndex = 0;
            int startEntityIndex = 0;
            out:
            for (int i = 0; i < tokenisedValue.size(); i++) {
                int idxExpectedStart = checkIndex;
                int idxExpectedEnd = checkIndex + tokenisedValue.get(i).getText().length();

                for (int j = startEntityIndex; j < getEntities().size(); j++) {
                    Entity entity = getEntities().get(j);
                    if (idxExpectedStart >= entity.getOffsetStart() && idxExpectedEnd <= entity.getOffsetEnd()) {
                        entityIndexList.add(j);
                        idxExpectedStart = idxExpectedEnd;
                        checkIndex = idxExpectedEnd;
                        continue out;
                    }
                }
                entityIndexList.add(-1);
                idxExpectedStart = idxExpectedEnd;
                checkIndex = idxExpectedEnd;
            }

        } else {
            for (LayoutToken token : getTokenisedValue()) {
                entityIndexList.add(-1);
            }
        }
        this.entityIndexList.addAll(entityIndexList);
    }

    public List<LayoutToken> getTokenisedValue() {
        return this.tokenisedValue;
    }

    public void setEntityIndexList(List<Integer> entityIndexList) {
        this.entityIndexList = entityIndexList;
    }

    public List<Integer> getEntityIndexList() {
        return entityIndexList;
    }
}