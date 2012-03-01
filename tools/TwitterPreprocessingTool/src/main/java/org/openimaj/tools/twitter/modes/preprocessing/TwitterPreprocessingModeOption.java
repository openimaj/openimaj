/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

