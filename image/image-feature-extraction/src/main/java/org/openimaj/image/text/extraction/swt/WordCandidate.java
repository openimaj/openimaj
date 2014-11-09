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
package org.openimaj.image.text.extraction.swt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.util.pair.FloatFloatPair;

/**
 * This class models a candidate word (a collection of letter candidates with a
 * consistent inter-character spacing) from the {@link SWTTextDetector}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class WordCandidate extends Candidate {
	/**
	 * The line to which this word belongs
	 */
	protected LineCandidate line;

	/**
	 * The letters in this word
	 */
	protected List<LetterCandidate> letters = new ArrayList<LetterCandidate>();

	protected static List<WordCandidate> extractWords(LineCandidate line, SWTTextDetector.Options options) {
		final List<WordCandidate> words = new ArrayList<WordCandidate>();

		// sort the letters
		Collections.sort(line.letters, new Comparator<LetterCandidate>() {
			@Override
			public int compare(LetterCandidate o1, LetterCandidate o2) {
				return o1.centroid.x - o2.centroid.x;
			}
		});

		// Collect inter-word spacings
		final float[] spacings = new float[line.letters.size() - 1];

		float mean = 0;
		int rng = 0;
		for (int i = 1; i < line.letters.size(); i++) {
			final LetterCandidate left = line.letters.get(i - 1);
			final LetterCandidate right = line.letters.get(i);

			spacings[i - 1] = Math.max(0,
					right.getRegularBoundingBox().x
							- (left.getRegularBoundingBox().x + left.getRegularBoundingBox().width));
			mean += spacings[i - 1];

			if (spacings[i - 1] >= rng)
				rng = (int) (spacings[i - 1] + 1);
		}
		mean /= spacings.length;

		// use Otsu's method to find the optimal threshold
		final FloatFloatPair threshVar = OtsuThreshold.calculateThresholdAndVariance(spacings, rng);
		final float threshold = threshVar.first;
		final float variance = threshVar.second;

		// System.out.println(Math.sqrt(variance) / mean + " " + variance + " "
		// + threshold);
		// if the variance is sufficiently high to suggest multiple words
		if (Math.sqrt(variance) > mean * options.wordBreakdownRatio)
		{
			WordCandidate word = new WordCandidate();
			word.line = line;
			word.letters.add(line.letters.get(0));
			words.add(word);
			for (int i = 0; i < spacings.length; i++) {
				if (spacings[i] > threshold) {
					word = new WordCandidate();
					words.add(word);
				}
				word.letters.add(line.letters.get(i + 1));
			}
		} else {
			final WordCandidate word = new WordCandidate();
			word.line = line;
			word.letters = line.letters;
			words.add(word);
		}

		for (final WordCandidate w : words) {
			w.regularBoundingBox = LetterCandidate.computeBounds(w.letters);

			for (final LetterCandidate letter : w.letters)
				letter.word = w;
		}

		return words;
	}

	/**
	 * Get the letters within this word.
	 * 
	 * @return the letters.
	 */
	public List<LetterCandidate> getLetters() {
		return letters;
	}

	/**
	 * Get the line containing this word.
	 * 
	 * @return the enclosing line.
	 */
	public LineCandidate getLine() {
		return line;
	}
}
