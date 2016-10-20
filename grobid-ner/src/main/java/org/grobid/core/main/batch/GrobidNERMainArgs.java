package org.grobid.core.main.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class GrobidNERMainArgs extends GrobidMainArgs {
	// english is the default language
	public String lang = "en"; 

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
}