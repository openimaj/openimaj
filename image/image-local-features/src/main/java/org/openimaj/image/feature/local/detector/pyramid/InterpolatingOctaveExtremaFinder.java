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
package org.openimaj.image.feature.local.detector.pyramid;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.citation.annotation.References;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;

import Jama.Matrix;

/**
 * Implementation of the method described in
 * "Invariant Features from Interest Point Groups" by Matthew Brown and David
 * Lowe (http://www.cs.ubc.ca/~lowe/papers/brown02.pdf) for improving the
 * localisation of interest points detected in a difference-of-Gaussian by
 * fitting a 3D quadratic to the scale-space Laplacian (approximated by the
 * difference-of-Gaussian pyramid).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@References(references = {
		@Reference(
				type = ReferenceType.Inproceedings,
				author = { "Matthew Brown", "D Lowe" },
				title = "Invariant Features from Interest Point Groups",
				year = "2002",
				booktitle = "BMVC 2002: 13th British Machine Vision Conference",
				pages = { "253", "", "262" },
				month = "September"
		),
		@Reference(
				type = ReferenceType.Article,
				author = { "David Lowe" },
				title = "Distinctive image features from scale-invariant keypoints",
				year = "2004",
				journal = "IJCV",
				pages = { "91", "110" },
				month = "January",
				number = "2",
				volume = "60")
})
public class InterpolatingOctaveExtremaFinder extends BasicOctaveExtremaFinder {
	/**
	 * Default number of interpolation iterations
	 */
	public static final int DEFAULT_INTERPOLATION_ITERATIONS = 5;

	protected int numInterpolationIterations;

	private boolean[][] map;
	private int currentIteration;

	/**
	 * Default constructor using {@link #DEFAULT_MAGNITUDE_THRESHOLD} for the
	 * magnitude threshold, {@link #DEFAULT_EIGENVALUE_RATIO} for the Eigenvalue
	 * ratio threshold and {@link #DEFAULT_INTERPOLATION_ITERATIONS} for the
	 * number of iterations.
	 */
	public InterpolatingOctaveExtremaFinder() {
		this(DEFAULT_MAGNITUDE_THRESHOLD, DEFAULT_EIGENVALUE_RATIO, DEFAULT_INTERPOLATION_ITERATIONS);
	}

	/**
	 * Construct with the given magnitude threshold,
	 * {@link #DEFAULT_EIGENVALUE_RATIO} for the Eigenvalue ratio threshold and
	 * {@link #DEFAULT_INTERPOLATION_ITERATIONS} for the number of iterations.
	 * 
	 * @param magnitudeThreshold
	 *            the magnitude threshold
	 */
	public InterpolatingOctaveExtremaFinder(float magnitudeThreshold) {
		this(magnitudeThreshold, DEFAULT_EIGENVALUE_RATIO, DEFAULT_INTERPOLATION_ITERATIONS);
	}

	/**
	 * Construct with the given magnitude and Eigenvalue thresholds andnumber of
	 * iterations.
	 * 
	 * @param magnitudeThreshold
	 *            the magnitude threshold
	 * @param eigenvalueRatio
	 *            the Eigenvalue threshold
	 * @param numInterpolationIterations
	 *            the number of interpolation iterations
	 */
	public InterpolatingOctaveExtremaFinder(float magnitudeThreshold, float eigenvalueRatio,
			int numInterpolationIterations)
	{
		super(magnitudeThreshold, eigenvalueRatio);
		this.numInterpolationIterations = numInterpolationIterations;
	}

	@Override
	public void process(GaussianOctave<FImage> octave) {
		map = new boolean[octave.images[0].height][octave.images[0].width];

		super.process(octave);
	}

	@Override
	protected void processExtrema(FImage[] dogs, int s, int x, int y, float octSize) {
		currentIteration = 0;
		processExtremaInternal(dogs, s, x, y, octSize);
	}

	protected void processExtremaInternal(FImage[] dogs, int s, int x, int y, float octSize) {
		int newy = y, newx = x;

		// fit 3d quadratic
		final FitResult fit = fitQuadratic3D(dogs, s, x, y);

		if (fit.offset.get(1, 0) > 0.5 && y < dogs[0].height - octave.options.getBorderPixels())
			newy++;
		if (fit.offset.get(1, 0) < -0.5 && y > octave.options.getBorderPixels())
			newy--;
		if (fit.offset.get(2, 0) > 0.5 && x < dogs[0].width - octave.options.getBorderPixels())
			newx++;
		if (fit.offset.get(2, 0) < -0.5 && x > octave.options.getBorderPixels())
			newx--;
		if (currentIteration < numInterpolationIterations && (newy != y || newx != x)) {
			currentIteration++;
			processExtremaInternal(dogs, s, newx, newy, octSize);
			return;
		}

		// if the offset is too great, or the peak doesn't have enough contrast
		// w.r.t. the threshold, then stop
		if (Math.abs(fit.offset.get(0, 0)) > 1.5 || Math.abs(fit.offset.get(1, 0)) > 1.5
				|| Math.abs(fit.offset.get(2, 0)) > 1.5 || Math.abs(fit.peakval) < normMagnitudeScales)
			return;

		// avoid duplicates by checking a map of locations
		if (map[y][x] == true)
			return;
		map[y][x] = true;

		// calculate the new scale
		final float octaveScale = octave.options.getInitialSigma()
				* (float) Math.pow(2.0, (s + fit.offset.get(0, 0)) / octave.options.getScales());

		// fire the listener
		if (listener != null)
			listener.foundInterestPoint(this, x + (float) fit.offset.get(2, 0), y + (float) fit.offset.get(1, 0),
					octaveScale);
	}

	class FitResult {
		Matrix offset; // the offset of 0 or the peak
		float peakval; // the interpolated value at the peak
	}

	// fit a quadratic around the point
	FitResult fitQuadratic3D(FImage[] dogs, int s, int x, int y) {
		final float[][] dog0 = dogs[s - 1].pixels;
		final float[][] dog1 = dogs[s].pixels;
		final float[][] dog2 = dogs[s + 1].pixels;

		final Matrix H = new Matrix(3, 3); // hessian matrix; derivatives
											// estimated by differences across
											// scales
		H.set(0, 0, dog0[y][x] - 2.0 * dog1[y][x] + dog2[y][x]);
		H.set(0, 1, ((dog2[y + 1][x] - dog2[y - 1][x]) - (dog0[y + 1][x] - dog0[y - 1][x])) / 4.0);
		H.set(0, 2, ((dog2[y][x + 1] - dog2[y][x - 1]) - (dog0[y][x + 1] - dog0[y][x - 1])) / 4.0);

		H.set(1, 0, H.get(0, 1));
		H.set(1, 1, dog1[y - 1][x] - 2.0 * dog1[y][x] + dog1[y + 1][x]);
		H.set(1, 2, ((dog1[y + 1][x + 1] - dog1[y + 1][x - 1]) - (dog1[y - 1][x + 1] - dog1[y - 1][x - 1])) / 4.0);

		H.set(2, 0, H.get(0, 2));
		H.set(2, 1, H.get(1, 2));
		H.set(2, 2, dog1[y][x - 1] - 2.0 * dog1[y][x] + dog1[y][x + 1]);

		// gradient vector
		final Matrix gM = new Matrix(
				new double[][] {
						{ (dog2[y][x] - dog0[y][x]) / 2.0f }, { (dog1[y + 1][x] - dog1[y - 1][x]) / 2.0f }, { (dog1[y][x + 1] - dog1[y][x - 1]) / 2.0f }
				});

		final Matrix offsetM = H.solve(gM.times(-1));

		final FitResult result = new FitResult();
		result.offset = offsetM;

		final float dp = (float) (offsetM.get(0, 0) * gM.get(0, 0) + offsetM.get(1, 0) * gM.get(1, 0) + offsetM.get(2, 0)
				* gM.get(2, 0));
		result.peakval = (dog1[y][x] + 0.5f * dp);

		return result;
	}
}
