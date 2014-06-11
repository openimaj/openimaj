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
package org.openimaj.image.pixel.statistics;

import java.util.Arrays;

import org.apache.commons.math.DimensionMismatchException;
import org.apache.commons.math.stat.descriptive.MultivariateSummaryStatistics;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.sampling.FLineSampler;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.util.FloatArrayStatsUtils;
import org.openimaj.util.array.ArrayUtils;

import Jama.Matrix;

/**
 * An {@link MBFStatisticalPixelProfileModel} is a statistical model of pixels
 * from an {@link MBFImage} sampled along a line.
 * 
 * The model allows for various sampling strategies (see {@link FLineSampler})
 * and uses the mean and covariance as its internal state.
 * 
 * The model is updateable, but does not hold on to previously seen samples to
 * reduce memory usage.
 * 
 * Internally, the model is one-dimensional, and is created by stacking the
 * samples from each image band.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@SuppressWarnings("deprecation")
public class MBFStatisticalPixelProfileModel implements PixelProfileModel<MBFImage> {
	private MultivariateSummaryStatistics statistics;
	private int nsamples;
	private FLineSampler sampler;

	private double[] mean;
	private Matrix invCovar;

	/**
	 * Construct a new {@link MBFStatisticalPixelProfileModel} with the given
	 * number of samples per line, and the given sampling strategy.
	 * 
	 * @param nsamples
	 *            number of samples
	 * @param sampler
	 *            line sampling strategy
	 */
	public MBFStatisticalPixelProfileModel(int nsamples, FLineSampler sampler) {
		this.nsamples = nsamples;
		this.statistics = new MultivariateSummaryStatistics(nsamples, true);
		this.sampler = sampler;
	}

	private float[] normaliseSamples(float[] samples) {
		final float sum = FloatArrayStatsUtils.sum(samples);

		for (int i = 0; i < samples.length; i++) {
			samples[i] /= sum;
		}

		return samples;
	}

	@Override
	public void updateModel(MBFImage image, Line2d line) {
		final float[] samples = extractNormalisedStacked(image, line);

		try {
			statistics.addValue(ArrayUtils.convertToDouble(samples));
		} catch (final DimensionMismatchException e) {
			throw new RuntimeException(e);
		}

		invCovar = null;
		mean = null;
	}

	private float[][] extract(MBFImage image, Line2d line, int numSamples) {
		final float[][] samples = new float[image.numBands()][];

		for (int i = 0; i < image.numBands(); i++) {
			samples[i] = sampler.extractSamples(line, image.getBand(i), numSamples);
		}

		return samples;
	}

	/**
	 * Extract normalised stacked samples b1b1b1bb2b2b2b3b3b3...
	 * 
	 * @param image
	 * @param line
	 * @return
	 */
	private float[] extractNormalisedStacked(MBFImage image, Line2d line) {
		final float[] samples = new float[nsamples * image.numBands()];

		for (int i = 0; i < image.numBands(); i++) {
			final float[] s = sampler.extractSamples(line, image.getBand(i), nsamples);
			System.arraycopy(s, 0, samples, i * nsamples, nsamples);
		}

		return normaliseSamples(samples);
	}

	/**
	 * @return the mean of the model
	 */
	public double[] getMean() {
		if (mean == null) {
			mean = statistics.getMean();
			invCovar = new Matrix(statistics.getCovariance().getData()).inverse();
		}

		return mean;
	}

	/**
	 * @return the covariance of the model
	 */
	public Matrix getCovariance() {
		if (mean == null) {
			mean = statistics.getMean();
			invCovar = new Matrix(statistics.getCovariance().getData()).inverse();
		}
		return new Matrix(statistics.getCovariance().getData());
	}

	/**
	 * @return the inverse of the covariance matrix
	 */
	public Matrix getInverseCovariance() {
		if (mean == null) {
			mean = statistics.getMean();
			invCovar = new Matrix(statistics.getCovariance().getData()).inverse();
		}
		return invCovar;
	}

	/**
	 * Compute the Mahalanobis distance of the given vector to the internal
	 * model. The vector must have the same size as the number of samples given
	 * during construction.
	 * 
	 * @param vector
	 *            the vector
	 * @return the computed Mahalanobis distance
	 */
	public float computeMahalanobis(float[] vector) {
		if (mean == null) {
			mean = statistics.getMean();
			try {
				invCovar = new Matrix(statistics.getCovariance().getData()).inverse();
			} catch (final RuntimeException e) {
				invCovar = Matrix.identity(nsamples, nsamples);
			}
		}

		final double[] meanCentered = new double[mean.length];
		for (int i = 0; i < mean.length; i++) {
			meanCentered[i] = vector[i] - mean[i];
		}

		final Matrix mct = new Matrix(new double[][] { meanCentered });
		final Matrix mc = mct.transpose();

		final Matrix dist = mct.times(invCovar).times(mc);

		return (float) dist.get(0, 0);
	}

	/**
	 * Compute the Mahalanobis distance of a vector of samples extracted along a
	 * line in the given image to the internal model.
	 * 
	 * @param image
	 *            the image to sample
	 * @param line
	 *            the line to sample along
	 * @return the computed Mahalanobis distance
	 */
	public float computeMahalanobis(MBFImage image, Line2d line) {
		final float[] samples = extractNormalisedStacked(image, line);
		return computeMahalanobis(samples);
	}

	/**
	 * Extract numSamples samples from the line in the image and then compare
	 * this model at each overlapping position starting from the first sample at
	 * the beginning of the line.
	 * 
	 * numSamples must be bigger than the number of samples used to construct
	 * the model. In addition, callers are responsible for ensuring the sampling
	 * rate between the new samples and the model is equal.
	 * 
	 * @param image
	 *            the image to sample
	 * @param line
	 *            the line to sample along
	 * @param numSamples
	 *            the number of samples to make
	 * @return an array of the computed Mahalanobis distances at each offset
	 */
	public float[] computeMahalanobisWindowed(MBFImage image, Line2d line, int numSamples) {
		final float[][] samples = extract(image, line, numSamples);
		return computeMahalanobisWindowed(samples);
	}

	@Override
	public Point2d computeNewBest(MBFImage image, Line2d line, int numSamples) {
		final float[] resp = computeMahalanobisWindowed(image, line, numSamples);

		final int minIdx = ArrayUtils.minIndex(resp);
		final int offset = (numSamples - nsamples) / 2;

		if (resp[offset] == resp[minIdx]) // prefer the centre over another
											// value if same response
			return line.calculateCentroid();

		// the sample line might be different, so we need to measure relative to
		// it...
		line = this.sampler.getSampleLine(line, image.getBand(0), numSamples);

		final float x = line.begin.getX();
		final float y = line.begin.getY();
		final float dxStep = (line.end.getX() - x) / (numSamples - 1);
		final float dyStep = (line.end.getY() - y) / (numSamples - 1);

		return new Point2dImpl(x + (minIdx + offset) * dxStep, y + (minIdx + offset) * dyStep);
	}

	@Override
	public float computeMovementDistance(MBFImage image, Line2d line, int numSamples, Point2d pt) {
		final Line2d sampleLine = sampler.getSampleLine(line, image.getBand(0), numSamples);

		return (float) (2 * Line2d.distance(sampleLine.calculateCentroid(), pt) / sampleLine.calculateLength());
	}

	/**
	 * Compare this model at each overlapping position of the given vector
	 * starting from the first sample and return the distance for each overlap.
	 * 
	 * The length of the vector must be bigger than the number of samples used
	 * to construct the model. In addition, callers are responsible for ensuring
	 * the sampling rate between the new samples and the model is equal.
	 * 
	 * @param vector
	 *            array of samples, one vector per band
	 * @return an array of the computed Mahalanobis distances at each offset
	 */
	public float[] computeMahalanobisWindowed(float[][] vector) {
		final int maxShift = vector.length - nsamples + 1;

		final float[] responses = new float[maxShift];
		float[] samples = new float[nsamples * vector.length];
		for (int i = 0; i < maxShift; i++) {
			for (int j = 0; j < vector.length; j++) {
				System.arraycopy(vector[j], i, samples, nsamples * j, nsamples);
			}
			samples = normaliseSamples(samples);
			responses[i] = computeMahalanobis(samples);
		}

		return responses;
	}

	@Override
	public String toString() {
		return "\nPixelProfileModel[\n" +
				"\tcount = " + statistics.getN() + "\n" +
				"\tmean = " + Arrays.toString(statistics.getMean()) + "\n" +
				"\tcovar = " + statistics.getCovariance() + "\n" +
				"]";
	}

	/**
	 * @return the number of samples used along each profile line
	 */
	public int getNumberSamples() {
		return nsamples;
	}

	/**
	 * @return the sampler used by the model to extract samples along profiles
	 */
	public FLineSampler getSampler() {
		return sampler;
	}

	@Override
	public float computeCost(MBFImage image, Line2d line) {
		return computeMahalanobis(image, line);
	}
}
