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
package org.openimaj.image.feature.local.descriptor.gradient;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.feature.local.descriptor.LocalFeatureProvider;
import org.openimaj.math.util.Interpolation;

/**
 * Interface for classes capable of building local descriptors from the gradient
 * (magnitude and orientation) information in an image patch. The model for the
 * construction of various concrete feature extractors that implement this
 * interface is that some external code will provide individual pixel samples
 * (consisting of both magnitude and orientation values) and their relative
 * positions within a unit sampling patch (or slightly beyond if interpolation
 * is used; see {@link Interpolation#bilerp }). <br/>
 * <br/>
 * The general contract for methods using this interface is as follows: <br/>
 * <br/>
 * <code>
 * 	f = new GradientBasedLocalFeatureExtractorImpl() <br/>
 * 	f.setPatchOrientation(orientation) <br/>
 * 	for (pixels in patch) <br/>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;f.addSample(normalised-pixel-position, pixel-magnitude, pixel-orientation) <br/>
 * </code>
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public interface GradientFeatureProvider extends LocalFeatureProvider<OrientedFeatureVector> {
	/**
	 * Set the primary orientation of the sample patch in the image being
	 * processed. This might be used by concrete implementations to provide
	 * rotation invariance.
	 *
	 * This method should only be called once by calling code, and the call
	 * should be before any calls to
	 * {@link #addSample(float, float, float, float)} are made.
	 *
	 * @param patchOrientation
	 *            the actual orientation of the square in the image
	 */
	public abstract void setPatchOrientation(float patchOrientation);

	/**
	 * Add a sample to the feature. The x and y coordinates are given in terms
	 * of a unit index square (i.e. 0<=x<=1 and 0<=y<=1).
	 *
	 * @param x
	 *            x-coordinate within the unit indexing square
	 * @param y
	 *            y-coordinate within the unit indexing square
	 * @param gradmag
	 *            the gradient magnitude at the given coordinate
	 * @param gradori
	 *            the gradient direction at the given coordinate
	 */
	public abstract void addSample(float x, float y, float gradmag, float gradori);

	@Override
	public OrientedFeatureVector getFeatureVector();
}
