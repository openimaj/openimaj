/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
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
package org.openimaj.text.nlp.namedentity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to remove stopwords from a list of tokens, or to check if a word is a
 * stopword.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class IgnoreTokenStripper {
	/**
	 * Language to build stripper from.
	 */
	public enum Language {
		English
	};

	private String units = "one|two|three|four|five|six|seven|eight|nine";
	private String tens = "twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety";
	private String teens = "ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen";
	private String and = "\\s*-?\\s*and\\s*-?\\s*";
	private String toNN = "[" + units + "|" + teens + "] | [" + tens + "]\\s*-?\\s*[" + units + "]";
	private String toNNN = toNN + " | [[" + units + "]\\s*-?\\s*hundred [" + and + "[" + toNN + "]+]+]";
	/*
	 * This currently recognizes written numbers up to nine hundred and ninety
	 * nine.
	 */
	private Pattern writtenNumbers = Pattern.compile("[" + toNNN + "]+");

	private HashSet<String> ignoreTokens;

	/**
	 * Constructor for specified language.
	 * 
	 * @param language
	 */
	public IgnoreTokenStripper(Language language) {
		this.ignoreTokens = new HashSet<String>();
		for (final InputStream fstream : getListStreams(language)) {
			addToIgnoreSet(fstream);
		}
	}

	private void addToIgnoreSet(InputStream fstream) {
		try {
			final DataInputStream in = new DataInputStream(fstream);
			final BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				ignoreTokens.add(strLine.trim());
			}
			// Close the input stream
			in.close();
		} catch (final Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	private List<InputStream> getListStreams(Language language) {
		final ArrayList<InputStream> res = new ArrayList<InputStream>();
		if (language.equals(Language.English)) {
			res.add(this.getClass().getResourceAsStream(
					"/org/openimaj/text/stopwords/en_stopwords.txt"));
			res.add(this.getClass().getResourceAsStream(
					"/org/openimaj/text/stopwords/en_nouns.txt"));
			res.add(this.getClass().getResourceAsStream(
					"/org/openimaj/text/stopwords/en_countries.txt"));
			return res;
		}
		else
			return null;
	}

	/**
	 * Strips given list of tokens of all ignore words.
	 * 
	 * @param intokens
	 * @return list of clean tokens.
	 */
	public ArrayList<String> getNonStopWords(List<String> intokens) {
		final ArrayList<String> result = new ArrayList<String>();
		for (final String string : intokens) {
			if (!isIgnoreToken(string)) {
				result.add(string);
			}
		}
		return result;
	}

	/**
	 * Checks if given token is an ignore word
	 * 
	 * @param token
	 * @return true if ignore Token
	 */
	public boolean isIgnoreToken(String token) {
		// check if in ignore list
		if (ignoreTokens.contains(token))
			return true;
		// check if it is a number
		try {
			Double.parseDouble(token);
			return true;
		} catch (final Exception e) {
		}
		// check if it is a number written as a word
		final Matcher m = writtenNumbers.matcher(token.toLowerCase());
		if (m.matches())
			return true;
		return false;
	}
}
