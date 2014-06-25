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

import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.statistics.HistogramModel;

/**
 * An {@link MBFPixelClassificationModel} that classifies an individual pixel by
 * comparing it to a joint (colour) histogram. The histogram is learnt from the
 * positive pixel samples given in training. The probability returned by the
 * classification is determined from the value of the histogram bin in which the
 * pixel being classified falls.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HistogramPixelModel extends MBFPixelClassificationModel {
	private static final long serialVersionUID = 1L;

	/**
	 * The model histogram; public for speed.
	 */
	public HistogramModel model;

	/**
	 * Construct with the given number of histogram bins per dimension.
	 * 
	 * @param nbins
	 *            number of bins per dimension.
	 */
	public HistogramPixelModel(int... nbins) {
		super(nbins.length);
		model = new HistogramModel(nbins);
	}

	@Override
	protected float classifyPixel(Float[] pix) {
		int bin = 0;

		for (int i = 0; i < ndims; i++) {
			int b = (int) (pix[i] * (model.histogram.nbins[i]));
			if (b >= model.histogram.nbins[i])
				b = model.histogram.nbins[i] - 1;

			int f = 1;
			for (int j = 0; j < i; j++)
				f *= model.histogram.nbins[j];

			bin += f * b;
		}

		return (float) model.histogram.values[bin];
	}

	@Override
	public String toString() {
		return model.toString();
	}

	@Override
	public HistogramPixelModel clone() {
		final HistogramPixelModel newmodel = new HistogramPixelModel();
		newmodel.model = model.clone();
		newmodel.ndims = ndims;
		return newmodel;
	}

	@Override
	public void learnModel(MBFImage... images) {
		model.estimateModel(images);
	}
}
