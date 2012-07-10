package org.openimaj.text.jaspell;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This is a wrapper around the command aspell (version .33.7.1-alpha)
 * with its output reformated so that it can be compared with that of JaSpell.
 *
 * @author      Bruno Martins
 */
public final class Aspell {

	/** The process running the aspell command. */
	private Process process = null;

	/** The standard output for the aspell command. */
	private BufferedReader aspellOutputStream = null;

	/** The standard input for the aspell command. */
	private PrintStream aspellInputStream = null;

	/** The standard error for the aspell command. */
	private BufferedReader aspellErrorStream = null;

	/**
	 * Sole constructor for aspell.
	 * 
	 * @throws IOException A problem occured while creating the backgroud process running aspell.
	 */
	public Aspell() throws IOException {
		String[] aspellCommand = new String[4];
		aspellCommand[0] = "aspell";
		aspellCommand[1] = "-l";
		aspellCommand[2] = "--keymapping=ispell";
		aspellCommand[3] = "--lang=pt";
		//aspellCommand[4] = "--sug-mode=aspell";
		String[] envArray = new String[0];
		process = Runtime.getRuntime().exec(aspellCommand, envArray);
		aspellOutputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
		aspellInputStream = new PrintStream(new BufferedOutputStream(process.getOutputStream()),true);
		aspellErrorStream =	new BufferedReader(new InputStreamReader(process.getErrorStream()));
	}

	/**
	 * Find spelling corrections for a given misspelled word.
	 * 
	 * @param pTerm The word to correct.
	 * @return An array of possible corrections. 
	 */
	public String[] find(String pTerm) {
		String[] candidates = null;
		String badTerm = pTerm.trim().toLowerCase();
		aspellInputStream.flush();
		aspellInputStream.println(badTerm);
		aspellInputStream.flush();
		try {
			String line = aspellOutputStream.readLine();
			line = aspellOutputStream.readLine();
			if (line.trim().length() <= 0) aspellInputStream.println();
			candidates = convertFromAspell(pTerm, line);
		} catch (Exception e) {	}
		return (candidates);
	}


	/**
	 * Find the best spelling correction for a given misspelled word.
	 * 
	 * @param pTerm The word to correct.
	 * @return The best possible correction. 
	 */
	public String findMostSimilar(String pTerm) {
		String[] candidates = find(pTerm);
		if(candidates.length==0) return null;
		return candidates[0];
	}

	/**
	 * Find spelling corrections for a given misspelled word.
	 * 
	 * @param pTerm The word to correct.
	 * @return A <code>List</code> with the possible corrections. 
	 */
	public List<String> findMostSimilarList(String pTerm) {
		String[] candidates = find(pTerm);
		List<String> aux = new Vector<String>();
		for(int i=0; i<candidates.length; i++) aux.add(candidates[i]);
		return aux;
	}


	/**
	 * Cleanup the process running aspell.
	 */
	public void cleanup() {
		try {
			aspellOutputStream.close();
			aspellInputStream.close();
			aspellErrorStream.close();
			process = null;
		} catch (Exception e) {
		};
	}

	/**
	 * Converts the result from the aspell spelling checker 
	 * into a  <code>String</code> array with the possible suggestions.
	 * 
	 * @param pTerm The correctly spelled word.
	 * @param pSuggestions A <code>String</code> with the suggestions in aspell format.
	 * @return An array with the suggestions.
	 */
	private String[] convertFromAspell(String pTerm, String pSuggestions) {
		String[] candidates = null;
		int numberOfCandidates = 0;
		char[] aDocumentArray = null;
		char[] lowerCasedADocumentArray = null;
		char[] pTermArray = pTerm.toCharArray();
		char[] lowerCasedTermArray = pTerm.toLowerCase().toCharArray();
		try {
			if (pSuggestions.equals("*")) {
				candidates = new String[1];
				candidates[numberOfCandidates] = new String(pTerm);
				numberOfCandidates++;
			} else {
				StringTokenizer st =
					new StringTokenizer(pSuggestions, ":", false);
				if (st.hasMoreTokens()) {
					String stuffBeforeColon = st.nextToken();
					String stuffAfterColon = st.nextToken();
					StringTokenizer st2 =
						new StringTokenizer(stuffAfterColon, ",", false);
					candidates = new String[st2.countTokens()];
					String suggestion = null;
					String aCandidate = null;
					int rank = 1000000;
					while (st2.hasMoreTokens()) {
						suggestion = st2.nextToken().trim().toLowerCase();
						aDocumentArray = suggestion.toCharArray();
						lowerCasedADocumentArray =
							suggestion.toLowerCase().toCharArray();
						candidates[numberOfCandidates] = new String(suggestion);
						numberOfCandidates++;
					}
					st2 = null;
					st = null;
				}
			}
		} catch (Exception e) {
		}
		return (candidates);
	}

	/**
	 * Main method . 
	 *
	 * @param argv 	The command line input, tokenized.
	 */
	public static void main(String[] argv) throws Exception {
	  try {
		Aspell aspell = new Aspell();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("Enter text to spell check: ");
			String line = in.readLine();
			if (line.length() <= 0) break;
			DefaultWordFinder finder = new DefaultWordFinder(line);
			String aux = null;
			while ((aux = finder.next()) != null) {
				String aux2 = aspell.findMostSimilar(aux);
				List suggestions = aspell.findMostSimilarList(aux);
				if (aux2 == null) {
					System.out.println("MISSPELT WORD: " + aux);
					System.out.println("\tNo suggestions");
				} else if (!aux2.equals(aux.toLowerCase())) {
					System.out.println("MISSPELT WORD: " + aux);
					if (suggestions.size() == 0) {
						System.out.println("\tNo suggestions");
					} else
						System.out.println("\tBest Suggestion: " + aux2);
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
		aspell.cleanup();
	} catch (Exception e) {
		e.printStackTrace();
	}
}

}
