package org.grobid.core.lexicon;

import org.apache.commons.collections4.CollectionUtils;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.layout.LayoutToken;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

public class LexiconPositionsIndexes {

    private Lexicon lexicon;

    private List<OffsetPosition> localLocationPositions = new ArrayList<OffsetPosition>();
    private List<OffsetPosition> localPersonTitlePositions = new ArrayList<OffsetPosition>();
    private List<OffsetPosition> localOrganisationPositions = new ArrayList<OffsetPosition>();
    private List<OffsetPosition> localOrgFormPositions = new ArrayList<OffsetPosition>();

    public LexiconPositionsIndexes(Lexicon lexicon) {
        this.lexicon = lexicon;
    }

    public void computeIndexes(String text) {
        localLocationPositions = lexicon.inLocationNames(text);
        localPersonTitlePositions = lexicon.inPersonTitleNames(text);
        localOrganisationPositions = lexicon.inOrganisationNames(text);
        localOrgFormPositions = lexicon.inOrgFormNames(text);
    }

    public void computeIndexes(List<LayoutToken> tokens) {
        // we should extend the methods to avoid a copy of the strings here
        List<String> strings = getTexts(tokens);
        localLocationPositions = lexicon.inLocationNames(strings);
        localPersonTitlePositions = lexicon.inPersonTitleNames(strings);
        localOrganisationPositions = lexicon.inOrganisationNames(strings);
        localOrgFormPositions = lexicon.inOrgFormNames(strings);
    }

    /**
     * return true if the token position are within the boundaries of a lexicon token.
     * It assumes the scan is done incrementally.
     * It updates the currentPosition to avoid iterating through the lexicon positions.
     */
    public static boolean isTokenInLexicon(List<OffsetPosition> listPositionInLexicon, int currentPosition) {
        if (CollectionUtils.isNotEmpty(listPositionInLexicon)) {
            for (int mm = 0; mm < listPositionInLexicon.size(); mm++) {
                if ((currentPosition >= listPositionInLexicon.get(mm).start) &&
                        (currentPosition <= listPositionInLexicon.get(mm).end)) {
                    return true;
                } else if (currentPosition < listPositionInLexicon.get(mm).start) {
                    return false;
                } else if (currentPosition > listPositionInLexicon.get(mm).end) {
                    continue;
                }
            }
        }
        return false;
    }

    public List<OffsetPosition> getLocalLocationPositions() {
        return localLocationPositions;
    }

    public void setLocalLocationPositions(List<OffsetPosition> localLocationPositions) {
        this.localLocationPositions = localLocationPositions;
    }

    public List<OffsetPosition> getLocalPersonTitlePositions() {
        return localPersonTitlePositions;
    }

    public void setLocalPersonTitlePositions(List<OffsetPosition> localPersonTitlePositions) {
        this.localPersonTitlePositions = localPersonTitlePositions;
    }

    public List<OffsetPosition> getLocalOrganisationPositions() {
        return localOrganisationPositions;
    }

    public void setLocalOrganisationPositions(List<OffsetPosition> localOrganisationPositions) {
        this.localOrganisationPositions = localOrganisationPositions;
    }

    public List<OffsetPosition> getLocalOrgFormPositions() {
        return localOrgFormPositions;
    }

    public void setLocalOrgFormPositions(List<OffsetPosition> localOrgFormPositions) {
        this.localOrgFormPositions = localOrgFormPositions;
    }

    /**
     * Give the list of textual tokens from a list of LayoutToken
     */
    private static List<String> getTexts(List<LayoutToken> tokenizations) {
        List<String> texts = new ArrayList<String>();
        for (LayoutToken token : tokenizations) {
            if (isNotEmpty(trim(token.getText())) && 
                !token.getText().equals(" ") &&
                !token.getText().equals("\n") && 
                !token.getText().equals("\r") &&  
                !token.getText().equals("\t") && 
                !token.getText().equals("\u00A0")) {
                    texts.add(token.getText());
            }
        }
        return texts;
    }
}
