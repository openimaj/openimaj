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
package org.openimaj.image.processing.convolution;

import odk.lang.FastMath;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * Image processor for calculating gradients and orientations using
 * finite-differences. Both signed (+/- PI) orientations and unsigned (+/- PI/2)
 * are computable.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class FImageGradients implements ImageAnalyser<FImage> {
	/**
	 * Modes of operation for signed and unsigned orientations
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public enum Mode {
		/**
		 * Unsigned orientations in +/- PI/2 computed using <code>atan</code>.
		 */
		Unsigned(-PI_OVER_TWO_FLOAT, PI_OVER_TWO_FLOAT) {
			@Override
			void gradientMagnitudesAndOrientations(FImage image, FImage magnitudes, FImage orientations) {
				FImageGradients.gradientMagnitudesAndUnsignedOrientations(image, magnitudes, orientations);
			}
		},
		/**
		 * Signed orientations +/- PI computed using <code>atan2</code>.
		 */
		Signed(-PI_FLOAT, PI_FLOAT) {
			@Override
			void gradientMagnitudesAndOrientations(FImage image, FImage magnitudes, FImage orientations) {
				FImageGradients.gradientMagnitudesAndOrientations(image, magnitudes, orientations);
			}
		};

		private float min;
		private float max;

		private Mode(float min, float max) {
			this.min = min;
			this.max = max;
		}

		abstract void gradientMagnitudesAndOrientations(FImage image, FImage magnitudes, FImage orientations);

		/**
		 * Get the minimum angular value (in radians) computed by this mode.
		 * 
		 * @return the minimum angular value.
		 */
		public float minAngle() {
			return min;
		}

		/**
		 * Get the maximum angular value (in radians) computed by this mode.
		 * 
		 * @return the maximum angular value.
		 */
		public float maxAngle() {
			return max;
		}
	}

	private final static float PI_FLOAT = (float) Math.PI;
	private final static float PI_OVER_TWO_FLOAT = (float) Math.PI / 2f;
	private final static float TWO_PI_FLOAT = (float) (Math.PI * 2);

	/**
	 * The gradient magnitudes
	 */
	public FImage magnitudes;

	/**
	 * The gradient orientations
	 */
	public FImage orientations;

	/**
	 * The orientation mode
	 */
	public Mode mode;

	/**
	 * Default constructor using {@link Mode#Signed} mode.
	 */
	public FImageGradients() {
		this.mode = Mode.Signed;
	}

	/**
	 * Construct using the given {@link Mode}.
	 * 
	 * @param mode
	 *            the mode
	 */
	public FImageGradients(Mode mode) {
		this.mode = mode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image
	 * .Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		if (magnitudes == null ||
				magnitudes.height != image.height ||
				magnitudes.width != image.width)
		{
			magnitudes = new FImage(image.width, image.height);
			orientations = new FImage(image.width, image.height);
		}

		mode.gradientMagnitudesAndOrientations(image, magnitudes, orientations);
	}

	/**
	 * Static helper to create a new {@link FImageGradients} and call
	 * {@link FImageGradients#analyseImage(FImage)} with the image.
	 * 
	 * @param image
	 *            the image
	 * @return a FImageGradients for the image
	 */
	public static FImageGradients getGradientMagnitudesAndOrientations(FImage image) {
		final FImageGradients go = new FImageGradients();
		go.analyseImage(image);

		return go;
	}

	/**
	 * Static helper to create a new {@link FImageGradients} and call
	 * {@link FImageGradients#analyseImage(FImage)} with the image.
	 * 
	 * @param image
	 *            the image
	 * @param mode
	 *            the orientation mode
	 * @return a FImageGradients for the image
	 */
	public static FImageGradients getGradientMagnitudesAndOrientations(FImage image, Mode mode) {
		final FImageGradients go = new FImageGradients(mode);
		go.analyseImage(image);

		return go;
	}

	/**
	 * Estimate gradients magnitudes and orientations by calculating pixel
	 * differences. Edges get special treatment. The resultant gradients and
	 * orientations are returned though the gradients and orientations
	 * parameters respectively. The images represented by the gradients and
	 * orientations parameters are assumed to be initialized to the same size as
	 * the input image. Gradients are computed using the <code>atan2</code>
	 * function and will be in the range +/-PI.
	 * 
	 * @param image
	 *            the input image
	 * @param magnitudes
	 *            the output gradient magnitudes
	 * @param orientations
	 *            the output gradient orientations
	 */
	public static void gradientMagnitudesAndOrientations(FImage image, FImage magnitudes, FImage orientations)
	{
		// Note: unrolling this loop to remove the if's doesn't
		// actually seem to make it faster!
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				float xgrad, ygrad;

				if (c == 0)
					xgrad = 2.0f * (image.pixels[r][c + 1] - image.pixels[r][c]);
				else if (c == image.width - 1)
					xgrad = 2.0f * (image.pixels[r][c] - image.pixels[r][c - 1]);
				else
					xgrad = image.pixels[r][c + 1] - image.pixels[r][c - 1];
				if (r == 0)
					ygrad = 2.0f * (image.pixels[r][c] - image.pixels[r + 1][c]);
				else if (r == image.height - 1)
					ygrad = 2.0f * (image.pixels[r - 1][c] - image.pixels[r][c]);
				else
					ygrad = image.pixels[r - 1][c] - image.pixels[r + 1][c];

				// magnitudes.pixels[r][c] = (float) Math.sqrt( xgrad * xgrad +
				// ygrad * ygrad );
				// orientations.pixels[r][c] = (float) Math.atan2( ygrad, xgrad
				// );

				// JH - my benchmarking shows that (at least on OSX) Math.atan2
				// is really
				// slow... FastMath provides an alternative that is much faster
				magnitudes.pixels[r][c] = (float) Math.sqrt(xgrad * xgrad + ygrad * ygrad);
				orientations.pixels[r][c] = (float) FastMath.atan2(ygrad, xgrad);
			}
		}
	}

	/**
	 * Estimate gradients magnitudes and orientations by calculating pixel
	 * differences. Edges get special treatment. The resultant gradients and
	 * orientations are returned though the gradients and orientations
	 * parameters respectively. The images represented by the gradients and
	 * orientations parameters are assumed to be initialized to the same size as
	 * the input image. Gradients are computed using the <code>atan</code>
	 * function and will be in the range +/- PI/2.
	 * 
	 * @param image
	 *            the input image
	 * @param magnitudes
	 *            the output gradient magnitudes
	 * @param orientations
	 *            the output gradient orientations
	 */
	public static void gradientMagnitudesAndUnsignedOrientations(FImage image, FImage magnitudes, FImage orientations)
	{
		// Note: unrolling this loop to remove the if's doesn't
		// actually seem to make it faster!
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				float xgrad, ygrad;

				if (c == 0)
					xgrad = 2.0f * (image.pixels[r][c + 1] - image.pixels[r][c]);
				else if (c == image.width - 1)
					xgrad = 2.0f * (image.pixels[r][c] - image.pixels[r][c - 1]);
				else
					xgrad = image.pixels[r][c + 1] - image.pixels[r][c - 1];
				if (r == 0)
					ygrad = 2.0f * (image.pixels[r][c] - image.pixels[r + 1][c]);
				else if (r == image.height - 1)
					ygrad = 2.0f * (image.pixels[r - 1][c] - image.pixels[r][c]);
				else
					ygrad = image.pixels[r - 1][c] - image.pixels[r + 1][c];

				magnitudes.pixels[r][c] = (float) Math.sqrt(xgrad * xgrad + ygrad * ygrad);
				if (magnitudes.pixels[r][c] == 0)
					orientations.pixels[r][c] = 0;
				else
					orientations.pixels[r][c] = (float) FastMath.atan(ygrad / xgrad);
			}
		}
	}

	/**
	 * Estimate gradients magnitudes and orientations by calculating pixel
	 * differences. Edges get special treatment.
	 * <p>
	 * The orientations are quantised into <code>magnitudes.length</code> bins
	 * and the magnitudes are spread to the adjacent bin through linear
	 * interpolation. The magnitudes parameter must be fully allocated as an
	 * array of num orientation bin images, each of the same size as the input
	 * image.
	 * 
	 * @param image
	 * @param magnitudes
	 */
	public static void gradientMagnitudesAndQuantisedOrientations(FImage image, FImage[] magnitudes)
	{
		final int numOriBins = magnitudes.length;

		// Note: unrolling this loop to remove the if's doesn't
		// actually seem to make it faster!
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				float xgrad, ygrad;

				if (c == 0)
					xgrad = 2.0f * (image.pixels[r][c + 1] - image.pixels[r][c]);
				else if (c == image.width - 1)
					xgrad = 2.0f * (image.pixels[r][c] - image.pixels[r][c - 1]);
				else
					xgrad = image.pixels[r][c + 1] - image.pixels[r][c - 1];
				if (r == 0)
					ygrad = 2.0f * (image.pixels[r][c] - image.pixels[r + 1][c]);
				else if (r == image.height - 1)
					ygrad = 2.0f * (image.pixels[r - 1][c] - image.pixels[r][c]);
				else
					ygrad = image.pixels[r - 1][c] - image.pixels[r + 1][c];

				// JH - my benchmarking shows that (at least on OSX) Math.atan2
				// is really
				// slow... FastMath provides an alternative that is much faster
				final float mag = (float) Math.sqrt(xgrad * xgrad + ygrad * ygrad);
				float ori = (float) FastMath.atan2(ygrad, xgrad);

				// adjust range
				ori = ((ori %= TWO_PI_FLOAT) >= 0 ? ori : (ori + TWO_PI_FLOAT));

				final float po = numOriBins * ori / TWO_PI_FLOAT; // po is now
																	// 0<=po<oriSize

				final int oi = (int) Math.floor(po);
				final float of = po - oi;

				// reset
				for (int i = 0; i < magnitudes.length; i++)
					magnitudes[i].pixels[r][c] = 0;

				// set
				magnitudes[oi % numOriBins].pixels[r][c] = (1f - of) * mag;
				magnitudes[(oi + 1) % numOriBins].pixels[r][c] = of * mag;
			}
		}
	}

	/**
	 * Estimate gradients magnitudes and orientations by calculating pixel
	 * differences. Edges get special treatment.
	 * <p>
	 * The orientations are quantised into <code>magnitudes.length</code> bins.
	 * Magnitudes are optionally spread to the adjacent bin through linear
	 * interpolation. The magnitudes parameter must be fully allocated as an
	 * array of num orientation bin images, each of the same size as the input
	 * image.
	 * 
	 * @param image
	 * @param magnitudes
	 * @param interp
	 * @param mode
	 */
	public static void gradientMagnitudesAndQuantisedOrientations(FImage image, FImage[] magnitudes, boolean interp,
			Mode mode)
	{
		final int numOriBins = magnitudes.length;

		// Note: unrolling this loop to remove the if's doesn't
		// actually seem to make it faster!
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				float xgrad, ygrad;

				if (c == 0)
					xgrad = 2.0f * (image.pixels[r][c + 1] - image.pixels[r][c]);
				else if (c == image.width - 1)
					xgrad = 2.0f * (image.pixels[r][c] - image.pixels[r][c - 1]);
				else
					xgrad = image.pixels[r][c + 1] - image.pixels[r][c - 1];
				if (r == 0)
					ygrad = 2.0f * (image.pixels[r][c] - image.pixels[r + 1][c]);
				else if (r == image.height - 1)
					ygrad = 2.0f * (image.pixels[r - 1][c] - image.pixels[r][c]);
				else
					ygrad = image.pixels[r - 1][c] - image.pixels[r + 1][c];

				// JH - my benchmarking shows that (at least on OSX) Math.atan2
				// is really
				// slow... FastMath provides an alternative that is much faster
				final float mag = (float) Math.sqrt(xgrad * xgrad + ygrad * ygrad);

				float po;
				if (mode == Mode.Unsigned) {
					final float ori = mag == 0 ? PI_OVER_TWO_FLOAT : (float) FastMath.atan(ygrad / xgrad)
							+ PI_OVER_TWO_FLOAT;

					po = numOriBins * ori / PI_FLOAT; // po is now 0<=po<oriSize
				} else {
					float ori = (float) FastMath.atan2(ygrad, xgrad);

					// adjust range
					ori = ((ori %= TWO_PI_FLOAT) >= 0 ? ori : (ori + TWO_PI_FLOAT));

					po = numOriBins * ori / TWO_PI_FLOAT; // po is now
															// 0<=po<oriSize
				}

				// reset
				for (int i = 0; i < magnitudes.length; i++)
					magnitudes[i].pixels[r][c] = 0;

				int oi = (int) Math.floor(po);
				final float of = po - oi;

				// set
				if (interp) {
					magnitudes[oi % numOriBins].pixels[r][c] = (1f - of) * mag;
					magnitudes[(oi + 1) % numOriBins].pixels[r][c] = of * mag;
				} else {
					// if (of > 0.5)
					// magnitudes[(oi + 1) % numOriBins].pixels[r][c] = mag;
					// else
					if (oi > numOriBins - 1)
						oi = numOriBins - 1;
					magnitudes[oi].pixels[r][c] = mag;
				}
			}
		}
	}
}
