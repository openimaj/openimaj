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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.Pair;
import org.openimaj.util.set.DisjointSetForest;

/**
 * This class models a candidate line of text, with one of more word candidates
 * within it, from the {@link SWTTextDetector}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class LineCandidate extends Candidate {
	protected List<LetterCandidate> letters = new ArrayList<LetterCandidate>();
	protected List<WordCandidate> words;

	protected LineCandidate() {
	}

	/**
	 * Computes lines of text by merging pairs of characters that have similar
	 * directions.
	 * 
	 * @param letters
	 * @param options
	 * @return
	 */
	protected static List<LineCandidate> extractLines(List<LetterCandidate> letters, SWTTextDetector.Options options) {
		final List<Pair<LetterCandidate>> pairs = createLetterPairs(letters, options);

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

			if (lcs.size() < options.minLettersPerLine)
				continue;

			final LineCandidate lc = new LineCandidate();
			lc.letters = new ArrayList<LetterCandidate>(lcs);

			// set the line
			for (final LetterCandidate letter : lc.letters)
				letter.line = lc;

			lc.regularBoundingBox = LetterCandidate.computeBounds(lc.letters);
			lc.words = WordCandidate.extractWords(lc, options);

			chains.add(lc);
		}

		return chains;
	}

	/**
	 * Compute all likely pairs of letters on the basis that they close
	 * together, have similar stroke widths & similar heights.
	 * 
	 * @param letters
	 *            the candidate letters
	 * @param options
	 *            the options
	 * @return a list of potentially valid pairs
	 */
	private static List<Pair<LetterCandidate>> createLetterPairs(List<LetterCandidate> letters,
			SWTTextDetector.Options options)
	{
		final List<Pair<LetterCandidate>> pairs = new ArrayList<Pair<LetterCandidate>>();

		final int numLetters = letters.size();

		for (int j = 0; j < numLetters; j++) {
			final LetterCandidate l1 = letters.get(j);

			for (int i = j + 1; i < numLetters; i++) {
				final LetterCandidate l2 = letters.get(i);

				// similar stroke width (median ratio < 2)
				if (Math.max(l1.medianStrokeWidth, l2.medianStrokeWidth)
						/ Math.min(l1.medianStrokeWidth, l2.medianStrokeWidth) > options.medianStrokeWidthRatio)
					continue;

				// similar height
				if (Math.max(l1.regularBoundingBox.height, l2.regularBoundingBox.height)
						/ Math.min(l1.regularBoundingBox.height, l2.regularBoundingBox.height) > options.letterHeightRatio)
					continue;

				// similar color (technically intensity)
				if (Math.abs(l1.averageBrightness - l2.averageBrightness) > options.intensityThreshold)
					continue;

				// small distance between
				final double distance = l1.centroid.x - l2.centroid.x;
				if (Math.abs(distance) > options.widthMultiplier
						* Math.max(l1.regularBoundingBox.width, l2.regularBoundingBox.width))
					continue;

				// approximately level
				// FIXME: vertical text...
				final int oy = (int) (Math.min(l1.regularBoundingBox.y +
						l1.regularBoundingBox.height,
						l2.regularBoundingBox.y + l2.regularBoundingBox.height) -
						Math.max(l1.regularBoundingBox.y,
								l2.regularBoundingBox.y));
				if (oy * options.intersectRatio < Math.min(l1.regularBoundingBox.height,
						l2.regularBoundingBox.height))
					continue;

				// tests passed... merge
				pairs.add(new Pair<LetterCandidate>(l1, l2));
			}
		}

		return pairs;
	}

	/**
	 * Get the letters corresponding to this line.
	 * 
	 * @return the letters
	 */
	public List<LetterCandidate> getLetters() {
		return this.letters;
	}

	/**
	 * Get the words corresponding to this line
	 * 
	 * @return the words
	 */
	public List<WordCandidate> getWords() {
		return words;
	}
}
