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
