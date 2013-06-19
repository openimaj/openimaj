package org.openimaj.image.feature.dense.gradient.dsift;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IntObjectPair;

/**
 * A scale-space pyramid of dense SIFT. Dense sift features are extracted for
 * the given bin sizes (scales). The image is optionally smoothed with a
 * Gaussian before each scale.
 * <p>
 * The {@link PyramidDenseSIFT} is not thread safe, but is reusable like the
 * {@link DenseSIFT} analyser.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class PyramidDenseSIFT implements ImageAnalyser<FImage> {
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

	DenseSIFT dsift;
	DenseSIFT.WorkingData[] workingData;

	float[][][] descriptors;

	/**
	 * Construct the pyramid dense sift extractor. The magnification factor is
	 * used to determine how to smooth the image before extracting the features
	 * at each level: the smoothing sigma at each level is the bin size at that
	 * level divided by the magnification factor. If the magnification factor is
	 * 0, then no smoothing will be applied at any level.
	 * 
	 * @param dsift
	 *            the underlying dense sift extractor
	 * @param magFactor
	 *            the magnification factor
	 * @param sizes
	 *            the scales (bin sizes for dense sift)
	 */
	public PyramidDenseSIFT(DenseSIFT dsift, float magFactor, int... sizes) {
		this.dsift = dsift;
		this.sizes = sizes;
		this.magnificationFactor = magFactor;

		workingData = new DenseSIFT.WorkingData[sizes.length];
		for (int i = 0; i < sizes.length; i++) {
			workingData[i] = dsift.new WorkingData();
		}
	}

	/**
	 * Compute the pyramidal dense sift descriptors of the given image. The
	 * entire image will be sampled.
	 * 
	 * @param image
	 *            the image
	 */
	@Override
	public void analyseImage(FImage image) {
		final Rectangle bounds = image.getBounds();

		analyseImage(image, bounds);
	}

	/**
	 * Compute the pyramidal dense sift descriptors inside the bounds rectangle
	 * of the given image.
	 * 
	 * @param image
	 *            the image
	 * @param originalBounds
	 *            the bounds rectangle
	 */
	public void analyseImage(FImage image, Rectangle originalBounds) {
		final Rectangle bounds = originalBounds;

		descriptors = new float[sizes.length][][];

		for (int i = 0; i < sizes.length; i++) {
			final int size = sizes[i];
			final int offset = (int) Math.floor(3f / 2f * (ArrayUtils.maxValue(sizes) - size));

			final FImage smoothed;
			if (magnificationFactor == 0) {
				smoothed = image;
			} else {
				final float sigma = size / magnificationFactor;
				smoothed = image.process(new FGaussianConvolve(sigma));
			}

			// extract DSIFT
			bounds.x = originalBounds.x + offset;
			bounds.y = originalBounds.y + offset;
			dsift.data = workingData[i];
			dsift.binWidth = size;
			dsift.binHeight = size;
			dsift.analyseImage(smoothed, bounds);
			descriptors[i] = dsift.descriptors;
		}
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(FImage)} or {@link #analyseImage(FImage, Rectangle)}
	 * in the form of a list of local features with float vectors.
	 * 
	 * @return a list of {@link FloatDSIFTKeypoint}s.
	 */
	public LocalFeatureList<FloatDSIFTKeypoint> getFloatKeypoints() {
		final LocalFeatureList<FloatDSIFTKeypoint> kpts = new MemoryLocalFeatureList<FloatDSIFTKeypoint>(dsift.numOriBins
				* dsift.numBinsX * dsift.numBinsY);

		for (int i = 0; i < sizes.length; i++) {
			dsift.descriptors = descriptors[i];
			kpts.addAll(dsift.getFloatKeypoints());
		}

		return kpts;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(FImage)} or {@link #analyseImage(FImage, Rectangle)}
	 * in the form of a list of local features with byte vectors.
	 * 
	 * @return a list of {@link ByteDSIFTKeypoint}s.
	 */
	public LocalFeatureList<ByteDSIFTKeypoint> getByteKeypoints() {
		final LocalFeatureList<ByteDSIFTKeypoint> kpts = new MemoryLocalFeatureList<ByteDSIFTKeypoint>(dsift.numOriBins
				* dsift.numBinsX * dsift.numBinsY);

		for (int i = 0; i < sizes.length; i++) {
			dsift.descriptors = descriptors[i];
			kpts.addAll(dsift.getByteKeypoints());
		}

		return kpts;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(FImage)} or {@link #analyseImage(FImage, Rectangle)}
	 * in the form of a list of local features with float vectors. Only the
	 * features with an energy above the given threshold will be returned.
	 * 
	 * @param energyThreshold
	 *            the threshold on the feature energy
	 * 
	 * @return a list of {@link FloatDSIFTKeypoint}s.
	 */
	public LocalFeatureList<FloatDSIFTKeypoint> getFloatKeypoints(float energyThreshold) {
		final LocalFeatureList<FloatDSIFTKeypoint> kpts = new MemoryLocalFeatureList<FloatDSIFTKeypoint>(dsift.numOriBins
				* dsift.numBinsX * dsift.numBinsY);

		for (int i = 0; i < sizes.length; i++) {
			dsift.descriptors = descriptors[i];
			kpts.addAll(dsift.getFloatKeypoints(energyThreshold));
		}

		return kpts;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(FImage)} or {@link #analyseImage(FImage, Rectangle)}
	 * in the form of a list of local features with byte vectors. Only the
	 * features with an energy above the given threshold will be returned.
	 * 
	 * @param energyThreshold
	 *            the threshold on the feature energy
	 * 
	 * @return a list of {@link ByteDSIFTKeypoint}s.
	 */
	public LocalFeatureList<ByteDSIFTKeypoint> getByteKeypoints(float energyThreshold) {
		final LocalFeatureList<ByteDSIFTKeypoint> kpts = new MemoryLocalFeatureList<ByteDSIFTKeypoint>(dsift.numOriBins
				* dsift.numBinsX * dsift.numBinsY);

		for (int i = 0; i < sizes.length; i++) {
			dsift.descriptors = descriptors[i];
			kpts.addAll(dsift.getByteKeypoints(energyThreshold));
		}

		return kpts;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(FImage)} or {@link #analyseImage(FImage, Rectangle)}
	 * in the form of a list of local features with float vectors.
	 * 
	 * @return a list of {@link FloatDSIFTKeypoint}s.
	 */
	public List<IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>> getFloatKeypointsGrouped() {
		final List<IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>> prs = new ArrayList<IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>>(
				sizes.length);

		for (int i = 0; i < sizes.length; i++) {
			dsift.descriptors = descriptors[i];

			prs.add(new IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>(sizes[i], dsift.getFloatKeypoints()));
		}

		return prs;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(FImage)} or {@link #analyseImage(FImage, Rectangle)}
	 * in the form of a list of local features with byte vectors.
	 * 
	 * @return a list of {@link ByteDSIFTKeypoint}s.
	 */
	public List<IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>> getByteKeypointsGrouped() {
		final List<IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>> prs = new ArrayList<IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>>(
				sizes.length);

		for (int i = 0; i < sizes.length; i++) {
			dsift.descriptors = descriptors[i];

			prs.add(new IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>(sizes[i], dsift.getByteKeypoints()));
		}

		return prs;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(FImage)} or {@link #analyseImage(FImage, Rectangle)}
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
			dsift.descriptors = descriptors[i];

			prs.add(new IntObjectPair<LocalFeatureList<FloatDSIFTKeypoint>>(sizes[i], dsift
					.getFloatKeypoints(energyThreshold)));
		}

		return prs;
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(FImage)} or {@link #analyseImage(FImage, Rectangle)}
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
			dsift.descriptors = descriptors[i];

			prs.add(new IntObjectPair<LocalFeatureList<ByteDSIFTKeypoint>>(sizes[i], dsift
					.getByteKeypoints(energyThreshold)));
		}

		return prs;
	}

	/**
	 * Get the computed raw dense SIFT descriptors from the previous call to
	 * {@link #analyseImage(FImage)} or {@link #analyseImage(FImage, Rectangle)}
	 * . The descriptors are grouped by the sizes at which they were extracted.
	 * 
	 * @return the descriptors.
	 */
	public float[][][] getDescriptors() {
		return descriptors;
	}

	/**
	 * Get the bin sizes
	 * 
	 * @return the bin sizes
	 */
	public int[] getSizes() {
		return sizes;
	}
}
