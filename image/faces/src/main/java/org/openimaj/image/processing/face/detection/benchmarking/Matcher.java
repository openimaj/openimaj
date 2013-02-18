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
package org.openimaj.image.processing.face.detection.benchmarking;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.math.combinatorics.optimisation.HungarianAlgorithm;
import org.openimaj.math.geometry.shape.Polygon;

public class Matcher {
	public static class Match {
		DetectedFace groundTruth;
		DetectedFace detected;
		double score;

		public Match(DetectedFace groundTruth, DetectedFace detected, double score) {
			this.groundTruth = groundTruth;
			this.detected = detected;
			this.score = score;
		}

		@Override
		public String toString() {
			return String.format("%s->%s : %f", groundTruth, detected, score);
		}
	}

	private double[][] computePairwiseScores(
			List<? extends DetectedFace> groundTruth,
			List<? extends DetectedFace> detected)
	{
		final int numGround = groundTruth.size();
		final int numDet = detected.size();
		final double[][] scores = new double[numGround][numDet];

		for (int i = 0; i < numGround; i++) {
			for (int j = 0; j < numDet; j++) {
				scores[i][j] = computeScore(groundTruth.get(i), detected.get(j));
			}
		}

		return scores;
	}

	private double computeScore(DetectedFace ground, DetectedFace detected) {
		final Polygon gs = ground.getShape().asPolygon();
		final Polygon ds = detected.getShape().asPolygon();

		return gs.intersect(ds).calculateArea() / gs.union(ds).calculateArea();
	}

	public List<Match> match(
			List<? extends DetectedFace> groundTruth,
			List<? extends DetectedFace> detected)
	{
		final double[][] scores = computePairwiseScores(groundTruth, detected);
		final double[][] filtered = collapse(scores);

		if (filtered.length == 0 || filtered[0].length == 0)
			return null;

		final HungarianAlgorithm ha = new HungarianAlgorithm(filtered);
		final int[] assignments = ha.execute();

		final List<Match> results = new ArrayList<Match>();
		for (int gtIdx = 0; gtIdx < assignments.length; gtIdx++) {
			if (assignments[gtIdx] >= 0) {
				final int detIdx = assignments[gtIdx];
				final double score = scores[gtIdx][detIdx];

				if (score > 0) {
					results.add(new Match(groundTruth.get(gtIdx), detected.get(detIdx), score));
				}
			}
		}
		return results;
	}

	/**
	 * Remove any rows or cols that sum to zero.
	 * 
	 * @param scores
	 * @return
	 */
	private double[][] collapse(double[][] scores) {
		// rows:
		final TIntArrayList rowsToKeep = new TIntArrayList(scores[0].length);
		for (int r = 0; r < scores.length; r++) {
			double sum = 0;
			for (int c = 0; c < scores[0].length; c++) {
				sum += scores[r][c];
			}
			if (sum != 0)
				rowsToKeep.add(r);
		}

		// cols:
		final TIntArrayList colsToKeep = new TIntArrayList(scores.length);
		for (int c = 0; c < scores[0].length; c++) {
			double sum = 0;
			for (int r = 0; r < scores.length; r++) {
				sum += scores[r][c];
			}
			if (sum != 0)
				colsToKeep.add(c);
		}

		if (rowsToKeep.size() == scores[0].length && colsToKeep.size() == scores.length)
			return scores;

		final double[][] filtered = new double[rowsToKeep.size()][colsToKeep.size()];

		for (int r = 0; r < filtered.length; r++) {
			for (int c = 0; c < filtered[0].length; c++) {
				filtered[r][c] = scores[rowsToKeep.get(r)][colsToKeep.get(c)];
			}
		}

		return filtered;
	}
}
