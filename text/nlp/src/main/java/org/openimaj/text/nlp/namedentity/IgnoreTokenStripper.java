package org.openimaj.text.nlp.namedentity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Class to remove stopwords from a list of tokens, or to check if a word is a
 * stopword.
 * 
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 */
public class IgnoreTokenStripper {
	/**
	 * Language to build stripper from.
	 */
	public enum Language {
		@SuppressWarnings("javadoc")
		English
	};

	private HashSet<String> ignoreTokens;

	/**
	 * Constructor for specified language.
	 * 
	 * @param language
	 */
	public IgnoreTokenStripper(Language language) {
		this.ignoreTokens = new HashSet<String>();
		try {
			InputStream fstream = getListStream(language);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				ignoreTokens.add(strLine.trim());
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	private InputStream getListStream(Language language) {
		if (language.equals(Language.English))
			return this.getClass().getResourceAsStream(
					"/org/openimaj/text/stopwords/en_stopwords.txt");
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
		ArrayList<String> result = new ArrayList<String>();
		for (String string : intokens) {
			if (!ignoreTokens.contains(string)) {
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
		if (ignoreTokens.contains(token))
			return true;
		return false;
	}
}