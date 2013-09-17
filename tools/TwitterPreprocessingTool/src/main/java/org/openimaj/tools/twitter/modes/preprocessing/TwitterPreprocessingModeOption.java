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

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * The mode of the twitter processing tool
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum TwitterPreprocessingModeOption implements CmdLineOptionsProvider {
	/**
	 * Tokenise the tweet using the twokeniser implementation
	 */
	TOKENISE {
		@Override
		public TokeniseMode getOptions() {
			return new TokeniseMode();
		}

	},
// TODO: Fix the NER stuff
//	NER{
//
//		@Override
//		public NERMode getOptions() {
//				return new NERMode();
//		}
//
//	},
	/**
	 * Language detection using the langid implementation
	 */
	LANG_ID {
		@Override
		public LanguageDetectionMode getOptions()  {
			try {
				return new LanguageDetectionMode();
			} catch (IOException e) {
				return null;
			}
		}

	},
	/**
	 * Stem tweets. Don't bother with non english tweets.
	 */
	PORTER_STEM {
		@Override
		public TwitterPreprocessingMode<List<String>> getOptions() {
			try {
				return new StemmingMode();
			} catch (IOException e) {
				return null;
			}
		}
	},
	/**
	 * Remove all stopwords from tokenised items
	 */
	REMOVE_STOPWORDS{

		@Override
		public TwitterPreprocessingMode<List<String>> getOptions() {
			try {
				return new StopwordMode();
			} catch (IOException e) {
				return null;
			}
		}
		
	},
	/**
	 * Ascribe sentiment
	 */
	SENTIMENT{

		@Override
		public TwitterPreprocessingMode<?> getOptions() {
			try {
				return new SentimentExtractionMode();
			} catch (IOException e) {
				return null;
			}
		}
		
	}
	;

	/**
	 * @return An instance (initialising any heavyweight analysis objects) of the mode
	 */
	@Override
	public abstract TwitterPreprocessingMode<?> getOptions() ;

}

