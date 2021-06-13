package org.grobid.core.utilities;

import org.grobid.core.utilities.GrobidConfig.ModelParameters;
import java.util.*;
import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrobidNerConfiguration {

    private static Logger LOGGER = LoggerFactory.getLogger(GrobidNerConfiguration.class);

    public String grobidHome;
    public String reutersPaths;
    public String reutersConllPath;
    public String reutersIdiliaPath;
    public String extraCorpus;
    public String leMondeCorpusPath;
    public String wapitiExecPath;

    public GrobidNerConfiguration getInstance() {
        return getInstance(null);
    }

    public static GrobidNerConfiguration getInstance(String projectRootPath) {

        GrobidNerConfiguration grobidNerConfiguration = null;
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            if (projectRootPath == null)
                grobidNerConfiguration = mapper.readValue(new File("resources/config/grobid-ner.yaml"), GrobidNerConfiguration.class);
            else
                grobidNerConfiguration = mapper.readValue(new File(projectRootPath + "/resources/config/grobid-ner.yaml"), GrobidNerConfiguration.class);
        } catch(Exception e) {
            LOGGER.error("The config file does not appear valid, see resources/config/grobid-astro.yaml", e);
        }
        return grobidNerConfiguration;
    }

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
