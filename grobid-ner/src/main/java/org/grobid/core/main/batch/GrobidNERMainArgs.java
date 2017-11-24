package org.grobid.core.main.batch;

public class GrobidNERMainArgs extends GrobidMainArgs {
    // english is the default language
    private String lang = "en";
    private double confidenceThreshold = 0.0;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }
}