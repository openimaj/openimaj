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
package org.openimaj.demos.sandbox.image.text;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.math.geometry.shape.Rectangle;

public class WordCandidate {
	List<LetterCandidate> letters = new ArrayList<LetterCandidate>();
	Rectangle regularBoundingBox;

	public static List<WordCandidate> extractWords(LineCandidate line) {
		final List<WordCandidate> words = new ArrayList<WordCandidate>();

		// Collect inter-word spacings
		final float[] spacings = new float[line.letters.size() - 1];

		// ...

		// if the variance is sufficiently high to suggest multiple words
		// float var = FloatArrayStatsUtils.var(spacings);
		// if (var > splitThresh)

		// use Otsu's method to find the optimal threshold
		final float threshold = OtsuThreshold.calculateThreshold(spacings, 10);

		WordCandidate word = new WordCandidate();
		words.add(word);
		for (int i = 0; i < spacings.length; i++) {
			word.letters.add(line.letters.get(i));
			if (spacings[i] < threshold) {
				word.letters.add(line.letters.get(i + 1));
			} else {
				word = new WordCandidate();
				words.add(word);

				word.letters.add(line.letters.get(i + 1));
			}
		}

		for (final WordCandidate w : words) {
			w.regularBoundingBox = LetterCandidate.computeBounds(w.letters);
		}

		return words;
	}
}
