package org.openimaj.text.jaspell;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 *  The main class of the spell checking package.
 * 
 * @author Bruno Martins
 */
public class SpellChecker {

	/** The main dictionary for the spelling checker. */
	private TernarySearchTrie dictionary = null;
	
	/** A dictionary of common misspellings */
	private CommonMisspellings commonErrors = null;
	
	/** Use bigrams for context dependent spelling correction */ 
	private boolean useBigrams = false;

	/**
	 * Return an instance of this class. This method is here only for backward
	 * compatibility, in a previous version this class was Singleton and the dictionary
	 * was stored in a static variable.
     * 
	 * @deprecated TODO: Remove this method and check dependencies with other code.
	 * @return An instance of <code>SpellChecker</code>.
	 */
	public SpellChecker getInstance() {
		SpellChecker checker = new SpellChecker(); 
		if(dictionary!=null) checker.dictionary = this.dictionary;
		return checker; 
	}
	
	/**
	 * Phonetic heuristics for the Portuguese language, taking as
	 * input a Portuguese word and replacing letters and groups of letter that 
	 * correspond to a specific "sound" by a cannonical representation.
	 * 
	 * @param str A <code>String</code> with a Portuguese word.
	 * @return A "normalized" representation for the portuguese word, where groups
	 * of letters that have the same sound are represented in a cannonical way.
	 */
	private static String heuristicsPortuguese(String str) {
			int len = str.length();
			int different = -1;
			int i, j;
			char ch;
			char ch2;
			String chars = "";
			for (i = 0, j=0; i < len; i++, j++) {
				ch = str.charAt(i);
				if (ch == 'ç' ) {
                  chars += "ss";
				} else if (ch == 'x') {
				  chars += "ch";
				} else if (ch == 'i') {
				  chars += "e";
				} else if (ch == 'u') {
				  chars += "o";
				} else if (ch == 'z') {
				  chars += "s";
				} else if (ch == 'c') {
				  chars += "s";
				} else if (ch == 'v') {
				  chars += "b";
				} else if (ch == 'y') {
				  chars += "i";
				} else if (ch == 'ã') {
				  chars += "an";
				} else if (ch == 'k') {
				  chars += "qu";
				} else chars += ch;
			}
			return new String(chars);
		}

	/**
	 * Reads the dictionary to memory.
	 * 
	 *@param path  The <code>File</code> path leading up to the dictionary.
	 * @throws Exception an Exception indicating if any problem occured while reading the dictionary.
	 */
	public synchronized void initialize(String path) throws Exception {
			dictionary = new TernarySearchTrie(new File(path));
	}

	/**
	 * Reads the dictionary to memory.
	 * 
	 *@param path1  The <code>File</code> path leading up to the dictionary.
	 *@param path2  The <code>File</code> path leading up to a dictionary of common misspellings.
	 * @throws Exception an Exception indicating if any problem occured while reading the dictionary.
	 */
	public synchronized void initialize(String path1, String path2) throws Exception {
			dictionary = new TernarySearchTrie(new File(path1));
			if(path2!=null) commonErrors = new CommonMisspellings(path2);
	}

	/**
	 * Reads the dictionary to memory.
	 * 
	 *@param path1  The <code>File</code> path leading up to the dictionary.
	 *@param path2 The <code>File</code> path leading up to a dictionary of common misspellings.
	 *@param path3 The <code>File</code> path leading up to a dictionary of correct spellings.
	 * @throws Exception an Exception indicating if any problem occured while reading the dictionary.
	 */
	public synchronized void initialize(String path1, String path2, String path3) throws Exception {
			dictionary = new TernarySearchTrie(new File(path1));
			if(path2!=null) commonErrors = new CommonMisspellings(path2,path3);
	}

	/**
	 * Takes a word and returns the most similar word from the dictionary,
	 * using Levenshtein Distance, Phonetic similarity, Keyboard Proximity and
	 * other heuristics to measure similarity. 
	 *
	 *@param  key The word to check in the dictionary.
	 *@return  The most similar word in the dictionary.
	 */
	public synchronized String findMostSimilar(String key) {
		return findMostSimilar(key, false);
	}

