package org.openimaj.text.nlp.language;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.junit.Test;

/**
 * Test the markov chain language model
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class MarkovChainLanguageModelTest {
	
	@Test
	public void testGenerate() throws UnsupportedEncodingException{
		MarkovChainLanguageModel model = new MarkovChainLanguageModel();
		model.train(Locale.ENGLISH, "This is an english sentence", "UTF-8");
		System.out.println(model.generate(Locale.ENGLISH, 100, "UTF-8"));
	}
	
	@Test
	public void testGenerateWarAndPeace() throws IOException{
		MarkovChainLanguageModel model = new MarkovChainLanguageModel();
		model.train(Locale.ENGLISH, MarkovChainLanguageModel.class.getResourceAsStream("/org/openimaj/text/nlp/warandpeace.txt"));
		model.train(Locale.CHINESE, MarkovChainLanguageModel.class.getResourceAsStream("/org/openimaj/text/nlp/TouPengHsienHua.txt"));
		System.out.println(model.generate(Locale.ENGLISH, 1000, "UTF-8"));
		System.out.println(model.generate(Locale.CHINESE, 10000, "UTF-8"));
		
	}
}
