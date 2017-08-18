package org.grobid.core.data.dates;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Period {
    public enum Type {
        INTERVAL("interval"),
        VALUE("value"),
        LIST("list");

        private String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Type typeFor(String type) {
            for (Type element : Type.values()) {
                if (StringUtils.equalsIgnoreCase(type, element.toString())) {
                    return element;
                }
            }
            return null;
        }

        public String toString() {
            return getName();
        }

    }

    private Type type;
    private DateWrapper value;
    private DateWrapper fromDate;
    private DateWrapper toDate;
    private String rawText;

    public List<DateWrapper> getList() {
        return list;
    }

    private List<DateWrapper> list = new ArrayList<>();

    // Interval
    public Period(DateWrapper fromDate, DateWrapper toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.type = Type.INTERVAL;
    }

    // Value
    public Period(DateWrapper when) {
        this.value = when;
        this.type = Type.VALUE;
    }

    // list
    public Period(List<DateWrapper> list) {
        this.list = list;
        this.type = Type.LIST;
    }

    public DateWrapper getValue() {
        return value;
    }

    public void setValue(DateWrapper value) {
        this.value = value;
        this.setType(Type.VALUE);
    }

    public DateWrapper getFromDate() {
        return fromDate;
    }

    public void setFromDate(DateWrapper fromDate) {
        this.fromDate = fromDate;
        this.setType(Period.Type.INTERVAL);
    }

    public DateWrapper getToDate() {
        return toDate;
    }

    public void setToDate(DateWrapper toDate) {
        this.toDate = toDate;
        this.setType(Period.Type.INTERVAL);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Period() {
    }

    public void addDate(DateWrapper date) {
        this.list.add(date);
        this.type = Type.LIST;
    }

    public String toString() {
        switch (type) {
            case VALUE:
                return "" + getValue();

            case INTERVAL:
                String out = "";
                if (fromDate != null) {
                    out += "From: " + fromDate;
                    out += " ";
                }

                if (toDate != null) {
                    out += "to: " + toDate;
                }

                return out;

            case LIST:
                return Arrays.toString(list.toArray());

        }

        return null;
    }

    public String toXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<measure type=\"" + type + "\">");

        switch (type) {
            case VALUE:
                sb.append("<date>" + value + "</date>");
                break;

            case INTERVAL:
                if (fromDate != null) {
                    sb.append("<date>" + fromDate + "</date>");
                }

                if (toDate != null) {
                    sb.append("<date>" + toDate + "</date>");
                }
                break;

            case LIST:
                for(DateWrapper date : list) {
                    sb.append("<date>" + date.getRawDate() + "</date>");
                }
                break;
        }
        return sb.toString();
    }
    

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
}
