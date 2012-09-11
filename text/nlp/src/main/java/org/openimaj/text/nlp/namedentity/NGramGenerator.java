package org.openimaj.text.nlp.namedentity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class which allows generation of N-grams of items in a list. Ngrams
 * are defined as "n" neighbouring items
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 */
public class NGramGenerator<T> {

	/**
	 * Produce n-grams of strings
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class StringNGramGenerator extends NGramGenerator<String> {

		/**
		 * Class {@link NGramGenerator#NGramGenerator(Class)} with String#class
		 */
		public StringNGramGenerator() {
			super(String.class);
		}

	}

	private final Class<T> clazz;

	/**
	 * @param type
	 *            provide the type of the items in the list being generated
	 */
	public NGramGenerator(Class<T> type) {
		clazz = type;
	}

	/**
	 * @param tokens
	 *            tokens to combine as ngrams
	 * @param ngrams
	 *            the numbers of ngrams to generat
	 * @return a list of ngrams
	 */
	@SuppressWarnings("unchecked")
	public <R extends T> List<T[]> getNGrams(List<R> tokens, int... ngrams) {
		if (tokens == null)
			return new ArrayList<T[]>();
		final ArrayList<T[]> result = new ArrayList<T[]>();
		for (int i = 0; i < tokens.size(); i++) {
			for (final int nsize : ngrams) {
				if (i + nsize <= tokens.size()) {
					final R[] ngram = (R[]) Array.newInstance(clazz, nsize);
					for (int j = 0; j < nsize; j++) {
						ngram[j] = tokens.get(i + j);
					}
					result.add(ngram);
				}
			}
		}
		return result;
	}

	/**
	 * lightweight test with some really horrible unicode
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// final String[] tokA = "#まひろ同盟".split(" ");
		// final ArrayList<String> tokens = new
		// ArrayList<String>(Arrays.asList(tokA));
		final NGramGenerator<String> ngg = new StringNGramGenerator();

		final List<String[]> ngrams = ngg.getNGrams(null, 3);
		for (final String[] ngram : ngrams) {
			System.out.println(StringUtils.join(ngram, " "));
		}
	}

}
