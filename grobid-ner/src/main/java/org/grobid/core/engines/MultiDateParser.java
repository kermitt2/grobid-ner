package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.dates.DateWrapper;
import org.grobid.core.data.dates.Period;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.features.FeaturesVectorMultiDates;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.GrobidHome;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.trainer.MultiDateTrainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.grobid.core.engines.Models.MULTI_DATE;
import static org.grobid.core.engines.label.MultiDateLabels.*;

public class MultiDateParser extends AbstractParser {
    private static Logger LOGGER = LoggerFactory.getLogger(MultiDateParser.class);

    public MultiDateParser() {
        super(MULTI_DATE);
    }

    public List<Period> process(String text) {
        GrobidAnalyzer analyser = GrobidAnalyzer.getInstance();
        final List<LayoutToken> layoutTokens = analyser.tokenizeWithLayoutToken(text);

        return process(layoutTokens);
    }

    public List<Period> process(List<LayoutToken> tokens) {
        List<LayoutToken> cleanTokens = stripSpacesInPreviousToken(tokens);

        StringBuilder sb = new StringBuilder();
        for (LayoutToken token : cleanTokens) {
            FeaturesVectorMultiDates vector = FeaturesVectorMultiDates.addFeatures(token.getText(), null);
            sb.append(vector.printVector());
        }

        String results = label(sb.toString());

        List<Period> data = extractResults(results, tokens);

        return data;
    }

    public List<Period> extractResults(String results, List<LayoutToken> tokenisation) {
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(Models.MULTI_DATE, results, tokenisation);
        List<TaggingTokenCluster> clusters = clusteror.cluster();
        List<Period> list = new ArrayList<>();

        Period currentPeriod = null;
        int pos = 0; // position in term of characters for creating the offsets

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i(clusterLabel);

            List<LayoutToken> theTokens = cluster.concatTokens();
            final String clusterText = LayoutTokensUtil.toText(cluster.concatTokens());
            String clusterContent = LayoutTokensUtil.normalizeText(clusterText);
            final int offsetStart = theTokens.get(0).getOffset();
            final int offsetEnd = offsetStart + clusterContent.length();

            if (clusterLabel.equals(DATE_VALUE)) {
                final DateWrapper when = new DateWrapper(clusterContent.toString());
                when.setOffsetPosition(new OffsetPosition(offsetStart, offsetEnd));
                list.add(new Period(when));
            } else if (clusterLabel.equals(DATE_INTERVAL_FROM)) {
                final DateWrapper fromDate = new DateWrapper(clusterContent.toString());
                fromDate.setOffsetPosition(new OffsetPosition(offsetStart, offsetEnd));
                if (currentPeriod == null) {
                    currentPeriod = new Period();
                    list.add(currentPeriod);
                    currentPeriod.setFromDate(fromDate);
                } else if (currentPeriod.getToDate() != null) {
                    currentPeriod.setFromDate(fromDate);
                    currentPeriod = null;
                }
            } else if (clusterLabel.equals(DATE_INTERVAL_TO)) {
                final DateWrapper toDate = new DateWrapper(clusterContent.toString());
                toDate.setOffsetPosition(new OffsetPosition(offsetStart, offsetEnd));
                if (currentPeriod == null) {
                    currentPeriod = new Period();
                    list.add(currentPeriod);
                    currentPeriod.setToDate(toDate);
                } else if (currentPeriod.getFromDate() != null) {
                    currentPeriod.setToDate(toDate);
                    currentPeriod = null;
                }
            } else if (clusterLabel.equals(DATE_VALUE_LIST)) {
                if (currentPeriod == null) {
                    currentPeriod = new Period();
                    list.add(currentPeriod);
                }
                final DateWrapper date = new DateWrapper(clusterContent.toString());
                date.setOffsetPosition(new OffsetPosition(offsetStart, offsetEnd));
                currentPeriod.addDate(date);

            } else if (clusterLabel.equals(DATE_OTHER)) {
                // nothing to be done at this stage... and maybe forever :-)
            } else {
                LOGGER.error("Warning: unexpected figure model label - " + clusterLabel + " for " + clusterContent);
            }
        }

