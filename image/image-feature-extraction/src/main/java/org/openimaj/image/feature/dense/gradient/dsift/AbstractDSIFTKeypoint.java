package org.openimaj.image.feature.dense.gradient.dsift;

import java.io.Serializable;

import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.io.VariableLength;

/**
 * Abstract base for Dense SIFT keypoints with a location and vector. Also
 * includes the energy of the feature prior to normalisation in case
 * low-contrast features need removing.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of {@link ArrayFeatureVector} that this keypoint provides
 * @param <Q>
 *            Type of primitive array backing the feature
 */
abstract class AbstractDSIFTKeypoint<T extends ArrayFeatureVector<Q>, Q>
		extends
		SpatialLocation
		implements
		Serializable,
		LocalFeature<SpatialLocation, T>,
		VariableLength,
		Cloneable
{
	final static long serialVersionUID = 1234554345;

	/**
	 * Default length of standard SIFT features.
	 */
	final static int DEFAULT_LENGTH = 128;

	/**
	 * The energy of the descriptor prior to normalisation; computed as the sum
	 * of descriptor values divided by the number of sample pixels used to
	 * create it (hence comparable across different window sizes). Can be used
	 * to remove low-contrast descriptors.
	 */
	public float energy;

	/**
	 * The descriptor (normalised)
	 */
	public Q descriptor;

	@Override
	public SpatialLocation getLocation() {
		return this;
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public String asciiHeader() {
		return "";
	}
}
