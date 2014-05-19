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

import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.SingularValueDecomposition;

/**
 * Base class for implementation of classes that perform convolution operations
 * on @link{FImage}s as a @link{SinglebandImageProcessor}, with the kernel
 * itself formed from and @link{FImage}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FConvolution implements SinglebandImageProcessor<Float, FImage> {
	/** The kernel */
	public FImage kernel;

	private ConvolveMode mode;

	interface ConvolveMode {
		public void convolve(FImage f);

		class OneD implements ConvolveMode {
			private float[] kernel;
			private boolean rowMode;

			OneD(FImage image) {
				if (image.height == 1) {
					this.rowMode = true;
					this.kernel = image.pixels[0];

				}
				else {
					this.rowMode = false;
					this.kernel = new float[image.height];
					for (int i = 0; i < image.height; i++)
						this.kernel[i] = image.pixels[i][0];
				}
			}

			@Override
			public void convolve(FImage f) {
				if (this.rowMode)
					FImageConvolveSeparable.convolveHorizontal(f, kernel);
				else
					FImageConvolveSeparable.convolveVertical(f, kernel);
			}

		}

		class Separable implements ConvolveMode {
			private float[] row;
			private float[] col;

			Separable(SingularValueDecomposition svd) {

				final int nrows = svd.getU().getRowDimension();

				this.row = new float[nrows];
				this.col = new float[nrows];

				final float factor = (float) Math.sqrt(svd.getS().get(0, 0));
				for (int i = 0; i < nrows; i++) {
					this.row[i] = (float) svd.getU().get(i, 0) * factor;
					this.col[i] = (float) svd.getV().get(i, 0) * factor;
				}
			}

			@Override
			public void convolve(FImage f) {
				FImageConvolveSeparable.convolveHorizontal(f, row);
				FImageConvolveSeparable.convolveVertical(f, col);
			}
		}

		class BruteForce implements ConvolveMode {
			protected FImage kernel;

			BruteForce(FImage kernel) {
				this.kernel = kernel;
			}

			@Override
			public void convolve(FImage image) {
				final int kh = kernel.height;
				final int kw = kernel.width;
				final int hh = kh / 2;
				final int hw = kw / 2;
				final FImage clone = image.newInstance(image.width, image.height);
				for (int y = hh; y < image.height - (kh - hh); y++) {
					for (int x = hw; x < image.width - (kw - hw); x++) {
						float sum = 0;
						for (int j = 0, jj = kh - 1; j < kh; j++, jj--) {
							for (int i = 0, ii = kw - 1; i < kw; i++, ii--) {
								final int rx = x + i - hw;
								final int ry = y + j - hh;

								sum += image.pixels[ry][rx] * kernel.pixels[jj][ii];
							}
						}
						clone.pixels[y][x] = sum;
					}
				}
				image.internalAssign(clone);
			}
		}
	}

	/**
	 * Construct the convolution operator with the given kernel
	 * 
	 * @param kernel
	 *            the kernel
	 */
	public FConvolution(FImage kernel) {
		this.kernel = kernel;
		setup(false);
	}

	/**
	 * Construct the convolution operator with the given kernel
	 * 
	 * @param kernel
	 *            the kernel
	 */
	public FConvolution(float[][] kernel) {
		this.kernel = new FImage(kernel);
		setup(false);
	}

	/**
	 * Set brute-force convolution; disables kernel separation and other
	 * optimisations.
	 * 
	 * @param brute
	 */
	public void setBruteForce(boolean brute) {
		setup(brute);
	}

	private void setup(boolean brute) {
		if (brute) {
			this.mode = new ConvolveMode.BruteForce(this.kernel);
			return;
		}
		if (this.kernel.width == 1 || this.kernel.height == 1) {
			this.mode = new ConvolveMode.OneD(kernel);
		}
		else {
			MatrixUtils.matrixFromFloat(this.kernel.pixels);
			final SingularValueDecomposition svd = new SingularValueDecomposition(
					MatrixUtils.matrixFromFloat(this.kernel.pixels));
			if (svd.rank() == 1)
				this.mode = new ConvolveMode.Separable(svd);
			else
				this.mode = new ConvolveMode.BruteForce(this.kernel);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj
	 * .image.Image)
	 */
	@Override
	public void processImage(FImage image) {
		mode.convolve(image);
	}

	/**
	 * Return the kernel response at the x,y in the given image.
	 * 
	 * This method will throw an array index out of bounds if x,y requests
	 * pixels outside the image bounds
	 * 
	 * @param x
	 * @param y
	 * @param image
	 * @return the kernel response at the given coordinates
	 */
	public float responseAt(int x, int y, FImage image) {
		float sum = 0;
		final int kh = kernel.height;
		final int kw = kernel.width;
		final int hh = kh / 2;
		final int hw = kw / 2;

		for (int j = 0, jj = kh - 1; j < kh; j++, jj--) {
			for (int i = 0, ii = kw - 1; i < kw; i++, ii--) {
				final int rx = x + i - hw;
				final int ry = y + j - hh;

				sum += image.pixels[ry][rx] * kernel.pixels[jj][ii];
			}
		}
		return sum;
	}
}
