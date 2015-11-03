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
package org.openimaj.image.objectdetection.haar.training;

import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.ObjectFloatPair;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.Parallel.IntRange;

public class StumpClassifier {
	public static class WeightedLearner {
		// Trains using Error = \sum_{i=1}^{N} D_i * [y_i != h(x_i)]
		// and h(x) = classifier.sign * (2 * [xclassifier.dimension >
		// classifier.threshold] - 1)
		public ObjectFloatPair<StumpClassifier> learn(final HaarTrainingData trainingSet, final float[] _weights) {
			final StumpClassifier classifier = new StumpClassifier();

			// Search for minimum training set error
			final float[] minimumError = { Float.POSITIVE_INFINITY };

			final boolean[] classes = trainingSet.getClasses();
			final int nInstances = trainingSet.numInstances();

			// Determine total potential error
			float totalErrorC = 0.0f;
			for (int i = 0; i < nInstances; i++)
				totalErrorC += _weights[i];
			final float totalError = totalErrorC;

			// Initialise search error
			float initialErrorC = 0.0f;
			for (int i = 0; i < nInstances; i++)
				initialErrorC += !classes[i] ? _weights[i] : 0.0;
			final float initialError = initialErrorC;

			// Loop over possible dimensions
			// for (int d = 0; d < trainingSet.numFeatures(); d++) {
			Parallel.forRange(0, trainingSet.numFeatures(), 1, new Operation<IntRange>() {
				@Override
				public void perform(IntRange rng) {
					final StumpClassifier currClassifier = new StumpClassifier();
					currClassifier.dimension = -1;
					currClassifier.threshold = Float.NaN;
					currClassifier.sign = 0;

					float currMinimumError = Float.POSITIVE_INFINITY;

					for (int d = rng.start; d < rng.stop; d += rng.incr) {
						// Pre-sort data-items in dimension for efficient
						// threshold
						// search
						final float[] data = trainingSet.getResponses(d);
						final int[] indices = trainingSet.getSortedIndices(d);

						// Initialise search error
						float currentError = initialError;

						// Search through the sorted list to determine best
						// threshold
						for (int i = 0; i < nInstances - 1; i++) {
							// Update current error
							final int index = indices[i];
							if (classes[index])
								currentError += _weights[index];
							else
								currentError -= _weights[index];

							// Check for repeated values
							if (data[indices[i]] == data[indices[i + 1]])
								continue;

							// Compute the test threshold - maximises the margin
							// between
							// potential thresholds
							final float testThreshold = (data[indices[i]] + data[indices[i + 1]]) / 2.0f;

							// Compare to current best
							if (currentError < currMinimumError) // Good
							// classifier
							// with
							// classifier.sign
							// =
							// +1
							{
								currMinimumError = currentError;
								currClassifier.dimension = d;
								currClassifier.threshold = testThreshold;
								currClassifier.sign = +1;
							}
							if ((totalError - currentError) < currMinimumError) // Good
							// classifier
							// with
							// classifier.sign
							// =
							// -1
							{
								currMinimumError = (totalError - currentError);
								currClassifier.dimension = d;
								currClassifier.threshold = testThreshold;
								currClassifier.sign = -1;
							}
						}
					}

					synchronized (classifier) {
						if (currMinimumError < minimumError[0]) {
							minimumError[0] = currMinimumError;
							classifier.dimension = currClassifier.dimension;
							classifier.sign = currClassifier.sign;
							classifier.threshold = currClassifier.threshold;
						}
					}
				}
			});

			return new ObjectFloatPair<StumpClassifier>(classifier, minimumError[0]);
		}
	}

	int dimension;
	float threshold;
	int sign;

	public boolean classify(float[] instanceFeature) {
		return (instanceFeature[dimension] > threshold ? sign : -sign) == 1 ? true : false;
	}

	public boolean classify(float f) {
		return (f > threshold ? sign : -sign) == 1 ? true : false;
	}
}