	/**
	 * Takes a word and returns the most similar word from the dictionary,
	 * using Levenshtein Distance, Phonetic similarity, Keyboard Proximity and
	 * other heuristics to measure similarity. 
	 *
	 *@param  key The word to check in the dictionary.
	 *@param  useFrequency Use the relative frequency method. 
	 *@return  The most similar word in the dictionary.
	 */
	public synchronized String findMostSimilar(String key, boolean useFrequency) {
		if(commonErrors!=null) {
			String aux[] = commonErrors.find(key);
			if(aux!=null && aux.length>0) return aux[0];
		}
		Integer frequency;
		String keylower = StringUtils.toLowerCase(key, false);
		if ((frequency=dictionary.getAndIncrement(keylower)) != null && !useFrequency) {
			return key;
		}
		if(useFrequency && frequency==null) frequency=new Integer(0);
		boolean soundex = false;
		boolean firstchar = false;
		boolean sameRow = false;
		boolean heuristics = false;
		Integer int_aux;
		String auxs;
		String mostSimilar = null;
		String canonkey = StringUtils.toLowerCase(keylower, true);
		String phonekey = Phonetic.getMetaphone(keylower);
		String phonekeypt = heuristicsPortuguese(canonkey);
		List aux = findMostSimilarList(keylower,!useFrequency);
		if(useFrequency) aux.add(keylower);
		int currentFrequency = 0, oldFrequency = 0;
		for (int i = 0,
			val = -1,
			currentVal = -1,
			length1 = -1,
			length2 = 1000,
			length3 = keylower.length();
			i < aux.size();
			i++) {
			auxs = (String) (aux.get(i));
			if(auxs==null) continue;
			if(useFrequency) {
				oldFrequency = currentFrequency;
				Integer freq = ((Integer)(dictionary.get(auxs)));
				if(freq==null || freq.intValue()<currentFrequency) continue; else currentFrequency = freq.intValue();
			}			
			length1 = auxs.length();
			int_aux = (Integer) (dictionary.get(auxs));
			if (int_aux != null) {
				val = int_aux.intValue();
			} else {
				val = 0;
			}
			if (StringUtils.toLowerCase(auxs, true).equals(canonkey)) {
				return auxs;
			}
			if(useFrequency && currentFrequency>oldFrequency) {
				currentVal = val;
				mostSimilar = auxs;
				length2 = length1;   
			} else if (currentFrequency==oldFrequency && heuristicsPortuguese(auxs).equals(heuristicsPortuguese(canonkey))) {
				if (!heuristics) {
					currentVal = val;
					mostSimilar = auxs;
					length2 = length1;
				} else if (
					currentVal < val
						|| (length1 > length3 && length1 < length2)) {
					currentVal = val;
					mostSimilar = auxs;
					length2 = length1;
				}
				heuristics = true;
			} else if (currentFrequency==oldFrequency &&!heuristics && KeyboardProximity.stringsSameRow(auxs, canonkey)) {
				if (!sameRow) {
					currentVal = val;
					mostSimilar = auxs;
					length2 = length1;
				} else if (
					currentVal < val
						|| (length1 > length3 && length1 < length2)) {
					currentVal = val;
					mostSimilar = auxs;
					length2 = length1;
				}
				sameRow = true;
			} else if (currentFrequency==oldFrequency &&!heuristics &&
				!sameRow && Phonetic.getDoubleMetaphone(auxs).equals(phonekey)) {
				if (!soundex) {
					currentVal = val;
					mostSimilar = auxs;
					length2 = length1;
				} else if (
					currentVal < val
						|| (length1 > length3 && length1 < length2)) {
					currentVal = val;
					mostSimilar = auxs;
					length2 = length1;
				}
				soundex = true;
			} else if (currentFrequency==oldFrequency &&!heuristics &&
				!sameRow && !soundex && auxs.charAt(0) == canonkey.charAt(0)) {
				if (!firstchar) {
					currentVal = val;
					mostSimilar = auxs;
					length2 = length1;
				} else if (
					currentVal < val
						|| (length1 > length3 && length1 < length2)) {
					currentVal = val;
					mostSimilar = auxs;
					length2 = length1;
				}
				firstchar = true;
			} else if (currentFrequency==oldFrequency && !heuristics &&
				!sameRow
					&& !soundex
					&& !firstchar
					&& (currentVal < val
						|| (length1 > length3 && length1 < length2))) {
				currentVal = val;
				mostSimilar = auxs;
				length2 = length1;
			}
		}
		if (mostSimilar != null) {
			if(useFrequency) {
				Integer frequency2 = (Integer)(dictionary.get(mostSimilar));
				if(frequency2==null || frequency2.intValue()==0 || frequency2.intValue()<=frequency.intValue()) {
					if(frequency.intValue()==0) return null;
					mostSimilar = key;
				} else if (frequency.intValue()!=0 && frequency.intValue()/frequency2.intValue()>0.5) {
					mostSimilar = key;
				} else if(frequency2.intValue()>1000) {
					String mostSimilar2 = findMostSimilar(mostSimilar,true);
					if(mostSimilar2!=null) mostSimilar = mostSimilar2;
				}
			}
			if (key.toUpperCase().equals(key)) {
				return mostSimilar.toUpperCase();
			}
			if (Character.toUpperCase(key.charAt(0)) == key.charAt(0)) {
				mostSimilar = Character.toUpperCase(mostSimilar.charAt(0)) + mostSimilar.substring(1);
			}
		}
		return mostSimilar;
	}

