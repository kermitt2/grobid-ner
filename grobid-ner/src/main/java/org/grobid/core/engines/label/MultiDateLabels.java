package org.grobid.core.engines.label;

import org.grobid.core.engines.Models;

/**
 * Values:
 *  - <dateValue>
 *  - <dateList>
 *  - <dateFrom>
 *  - <dateTo>
 *  - <other>
 */
public class MultiDateLabels extends TaggingLabels {
    
    public static final String DATE_VALUE_LABEL = "<dateValue>";
    public static final String DATE_INTERVAL_FROM_LABEL = "<dateFrom>";
    public static final String DATE_INTERVAL_TO_LABEL = "<dateTo>";
    public static final String DATE_VALUE_LIST_LABEL = "<dateList>";
    public static final String DATE_OTHER_LABEL = "<other>";

    public static final TaggingLabel DATE_VALUE= new TaggingLabelImpl(Models.MULTI_DATE, DATE_VALUE_LABEL);
    public static final TaggingLabel DATE_INTERVAL_FROM = new TaggingLabelImpl(Models.MULTI_DATE, DATE_INTERVAL_FROM_LABEL);
    public static final TaggingLabel DATE_INTERVAL_TO = new TaggingLabelImpl(Models.MULTI_DATE, DATE_INTERVAL_TO_LABEL);
    public static final TaggingLabel DATE_VALUE_LIST = new TaggingLabelImpl(Models.MULTI_DATE, DATE_VALUE_LIST_LABEL);
    public static final TaggingLabel DATE_OTHER = new TaggingLabelImpl(Models.MULTI_DATE, DATE_OTHER_LABEL);

    static {
        register(DATE_VALUE);
        register(DATE_INTERVAL_FROM);
        register(DATE_INTERVAL_TO);
        register(DATE_VALUE_LIST);
        register(DATE_OTHER);
    }
}
