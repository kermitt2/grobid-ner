package org.grobid.core.engines;

import org.grobid.core.EngineMockTest;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.dates.Period;
import org.grobid.core.layout.LayoutToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class MultiDateParserTest extends EngineMockTest {

    MultiDateParser target;

    @Before
    public void setUp() throws Exception {
        target  = new MultiDateParser();
    }

    @Test
    public void testExtract_Interval() throws Exception {
        String output = "between\tbetween\tb\tbe\tbet\tbetw\tn\ten\teen\tween\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\txxxx\t0\t<other>\n" +
                "February\tfebruary\tF\tFe\tFeb\tFebr\ty\try\tary\tuary\tINITCAP\tNODIGIT\t0\t0\t1\tNOPUNCT\tXxxx\t0\tI-<dateFrom>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\tdd\t0\t<dateFrom>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\txxx\t0\t<other>\n" +
                "February\tfebruary\tF\tFe\tFeb\tFebr\ty\try\tary\tuary\tINITCAP\tNODIGIT\t0\t0\t1\tNOPUNCT\tXxxx\t0\tI-<dateTo>\n" +
                "28\t28\t2\t28\t28\t28\t8\t28\t28\t28\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\tdd\t0\t<dateTo>\n" +
                "1942\t1942\t1\t19\t194\t1942\t2\t42\t942\t1942\tNOCAPS\tALLDIGIT\t0\t1\t0\tNOPUNCT\tdddd\t0\t<dateTo>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\t0\t0\tPUNCT\t.\t0\t<other>";

        final String text = "between February 10 and February 28 1942.";

        List<LayoutToken> layoutTokenTokenisation = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        List<Period> results = target.extractResults(output, layoutTokenTokenisation);

        assertThat(results, hasSize(1));
        assertThat(results.get(0).getType(), is(Period.Type.INTERVAL));
        assertThat(results.get(0).getFromDate().getRawDate(), is("February 10"));
        assertThat(results.get(0).getFromDate().getOffsetStart(), is(8));
        assertThat(results.get(0).getFromDate().getOffsetEnd(), is(19));
        assertThat(text.substring(results.get(0).getFromDate().getOffsetStart(),
                results.get(0).getFromDate().getOffsetEnd()), is(results.get(0).getFromDate().getRawDate()));
        assertThat(results.get(0).getToDate().getRawDate(), is("February 28 1942"));
        assertThat(results.get(0).getToDate().getOffsetStart(), is(24));
        assertThat(results.get(0).getToDate().getOffsetEnd(), is(40));
        assertThat(text.substring(results.get(0).getToDate().getOffsetStart(),
                results.get(0).getToDate().getOffsetEnd()), is(results.get(0).getToDate().getRawDate()));
    }

    @Test
    public void testExtract_HalfInterval_onlyTo() throws Exception {
        String output = "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\t0\t0\tHYPHEN\t-\t0\t<other>\n" +
                "1941\t1941\t1\t19\t194\t1941\t1\t41\t941\t1941\tNOCAPS\tALLDIGIT\t0\t1\t0\tNOPUNCT\tdddd\t0\tI-<dateTo>";

        final String text = "- 1941";

        List<LayoutToken> layoutTokenTokenisation = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        List<Period> results = target.extractResults(output, layoutTokenTokenisation);

        assertThat(results, hasSize(1));
        assertThat(results.get(0).getType(), is(Period.Type.INTERVAL));
        assertNull(results.get(0).getFromDate());
        assertThat(results.get(0).getToDate().getRawDate(), is("1941"));
    }





    @Test
    public void testExtract_Value() throws Exception {
        String output = "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\txx\t0\t<other>\n" +
                "February\tfebruary\tF\tFe\tFeb\tFebr\ty\try\tary\tuary\tINITCAP\tNODIGIT\t0\t0\t1\tNOPUNCT\tXxxx\t0\tI-<dateValue>\n" +
                "28\t28\t2\t28\t28\t28\t8\t28\t28\t28\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\tdd\t0\t<dateValue>\n" +
                "1942\t1942\t1\t19\t194\t1942\t2\t42\t942\t1942\tNOCAPS\tALLDIGIT\t0\t1\t0\tNOPUNCT\tdddd\t0\t<dateValue>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\t0\t0\tPUNCT\t.\t0\t<other>";

        final String text = "in February 28 1942.";

        List<LayoutToken> layoutTokenTokenisation = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        List<Period> results = target.extractResults(output, layoutTokenTokenisation);

        assertThat(results, hasSize(1));
        assertThat(results.get(0).getType(), is(Period.Type.VALUE));
        assertThat(results.get(0).getValue().getRawDate(), is("February 28 1942"));
        assertThat(results.get(0).getValue().getOffsetStart(), is(3));
        assertThat(results.get(0).getValue().getOffsetEnd(), is(19));
        assertThat(text.substring(results.get(0).getValue().getOffsetStart(),
                results.get(0).getValue().getOffsetEnd()), is(results.get(0).getValue().getRawDate()));
    }

    @Test
    public void testExtract_List() throws Exception {
        String output = "1942\t1942\t1\t19\t194\t1942\t2\t42\t942\t1942\tNOCAPS\tALLDIGIT\t0\t1\t0\tNOPUNCT\tdddd\t0\tI-<dateList>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\t0\t0\tHYPHEN\t-\t0\t<dateList>\n" +
                "07\t07\t0\t07\t07\t07\t7\t07\t07\t07\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\tdd\t0\t<dateList>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\t0\t0\tHYPHEN\t-\t0\t<dateList>\n" +
                "27\t27\t2\t27\t27\t27\t7\t27\t27\t27\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\tdd\t0\t<dateList>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\t0\t0\tPUNCT\t,\t0\t<other>\n" +
                "19\t19\t1\t19\t19\t19\t9\t19\t19\t19\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\tdd\t0\t<dateList>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\t0\t0\tHYPHEN\t-\t0\t<dateList>\n" +
                "12\t12\t1\t12\t12\t12\t2\t12\t12\t12\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\tdd\t0\t<dateList>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\t0\t0\tHYPHEN\t-\t0\t<dateList>\n" +
                "1932\t1932\t1\t19\t193\t1932\t2\t32\t932\t1932\tNOCAPS\tALLDIGIT\t0\t1\t0\tNOPUNCT\tdddd\t0\t<dateList>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\t0\t0\tPUNCT\t,\t0\t<other>\n" +
                "12\t12\t1\t12\t12\t12\t2\t12\t12\t12\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\tdd\t0\t<dateList>\n" +
                "February\tfebruary\tF\tFe\tFeb\tFebr\ty\try\tary\tuary\tINITCAP\tNODIGIT\t0\t0\t1\tNOPUNCT\tXxxx\t0\t<dateList>\n" +
                "1983\t1983\t1\t19\t198\t1983\t3\t83\t983\t1983\tNOCAPS\tALLDIGIT\t0\t1\t0\tNOPUNCT\tdddd\t0\t<dateList>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\t0\t0\tPUNCT\t,\t0\t<other>\n" +
                "18\t18\t1\t18\t18\t18\t8\t18\t18\t18\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\tdd\t0\t<dateList>\n" +
                "January\tjanuary\tJ\tJa\tJan\tJanu\ty\try\tary\tuary\tINITCAP\tNODIGIT\t0\t0\t1\tNOPUNCT\tXxxx\t0\t<dateList>\n" +
                "2220\t2220\t2\t22\t222\t2220\t0\t20\t220\t2220\tNOCAPS\tALLDIGIT\t0\t1\t0\tNOPUNCT\tdddd\t0\t<dateList>";

        final String text = "1942-07-27, 19-12-1932, 12 February 1983, 18 January 2220";

        List<LayoutToken> layoutTokenTokenisation = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        List<Period> results = target.extractResults(output, layoutTokenTokenisation);

        assertThat(results, hasSize(1));
        assertThat(results.get(0).getType(), is(Period.Type.LIST));
        assertThat(results.get(0).getList(), hasSize(4));
        assertThat(results.get(0).getList().get(0).getRawDate(), is("1942-07-27"));
        assertThat(text.substring(results.get(0).getList().get(0).getOffsetStart(),
                results.get(0).getList().get(0).getOffsetEnd()), is(results.get(0).getList().get(0).getRawDate()));
        assertThat(results.get(0).getList().get(1).getRawDate(), is("19-12-1932"));
        assertThat(text.substring(results.get(0).getList().get(1).getOffsetStart(),
                results.get(0).getList().get(1).getOffsetEnd()), is(results.get(0).getList().get(1).getRawDate()));
        assertThat(results.get(0).getList().get(2).getRawDate(), is("12 February 1983"));
        assertThat(text.substring(results.get(0).getList().get(2).getOffsetStart(),
                results.get(0).getList().get(2).getOffsetEnd()), is(results.get(0).getList().get(2).getRawDate()));
        assertThat(results.get(0).getList().get(3).getRawDate(), is("18 January 2220"));
        assertThat(text.substring(results.get(0).getList().get(3).getOffsetStart(),
                results.get(0).getList().get(3).getOffsetEnd()), is(results.get(0).getList().get(3).getRawDate()));
    }

    @Test
    public void testCreateTrainingData() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("rawData.sample.test.txt");

        String output = target.createTrainingData(is);
        System.out.println(output);
    }


    // -- these are not unit tests... only used to develop the other unit tests

    @Test
    public void testProcess_integration() throws Exception {

        final String text = "------";
        List<Period> results = target.process(text);

        for(Period period : results) {
            System.out.println(period);
        }

        fail("To avoid committing by mistake.");
    }
}