	/**
	 * Takes a word and returns a <code>List</code> with similar words from the dictionary,
	 * using Levenshtein Distance to rank words in the list. 
	 *
	 *@param  key The word to check in the dictionary.
	 *@return  A <code>List</code> of similar words from the dictionary.
	 */
	public synchronized List findMostSimilarList(String key) {
		return findMostSimilarList(key,true);
	}

	/**
	 * Takes a word and returns a <code>List</code> with similar words from the dictionary,
	 * using Levenshtein Distance to rank words in the list. 
	 *
	 *@param  key The word to check in the dictionary.
	 *@param heuristics Use "best candidates" heuristics to select a shorter list of corrections.
	 *@return  A <code>List</code> of similar words from the dictionary.
	 */
	public synchronized List findMostSimilarList(String key, boolean heuristics) {
		List aux = new Vector();
		int size = key.length();
		if (size == 0) return aux;
		if(commonErrors!=null) {
			String auxerr[] = commonErrors.find(key);
			if(auxerr!=null && auxerr.length>0) {
				for(int i=0; i<auxerr.length; i++) aux.add(auxerr[i]);
				return aux; 
			}
		}
		int maxAux = 1;
		int i;
		int j;
		String auxs;
		String auxs2;

		// maximum of 2 letters difference
		for (maxAux = 1;
			maxAux <= 2 && aux.size() == 0 && size >= maxAux * 2;
			maxAux++) {
			dictionary.setMatchAlmostDiff(maxAux);
			aux = dictionary.matchAlmost(key);
		}

		// 1 letter removed or added and 1 letter removed or added and 1 different
		if (size > 2 && (!heuristics || aux.size() == 0)) {
			for (maxAux = 0; maxAux <= 1 && aux.size() == 0; maxAux++) {
				dictionary.setMatchAlmostDiff(maxAux);
				for (i = size - 1; i >= 0; i--) {
					aux.addAll(dictionary.matchAlmost(key.substring(0, i) + key.substring(i + 1, size)));
					for (j = 'a'; j < 'z'; j++) {
						aux.addAll(dictionary.matchAlmost(
									key.substring(0, i)
										+ ((char) j)
										+ key.substring(i, size)));
					}
					for (j = 0; j < StringUtils.getSpecialChars().length; j++) {
						aux.addAll(dictionary.matchAlmost(
									key.substring(0, i)
										+ StringUtils.getSpecialChars()[j]
										+ key.substring(i, size)));
					}
				}
			}
		}
		// repeated characters removed and 2 concatenated words
		if (!heuristics || aux.size() == 0) {
			for (i = j = 1; i < size; i++) {
				if (key.charAt(i) == key.charAt(i - 1)) {
					auxs = key.substring(0, j) + key.substring(i + 1, size);
					if (dictionary.get(auxs) != null) {
						aux.add(auxs);
					}
				} else {
					j = i + 1;
				}
				auxs = key.substring(0, i);
				if (dictionary.get(auxs) != null) {
					auxs2 = key.substring(i, size);
					if (dictionary.get(auxs2) != null) {
						aux.add(auxs + " " + auxs2);
					}
				}
			}
		}
		// two consecutive letters exchanged and 1 character different
		dictionary.setMatchAlmostDiff(1);
		if (!heuristics || aux.size() == 0) {
			for (i = 0; i < size - 1; i++) {
				auxs =
					key.charAt(i + 1)
						+ key.charAt(i)
						+ key.substring(i + 2, size);
				aux.addAll(dictionary.matchAlmost(auxs));
			}
		}
		// prefixes
		if (!heuristics || aux.size() == 0) {
			aux.addAll(dictionary.matchPrefix(key));
		}
		// repeated characters removed and 1 character different
		if (!heuristics || aux.size() == 0) {
			for (i = 1; i < size - 1; i++) {
				if (key.charAt(i) == key.charAt(i - 1)) {
					auxs = key.substring(0, i) + key.substring(i + 1, size);
					aux.addAll(dictionary.matchAlmost(auxs));
				}
			}
		}
		return aux;
	}

