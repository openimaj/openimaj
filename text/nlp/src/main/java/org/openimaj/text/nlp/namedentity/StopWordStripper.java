package org.openimaj.text.nlp.namedentity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to remove stopwords from a list of tokens, or to check if a word is a
 * stopword.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * 
 */
public class StopWordStripper {

	/**
	 * The language enum for supported lists of stopwords
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static enum Language {
		/**
		 * english stop words
		 */
		ENGLISH
	}

	private final ArrayList<String> stopwords;

	/**
	 * @param language
	 *            the language of the stopwords
	 */
	public StopWordStripper(Language language) {
		this.stopwords = new ArrayList<String>();
		try {
			final InputStream fstream = getListStream(language);
			final DataInputStream in = new DataInputStream(fstream);
			final BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				stopwords.add(strLine.trim().toLowerCase());
			}
			// Close the input stream
			in.close();
		} catch (final Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		Collections.sort(stopwords);
	}

	private InputStream getListStream(Language language) {
		switch (language) {
		case ENGLISH:
			return this.getClass().getResourceAsStream("/org/openimaj/text/stopwords/en_stopwords.txt");
		default:
			return null;
		}
	}

	/**
	 * @param intokens
	 * @return the strings in the list which are not stop words
	 */
	public ArrayList<String> getNonStopWords(List<String> intokens) {
		final ArrayList<String> result = new ArrayList<String>();
		for (final String string : intokens) {
			if (Collections.binarySearch(stopwords, string) < 0) {
				result.add(string);
			}
		}
		return result;
	}

	/**
	 * @param check
	 * @return true if the word is a stopword
	 */
	public boolean isStopWord(String check) {
		if (stopwords.contains(check.toLowerCase()))
			return true;
		return false;
	}

	/**
	 * instantiate
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new StopWordStripper(StopWordStripper.Language.ENGLISH);
	}

}
