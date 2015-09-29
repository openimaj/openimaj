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
package org.openimaj.image.model;

import java.io.Serializable;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.math.model.EstimatableModel;

/**
 * An ImageClassificationModel is a {@link EstimatableModel} constructed between
 * an generic image and a probability map in the form of an FImage.
 *
 * Potential uses for such a model are for the prediction of certain classes of
 * pixels in an image. For example, a model could be constructed that predicted
 * skin-tones in an image based on hue and saturation values of pixels. With
 * such a model, a colour image could be presented, and a probability map would
 * be returned.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <T>
 *            the type of image that the model can be applied to
 *
 */
public interface ImageClassificationModel<T extends Image<?, T>> extends EstimatableModel<T, FImage>, Serializable {
	/**
	 * Learn the model from the given {@link MBFImage}s.
	 *
	 * @param images
	 *            the images to learn from
	 */
	public abstract void learnModel(@SuppressWarnings("unchecked") T... images);

	/**
	 * Classify the given image and return the corresponding probability map
	 *
	 * @param im
	 *            the image to classify
	 * @return the probability map
	 */
	public abstract FImage classifyImage(T im);

	@Override
	public abstract ImageClassificationModel<T> clone();
}