	/**
	 * Checks spelling errors in terms for a search engine query, ignoring
	 * commands to the search system.
	 *
	 *@param  s A <code>String</code> with a search engine query.
	 *@return   The <code>String</code> with spelling errors identifyed.
	 *@see #spellCheckWord(String)
	 */
	public String spellCheckQuery(String s) {
		return spellCheckQuery(s,false);
	}

	/**
	 * Checks spelling errors in terms for a search engine query, ignoring
	 * commands to the search system.
	 *
	 *@param  s A <code>String</code> with a search engine query.
	 *@param  useFrequencyMethod Use the relative frequency method.
	 *@return   The <code>String</code> with spelling errors identifyed.
	 *@see #spellCheckWord(String)
	 */
	public String spellCheckQuery(String s, boolean useFrequencyMethod) {
		String auxss = "";
		StringBuffer aux3 = new StringBuffer();
		StringBuffer aux2 = null;
		if (s.startsWith("site:")
			|| s.startsWith("related:")
			|| s.startsWith("cache:")
			|| s.startsWith("inlinks:")
			|| s.startsWith("outlinks:")
			|| s.startsWith("url:")) {
			int index = s.indexOf(" ");
			if (index == -1) index = s.indexOf("+");
			if (index != -1) {
				auxss = s.substring(0, index);
				s = s.substring(index);
			} else s = "";
		}
		char aux = ' ';
		char noaccent;
		if (s == null) return s;
		for (int i = 0; i < s.length(); i++) {
			aux = s.charAt(i);
			noaccent = StringUtils.replaceAccent(Character.toLowerCase(aux));
			if ((noaccent >= 'a' && noaccent <= 'z')
				|| (aux2 != null && noaccent >= '0' && noaccent <= '9')) {
				if (aux2 == null) aux2 = new StringBuffer();
				aux2.append((char)aux);
			} else {
				if(aux2!=null) aux3.append(spellCheckWord(aux2.toString(),useFrequencyMethod));
				if (aux != -1) aux3.append((char)aux);
				aux2 = null;
			}
		}
		if(aux2!=null) aux3.append(spellCheckWord(aux2.toString(),useFrequencyMethod));
		return auxss + aux3.toString();
	}

	/**
	 * Checks spelling errors in terms from a given <code>String</code>.
	 *
	 *@param  s A <code>String</code>.
	 *@return   The <code>String</code> with spelling errors identifyed.
	 *@see #spellCheckWord(String)
	 */
	public String spellCheck(String s) {
		return spellCheck(s,false);
	}
	
	/**
	 * Checks spelling errors in terms from a given <code>String</code>.
	 *
	 *@param  s A <code>String</code>.
	 *@param  useFrequencyMethod Use the relative frequency method.
	 *@return   The <code>String</code> with spelling errors identifyed.
	 *@see #spellCheckWord(String)
	 */
	public String spellCheck(String s, boolean useFrequencyMethod) {
		DefaultWordFinder aux = new DefaultWordFinder(s);
		aux.replace(spellCheckWord(aux.current(),useFrequencyMethod));
		while (aux.hasNext()) {
			String word = aux.next();
			aux.replace(spellCheckWord(word,useFrequencyMethod));
		}
		return aux.getText();
	}

	/**
	 * Checks spelling errors in terms from a TeX document.
	 *
	 *@param  s A <code>String</code> with the TeX document.
	 *@return   The <code>String</code> with spelling errors identifyed.
	 *@see #spellCheckWord(String)
	 */
	public String spellCheckTeX(String s) {
		return spellCheckTeX(s,false);
	}
	
	/**
	 * Checks spelling errors in terms from a TeX document.
	 *
	 *@param  s A <code>String</code> with the TeX document.
	 *@param  useFrequencyMethod Use the relative frequency method.
	 *@return   The <code>String</code> with spelling errors identifyed.
	 *@see #spellCheckWord(String)
	 */
	public String spellCheckTeX(String s, boolean useFrequencyMethod) {
		TeXWordFinder aux = new TeXWordFinder(s);
		aux.replace(spellCheckWord(aux.current(),useFrequencyMethod));
		while (aux.hasNext()) {
			String word = aux.next();
			aux.replace(spellCheckWord(word,useFrequencyMethod));
		}
		return aux.getText();
	}

	/**
	 * Checks spelling errors in terms from an XML document. 
	 *
	 *@param  s A <code>String</code> with the XML document.
	 *@return   The <code>String</code> with spelling errors identifyed.
	 *@see #spellCheckWord(String)
	 */
	public String spellCheckXML(String s) {
		return spellCheckXML(s,false);
	}
	
