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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IntObjectPair;

/**
 * A scale-space pyramid of dense SIFT for {@link FImage}s. Dense sift features
 * are extracted for the given bin sizes (scales). The image is optionally
 * smoothed with a Gaussian before each scale.
 * <p>
 * The {@link PyramidDenseSIFT} is not thread safe, but is reusable like the
 * {@link DenseSIFT} analyser.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <IMAGE>
 *            Type of image to be processed.
 * 
 */
public class PyramidDenseSIFT<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends
		AbstractDenseSIFT<IMAGE>
{
	/**
	 * Scales at which the dense SIFT features are extracted. Each value is used
	 * as bin size for the {@link DenseSIFT}.
	 */
	int[] sizes;

	/**
	 * The image is smoothed by a Gaussian kernel of standard deviation size /
	 * magnificationFactor.
	 */
	float magnificationFactor;

	private List<AbstractDenseSIFT<IMAGE>> levels;

	/**
	 * Construct the pyramid dense sift extractor. The magnification factor is
	 * used to determine how to smooth the image before extracting the features
	 * at each level: the smoothing sigma at each level is the bin size at that
	 * level divided by the magnification factor. If the magnification factor is
	 * 0, then no smoothing will be applied at any level.
	 * 
	 * @param dsift
	 *            the underlying dense sift extractor (typically a
	 *            {@link DenseSIFT} (or {@link ApproximateDenseSIFT}) or
	 *            {@link ColourDenseSIFT} depending on the image type).
	 * @param magFactor
	 *            the magnification factor
	 * @param sizes
	 *            the scales (bin sizes for dense sift)
	 */
	public PyramidDenseSIFT(AbstractDenseSIFT<IMAGE> dsift, float magFactor, int... sizes) {
		this.sizes = sizes;
		this.magnificationFactor = magFactor;

		levels = new ArrayList<AbstractDenseSIFT<IMAGE>>(sizes.length);
		for (int i = 0; i < sizes.length; i++) {
			levels.add(dsift.clone());
		}
	}

	@Override
	public void analyseImage(IMAGE image, Rectangle originalBounds) {
		final Rectangle bounds = originalBounds;

		for (int i = 0; i < sizes.length; i++) {
			final int size = sizes[i];
			final int offset = (int) Math.floor(3f / 2f * (ArrayUtils.maxValue(sizes) - size));

			final IMAGE smoothed;
			if (magnificationFactor == 0) {
				smoothed = image;
			} else {
				final float sigma = size / magnificationFactor;
				smoothed = image.process(new FGaussianConvolve(sigma));
			}

			// extract DSIFT
			bounds.x = originalBounds.x + offset;
			bounds.y = originalBounds.y + offset;

			final AbstractDenseSIFT<IMAGE> dsift = levels.get(i);
			dsift.setBinWidth(size);
			dsift.setBinHeight(size);
			dsift.analyseImage(smoothed, bounds);
		}
	}

	@Override
	public LocalFeatureList<FloatDSIFTKeypoint> getFloatKeypoints() {
		final LocalFeatureList<FloatDSIFTKeypoint> kpts = new MemoryLocalFeatureList<FloatDSIFTKeypoint>(getNumOriBins()
				* getNumBinsX() * getNumBinsY());

		for (int i = 0; i < sizes.length; i++) {
			final AbstractDenseSIFT<IMAGE> dsift = levels.get(i);
			kpts.addAll(dsift.getFloatKeypoints());
		}

		return kpts;
	}

	@Override
	public LocalFeatureList<ByteDSIFTKeypoint> getByteKeypoints() {
		final LocalFeatureList<ByteDSIFTKeypoint> kpts = new MemoryLocalFeatureList<ByteDSIFTKeypoint>(getNumOriBins()
				* getNumBinsX() * getNumBinsY());

		for (int i = 0; i < sizes.length; i++) {
			final AbstractDenseSIFT<IMAGE> dsift = levels.get(i);
			kpts.addAll(dsift.getByteKeypoints());
		}

		return kpts;
	}

	@Override
	public LocalFeatureList<FloatDSIFTKeypoint> getFloatKeypoints(float energyThreshold) {
		final LocalFeatureList<FloatDSIFTKeypoint> kpts = new MemoryLocalFeatureList<FloatDSIFTKeypoint>(getNumOriBins()
				* getNumBinsX() * getNumBinsY());

		for (int i = 0; i < sizes.length; i++) {
			final AbstractDenseSIFT<IMAGE> dsift = levels.get(i);
			kpts.addAll(dsift.getFloatKeypoints(energyThreshold));
		}

		return kpts;
	}

	@Override
	public LocalFeatureList<ByteDSIFTKeypoint> getByteKeypoints(float energyThreshold) {
		final LocalFeatureList<ByteDSIFTKeypoint> kpts = new MemoryLocalFeatureList<ByteDSIFTKeypoint>(getNumOriBins()
				* getNumBinsX() * getNumBinsY());

		for (int i = 0; i < sizes.length; i++) {
			final AbstractDenseSIFT<IMAGE> dsift = levels.get(i);
			kpts.addAll(dsift.getByteKeypoints(energyThreshold));
		}

		return kpts;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(Image)} or {@link #analyseImage(Image, Rectangle)}
	 * in the form of a list of local features with float vectors.
	 * 
	 * @return a list of {@link FloatDSIFTKeypoint}s.
	 */
	public List<IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>> getFloatKeypointsGrouped() {
		final List<IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>> prs = new ArrayList<IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>>(
				sizes.length);

		for (int i = 0; i < sizes.length; i++) {
			final AbstractDenseSIFT<IMAGE> dsift = levels.get(i);

			prs.add(new IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>(sizes[i], dsift.getFloatKeypoints()));
		}

		return prs;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(Image)} or {@link #analyseImage(Image, Rectangle)}
	 * in the form of a list of local features with byte vectors.
	 * 
	 * @return a list of {@link ByteDSIFTKeypoint}s.
	 */
	public List<IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>> getByteKeypointsGrouped() {
		final List<IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>> prs = new ArrayList<IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>>(
				sizes.length);

		for (int i = 0; i < sizes.length; i++) {
			final AbstractDenseSIFT<IMAGE> dsift = levels.get(i);

			prs.add(new IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>(sizes[i], dsift.getByteKeypoints()));
		}

		return prs;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(Image)} or {@link #analyseImage(Image, Rectangle)}
	 * in the form of a list of local features with float vectors. Only the
	 * features with an energy above the given threshold will be returned.
	 * 
	 * @param energyThreshold
	 *            the threshold on the feature energy
	 * 
	 * @return a list of {@link FloatDSIFTKeypoint}s.
	 */
	public List<IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>> getFloatKeypointsGrouped(float energyThreshold) {
		final List<IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>> prs = new ArrayList<IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>>(
				sizes.length);

		for (int i = 0; i < sizes.length; i++) {
			final AbstractDenseSIFT<IMAGE> dsift = levels.get(i);

			prs.add(new IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>(sizes[i], dsift
					.getFloatKeypoints(energyThreshold)));
		}

		return prs;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(Image)} or {@link #analyseImage(Image, Rectangle)}
	 * in the form of a list of local features with byte vectors. Only the
	 * features with an energy above the given threshold will be returned.
	 * 
	 * @param energyThreshold
	 *            the threshold on the feature energy
	 * 
	 * @return a list of {@link ByteDSIFTKeypoint}s.
	 */
	public List<IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>> getByteKeypointsGrouped(float energyThreshold) {
		final List<IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>> prs = new ArrayList<IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>>(
				sizes.length);

		for (int i = 0; i < sizes.length; i++) {
			final AbstractDenseSIFT<IMAGE> dsift = levels.get(i);

			prs.add(new IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>(sizes[i],
					dsift.getByteKeypoints(energyThreshold)));
		}

		return prs;
	}

	/**
	 * Get the computed raw dense SIFT descriptors from the previous call to
	 * {@link #analyseImage(Image)} or {@link #analyseImage(Image, Rectangle)} .
	 * The descriptors are grouped by the sizes at which they were extracted.
	 * 
	 * @return the descriptors.
	 */
	public float[][][] getLevelDescriptors() {
		final float[][][] descr = new float[sizes.length][][];

		for (int i = 0; i < sizes.length; i++) {
			descr[i] = levels.get(i).getDescriptors();
		}

		return descr;
	}

	/**
	 * Get the bin sizes
	 * 
	 * @return the bin sizes
	 */
	public int[] getSizes() {
		return sizes;
	}

	/**
	 * Not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void setBinWidth(int size) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void setBinHeight(int size) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This returns the bin size of the zeroth level only. {@inheritDoc}
	 */
	@Override
	public int getBinWidth() {
		return sizes[0];
	}

	/**
	 * This returns the bin size of the zeroth level only. {@inheritDoc}
	 */
	@Override
	public int getBinHeight() {
		return sizes[0];
	}

	@Override
	public int getNumBinsX() {
		return levels.get(0).getNumBinsX();
	}

	@Override
	public int getNumBinsY() {
		return levels.get(0).getNumBinsY();
	}

	@Override
	public int getNumOriBins() {
		return levels.get(0).getNumOriBins();
	}

	@Override
	public float[][] getDescriptors() {
		int len = 0;
		for (int i = 0; i < sizes.length; i++) {
			len += levels.get(i).getDescriptors().length;
		}

		final float[][] descr = new float[len][];

		int offset = 0;
		for (int i = 0; i < sizes.length; i++) {
			final float[][] ldescr = levels.get(i).getDescriptors();
			for (int j = 0; j < ldescr.length; j++) {
				descr[offset++] = ldescr[j];
			}
		}

		return descr;
	}
}
