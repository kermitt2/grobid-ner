package org.grobid.trainer.evaluation;

import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class ConfusionMatrix {
    private static Logger LOGGER = LoggerFactory.getLogger(ConfusionMatrix.class);
    private final String OTHER_CLASS = "O";

    private int truePositives = 0; // true positive
    private int falsePositives = 0; // false positive
    private int trueNegatives = 0; // true negative
    private int falseNegatives = 0; // false negative

    private int nbToken = 0;

    /**
     * The format is a list of string of rows tab separated, where:
     * - first element is the token,
     * - second-last element is the expected class
     * - last element is the evaluated class
     */
    public void computeResults(List<String> results) {
        computeResults(results, null);
    }

    public void computeResults(List<String> results, Writer writer) {
        try {
            for (String result : results) {
                processRow(result, writer);
                nbToken++;
            }
        } catch (IOException e) {
            LOGGER.error("Cannot write evaluation output.", e);
        }
    }

    public void processRow(String result, Writer writer) throws IOException {
        String[] cells = result.split("\t");

        String expected = cells[cells.length - 2];
        String estimated = cells[cells.length - 1];

        if (estimated.equals(expected) && !expected.equals(OTHER_CLASS)) {
            truePositives++;
        } else if (estimated.equals(expected) && expected.equals(OTHER_CLASS)) {
            trueNegatives++;
        } else if (!estimated.equals(expected) && expected.equals(OTHER_CLASS)) {
            falsePositives++;
        } else {
            trueNegatives++;
        }
        if (writer != null) {
            writer.write(result.replace("\t", " ") + " " + estimated + "\n");
        }
    }

    public StringBuffer appendReportTo(StringBuffer report) {
        return report.append(getReport());
    }

    public String getReport() {
        String report = new String();

        report.concat("\n\n");
        report.concat("Total sentences: " + nbToken + "\n");
        report.concat("Total tokens: " + nbToken + "\n\n");

        report.concat("True Positive: " + truePositives + "\n");
        report.concat("False Positive: " + falsePositives + "\n");
        report.concat("True Negative: " + trueNegatives + "\n");
        report.concat("False Negative: " + falseNegatives + "\n");

        report.concat("\nToken level\n-----------\n");
        double precision = ((double) (truePositives)) / (truePositives + falsePositives);
        double recall = ((double) (truePositives)) / (truePositives + falseNegatives);
        double f1 = ((double) (2 * truePositives)) / (2 * truePositives + falsePositives + falseNegatives);

        report.concat("Precision: " + TextUtilities.formatTwoDecimals(precision * 100) + "\n");
        report.concat("Recall: " + TextUtilities.formatTwoDecimals(recall * 100) + "\n");
        report.concat("f1: " + TextUtilities.formatTwoDecimals(f1 * 100) + "\n");

        return report;
    }

    public int getTruePositives() {
        return truePositives;
    }

    public void setTruePositives(int truePositives) {
        this.truePositives = truePositives;
    }

    public int getFalsePositives() {
        return falsePositives;
    }

    public void setFalsePositives(int falsePositives) {
        this.falsePositives = falsePositives;
    }

    public int getTrueNegatives() {
        return trueNegatives;
    }

    public void setTrueNegatives(int trueNegatives) {
        this.trueNegatives = trueNegatives;
    }

    public int getFalseNegatives() {
        return falseNegatives;
    }

    public void setFalseNegatives(int falseNegatives) {
        this.falseNegatives = falseNegatives;
    }
}
