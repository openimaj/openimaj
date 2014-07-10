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
package org.openimaj.image.model.pixel;

import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.model.ImageClassificationModel;
import org.openimaj.util.pair.IndependentPair;

/**
 * Simple model for classifying pixels. When learning assumes ALL provided
 * sample pixels are positive exemplars, and that anything not given is
 * negative.
 * 
 * @author Jonathon Hare
 * @param <Q>
 *            Type of pixel
 * @param <T>
 *            Type of image
 * 
 */
public abstract class PixelClassificationModel<Q, T extends Image<Q, T>> implements ImageClassificationModel<T> {
	private static final long serialVersionUID = 1L;

	protected abstract float classifyPixel(Q pix);

	@Override
	public FImage classifyImage(T im) {
		final FImage out = new FImage(im.getWidth(), im.getHeight());

		for (int y = 0; y < im.getHeight(); y++) {
			for (int x = 0; x < im.getWidth(); x++) {
				out.pixels[y][x] = classifyPixel(im.getPixel(x, y));
			}
		}

		return out;
	}

	protected abstract T[] getArray(int length);

	@Override
	public boolean estimate(List<? extends IndependentPair<T, FImage>> data) {
		final T[] samples = getArray(data.size());
		for (int i = 0; i < data.size(); i++) {
			samples[i] = data.get(i).firstObject();
		}
		learnModel(samples);
		return true;
	}

	@Override
	public int numItemsToEstimate() {
		return 1; // need a minimum of 1 sample
	}

	@Override
	public FImage predict(T data) {
		return classifyImage(data);
	}

	@Override
	public abstract PixelClassificationModel<Q, T> clone();
}
