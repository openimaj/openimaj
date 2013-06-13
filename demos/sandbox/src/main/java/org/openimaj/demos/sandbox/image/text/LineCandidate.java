package org.openimaj.demos.sandbox.image.text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.Pair;
import org.openimaj.util.set.DisjointSetForest;

public class LineCandidate {
	List<LetterCandidate> letters = new ArrayList<LetterCandidate>();
	Rectangle regularBoundingBox;

	LineCandidate() {
	}

	public static List<LineCandidate> extractLines(List<LetterCandidate> letters) {
		final List<Pair<LetterCandidate>> pairs = createLetterPairs(letters);

		final Set<Set<Pair<LetterCandidate>>> sets = DisjointSetForest.partitionSubsets(pairs,
				new Comparator<Pair<LetterCandidate>>() {
					@Override
					public int compare(Pair<LetterCandidate> pair1, Pair<LetterCandidate> pair2) {
						final Pixel pair1d = computeDelta(pair1.firstObject(), pair1.secondObject());
						final Pixel pair2d = computeDelta(pair2.firstObject(), pair2.secondObject());

						if (pair1.firstObject() == pair2.firstObject() || pair1.secondObject() == pair2.secondObject())
						{
							final int tn = pair1d.y * pair2d.x - pair1d.x * pair2d.y;
							final int td = pair1d.x * pair2d.x + pair1d.y * pair2d.y;
							// share the same end, opposite direction
							if (tn * 7 < -td * 4 && tn * 7 > td * 4)
								return 0;
						} else if (pair1.firstObject() == pair2.secondObject()
								|| pair1.secondObject() == pair2.firstObject())
						{
							final int tn = pair1d.y * pair2d.x - pair1d.x * pair2d.y;
							final int td = pair1d.x * pair2d.x + pair1d.y * pair2d.y;
							// share the other end, same direction
							if (tn * 7 < td * 4 && tn * 7 > -td * 4)
								return 0;
						}

						return 1;
					}

					private Pixel computeDelta(LetterCandidate firstObject, LetterCandidate secondObject) {
						final Rectangle frect = firstObject.regularBoundingBox;
						final Rectangle srect = secondObject.regularBoundingBox;

						final int dx = (int) (frect.x - srect.x + (frect.width - srect.width) / 2);
						final int dy = (int) (frect.y - srect.y + (frect.height - srect.height) / 2);
						return new Pixel(dx, dy);
					}
				});

		final List<LineCandidate> chains = new ArrayList<LineCandidate>();
		for (final Set<Pair<LetterCandidate>> line : sets) {
			final Set<LetterCandidate> lcs = new HashSet<LetterCandidate>();

			for (final Pair<LetterCandidate> p : line) {
				lcs.add(p.firstObject());
				lcs.add(p.secondObject());
			}

			final LineCandidate lc = new LineCandidate();
			lc.letters = new ArrayList<LetterCandidate>(lcs);
			lc.regularBoundingBox = LetterCandidate.computeBounds(lc.letters);
			chains.add(lc);
		}

		computeBounds(chains);

		return chains;// chainPairs(pairs);
	}

	private static void computeBounds(List<LineCandidate> lines) {
		for (final LineCandidate line : lines) {
			line.regularBoundingBox = LetterCandidate.computeBounds(line.letters);
		}
	}

	private static List<Pair<LetterCandidate>> createLetterPairs(List<LetterCandidate> letters) {
		final List<Pair<LetterCandidate>> pairs = new ArrayList<Pair<LetterCandidate>>();

		final int numLetters = letters.size();

		for (int j = 0; j < numLetters; j++) {
			final LetterCandidate l1 = letters.get(j);

			for (int i = j + 1; i < numLetters; i++) {
				final LetterCandidate l2 = letters.get(i);

				// similar stroke width (median ratio < 2)
				if (Math.max(l1.medianStrokeWidth, l2.medianStrokeWidth)
						/ Math.min(l1.medianStrokeWidth, l2.medianStrokeWidth) > 2.0)
					continue;

				// similar height
				if (Math.max(l1.regularBoundingBox.height, l2.regularBoundingBox.height)
						/ Math.min(l1.regularBoundingBox.height, l2.regularBoundingBox.height) > 2.0)
					continue;

				// similar color
				if (Math.abs(l1.averageBrightness - l2.averageBrightness) > 0.12f)
					continue;

				// small distance between
				final double distance = l1.centroid.x - l2.centroid.x;
				if (Math.abs(distance) > 3 * Math.max(l1.regularBoundingBox.width, l2.regularBoundingBox.width))
					continue;

				// approximately level
				final int oy = (int) (Math.min(l1.regularBoundingBox.y + l1.regularBoundingBox.height,
						l2.regularBoundingBox.y + l2.regularBoundingBox.height) - Math.max(l1.regularBoundingBox.y,
						l2.regularBoundingBox.y));
				if (oy * 1.3f < Math.min(l1.regularBoundingBox.height, l2.regularBoundingBox.height))
					continue;

				// tests passed... merge
				pairs.add(new Pair<LetterCandidate>(l1, l2));
			}
		}

		return pairs;
	}
}
