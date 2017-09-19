package org.grobid.core.utilities;

import org.grobid.core.layout.LayoutToken;

import java.util.ArrayList;
import java.util.List;

public class LayoutTokensNERUtility {

    public static List<LayoutToken> mapFromTokenisedList(List<String> labeled) {

        List<LayoutToken> tokens = new ArrayList<>();

        for (String label : labeled) {
            tokens.add(new LayoutToken(label));
        }

        return tokens;
    }

}
