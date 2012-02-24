package org.openimaj.tools.twitter.modes.preprocessing;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.twitter.TwitterStatus;

/**
 * The mode of the twitter processing tool
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("unchecked")
public enum TwitterPreprocessingModeOption implements CmdLineOptionsProvider {
	/**
	 * Tokenise the tweet using the twokeniser implementation
	 */
	TOKENISE{
		@Override
		public TokeniseMode createMode() {
			return new TokeniseMode();
		}
		@Override
		public String getAnalysisKey(){
			return TokeniseMode.TOKENS;
		}
	},
	/**
	 * Language detection using the langid implementation
	 */
	LANG_ID{
		@Override
		public LanguageDetectionMode createMode() throws IOException {
			return new LanguageDetectionMode();
		}
		@Override
		public String getAnalysisKey() {
			return LanguageDetectionMode.LANGUAGES;
		}
		
	},
	/**
	 * Stem tweets. Don't bother with non english tweets.
	 */
	PORTER_STEM{

		@Override
		public TwitterPreprocessingMode<List<String>> createMode() throws Exception {
			return new StemmingMode();
		}

		@Override
		public String getAnalysisKey() {
			return StemmingMode.STEMMED;
		}
		
	}
	;
	
	/**
	 * Given a twitter status, attempts to extract the analysis for this mode. 
	 * If the analysis does not exist, the provided mode instance is used 
	 * to create the analysis. If the provided mode is null a new mode is created. This
	 * mode creation might be slow, be careful about using this in this way.
	 * 
	 * @param <T> The type of the analysis
	 * @param status the twitter status to be analysed
	 * @param mode the mode to use if the analysis does no exist in the tweet
	 * @return the analysis results. These results are also injected into the tweet's analysis
	 * @throws Exception 
	 */
	public <T> T results(TwitterStatus status,TwitterPreprocessingMode<T> mode) throws Exception{
		T result = status.getAnalysis(this.getAnalysisKey());
		if(result == null){
			if(mode == null) mode = createMode(); // This might be horribly inefficient (e.g. language model)
			result = mode.process(status);
		}
		return result ;
	}
	
	/**
	 * Given a twitter status, attempts to extract the analysis for this mode. 
	 * If the analysis does not exist, a new mode is created and used to analyse the status 
	 * 
	 * @param <T> The type of the analysis data
	 * @param status the twitter status to be analysed
	 * @return the analysis results. These results are also injected into the tweet's analysis
	 * @throws Exception
	 */
	public <T> T results(TwitterStatus status) throws Exception{
		return results(status, null);
	}
	
	@Override
	public Object getOptions() {return this;}

	/**
	 * 
	 * @param <T> The type of data the preprocessing mode saves
	 * @return Create an instance (initialising any heavyweight analysis objects) of the mode
	 * @throws Exception
	 */
	public abstract <T> TwitterPreprocessingMode<T> createMode() throws Exception;

	/**
	 * @return the keys this mode adds to the twitter analysis map
	 */
	public abstract String getAnalysisKey();
	
}

