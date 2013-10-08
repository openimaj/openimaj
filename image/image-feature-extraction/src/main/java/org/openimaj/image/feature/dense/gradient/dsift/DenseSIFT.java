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
package org.openimaj.image.feature.dense.gradient.dsift;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FImageConvolveSeparable;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.array.ArrayUtils;

/**
 * Implementation of a dense SIFT feature extractor for {@link FImage}s.
 * Extracts upright SIFT features at a single scale on a grid.
 * <p>
 * Implementation inspired by the <a
 * href="http://www.vlfeat.org/api/dsift.html#dsift-usage">VLFeat extractor</a>.
 * <p>
 * <b>Implementation Notes</b>. The analyser is not thread-safe, however, it is
 * safe to reuse the analyser. In multi-threaded environments, a separate
 * instance must be made for each thread. Internally, this implementation
 * allocates memory for the gradient images, and if possible re-uses these
 * between calls. Re-use requires that the input image is the same size between
 * calls to the analyser.
 * 
 * @see "http://www.vlfeat.org/api/dsift.html#dsift-usage"
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class DenseSIFT extends AbstractDenseSIFT<FImage> {
	static class WorkingData {
		/**
		 * minimum X bound for sampling the descriptors (inclusive)
		 */
		protected int boundMinX;

		/**
		 * maximum X bound for sampling the descriptors (inclusive)
		 */
		protected int boundMaxX;

		/**
		 * minimum Y bound for sampling the descriptors (inclusive)
		 */
		protected int boundMinY;

		/**
		 * maximum X bound for sampling the descriptors (inclusive)
		 */
		protected int boundMaxY;

		/**
		 * Quantised orientation/gradient magnitude data. Each element
		 * corresponds to an angular bin of the orientations, and the individual
		 * images correspond to the gradient magnitudes for the respective
		 * orientations.
		 */
		protected FImage[] gradientMagnitudes;

		/**
		 * Setup the required space for holding gradient maps and descriptors
		 * 
		 * @param image
		 *            the image being analysed
		 */
		protected void setupWorkingSpace(FImage image, DenseSIFT dsift) {
			if (gradientMagnitudes == null) {
				gradientMagnitudes = new FImage[dsift.numOriBins];
			}

			if (gradientMagnitudes[0] == null || gradientMagnitudes[0].width != image.width
					|| gradientMagnitudes[0].height != image.height)
			{
				for (int i = 0; i < dsift.numOriBins; i++)
					gradientMagnitudes[i] = new FImage(image.width, image.height);
			}

			final int rangeX = boundMaxX - boundMinX - (dsift.numBinsX - 1) * dsift.binWidth;
			final int rangeY = boundMaxY - boundMinY - (dsift.numBinsY - 1) * dsift.binHeight;

			final int numWindowsX = (rangeX >= 0) ? rangeX / dsift.stepX + 1 : 0;
			final int numWindowsY = (rangeY >= 0) ? rangeY / dsift.stepY + 1 : 0;

			final int numFeatures = numWindowsX * numWindowsY;

			dsift.descriptors = new float[numFeatures][dsift.numOriBins * dsift.numBinsX * dsift.numBinsY];
			dsift.energies = new float[numFeatures];
		}
	}

	/**
	 * Step size of sampling window in x-direction (in pixels)
	 */
	protected int stepX = 5;

	/**
	 * Step size of sampling window in y-direction (in pixels)
	 */
	protected int stepY = 5;

	/**
	 * Width of a single bin of the sampling window (in pixels). Sampling window
	 * width is this multiplied by #numBinX.
	 */
	protected int binWidth = 5;

	/**
	 * Height of a single bin of the sampling window (in pixels). Sampling
	 * window height is this multiplied by #numBinY.
	 */
	protected int binHeight = 5;

	/**
	 * Number of spatial bins in the X direction
	 */
	protected int numBinsX = 4;

	/**
	 * Number of spatial bins in the Y direction
	 */
	protected int numBinsY = 4;

	/** The number of orientation bins */
	protected int numOriBins = 8;

	/**
	 * Size of the Gaussian window (in relative to of the size of a bin)
	 */
	protected float gaussianWindowSize = 2f;

	/**
	 * Threshold for clipping the SIFT features
	 */
	protected float valueThreshold = 0.2f;

	protected volatile WorkingData data = new WorkingData();

	/**
	 * Extracted descriptors
	 */
	protected volatile float[][] descriptors;

	/**
	 * Descriptor energies
	 */
	protected volatile float[] energies;

	/**
	 * Construct with the default configuration: standard SIFT geometry (4x4x8),
	 * 5px x 5px spatial bins, 5px step size, gaussian window size of 2 and
	 * value threshold of 0.2.
	 */
	public DenseSIFT() {
	}

	/**
	 * Construct with the given step size (for both x and y) and binSize. All
	 * other values are the defaults.
	 * 
	 * @param step
	 *            the step size
	 * @param binSize
	 *            the spatial bin size
	 */
	public DenseSIFT(int step, int binSize) {
		this.binWidth = binSize;
		this.binHeight = binSize;
		this.stepX = step;
		this.stepY = step;
	}

	/**
	 * Construct with the given configuration. The gaussian window size is set
	 * to 2, and value threshold to 0.2.
	 * 
	 * @param stepX
	 *            step size in x direction
	 * @param stepY
	 *            step size in y direction
	 * @param binWidth
	 *            width of spatial bins
	 * @param binHeight
	 *            height of spatial bins
	 * @param numBinsX
	 *            number of bins in x direction for each descriptor
	 * @param numBinsY
	 *            number of bins in y direction for each descriptor
	 * @param numOriBins
	 *            number of orientation bins for each descriptor
	 */
	public DenseSIFT(int stepX, int stepY, int binWidth, int binHeight, int numBinsX, int numBinsY, int numOriBins) {
		this(stepX, stepY, binWidth, binHeight, numBinsX, numBinsY, numOriBins, 2f);
	}

	/**
	 * Construct with the given configuration. The value threshold is set to
	 * 0.2.
	 * 
	 * @param stepX
	 *            step size in x direction
	 * @param stepY
	 *            step size in y direction
	 * @param binWidth
	 *            width of spatial bins
	 * @param binHeight
	 *            height of spatial bins
	 * @param numBinsX
	 *            number of bins in x direction for each descriptor
	 * @param numBinsY
	 *            number of bins in y direction for each descriptor
	 * @param numOriBins
	 *            number of orientation bins for each descriptor
	 * @param gaussianWindowSize
	 *            the size of the gaussian weighting window
	 */
	public DenseSIFT(int stepX, int stepY, int binWidth, int binHeight, int numBinsX, int numBinsY, int numOriBins,
			float gaussianWindowSize)
	{
		this(stepX, stepY, binWidth, binHeight, numBinsX, numBinsY, numOriBins, gaussianWindowSize, 0.2f);
	}

	/**
	 * Construct with the given configuration. The value threshold is set to
	 * 0.2.
	 * 
	 * @param stepX
	 *            step size in x direction
	 * @param stepY
	 *            step size in y direction
	 * @param binWidth
	 *            width of spatial bins
	 * @param binHeight
	 *            height of spatial bins
	 * @param numBinsX
	 *            number of bins in x direction for each descriptor
	 * @param numBinsY
	 *            number of bins in y direction for each descriptor
	 * @param numOriBins
	 *            number of orientation bins for each descriptor
	 * @param gaussianWindowSize
	 *            the size of the gaussian weighting window
	 * @param valueThreshold
	 *            the threshold for clipping features
	 */
	public DenseSIFT(int stepX, int stepY, int binWidth, int binHeight, int numBinsX, int numBinsY, int numOriBins,
			float gaussianWindowSize, float valueThreshold)
	{
		this.binWidth = binWidth;
		this.binHeight = binHeight;
		this.stepX = stepX;
		this.stepY = stepY;
		this.numBinsX = numBinsX;
		this.numBinsY = numBinsY;
		this.numOriBins = numOriBins;
		this.gaussianWindowSize = gaussianWindowSize;
		this.valueThreshold = valueThreshold;
	}

	private float[] buildKernel(int binSize, int numBins, int binIndex, float windowSize) {
		final int kernelSize = 2 * binSize - 1;
		final float[] kernel = new float[kernelSize];
		final float delta = binSize * (binIndex - 0.5F * (numBins - 1));

		final float sigma = binSize * windowSize;

		for (int x = -binSize + 1, i = 0; x <= +binSize - 1; x++, i++) {
			final float z = (x - delta) / sigma;

			kernel[i] = (1.0F - Math.abs(x) / binSize) * ((binIndex >= 0) ? (float) Math.exp(-0.5F * z * z) : 1.0F);
		}

		return kernel;
	}

	/**
	 * Extract the DSIFT features
	 */
	protected void extractFeatures() {
		final int frameSizeX = binWidth * (numBinsX - 1) + 1;
		final int frameSizeY = binHeight * (numBinsY - 1) + 1;

		for (int biny = 0; biny < numBinsY; biny++) {
			final float[] yker = buildKernel(binHeight, numBinsY, biny, gaussianWindowSize);

			for (int binx = 0; binx < numBinsX; binx++) {
				final float[] xker = buildKernel(binWidth, numBinsX, binx, gaussianWindowSize);

				for (int bint = 0; bint < numOriBins; bint++) {
					final FImage conv = data.gradientMagnitudes[bint].process(new FImageConvolveSeparable(xker, yker));
					final float[][] src = conv.pixels;

					final int descriptorOffset = bint + binx * numOriBins + biny * (numBinsX * numOriBins);
					int descriptorIndex = 0;

					for (int framey = data.boundMinY; framey <= data.boundMaxY - frameSizeY + 1; framey += stepY)
					{
						for (int framex = data.boundMinX; framex <= data.boundMaxX - frameSizeX + 1; framex += stepX)
						{
							descriptors[descriptorIndex][descriptorOffset] =
									src[framey + biny * binHeight][framex + binx * binWidth];
							descriptorIndex++;
						}
					}
				}
			}
		}
	}

	@Override
	public void analyseImage(FImage image, Rectangle bounds) {
		if (data == null)
			data = new WorkingData();

		data.boundMinX = (int) bounds.x;
		data.boundMaxX = (int) (bounds.width - 1);
		data.boundMinY = (int) bounds.y;
		data.boundMaxY = (int) (bounds.height - 1);

		data.setupWorkingSpace(image, this);

		FImageGradients.gradientMagnitudesAndQuantisedOrientations(image, data.gradientMagnitudes);

		extractFeatures();

		normaliseDescriptors();
	}

	private void normaliseDescriptors() {
		final int frameSizeX = binWidth * (numBinsX - 1) + 1;
		final int frameSizeY = binHeight * (numBinsY - 1) + 1;
		final float energyNorm = frameSizeX * frameSizeY;

		for (int j = 0; j < descriptors.length; j++) {
			final float[] arr = descriptors[j];

			energies[j] = ArrayUtils.sumValues(arr) / energyNorm;

			ArrayUtils.normalise(arr);

			boolean changed = false;
			for (int i = 0; i < arr.length; i++) {
				if (arr[i] > valueThreshold) {
					arr[i] = valueThreshold;
					changed = true;
				}
			}

			if (changed)
				ArrayUtils.normalise(arr);
		}
	}

	@Override
	public LocalFeatureList<FloatDSIFTKeypoint> getFloatKeypoints() {
		final MemoryLocalFeatureList<FloatDSIFTKeypoint> keys = new MemoryLocalFeatureList<FloatDSIFTKeypoint>(numOriBins
				* numBinsX * numBinsY, descriptors.length);

		final int frameSizeX = binWidth * (numBinsX - 1) + 1;
		final int frameSizeY = binHeight * (numBinsY - 1) + 1;

		final float deltaCenterX = 0.5F * binWidth * (numBinsX - 1);
		final float deltaCenterY = 0.5F * binHeight * (numBinsY - 1);

		for (int framey = data.boundMinY, i = 0; framey <= data.boundMaxY - frameSizeY + 1; framey += stepY) {
			for (int framex = data.boundMinX; framex <= data.boundMaxX - frameSizeX + 1; framex += stepX, i++) {
				keys.add(new FloatDSIFTKeypoint(framex + deltaCenterX, framey + deltaCenterY, descriptors[i], energies[i]));
			}
		}

		return keys;
	}

	@Override
	public LocalFeatureList<ByteDSIFTKeypoint> getByteKeypoints() {
		final MemoryLocalFeatureList<ByteDSIFTKeypoint> keys = new MemoryLocalFeatureList<ByteDSIFTKeypoint>(numOriBins
				* numBinsX * numBinsY, descriptors.length);

		final int frameSizeX = binWidth * (numBinsX - 1) + 1;
		final int frameSizeY = binHeight * (numBinsY - 1) + 1;

		final float deltaCenterX = 0.5F * binWidth * (numBinsX - 1);
		final float deltaCenterY = 0.5F * binHeight * (numBinsY - 1);

		for (int framey = data.boundMinY, i = 0; framey <= data.boundMaxY - frameSizeY + 1; framey += stepY) {
			for (int framex = data.boundMinX; framex <= data.boundMaxX - frameSizeX + 1; framex += stepX, i++) {
				keys.add(new ByteDSIFTKeypoint(framex + deltaCenterX, framey + deltaCenterY, descriptors[i], energies[i]));
			}
		}

		return keys;
	}

	@Override
	public LocalFeatureList<FloatDSIFTKeypoint> getFloatKeypoints(float energyThreshold) {
		final MemoryLocalFeatureList<FloatDSIFTKeypoint> keys = new MemoryLocalFeatureList<FloatDSIFTKeypoint>(numOriBins
				* numBinsX * numBinsY);

		final int frameSizeX = binWidth * (numBinsX - 1) + 1;
		final int frameSizeY = binHeight * (numBinsY - 1) + 1;

		final float deltaCenterX = 0.5F * binWidth * (numBinsX - 1);
		final float deltaCenterY = 0.5F * binHeight * (numBinsY - 1);

		for (int framey = data.boundMinY, i = 0; framey <= data.boundMaxY - frameSizeY + 1; framey += stepY) {
			for (int framex = data.boundMinX; framex <= data.boundMaxX - frameSizeX + 1; framex += stepX, i++) {
				if (energies[i] >= energyThreshold)
					keys.add(new FloatDSIFTKeypoint(framex + deltaCenterX, framey + deltaCenterY, descriptors[i],
							energies[i]));
			}
		}

		return keys;
	}

	@Override
	public LocalFeatureList<ByteDSIFTKeypoint> getByteKeypoints(float energyThreshold) {
		final MemoryLocalFeatureList<ByteDSIFTKeypoint> keys = new MemoryLocalFeatureList<ByteDSIFTKeypoint>(numOriBins
				* numBinsX * numBinsY);

		final int frameSizeX = binWidth * (numBinsX - 1) + 1;
		final int frameSizeY = binHeight * (numBinsY - 1) + 1;

		final float deltaCenterX = 0.5F * binWidth * (numBinsX - 1);
		final float deltaCenterY = 0.5F * binHeight * (numBinsY - 1);

		for (int framey = data.boundMinY, i = 0; framey <= data.boundMaxY - frameSizeY + 1; framey += stepY) {
			for (int framex = data.boundMinX; framex <= data.boundMaxX - frameSizeX + 1; framex += stepX, i++) {
				if (energies[i] >= energyThreshold)
					keys.add(new ByteDSIFTKeypoint(framex + deltaCenterX, framey + deltaCenterY, descriptors[i],
							energies[i]));
			}
		}

		return keys;
	}

	/**
	 * Get the computed raw dense SIFT descriptors from the previous call to
	 * {@link #analyseImage(FImage)} or {@link #analyseImage(FImage, Rectangle)}
	 * .
	 * 
	 * @return the descriptors.
	 */
	@Override
	public float[][] getDescriptors() {
		return descriptors;
	}

	@Override
	public DenseSIFT clone() {
		final DenseSIFT clone = (DenseSIFT) super.clone();

		clone.descriptors = null;
		clone.energies = null;
		clone.data = null;

		return clone;
	}

	@Override
	public void setBinWidth(int size) {
		this.binWidth = size;
	}

	@Override
	public void setBinHeight(int size) {
		this.binHeight = size;
	}

	@Override
	public int getBinWidth() {
		return binWidth;
	}

	@Override
	public int getBinHeight() {
		return binHeight;
	}

	@Override
	public int getNumBinsX() {
		return numBinsX;
	}

	@Override
	public int getNumBinsY() {
		return numBinsY;
	}

	@Override
	public int getNumOriBins() {
		return numOriBins;
	}
}