        return list;
    }

    protected List<LayoutToken> toLayoutToken(List<String> tokenizedInput) {
        List<LayoutToken> layoutTokens = new ArrayList();

        if (tokenizedInput.size() == 0)
            return layoutTokens;


        for (String token : tokenizedInput) {
            layoutTokens.add(new LayoutToken(token));
        }

        return layoutTokens;
    }

    /**
     * Remove token with space and new lines, and encode the information in the n-1 token.
     **/
    protected List<LayoutToken> stripSpacesInPreviousToken(List<LayoutToken> layoutTokens) {
        List<LayoutToken> newList = new ArrayList<>();

        LayoutToken previousToken = new LayoutToken();
        for (LayoutToken tok : layoutTokens) {

            //Embedding in the previous token the information
            if (tok.getText().equals(" ")) {
                previousToken.setSpaceAfter(true);
            } else if (tok.getText().equals("\n")) {
                previousToken.setNewLineAfter(true);
            } else {
                newList.add(tok);
            }

            previousToken = tok;

        }

        return newList;
    }


    public int createTraining(String inputDirectory, String outputDirectory) throws IOException {
        try {
            File pathIn = new File(inputDirectory);
            if (!pathIn.exists()) {
                throw new GrobidException("Cannot create training data because input directory can not be accessed: " + inputDirectory);
            }

            File pathOut = new File(outputDirectory);
            if (!pathOut.exists()) {
                throw new GrobidException("Cannot create training data because output directory can not be accessed: " + outputDirectory);
            }

            Collection<File> corpus = FileUtils.listFiles(pathIn, new SuffixFileFilter(".txt"), null);

            if (isEmpty(corpus)) {
                throw new IllegalStateException("Folder " + pathIn.getAbsolutePath()
                        + " does not seem to contain training data. Please check");
            }

            LOGGER.info(corpus.size() + " files to be processed.");

            for (File file : corpus) {
                try {
                    String pathTEI = outputDirectory + File.separator + file.getName().substring(0, file.getName().length() - 4) + ".training.tei.xml";
                    InputStream is = new FileInputStream(file);
                    FileUtils.writeStringToFile(new File(pathTEI), createTrainingData(is), UTF_8);
                } catch (Exception exp) {
                    LOGGER.error("An error occurred while processing the following file: "
                            + file.getPath(), exp);
                }
            }

            return corpus.size();
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    protected String createTrainingData(InputStream is) throws IOException {
        StringBuilder output = new StringBuilder();

        output.append("<dates>");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is, UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                line = trim(line);
                output.append(toXml(line));
            }
        }
        output.append("</dates>");
        return output.toString();
    }

    private String toXml(String line) {
        StringBuilder output = new StringBuilder();
        int pos = 0;

        if (StringUtils.isEmpty(line)) {
            return output.toString();
        }
        List<Period> periods = process(line);
        for (Period period : periods) {
            output.append("<measure type=\"" + period.getType() + "\">");
            switch (period.getType()) {
                case VALUE:
                    int start = period.getValue().getOffsetStart();
                    if (pos < start) {
                        final String substring = line.substring(pos, start);
                        output.append(substring);
                        pos += substring.length();
                    }
                    if (pos == start) {
                        output.append("<date>" + period.getValue().getRawDate() + "</date>");
                        int end = period.getValue().getOffsetEnd();
                        pos = end;
                    }
                    break;
                case INTERVAL:
                    int startInterval = 0;
                    if (period.getFromDate() != null) {
                        startInterval = period.getFromDate().getOffsetStart();
                    } else if (period.getToDate() != null) {
                        startInterval = period.getToDate().getOffsetStart();
                    }

                    if (pos < startInterval) {
                        final String substring = line.substring(pos, startInterval);
                        output.append(substring);
                        pos += substring.length();
                    }
                    if(pos == startInterval) {
                        if (period.getFromDate() != null) {
                            output.append("<date type=\"from\">" + period.getFromDate().getRawDate() + "</date>");
                            pos += period.getFromDate().getRawDate().length();
                        }
                        if (period.getToDate() != null) {
                            final int start1 = period.getToDate().getOffsetStart();
                            if (pos < start1) {
                                final String substring = line.substring(pos, start1);
                                output.append(substring);
                                pos += substring.length();
                            }
                            output.append("<date type=\"to\">" + period.getToDate().getRawDate() + "</date>");
                            pos += period.getToDate().getRawDate().length();
                        }

                    }
                    break;

                case LIST:
                    for (DateWrapper wrapper : period.getList()) {
                        int start2 = wrapper.getOffsetStart();
                        if (pos < start2) {
                            final String substring = line.substring(pos, start2);
                            output.append(substring);
                            pos += substring.length();
                        }

                        if (pos == start2) {
                            output.append("<date>" + wrapper.getRawDate() + "</date>");
                            int end = wrapper.getOffsetEnd();
                            pos = end;
                        }
                    }
                    break;
            }
            output.append("</measure>");
        }


        if (pos < line.length()) {
            output.append(line.substring(pos, line.length()));
        }


        output.append("\n");

        return output.toString();
    }

    public static void main(String... args) throws Exception {
        String input = args[0];
        String output = args[1];


        GrobidHome.findGrobidHome();
        LibraryLoader.load();
        GrobidProperties.getInstance();
        MockContext.setInitialContext();


        MultiDateParser parser = new MultiDateParser();
        parser.createTraining(input, output);

        MockContext.destroyInitialContext();

    }

}
