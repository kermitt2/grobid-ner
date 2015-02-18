package org.grobid.core.test;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.grobid.core.data.Entity;
import org.grobid.core.lexicon.NERLexicon;
import org.grobid.core.engines.NERParser;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.trainer.AssembleNERCorpus;
import org.grobid.trainer.sax.ReutersSaxHandler;
import org.grobid.trainer.sax.SemDocSaxHandler;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  @author Patrice Lopez
 */
//@Ignore
public class TestAssembler {

	public File getResourceDir(String resourceDir) {
		File file = new File(resourceDir);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
			}
		}
		return(file);
	}
		
	@Test
	public void testAssembler() throws Exception {
		File reutersFile = 
			new File(this.getResourceDir("./src/test/resources/").getAbsoluteFile()+"/100100newsML.xml");
		if (!reutersFile.exists()) {
			throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
		}
		
		File semdocFile = 
			new File(this.getResourceDir("./src/test/resources/").getAbsoluteFile()+"/100100newsML.semdoc.xml");
		if (!semdocFile.exists()) {
			throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
		}
		
		ReutersSaxHandler reutersSax = new ReutersSaxHandler();
		
		// get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setFeature("http://xml.org/sax/features/namespaces", false);
        spf.setFeature("http://xml.org/sax/features/validation", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		//get a new instance of parser
        SAXParser p = spf.newSAXParser();
        p.parse(reutersFile, reutersSax);
		
		SemDocSaxHandler semdocSax = new SemDocSaxHandler(reutersSax.getTextVector());
		
        p = spf.newSAXParser();
        p.parse(semdocFile, semdocSax);
        
		if (semdocSax.getAnnotatedTextVector() != null) {
			//System.out.println(semdocSax.getAnnotatedTextVector().size());
		}
		//assertEquals(339, semdocSax.getAnnotatedTextVector().size());
		assertEquals(243, semdocSax.getAnnotatedTextVector().size());
	}
	
	@Test
	public void testRetokenize() throws Exception {
		List<String> tokens = new ArrayList<String>();
		tokens.add("around");
		tokens.add(" ");
		tokens.add("10");
		tokens.add(",");
		tokens.add("000");
		System.out.println(tokens);
		List<String> tokens2 = ReutersSaxHandler.retokenize(tokens);
		System.out.println(tokens2);
		
		tokens = new ArrayList<String>();
		tokens.add("10");
		tokens.add(",");
		tokens.add("000");
		tokens.add(",");
		tokens.add("000");
		tokens.add(".");
		tokens.add("00");
		tokens.add(" ");
		tokens.add("errors");
		System.out.println(tokens);
		tokens2 = ReutersSaxHandler.retokenize(tokens);
		System.out.println(tokens2);
	}
}