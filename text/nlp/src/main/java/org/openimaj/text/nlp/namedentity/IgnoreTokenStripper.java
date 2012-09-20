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
		@SuppressWarnings("javadoc")
		English
	};
	 private String units = "one|two|three|four|five|six|seven|eight|nine";
	 private String tens = "twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety";
	 private String teens = "ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen";
	 private String and = "\\s*-?\\s*and\\s*-?\\s*";
	 private String toNN = "["+units+"|"+teens+"] | ["+tens+"]\\s*-?\\s*["+units+"]";
	 private String toNNN = toNN+" | [["+units+"]\\s*-?\\s*hundred ["+and+"["+toNN+"]+]+]";
	 /*
	  * This currently recognizes written numbers up to nine hundred and ninety nine.
	  */
	 private Pattern writtenNumbers = Pattern.compile("["+toNNN+"]+");

	private HashSet<String> ignoreTokens;

	/**
	 * Constructor for specified language.
	 * 
	 * @param language
	 */
	public IgnoreTokenStripper(Language language) {
		this.ignoreTokens = new HashSet<String>();
		for(InputStream fstream:getListStreams(language)){
			addToIgnoreSet(fstream);
		}		
	}

	private void addToIgnoreSet(InputStream fstream) {
		try {
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

	private List<InputStream> getListStreams(Language language) {
		 ArrayList<InputStream> res = new ArrayList<InputStream>();
		if (language.equals(Language.English)){
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
		ArrayList<String> result = new ArrayList<String>();
		for (String string : intokens) {
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
		//check if in ignore list
		if (ignoreTokens.contains(token))
			return true;
		//check if it is a number
		try{
		Double.parseDouble(token);
		return true;
		}catch (Exception e){			
		}
		//check if it is a number written as a word
		Matcher m = writtenNumbers.matcher(token.toLowerCase());
		if(m.matches())return true;
		return false;
	}
}