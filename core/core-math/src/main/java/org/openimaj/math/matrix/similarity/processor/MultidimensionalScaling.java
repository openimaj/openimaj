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
package org.openimaj.math.matrix.similarity.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.matrix.similarity.SimilarityMatrix;
import org.openimaj.util.pair.IndependentPair;

/**
 * Implementation of Multidimensional Scaling.
 * <p>
 * Implementation originally based around Toby Segaran's python code.
 * 
 * @see "http://blog.kiwitobes.com/?p=44"
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MultidimensionalScaling implements SimilarityMatrixProcessor {
	protected Random rng = new Random();
	protected int numIterations = 1000;
	protected double rate = 0.01;
	protected List<IndependentPair<String, Point2d>> points;

	/**
	 * Default constructor. Sets the learning rate at 0.01 and the maximum
	 * number of iterations to 1000.
	 */
	public MultidimensionalScaling() {
		// do nothing
	}

	/**
	 * Construct with the given random number generator and default learning
	 * rate at 0.01 and the maximum number of iterations to 1000.
	 * 
	 * @param rng
	 *            the random number generator
	 */
	public MultidimensionalScaling(Random rng) {
		this.rng = rng;
	}

	/**
	 * Construct MDS with the given maximum number of iterations and rate.
	 * 
	 * @param numIterations
	 *            number of iterations
	 * @param rate
	 *            learning rate
	 */
	public MultidimensionalScaling(int numIterations, double rate) {
		this.numIterations = numIterations;
		this.rate = rate;
	}

	/**
	 * Construct MDS with the given maximum number of iterations, rate and
	 * random number generator.
	 * 
	 * @param numIterations
	 *            number of iterations
	 * @param rate
	 *            learning rate
	 * @param rng
	 *            the random number generator
	 */
	public MultidimensionalScaling(int numIterations, double rate, Random rng) {
		this.numIterations = numIterations;
		this.rate = rate;
		this.rng = rng;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.math.matrix.similarity.processor.SimilarityMatrixProcessor
	 * #process(org.openimaj.math.matrix.similarity.SimilarityMatrix)
	 */
	@Override
	public void process(SimilarityMatrix matrix) {
		final int sz = matrix.getRowDimension();

		final double[][] realDists = matrix.process(new NormaliseData(true)).getArray();

		// initialise points randomly
		points = new ArrayList<IndependentPair<String, Point2d>>(sz);
		for (int i = 0; i < sz; i++) {
			points.add(new IndependentPair<String, Point2d>(matrix.getIndexValue(i), Point2dImpl.createRandomPoint()));
		}

		final Point2dImpl[] grad = new Point2dImpl[sz];
		for (int i = 0; i < sz; i++)
			grad[i] = new Point2dImpl();

		double lastError = Double.MAX_VALUE;
		final double[][] fakeDists = new double[sz][sz];
		for (int m = 0; m < numIterations; m++) {
			for (int r = 0; r < sz; r++) {
				for (int c = r + 1; c < sz; c++) {
					final double dist = Line2d.distance(points.get(r).secondObject(), points.get(c).secondObject());
					fakeDists[r][c] = dist;
					fakeDists[c][r] = dist;
				}
			}

			for (int i = 0; i < sz; i++) {
				grad[i].x = 0;
				grad[i].y = 0;
			}

			double totalError = 0;
			for (int k = 0; k < sz; k++) {
				for (int j = 0; j < sz; j++) {
					if (k == j)
						continue;

					final double errorterm = (fakeDists[j][k] - realDists[j][k]) / realDists[j][k];

					grad[k].x += ((((Point2dImpl) points.get(k).secondObject()).x - points.get(j).secondObject().getX()) / fakeDists[j][k])
							* errorterm;
					grad[k].y += ((((Point2dImpl) points.get(k).secondObject()).y - points.get(j).secondObject().getY()) / fakeDists[j][k])
							* errorterm;

					totalError += Math.abs(errorterm);
				}
			}

			if (lastError < totalError)
				break;
			lastError = totalError;

			for (int k = 0; k < sz; k++) {
				((Point2dImpl) points.get(k).secondObject()).x -= rate * grad[k].x;
				((Point2dImpl) points.get(k).secondObject()).y -= rate * grad[k].y;
			}
		}
	}

	/**
	 * Get a list of the 2-D coordinates learned by the MDS algorithm for each
	 * element in the input similarity matrix.
	 * 
	 * @return list of <index, point>
	 */
	public List<IndependentPair<String, Point2d>> getPoints() {
		return points;
	}

	/**
	 * Get the predicted point for a specific element.
	 * 
	 * @param key
	 *            the element identifier
	 * @return the predicted point, or null if the key was not found.
	 */
	public Point2d getPoint(String key) {
		for (final IndependentPair<String, Point2d> pair : points)
			if (pair.firstObject().equals(key))
				return pair.secondObject();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (points == null)
			return super.toString();

		final StringBuilder sb = new StringBuilder();

		for (final IndependentPair<String, Point2d> pair : points) {
			sb.append(String.format("%s\t%4.3f\t%4.3f\n", pair.firstObject(), pair.secondObject().getX(), pair
					.secondObject().getY()));
		}

		return sb.toString();
	}
}
