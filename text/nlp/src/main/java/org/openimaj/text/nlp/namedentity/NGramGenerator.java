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
		// final ArrayList<String> tokens = new
		// ArrayList<String>(Arrays.asList(tokA));
		final NGramGenerator<String> ngg = new StringNGramGenerator();

		final List<String[]> ngrams = ngg.getNGrams(null, 3);
		for (final String[] ngram : ngrams) {
			System.out.println(StringUtils.join(ngram, " "));
		}
	}

}
