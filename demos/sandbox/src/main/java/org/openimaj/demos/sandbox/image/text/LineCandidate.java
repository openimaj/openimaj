package org.openimaj.demos.sandbox.image.text;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.Pair;

public class LineCandidate {
	List<LetterCandidate> letters;
	Rectangle regularBoundingBox;

	public static List<LineCandidate> extractLines(List<LetterCandidate> letters) {

		return null;
	}

	private static List<Pair<LetterCandidate>> createLetterPairs(List<LetterCandidate> letters) {
		final List<Pair<LetterCandidate>> pairs = new ArrayList<Pair<LetterCandidate>>();

		final int numLetters = letters.size();

		for (int j = 0; j < numLetters; j++) {
			for (int i = j + 1; i < numLetters; i++) {
				final LetterCandidate l1 = letters.get(j);
				final LetterCandidate l2 = letters.get(i);

				// similar stroke width (median ratio < 2)
				if (Math.max(l1.medianStrokeWidth, l2.medianStrokeWidth)
						/ Math.min(l1.medianStrokeWidth, l2.medianStrokeWidth) > 2.0)
					continue;

				// similar height
				if (Math.max(l1.height, l2.height) / Math.min(l1.height, l2.height) > 2.0)
					continue;

				// small distance between
				final double distance = Line2d.distance(l1.centroid, l2.centroid);
				if (distance > 3 * Math.max(l1.width, l2.width))
					continue;

				// similar color
				// if (FloatFVComparison.EUCLIDEAN.compare(l1.averageColour,
				// l2.averageColour) > 1)
				// continue;

				// tests passed... merge
				pairs.add(new Pair<LetterCandidate>(l1, l2));
			}
		}

		return pairs;
	}

	private static List<LineCandidate> chainPairs(List<Pair<LetterCandidate>> pairs) {
		final List<LineCandidate> lines = new ArrayList<LineCandidate>();

		boolean didMerge = true;
		while (didMerge) {
			didMerge = false;
			for (int j = 0; j < lines.size(); j++) {
				final LineCandidate chain1 = lines.get(j);

				for (int i = 0; i < lines.size(); i++) {
					final LineCandidate chain2 = lines.get(i);

					final List<LetterCandidate> merged = merge(chain1.letters, chain2.letters);
					if (merged != null) {
						chain1.letters = merged;
						lines.remove(chain2);
						didMerge = true;
						break;
					}
				}
			}
		}

		return lines;
	}

	private static List<LetterCandidate> merge(List<LetterCandidate> chain1, List<LetterCandidate> chain2) {
		if (chain1.get(0) == chain2.get(chain2.size() - 1)) {
			// TODO check direction

			chain2.addAll(chain1);
			return chain2;
		}

		if (chain1.get(chain1.size() - 1) == chain2.get(0)) {
			// TODO check direction

			chain1.addAll(chain2);
			return chain1;
		}

		return null;
	}
}
