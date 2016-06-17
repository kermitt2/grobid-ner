package org.grobid.ner.core.test;

import org.apache.commons.io.FileUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.ner.core.data.Entity;
import org.grobid.ner.core.engines.NERParser;
import org.grobid.ner.core.lexicon.NERLexicon;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 *  @author Patrice Lopez
 */
//@Ignore
public class TestNERParser extends EngineTest {

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
	public void testNERLexicon() throws Exception {
		NERLexicon lexicon = NERLexicon.getInstance();
		// make some test here ...
		
	}
		
	@Test
	public void testNERParser() throws Exception {
		File textFile = 
			new File(this.getResourceDir("./src/test/resources/").getAbsoluteFile()+"/test.txt");
		if (!textFile.exists()) {
			throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
		}
		String text = FileUtils.readFileToString(textFile);	
		NERParser parser = new NERParser();
		
		List<Entity> entities = parser.extractNE(text);

		System.out.println("\n" + text);
		if (entities != null) {
			for(Entity entity : entities) {
				System.out.print(text.substring(entity.getOffsetStart(), entity.getOffsetEnd()) + "\t");
				System.out.println(entity.toString());
			}
		}
		else {
			System.out.println("No entity found.");
		}
		System.out.println("\n");
	}
	
}