package org.openimaj.tools.twitter.modes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;

/**
 * The mode of the twitter processing tool
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public enum TwitterPreprocessingModeOption implements CmdLineOptionsProvider {
	/**
	 * Tokenise the tweet using the twokeniser implementation
	 */
	TOKENISE{
		@Override
		public TwitterPreprocessingMode createMode() {
			return new TokeniseMode();
		}
		@Override
		public List<String> getAnalysisKeys(){
			ArrayList<String> analysis = new ArrayList<String>();
			analysis.add(TokeniseMode.TOKENS);
			return analysis;
		}
	},
	/**
	 * Language detection using the langid implementation
	 */
	LANG_ID{
		@Override
		public TwitterPreprocessingMode createMode() throws IOException {
			return new LanguageDetectionMode();
		}
		@Override
		public List<String> getAnalysisKeys() {
			ArrayList<String> analysis = new ArrayList<String>();
			analysis.add(LanguageDetectionMode.LANGUAGES);
			return analysis;
		}
		
	}
	;
	
	

	
	
	@Override
	public Object getOptions() {return this;}

	/**
	 * 
	 * @return Create an instance (initialising any heavyweight analysis objects) of the mode
	 * @throws Exception
	 */
	public abstract TwitterPreprocessingMode createMode() throws Exception;

	/**
	 * @return the keys this mode adds to the twitter analysis map
	 */
	public abstract List<String> getAnalysisKeys();
	
}

