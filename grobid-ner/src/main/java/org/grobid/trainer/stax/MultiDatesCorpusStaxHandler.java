package org.grobid.trainer.stax;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Date;
import org.grobid.core.data.dates.DateWrapper;
import org.grobid.core.data.dates.Period;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.apache.commons.lang3.StringUtils.*;
import static org.grobid.core.data.dates.Period.Type.VALUE;
import static org.grobid.core.engines.label.TemporalExpressionLabels.*;

public class MultiDatesCorpusStaxHandler implements StaxParserContentHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(MultiDatesCorpusStaxHandler.class);

    private List<Period> periods;

    private List<Pair<String, String>> data;

    private Period currentPeriod = null;

    private DateWrapper currentDate = null;
    private String currentTag = DATE_OTHER_LABEL;
    private boolean inDateTag = false;

    private boolean inMeasureTag = false;
    private GrobidAnalyzer analyzer = GrobidAnalyzer.getInstance();

    public MultiDatesCorpusStaxHandler() {
        this.periods = new ArrayList<>();
        this.data = new ArrayList<>();
    }

    @Override
    public void onStartDocument(XMLStreamReader2 reader) {

    }

    @Override
    public void onEndDocument(XMLStreamReader2 reader) {

    }

    @Override
    public void onStartElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();

        if ("measure".equals(localName)) {
            this.inMeasureTag = true;
            currentPeriod = new Period();
            String type = reader.getAttributeValue("", "type");
            if (isEmpty(type)) {
                currentPeriod.setType(VALUE);
            } else {
                currentPeriod.setType(Period.Type.typeFor(type));
                if (currentPeriod.getType() == null) {
                    LOGGER.warn("Type " + type + " is not an accepted value.");
                }
            }

        } else if ("date".equals(localName)) {
            inDateTag = true;
            switch (this.currentPeriod.getType()) {
                case VALUE:
                    String when = reader.getAttributeValue("", "when");
                    DateWrapper wrapped = new DateWrapper();
                    if (isNotBlank(when)) {
                        wrapped = parseIsoDate(when);
                    }

                    currentPeriod.setValue(wrapped);
                    currentDate = wrapped;
                    currentTag = DATE_VALUE_LABEL;
                    break;
                case LIST:
                    String when2 = reader.getAttributeValue("", "when");
                    DateWrapper wrap = new DateWrapper();
                    if(isNotBlank(when2)) {
                        wrap = parseIsoDate(when2);
                    }
                    
                    currentPeriod.addDate(wrap);
                    currentDate = wrap;
                    currentTag = DATE_VALUE_LIST_LABEL;

                    break;
                case INTERVAL:
                    String type = reader.getAttributeValue("", "type");
                    
                    String fromIso = reader.getAttributeValue("", "from-iso");
                    if (isNotBlank(fromIso) || StringUtils.equals(type,"from"))  {
                        DateWrapper wrappedFrom = new DateWrapper();
                        if(isNotBlank(fromIso)) {
                            wrappedFrom = parseIsoDate(fromIso);
                        }
                        currentPeriod.setFromDate(wrappedFrom);
                        currentDate = wrappedFrom;
                        currentTag = DATE_INTERVAL_FROM_LABEL;
                        break;
                    }

                    String toIso = reader.getAttributeValue("", "to-iso");
                    if (isNotBlank(toIso) || StringUtils.equals(type, "to")) {
                        DateWrapper wrappedTo = new DateWrapper();
                        if(isNotBlank(fromIso)) {
                            wrappedTo = parseIsoDate(toIso);
                        }

                        currentPeriod.setToDate(wrappedTo);
                        currentDate = wrappedTo;
                        currentTag = DATE_INTERVAL_TO_LABEL;
                        break;
                    }

                    throw new GrobidException("The input tag <data> miss either the attributes @from-iso, @to-iso or the @type={from, to}");
                default:
                    LOGGER.warn("The type " + this.currentPeriod.getType() + " is inconsistent.");
            }
        }
    }

    private DateWrapper parseIsoDate(String when) {
        if (when == null) {
            throw new GrobidException("Inconsistency in the training data, " + currentPeriod.getType());
        }
        DateWrapper wrapped = new DateWrapper();

        LocalDate date = LocalDate.parse(when, ISO_LOCAL_DATE);
        final Date grobidDate = new Date();
        grobidDate.setDay(date.getDayOfMonth());
        grobidDate.setMonth(date.getMonthValue());
        grobidDate.setYear(date.getYear());
        wrapped.setIsoDate(grobidDate);
        return wrapped;
    }

    @Override
    public void onEndElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();

        if ("measure".equals(localName)) {
            inMeasureTag = false;
            periods.add(currentPeriod);
            currentPeriod = null;
            currentTag = DATE_OTHER_LABEL;
            data.add(new Pair("@newline", null));
        } else if ("date".equals(localName)) {
            inDateTag = false;
            currentDate = null;
            currentTag = DATE_OTHER_LABEL;
        }
    }

    @Override
    public void onCharacter(XMLStreamReader2 reader) {
        String text = reader.getText();

        if (inDateTag) {
            if (isEmpty(text)) {
                return;
            }

            switch (this.currentPeriod.getType()) {
                case LIST:
                case INTERVAL:
                case VALUE:
                    currentDate.setRawDate(text);
                    break;
                default:
                    LOGGER.warn("The type " + this.currentPeriod.getType() + " is inconsistent.");
            }
        }

        List<String> tokens = analyzer.tokenize(text);
        boolean first = true;
        for (String token : tokens) {
            if (isBlank(token)) {
                continue;
            }

            if (first && !DATE_OTHER_LABEL.equals(currentTag)) {
                data.add(new Pair<>(token, GenericTaggerUtils.START_ENTITY_LABEL_PREFIX + currentTag));
                first = false;
            } else {
                data.add(new Pair<>(token, currentTag));
            }
        }

    }

    @Override
    public void onAttribute(XMLStreamReader2 reader) {

    }

    public List<Period> getPeriods() {
        return periods;
    }

    public List<Pair<String, String>> getData() {
        return data;
    }
}
