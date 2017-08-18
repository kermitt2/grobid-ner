package org.grobid.core.data.dates;

import org.grobid.core.data.Date;
import org.grobid.core.utilities.OffsetPosition;

/**
 * Wrapper class around the org.grobid.Date (Temporary)
 * The reason is because I need to have raw and formatted date information in the same object.
 */
public class DateWrapper {

    private Date isoDate = new Date();
    private String rawDate = "";
    private OffsetPosition offsetPosition = new OffsetPosition();

    public DateWrapper(String rawDate) {
        this.rawDate = rawDate;
    }

    public DateWrapper() {

    }

    public String getRawDate() {
        return rawDate;
    }

    public void setRawDate(String rawDate) {
        this.rawDate = rawDate;
    }

    public Date getIsoDate() {
        return isoDate;
    }

    public void setIsoDate(Date isoDate) {
        this.isoDate = isoDate;
    }

    public String toString() {
        return rawDate;
    }

    public int getOffsetStart() {
        return offsetPosition.start;
    }

    public int getOffsetEnd() {
        return offsetPosition.end;
    }

    public void setOffsetPosition(OffsetPosition offsetPosition) {
        this.offsetPosition = offsetPosition;
    }
}
