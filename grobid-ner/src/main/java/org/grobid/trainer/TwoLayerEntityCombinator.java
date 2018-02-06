package org.grobid.trainer;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Entity;
import org.grobid.core.data.Paragraph;
import org.grobid.core.data.Sentence;
import org.grobid.core.data.TrainingDocument;
import org.grobid.core.engines.NEREnParser;
import org.grobid.core.engines.NERParser;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.lang.Language;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.trainer.stax.CustomEMEXFormatStaxHandler;
import org.grobid.trainer.stax.StaxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.grobid.core.engines.NERParserCommon.XML_OUTPUT_FOOTER;
import static org.grobid.core.engines.NERParserCommon.XML_OUTPUT_HEADER;

/**
 * This class combine files annotated with the largest entity match and add the second
 * layer of sub-annotations
 */
public class TwoLayerEntityCombinator {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TwoLayerEntityCombinator.class);

    private String nerCorpusPath;
    private String outputDirectory;
    private NERParser enParser;

    private WstxInputFactory inputFactory = new WstxInputFactory();

    public TwoLayerEntityCombinator() {

    }

    public TwoLayerEntityCombinator(String inputDirectory, String outputDirectory) {

        this.nerCorpusPath = inputDirectory;
        this.outputDirectory = outputDirectory;

        // read additional properties for this sub-project to get the paths to the resources
        Properties prop = new Properties();
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream("/grobid-ner.properties");
            prop.load(is);
        } catch (IOException ex) {
            throw new GrobidResourceException("An exception occurred when accessing/reading the grobid-ner property file.", ex);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static void main(String[] args) {
        GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList("../../grobid-home"));
        File grobidHome = grobidHomeFinder.findGrobidHomeOrFail();
        grobidHomeFinder.findGrobidPropertiesOrFail(grobidHome);

        GrobidProperties.getInstance(grobidHomeFinder);
        LibraryLoader.load();

        if (args.length < 2) {
            System.err.println("Usage Combinator [inputDirectory] [outputDirectory]");
            System.exit(-1);
        }

        String input = args[0];
        String output = args[1];

        if (!Files.exists(Paths.get(input))) {
            throw new GrobidResourceException("Corpus path " + input + " doesn't exists.");
        }

        if (!Files.exists(Paths.get(output))) {
            throw new GrobidResourceException("Output directory " + output + " doesn't exists.");
        }

        TwoLayerEntityCombinator combinator = new TwoLayerEntityCombinator(input, output);
        combinator.setEnParser(new NEREnParser());
        combinator.combine();

    }

    public int combine() {

        int totalExamples = 0;
        Writer outputWriter = null;
        try {
            System.out.println("sourcePathLabel: " + nerCorpusPath);
            totalExamples = processCorpus(nerCorpusPath, outputDirectory);

        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            IOUtils.closeQuietly(outputWriter);
        }

        return totalExamples;
    }

    private int processCorpus(String corpusPath, String outputDirectory) {
        int res = 0;
        Writer outputWriter = null;

        Collection<File> trainingFiles = FileUtils.listFiles(new File(corpusPath),
                new SuffixFileFilter("training.xml"), DirectoryFileFilter.INSTANCE);

        for (File trainingFile : trainingFiles) {
            System.out.println("Processing " + trainingFile.getAbsolutePath());
            try {
                InputStream resourceAsStream = new FileInputStream(trainingFile);
                XMLStreamReader2 reader = null;
                CustomEMEXFormatStaxHandler handler = new CustomEMEXFormatStaxHandler();

                reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(resourceAsStream);

                StaxUtils.traverse(reader, handler);

                List<TrainingDocument> documents = handler.getDocuments();

                String outputXml = constructXML(documents);
                outputWriter = new OutputStreamWriter(new FileOutputStream(outputDirectory + File.separator + generateNewFileName(trainingFile.getName())), StandardCharsets.UTF_8);
                outputWriter.append(outputXml);

            } catch (XMLStreamException | IOException e) {
                LOGGER.warn("The file " + trainingFile.getAbsolutePath() + " cannot be processed. Skipping it. ", e);
            } finally {
                IOUtils.closeQuietly(outputWriter);
            }
        }

        return res;
    }

    private String generateNewFileName(String trainingFile) {
        return FilenameUtils.removeExtension(trainingFile) + ".2layers.xml";
    }

    protected String constructXML(List<TrainingDocument> documents) {
        StringBuilder outputStringBuilder = new StringBuilder();
        for (TrainingDocument document : documents) {
            outputStringBuilder.append(XML_OUTPUT_HEADER);
            outputStringBuilder.append("\t\t<document name=\"" + generateNewFileName(document.getDocumentName()) + "\">\n");

            int p = 0;
            for (Paragraph paragraph : document.getParagraphs()) {
                final AtomicInteger cnt = new AtomicInteger(0);

                paragraph.getSentences()
                        .stream()
                        .forEach(s -> {
                            final GrobidAnalyzer analyzer = GrobidAnalyzer.getInstance();
                            s.setTokenisedValue(analyzer.tokenizeWithLayoutToken(s.getRawValue(), new Language(Language.EN, 1.0)));

                            int startSentence = cnt.getAndAdd(s.getRawValue().length());
                            s.setOffsetStart(startSentence);
                            s.setOffsetEnd(startSentence + s.getRawValue().length());
                        });

                String para = paragraph.getSentences()
                        .stream()
                        .map(e -> e.getRawValue())
                        .collect(Collectors.joining(""));


                paragraph.getSentences().stream().forEach(
                        s -> {
//                            System.out.println(s.getRawValue());
                            if (!s.getRawValue().equals(para.substring(s.getOffsetStart(), s.getOffsetEnd()))) {
                                LOGGER.warn("Error overlapping sentences in paragraphs -> " + s.getRawValue());
                            }
                        }
                );

                final List<Entity> entities = enParser.extractNE(para);


                List<Entity> mergeList = new ArrayList<>();

                //I rewrite the offsets before adding NER entities
                final List<Entity> userEntities = paragraph.getSentences().stream()
                        .flatMap(s -> {
                            List<Entity> copy = new ArrayList<>(s.getEntities());

                            copy.stream().forEach(e -> {
                                e.setOffsetStart(s.getOffsetStart() + e.getOffsetStart());
                                e.setOffsetEnd(s.getOffsetStart() + e.getOffsetEnd());
                            });

                            return copy.stream();
                        })
                        .sorted(Comparator.comparingInt(Entity::getOffsetStart))
                        .collect(Collectors.toList());

                // check
                userEntities.stream().forEach(
                        e -> {
                            if (!e.getRawName().equals(para.substring(e.getOffsetStart(), e.getOffsetEnd()))) {
                                LOGGER.warn("Error of entity overlapping in sentences/paragraph -> " + e);
                            }
                        }
                );

                mergeList.addAll(userEntities);
                mergeList.addAll(entities);

                //sorting
                //largest entities comes first:
                // order by startElement and if they are the same, the one with end element first
                // should come first
                mergeList.stream()
                        .sorted(Comparator
                                .comparingInt(Entity::getOffsetStart)
                                .thenComparing(Entity::getOffsetEnd))
                        .collect(Collectors.toList());


                // creating a reversed index with the offset start
                final Map<Integer, List<Entity>> offsetStartReverseMap = mergeList.stream()
                        .collect(Collectors.groupingBy(Entity::getOffsetStart));

                // creating a reversed index with the offset end
                final Map<Integer, List<Entity>> offsetEndReverseMap = mergeList.stream()
                        .collect(Collectors.groupingBy(Entity::getOffsetEnd));

                outputStringBuilder.append("\t\t\t<p xml:lang=\"" + paragraph.getLanguage() + "\" xml:id=\"" + paragraph.getId() + "\">\n");

                int sentenceIndex = 0;
                for (int s = 0; s < paragraph.getSentences().size(); s++) {

                    Sentence sentence = paragraph.getSentences().get(s);
                    int sentenceStart = sentence.getOffsetStart();
                    int sentenceEnd = sentence.getOffsetEnd();

                    outputStringBuilder.append("\t\t\t\t<sentence xml:id=\"" + sentence.getId() + "\">");
                    if (CollectionUtils.isEmpty(entities)) {
                        // don't forget to encode the text for XML
                        outputStringBuilder.append(TextUtilities.HTMLEncode(para.substring(sentenceStart, sentenceEnd)));
                        outputStringBuilder.append("</sentence>\n");
                        sentenceIndex++;
                        continue;
                    }

                    String sentenceText = para.substring(sentenceStart, sentenceEnd);
                    int sentenceBasedIndex = 0;

                    // small adjustment to avoid sentence starting with a space
//                    if (para.charAt(sentenceStart) == ' ') {
//                        sentenceStart++;
//                    }

                    StringBuilder sb = new StringBuilder();
                    for (int index = sentenceStart; index < sentenceEnd; index++) {
                        final List<Entity> entitiesByIndexStart = offsetStartReverseMap.get(index);
                        if (CollectionUtils.isNotEmpty(entitiesByIndexStart)) {
                            for (Entity entity : entitiesByIndexStart) {
                                if (entity.getOffsetEnd() <= sentenceEnd) {
                                    if (entity.getOrigin().equals(Entity.Origin.GROBID)) {
                                        if (entitiesByIndexStart.stream()
                                                .filter(entity1 -> entity1.getOffsetStart() == entity.getOffsetStart() &&
                                                        entity1.getOffsetEnd() == entity.getOffsetEnd() &&
                                                        entity1.getOrigin().equals(Entity.Origin.USER))
                                                .count() == 0) {
                                            sb.append("<ENAMEX subType=\"2\" type=\"" + entity.getType().getName() + "\">");
                                        }
                                    } else {
                                        sb.append("<ENAMEX type=\"" + entity.getType().getName() + "\">");
                                    }
                                }

                            }
                        }

                        if (CollectionUtils.isNotEmpty(offsetEndReverseMap.get(index))) {
                            final List<Entity> entitiesByIndexEnd = offsetEndReverseMap.get(index);
                            for (Entity entity : entitiesByIndexEnd) {
                                if (entity.getOffsetStart() >= sentenceStart) {
                                    if (entity.getOrigin().equals(Entity.Origin.GROBID)) {
                                        if (entitiesByIndexEnd.stream()
                                                .filter(entity1 -> entity1.getOffsetStart() == entity.getOffsetStart() &&
                                                        entity1.getOffsetEnd() == entity.getOffsetEnd() &&
                                                        entity1.getOrigin().equals(Entity.Origin.USER))
                                                .count() == 0) {
                                            sb.append("</ENAMEX>");
                                        }
                                    } else {
                                        sb.append("</ENAMEX>");
                                    }
                                }
                            }
                        }

                        sb.append(TextUtilities.HTMLEncode(String.valueOf(sentenceText.charAt(sentenceBasedIndex))));
                        sentenceBasedIndex++;
                    }
                    //Collecting all the entities finishing for the end of the sentence
                    if (CollectionUtils.isNotEmpty(offsetEndReverseMap.get(sentenceEnd))) {
                        final List<Entity> entitiesByIndexEnd = offsetEndReverseMap.get(sentenceEnd);
                        for (Entity entity : entitiesByIndexEnd) {
                            if (entity.getOffsetStart() >= sentenceStart) {
                                if (entity.getOrigin().equals(Entity.Origin.GROBID)) {
                                    if (entitiesByIndexEnd.stream()
                                            .filter(entity1 -> entity1.getOffsetStart() == entity.getOffsetStart() &&
                                                    entity1.getOffsetEnd() == entity.getOffsetEnd() &&
                                                    entity1.getOrigin().equals(Entity.Origin.USER))
                                            .count() == 0) {
                                        sb.append("</ENAMEX>");
                                    }
                                } else {
                                    sb.append("</ENAMEX>");
                                }
                            }
                        }
                    }

                    outputStringBuilder.append(sb.toString());
                    outputStringBuilder.append("</sentence>\n");
                    sentenceIndex++;

                }

                outputStringBuilder.append("\t\t\t</p>\n");


            }
            outputStringBuilder.append("\t\t</document>\n");
            outputStringBuilder.append(XML_OUTPUT_FOOTER);
        }

        return outputStringBuilder.toString();
    }

    public void setEnParser(NERParser enParser) {
        this.enParser = enParser;
    }
}
