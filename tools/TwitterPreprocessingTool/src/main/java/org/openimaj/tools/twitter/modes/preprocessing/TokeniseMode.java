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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;
import org.openimaj.twitter.USMFStatus;

/**
 * Use the twokeniser to tokenise tweets
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TokeniseMode extends TwitterPreprocessingMode<Map<String, List<String>>> {

	final static String TOKENS = "tokens";
	public static final String TOKENS_UNPROTECTED = "unprotected";
	public static final String TOKENS_PROTECTED = "protected";
	public static final String TOKENS_ALL = "all";
	private LanguageDetectionMode langMode;

	/**
	 * literally do nothing
	 */
	public TokeniseMode() {
		try {
			langMode = new LanguageDetectionMode();
		} catch (final IOException e) {
			// The langauge detector was not instantiated, tokens will be of
			// lower quality!
		}
	}

	@Override
	public Map<String, List<String>> process(USMFStatus twitterStatus) {
		TweetTokeniser tokeniser;
		final Map<String, List<String>> tokens = new HashMap<String, List<String>>();
		twitterStatus.addAnalysis(TOKENS, tokens);
		try {
			if (langMode != null) {
				final Map<String, Object> localeMap = TwitterPreprocessingMode.results(twitterStatus, langMode);
				final WeightedLocale locale = WeightedLocale.fromMap(localeMap);
				if (!TweetTokeniser.isValid(locale.language)) {
					return tokens;
				}
			}

			tokeniser = new TweetTokeniser(twitterStatus.text);
			tokens.put(TOKENS_ALL, tokeniser.getStringTokens());
			tokens.put(TOKENS_PROTECTED, tokeniser.getProtectedStringTokens());
			tokens.put(TOKENS_UNPROTECTED, tokeniser.getUnprotectedStringTokens());
			twitterStatus.addAnalysis(TOKENS, tokens);
		} catch (final Exception e) {
		}

		return tokens;

	}

	@Override
	public String getAnalysisKey() {
		return TokeniseMode.TOKENS;
	}
}