	/**
	 * Checks spelling errors in terms from an XML document. 
	 *
	 *@param  s A <code>String</code> with the XML document.
	 *@param  useFrequencyMethod Use the relative frequency method.
	 *@return   The <code>String</code> with spelling errors identifyed.
	 *@see #spellCheckWord(String)
	 */
	public String spellCheckXML(String s, boolean useFrequencyMethod) {
		XMLWordFinder aux = new XMLWordFinder(s);
		aux.replace(spellCheckWord(aux.current(),useFrequencyMethod));
		while (aux.hasNext()) {
			String word = aux.next();
			aux.replace(spellCheckWord(word,useFrequencyMethod));
		}
		return aux.getText();
	}

	/**
	 *  Checks if a word is correctly spelled, producing as output a string with the 
	 *  word plus <code>SGML</code> tags indicating if it is correctly spelled or not.
	 *  <p>
	 *  The possible <code>SGML</code> tags are:
	 * <p>
	 *   &lt;misspell&gt;  - The word was not found in the dictionary but a suggestion could not be generated.<br/>
	 *   &lt;plain&gt; - The word is correctly spelled.<br/>
	 *   &lt;suggestion&gt; - The word was not found in the dictionary and a suggestion was generated.<br/>
	 * 
	 *@param  word  The word to check.
	 *@return  A <code>String</code> with the word provided as input (or an appropriate correction) 
	 *				surrounded with <code>SGML</code> tags indicating if it is correctly spelled or not.
	 */
	public String spellCheckWord(String word) {
		return spellCheckWord(word,false);
	}
	
	/**
	 *  Checks if a word is correctly spelled, producing as output a string with the 
	 *  word plus <code>SGML</code> tags indicating if it is correctly spelled or not.
	 *  <p>
	 *  The possible <code>SGML</code> tags are:
	 * <p>
	 *   &lt;misspell&gt;  - The word was not found in the dictionary but a suggestion could not be generated.<br/>
	 *   &lt;plain&gt; - The word is correctly spelled.<br/>
	 *   &lt;suggestion&gt; - The word was not found in the dictionary and a suggestion was generated.<br/>
	 * 
	 *@param  word  The word to check.
	 *@param  useFrequencyMethod Use the relative frequency method.
	 *@return  A <code>String</code> with the word provided as input (or an appropriate correction) 
	 *				surrounded with <code>SGML</code> tags indicating if it is correctly spelled or not.
	 */
	public String spellCheckWord(String word, boolean useFrequencyMethod) {
		StringBuffer res = new StringBuffer();
		String aux = null;
		if (word != null) {
			aux = findMostSimilar(word.toString(),useFrequencyMethod);
			if (aux == null) {
				res.append("<misspell>");
				res.append(word);
				res.append("</misspell>");
			} else if (
				StringUtils.toLowerCase(aux, true).equals(
					StringUtils.toLowerCase(word.toString(), true))) {
				res.append("<plain>");
				res.append(word.toString());
				res.append("</plain>");
			} else if (word.length() == 1) {
				res.append("<misspell>");
				res.append(word.toString());
				res.append("</misspell>");
			} else {
				res.append("<suggestion>");
				res.append(aux);
				res.append("</suggestion>");
			}
		}
		return res.toString();
	}

	/**
	 * Main method.  
	 *
	 * @param args 	The command line input, tokenized.
	 */
	public static void main(String args[]) throws Exception {
		try {
			SpellChecker spellCheck = new SpellChecker();
			spellCheck.initialize("dict/portuguese.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				System.out.print("Enter text to spell check: ");
				String line = in.readLine();
				if (line.length() <= 0) break;
				DefaultWordFinder finder = new DefaultWordFinder(line);
				String aux =null;
				while ((aux = finder.next())!=null) {
					String aux2 = spellCheck.findMostSimilar(aux);
					List suggestions = spellCheck.findMostSimilarList(aux);
					if (aux2 == null) {
						System.out.println("MISSPELT WORD: " + aux);
						System.out.println("\tNo suggestions");
					} else if (!aux2.equals(aux.toLowerCase())) {
						System.out.println("MISSPELT WORD: " + aux);
						if (suggestions.size() == 0) {
							System.out.println("\tNo suggestions");
						} else System.out.println("\tBest Suggestion: "+aux2);
						for (Iterator suggestedWord = suggestions.iterator();
							suggestedWord.hasNext();
							) {
							System.out.println(
								"\tSuggested Word: " + suggestedWord.next());
						}
					} else {
						System.out.println("CORRECT WORD: " + aux);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
