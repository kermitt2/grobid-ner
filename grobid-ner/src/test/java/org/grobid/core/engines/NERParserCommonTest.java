package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Entity;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.grobid.core.lexicon.NERLexicon.NER_Type.LOCATION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class NERParserCommonTest {

    NERParserCommon target;

    @Before
    public void setUp() {
        target = new NERParserCommon();
    }


    @Test
    public void testresultExtraction_clusteror_simple() throws Exception {
        final String input = "Austria fought the enemies with Germany.";
        String result = "Austria\taustria\tA\tAu\tAus\tAust\tAustr\ta\tia\tria\ttria\tstria\tINITCAP\tNODIGIT\t0\t0\t0\t0\t1\t0\t0\t1\t1\t1\t1\tXxxx\tXx\t0\tB-UNKNOWN\n" +
                "fought\tfought\tf\tfo\tfou\tfoug\tfough\tt\tht\tght\tught\tought\tNOCAPS\tNODIGIT\t0\t1\t0\t0\t0\t0\t0\t0\t0\t0\t0\txxxx\tx\t0\tO\n" +
                "the\tthe\tt\tth\tthe\tthe\tthe\te\the\tthe\tthe\tthe\tNOCAPS\tNODIGIT\t0\t1\t0\t0\t0\t0\t0\t0\t0\t0\t0\txxx\tx\t0\tO\n" +
                "enemies\tenemies\te\ten\tene\tenem\tenemi\ts\tes\ties\tmies\temies\tNOCAPS\tNODIGIT\t0\t1\t0\t0\t0\t0\t0\t0\t0\t0\t0\txxxx\tx\t0\tO\n" +
                "with\twith\tw\twi\twit\twith\twith\th\tth\tith\twith\twith\tNOCAPS\tNODIGIT\t0\t1\t0\t0\t0\t0\t0\t0\t0\t0\t0\txxxx\tx\t0\tO\n" +
                "Germany\tgermany\tG\tGe\tGer\tGerm\tGerma\ty\tny\tany\tmany\trmany\tINITCAP\tNODIGIT\t0\t0\t0\t0\t1\t0\t0\t0\t0\t0\t0\tXxxx\tXx\t0\tB-LOCATION\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t.\t.\t0\tO";
        List<LayoutToken> tokenisation = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);


        final List<Entity> entities = target.resultExtraction(GrobidModels.ENTITIES_NER, result, tokenisation);

        assertThat(entities, hasSize(2));

        final Entity entity0 = entities.get(0);
        assertThat(entity0.getRawName(), is("Austria"));
        assertThat(entity0.getOffsetStart(), is(0));
        assertThat(entity0.getOffsetEnd(), is(7));

        final Entity entity1 = entities.get(1);
        assertThat(entity1.getRawName(), is("Germany"));
        assertThat(entity1.getOffsetStart(), is(32));
        assertThat(entity1.getOffsetEnd(), is(39));
    }

    @Test
    public void testresultExtraction_clusteror_simple2() throws Exception {
        final String input = "Austria Hungary fought the enemies with Germany.";
        String result = "Austria\taustria\tA\tAu\tAus\tAust\tAustr\ta\tia\tria\ttria\tstria\tINITCAP\tNODIGIT\t0\t0\t0\t0\t1\t0\t0\t1\t1\t1\t1\tXxxx\tXx\t0\tB-LOCATION\n" +
                "Hungary\thungary\tA\tAu\tAus\tAust\tAustr\ta\tia\tria\ttria\tstria\tINITCAP\tNODIGIT\t0\t0\t0\t0\t1\t0\t0\t1\t1\t1\t1\tXxxx\tXx\t0\tLOCATION\n" +
                "fought\tfought\tf\tfo\tfou\tfoug\tfough\tt\tht\tght\tught\tought\tNOCAPS\tNODIGIT\t0\t1\t0\t0\t0\t0\t0\t0\t0\t0\t0\txxxx\tx\t0\tO\n" +
                "the\tthe\tt\tth\tthe\tthe\tthe\te\the\tthe\tthe\tthe\tNOCAPS\tNODIGIT\t0\t1\t0\t0\t0\t0\t0\t0\t0\t0\t0\txxx\tx\t0\tO\n" +
                "enemies\tenemies\te\ten\tene\tenem\tenemi\ts\tes\ties\tmies\temies\tNOCAPS\tNODIGIT\t0\t1\t0\t0\t0\t0\t0\t0\t0\t0\t0\txxxx\tx\t0\tO\n" +
                "with\twith\tw\twi\twit\twith\twith\th\tth\tith\twith\twith\tNOCAPS\tNODIGIT\t0\t1\t0\t0\t0\t0\t0\t0\t0\t0\t0\txxxx\tx\t0\tO\n" +
                "Germany\tgermany\tG\tGe\tGer\tGerm\tGerma\ty\tny\tany\tmany\trmany\tINITCAP\tNODIGIT\t0\t0\t0\t0\t1\t0\t0\t0\t0\t0\t0\tXxxx\tXx\t0\tB-LOCATION\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t.\t.\t0\tO";
        List<LayoutToken> tokenisation = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);


        final List<Entity> entities = target.resultExtraction(GrobidModels.ENTITIES_NER, result, tokenisation);

        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).getRawName(), is("Austria Hungary"));
        assertThat(entities.get(0).getType(), is(LOCATION));
        assertThat(entities.get(0).getOffsetStart(), is(0));
        assertThat(entities.get(0).getOffsetEnd(), is(15));
        assertThat(input.substring(entities.get(0).getOffsetStart(), entities.get(0).getOffsetEnd()), is("Austria Hungary"));
    }

    /**
     * Testing the resultExtraction old method
     **/
    @Test
    public void testResultExtraction_oldMethod_simple() throws Exception {
        final String input = "Austria fought the enemies with Germany.";

        List<LayoutToken> tokenisation = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        List<Pair<String, String>> labels = new ArrayList<Pair<String, String>>();

        labels.add(new Pair<String, String>("Austria", "B-LOCATION"));
        labels.add(new Pair<String, String>("fought", "O"));
        labels.add(new Pair<String, String>("the", "O"));
        labels.add(new Pair<String, String>("enemies", "O"));
        labels.add(new Pair<String, String>("with", "O"));
        labels.add(new Pair<String, String>("Germany", "B-LOCATION"));

        final List<Entity> entities = target.resultExtraction(input, labels, tokenisation);

//        printEntitiesInfo(entities);

        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).getRawName(), is("Austria"));
        assertThat(entities.get(0).getType(), is(LOCATION));
        assertThat(entities.get(0).getOffsetStart(), is(0));
        assertThat(entities.get(0).getOffsetEnd(), is(7));
        assertThat(input.substring(entities.get(0).getOffsetStart(), entities.get(0).getOffsetEnd()), is("Austria"));

        assertThat(entities.get(1).getRawName(), is("Germany"));
        assertThat(entities.get(1).getType(), is(LOCATION));
        assertThat(entities.get(1).getOffsetStart(), is(32));
        assertThat(entities.get(1).getOffsetEnd(), is(39));
        assertThat(input.substring(entities.get(1).getOffsetStart(), entities.get(1).getOffsetEnd()), is("Germany"));
    }


    /**
     * Testing the resultExtraction old method
     **/
    @Test
    public void testResultExtraction_OldMethod_simple2() throws Exception {
        final String input = "Austria Hungary fought the enemies with Germany.";

        List<LayoutToken> tokenisation = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        List<Pair<String, String>> labels = new ArrayList<Pair<String, String>>();

        labels.add(new Pair<String, String>("Austria", "B-LOCATION"));
        labels.add(new Pair<String, String>("Hungary", "LOCATION"));
        labels.add(new Pair<String, String>("fought", "O"));
        labels.add(new Pair<String, String>("the", "O"));
        labels.add(new Pair<String, String>("enemies", "O"));
        labels.add(new Pair<String, String>("with", "O"));
        labels.add(new Pair<String, String>("Germany", "B-LOCATION"));

        final List<Entity> entities = target.resultExtraction(input, labels, tokenisation);

//        printEntitiesInfo(entities);

        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).getRawName(), is("Austria Hungary"));
        assertThat(entities.get(0).getType(), is(LOCATION));
        assertThat(entities.get(0).getOffsetStart(), is(0));
        assertThat(entities.get(0).getOffsetEnd(), is(15));
        assertThat(input.substring(entities.get(0).getOffsetStart(), entities.get(0).getOffsetEnd()), is("Austria Hungary"));
    }
}