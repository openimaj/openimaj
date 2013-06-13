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
package org.openimaj.text.nlp.language;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import Jama.Matrix;

/**
 * Code to train, classify and generate language specific text by building a
 * first order Markov chain.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class MarkovChainLanguageModel {

	private Map<Locale, Matrix> chains = new HashMap<Locale, Matrix>();
	private Map<Locale, long[]> chainCounts = new HashMap<Locale, long[]>();

	/**
	 * Generate a new empty markov chain language model
	 */
	public MarkovChainLanguageModel() {
		chains = new HashMap<Locale, Matrix>();
		chainCounts = new HashMap<Locale, long[]>();
	}

	/**
	 * 
	 * Add an example to a language's markov chain
	 * 
	 * @param language
	 *            the language the example is being added to
	 * @param example
	 *            the new example to learn from
	 * @param encoding
	 *            the encoding of the example
	 * @throws UnsupportedEncodingException
	 */
	public void train(Locale language, String example, String encoding) throws UnsupportedEncodingException {
		if (!chains.containsKey(language)) {
			chains.put(language, new Matrix(256 + 1, 256 + 1));
			chainCounts.put(language, new long[256 + 1]);
		}

		final Matrix chain = chains.get(language);
		final long[] chainCount = chainCounts.get(language);
		final byte[] data = example.getBytes(encoding);

		int currentIndex = 0;
		final double[][] chainData = chain.getArray();
		for (final byte b : data) {
			final int newIndex = (b & 0xff) + 1;
			chainData[currentIndex][newIndex] = chainData[currentIndex][newIndex] + 1;
			chainCount[currentIndex] += 1;
			currentIndex = newIndex;
		}

	}

	/**
	 * Train a given language on a stream of text
	 * 
	 * @param language
	 * @param stream
	 * @throws IOException
	 */
	public void train(Locale language, InputStream stream) throws IOException {
		if (!chains.containsKey(language)) {
			chains.put(language, new Matrix(256 + 1, 256 + 1));
			chainCounts.put(language, new long[256 + 1]);
		}

		final Matrix chain = chains.get(language);
		final long[] chainCount = chainCounts.get(language);

		int currentIndex = 0;
		final double[][] chainData = chain.getArray();
		int newIndex = -1;
		while ((newIndex = stream.read()) != -1) {
			newIndex += 1;
			chainData[currentIndex][newIndex] = chainData[currentIndex][newIndex] + 1;
			chainCount[currentIndex] += 1;
			currentIndex = newIndex;
		}
	}

	/**
	 * Generate a string using this model of the desired length
	 * 
	 * @param language
	 * 
	 * @param length
	 * @param encoding
	 * @return the generated string
	 * @throws UnsupportedEncodingException
	 */
	public String generate(Locale language, int length, String encoding) throws UnsupportedEncodingException {

		final Matrix chain = this.chains.get(language);
		if (chain == null)
			return null;
		final double[][] chainData = chain.getArray();
		final long[] chainCount = this.chainCounts.get(language);

		int currentIndex = 0;
		final byte[] newString = new byte[length];
		final Random r = new Random();
		for (int i = 0; i < length; i++) {
			final double prob = r.nextDouble();
			final double[] currentLine = chainData[currentIndex];
			double probSum = 0.0;
			int newIndex = 0;
			// System.out.println("CURRENT STATE:" + (char)(currentIndex-1));
			while (probSum + (currentLine[newIndex] / chainCount[currentIndex]) < prob) {
				final double probForIndex = (currentLine[newIndex++] / chainCount[currentIndex]);
				// System.out.println(probForIndex);
				// if(probForIndex > 0){
				// System.out.println("Prob to go to:" + (char)(newIndex-2) +
				// " = " + probForIndex);
				// }
				probSum += probForIndex;
			}
			// System.out.println("NEW STATE:" + (char)(newIndex-1));
			newString[i] = (byte) (newIndex - 1);
			currentIndex = newIndex;
		}

		return new String(newString, encoding);
	}

}
