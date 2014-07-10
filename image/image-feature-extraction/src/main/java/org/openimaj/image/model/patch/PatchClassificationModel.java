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
package org.openimaj.image.model.patch;

import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.model.ImageClassificationModel;
import org.openimaj.util.pair.IndependentPair;

/**
 * An {@link ImageClassificationModel} based on the idea of determining the
 * probability of a class of a pixel given the local patch of pixels surrounding
 * the pixel in question. A sliding window of a given size is moved across the
 * image (with overlap), and the contents of the window are analysed to
 * determine the probability belonging to the pixel at the centre of the window.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <Q>
 *            Type of pixel
 * @param <T>
 *            Type of {@link Image}
 */
public abstract class PatchClassificationModel<Q, T extends Image<Q, T>> implements ImageClassificationModel<T> {
	private static final long serialVersionUID = 1L;

	protected int patchHeight, patchWidth;

	/**
	 * Construct with the given dimensions for the sampling patch.
	 * 
	 * @param patchWidth
	 *            the width of the sampling patch
	 * @param patchHeight
	 *            the height of the sampling patch
	 */
	public PatchClassificationModel(int patchWidth, int patchHeight) {
		this.patchHeight = patchHeight;
		this.patchWidth = patchWidth;
	}

	/**
	 * Classify a patch, returning the probability of the central pixel
	 * belonging to the class.
	 * 
	 * @param patch
	 *            the patch.
	 * @return the probability of the central pixel belonging to the class.
	 */
	public abstract float classifyPatch(T patch);

	@Override
	public FImage classifyImage(T im) {
		final FImage out = new FImage(im.getWidth(), im.getHeight());
		final T roi = im.newInstance(patchWidth, patchHeight);

		final int hh = patchHeight / 2;
		final int hw = patchWidth / 2;

		for (int y = hh; y < im.getHeight() - (patchHeight - hh); y++) {
			for (int x = hw; x < im.getWidth() - (patchWidth - hw); x++) {
				im.extractROI(x - hw, y - hh, roi);
				out.pixels[y][x] = this.classifyPatch(roi);
			}
		}

		return out;
	}

	@Override
	public abstract PatchClassificationModel<Q, T> clone();

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
}
