package org.grobid.core.utilities;

import org.grobid.core.utilities.GrobidConfig.ModelParameters;
import java.util.*;

public class GrobidNerConfiguration {

    public String grobidHome;
    public String reutersPaths;
    public String reutersConllPath;
    public String reutersIdiliaPath;
    public String extraCorpus;
    public String leMondeCorpusPath;
    public String wapitiExecPath;

    // sequence labeling models
    public List<ModelParameters> models;

    public String getGrobidHome() {
        return this.grobidHome;
    }

    public void setGrobidHome(String grobidHome) {
        this.grobidHome = grobidHome;
    }

    public String getReutersPaths() {
        return this.reutersPaths;
    }

    public void setReutersPaths(String reutersPaths) {
        this.reutersPaths = reutersPaths;
    }

    public String getReutersConllPath() {
        return this.reutersConllPath;
    }

    public void setReutersConllPath(String reutersConllPath) {
        this.reutersConllPath = reutersConllPath;
    }

    public String getReutersIdiliaPath() {
        return this.reutersIdiliaPath;
    }

    public void setReutersIdiliaPath(String reutersIdiliaPath) {
        this.reutersIdiliaPath = reutersIdiliaPath;
    }

    public String getExtraCorpus() {
        return this.extraCorpus;
    }

    public void setExtraCorpus(String extraCorpus) {
        this.extraCorpus = extraCorpus;
    }

    public String getLeMondeCorpusPath() {
        return this.leMondeCorpusPath;
    }

    public void setLeMondeCorpusPath(String leMondeCorpusPath) {
        this.leMondeCorpusPath = leMondeCorpusPath;
    }

    public String getWapitiExecPath() {
        return this.wapitiExecPath;
    }

    public void setWapitiExecPath(String wapitiExecPath) {
        this.wapitiExecPath = wapitiExecPath;
    }

    public List<ModelParameters> getModels() {
        return this.models;
    }

    public void getModels(List<ModelParameters> models) {
        this.models = models;
    }

    //wapitiExec: "/home/lopez/Wapiti/build/wapiti_runner"

}
