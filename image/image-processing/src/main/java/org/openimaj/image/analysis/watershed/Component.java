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
package org.openimaj.image.analysis.watershed;

import java.util.HashSet;
import java.util.Set;

import org.openimaj.image.analysis.watershed.feature.ComponentFeature;
import org.openimaj.image.analysis.watershed.feature.PixelsFeature;
import org.openimaj.image.pixel.IntValuePixel;
import org.openimaj.image.pixel.Pixel;

/**
 * Represents a region or set of pixels (the name is based on the Microsoft
 * paper)
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class Component implements Cloneable
{
	/** Whether this component represents an MSER */
	public boolean isMSER = false;

	/** List of features representing this component */
	public ComponentFeature[] features;

	/**
	 * The pivot pixel
	 */
	public IntValuePixel pivot;
	private int size = 0;

	/**
	 * Default constructor.
	 *
	 * @param p
	 *            The grey level of the component
	 * @param featureClasses
	 *            the list of features to create for the component
	 */
	@SafeVarargs
	public Component(IntValuePixel p, Class<? extends ComponentFeature>... featureClasses)
	{
		this.pivot = p;

		features = new ComponentFeature[featureClasses.length];
		for (int i = 0; i < featureClasses.length; i++) {
			try {
				features[i] = featureClasses[i].newInstance();
			} catch (final Exception e) {
				throw new AssertionError(e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.util.AbstractCollection#toString()
	 */
	@Override
	public String toString()
	{
		return "Comp@" + super.hashCode() + "(px:" + size + ",gl:" + pivot.value + ")";
	};

	/**
	 * Add a pixel to the component
	 *
	 * @param p
	 *            the pixel to add
	 */
	public void accumulate(IntValuePixel p) {
		size++;

		for (final ComponentFeature f : features) {
			f.addSample(p);
		}
	}

	/**
	 * Merge another component with this one
	 *
	 * @param p
	 *            the component to merge into this
	 */
	public void merge(Component p) {
		size += p.size();

		for (int i = 0; i < features.length; i++) {
			features[i].merge(p.features[i]);
		}
	}

	/**
	 * The size of the component (i.e. the number of pixels)
	 *
	 * @return the size of the component
	 */
	public int size() {
		return size;
	}

	/**
	 * Get the pixels in the component. If the component contains a
	 * {@link PixelsFeature} then the pixels will be returned from that;
	 * otherwise a set containing just the pivot pixel will be returned.
	 *
	 * @return the pixels in the component if possible, or just the pivot pixel
	 */
	public Set<Pixel> getPixels() {
		for (final ComponentFeature f : features) {
			if (f instanceof PixelsFeature)
				return ((PixelsFeature) f).pixels;
		}

		final Set<Pixel> pix = new HashSet<Pixel>(1);
		pix.add(pivot);
		return pix;
	}

	@Override
	public Component clone() {
		Component result;
		try {
			result = (Component) super.clone();
			result.features = new ComponentFeature[features.length];
			for (int i = 0; i < features.length; i++)
				result.features[i] = features[i].clone();

			// result.pixels = pixels.clone();

			return result;
		} catch (final CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Get the feature at the given index
	 *
	 * @param index
	 *            the index
	 * @return the feature at the given index or null if it doesn't exist
	 */
	public ComponentFeature getFeature(int index) {
		if (index >= features.length)
			return null;

		return features[index];
	}

	/**
	 * Get the feature matching the given class if it exists. If more than one
	 * feature of the given class exists, then the first will be returned.
	 *
	 * @param <T>
	 *            the class of the feature
	 * @param featureClass
	 *            the class of the feature
	 * @return the feature with the given class; or null if no feature is found
	 */
	@SuppressWarnings("unchecked")
	public <T extends ComponentFeature> T getFeature(Class<T> featureClass) {
		for (final ComponentFeature f : features)
			if (f.getClass().isAssignableFrom(featureClass))
				return (T) f;

		return null;
	}
}
