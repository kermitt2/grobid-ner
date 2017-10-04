package org.grobid.core.data.dates;

import org.grobid.core.data.Date;
import org.junit.Test;

public class PeriodTest {

    @Test
    public void testJsonMarshal() throws Exception {
        final DateWrapper when = new DateWrapper("10 September 2010");
        final Date isoDate = new Date();
        isoDate.setYear(2010);
        isoDate.setMonth(9);
        isoDate.setDay(10);
        when.setIsoDate(isoDate);
        Period period = new Period(when);

        System.out.println(period.toJson());
    }

}