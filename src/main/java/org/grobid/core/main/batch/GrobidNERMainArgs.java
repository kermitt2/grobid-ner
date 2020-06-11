package org.grobid.core.main.batch;

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