package org.openimaj.image.feature.dense.gradient.dsift;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Base class for implementations of a dense SIFT feature extractors.
 * 
 * @see "http://www.vlfeat.org/api/dsift.html#dsift-usage"
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            Type of image being processed
 */
public abstract class AbstractDenseSIFT<IMAGE extends Image<?, IMAGE>> implements ImageAnalyser<IMAGE>, Cloneable {

	/**
	 * Compute the dense sift descriptors inside the bounds rectangle of the
	 * given image.
	 * 
	 * @param image
	 *            the image
	 * @param bounds
	 *            the bounds rectangle
	 */
	public abstract void analyseImage(IMAGE image, Rectangle bounds);

	/**
	 * Compute the dense sift descriptors of the given image. The entire image
	 * will be sampled.
	 * 
	 * @param image
	 *            the image
	 */
	@Override
	public final void analyseImage(IMAGE image) {
		analyseImage(image, image.getBounds());
	}

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(Image)} or {@link #analyseImage(Image, Rectangle)}
	 * in the form of a list of local features with float vectors.
	 * 
	 * @return a list of {@link FloatDSIFTKeypoint}s.
	 */
	public abstract LocalFeatureList<FloatDSIFTKeypoint> getFloatKeypoints();

	/**
	 * Get the SIFT descriptors from the previous call to
	 * {@link #analyseImage(Image)} or {@link #analyseImage(Image, Rectangle)}
	 * in the form of a list of local features with byte vectors.
	 * 
	 * @return a list of {@link ByteDSIFTKeypoint}s.
	 */
	public abstract LocalFeatureList<ByteDSIFTKeypoint> getByteKeypoints();

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
	public abstract LocalFeatureList<FloatDSIFTKeypoint> getFloatKeypoints(float energyThreshold);

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
	public abstract LocalFeatureList<ByteDSIFTKeypoint> getByteKeypoints(float energyThreshold);

	@SuppressWarnings("unchecked")
	@Override
	public AbstractDenseSIFT<IMAGE> clone() {
		try {
			return (AbstractDenseSIFT<IMAGE>) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * (Optional operation) Set the width of a single bin of the sampling window
	 * (in pixels). Sampling window width is this multiplied by
	 * {@link #getNumBinsX()}.
	 * 
	 * @param size
	 *            size to set
	 */
	public abstract void setBinWidth(int size);

	/**
	 * (Optional operation) Set the height of a single bin of the sampling
	 * window (in pixels). Sampling window height is this multiplied by
	 * {@link #getNumBinsY()}.
	 * 
	 * @param size
	 *            size to set
	 */
	public abstract void setBinHeight(int size);

	/**
	 * (Optional operation) Get the width of a single bin of the sampling window
	 * (in pixels). Sampling window width is this multiplied by
	 * {@link #getNumBinsX()}.
	 * 
	 * @return the bin width
	 */
	public abstract int getBinWidth();

	/**
	 * (Optional operation) Get the height of a single bin of the sampling
	 * window (in pixels). Sampling window height is this multiplied by
	 * {@link #getNumBinsY()}.
	 * 
	 * @return the bin height
	 */
	public abstract int getBinHeight();

	/**
	 * Get the number of spatial bins in the X direction
	 * 
	 * @return the number of bins in the x direction
	 */
	public abstract int getNumBinsX();

	/**
	 * Get the number of spatial bins in the Y direction
	 * 
	 * @return the number of bins in the y direction
	 */
	public abstract int getNumBinsY();

	/**
	 * Get the number of orientation bins
	 * 
	 * @return the number of orientation bins
	 */
	public abstract int getNumOriBins();

	/**
	 * Get the computed raw dense SIFT descriptors from the previous call to
	 * {@link #analyseImage(Image)} or {@link #analyseImage(Image, Rectangle)} .
	 * 
	 * @return the descriptors.
	 */
	public abstract float[][] getDescriptors();
}
