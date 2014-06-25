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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.math.statistics.distribution.CachingMultivariateGaussian;

/**
 * An {@link MBFPixelClassificationModel} that classifies an individual pixel by
 * comparing it to a {@link CachingMultivariateGaussian}. The Gaussian is learnt
 * from the values of the positive pixel samples given in training. The
 * probability returned by the classification is determined from the PDF of the
 * Gaussian at the given pixel.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SingleGaussianPixelModel extends MBFPixelClassificationModel {
	private static final long serialVersionUID = 1L;
	protected CachingMultivariateGaussian gauss;

	/**
	 * Construct with the given number of dimensions. This should be equal to
	 * the number of bands in the {@link MBFImage}s you wish to classify.
	 * 
	 * @param ndims
	 *            the number of dimensions.
	 */
	public SingleGaussianPixelModel(int ndims) {
		super(ndims);
	}

	@Override
	protected float classifyPixel(Float[] pix) {
		return (float) gauss.estimateProbability(pix);
	}

	@Override
	public void learnModel(MBFImage... images) {
		final List<float[]> data = new ArrayList<float[]>();

		for (int i = 0; i < images.length; i++) {

			for (int y = 0; y < images[i].getHeight(); y++) {
				for (int x = 0; x < images[i].getWidth(); x++) {
					final float[] d = new float[ndims];

					for (int j = 0; j < ndims; j++) {
						d[j] = images[i].getBand(j).pixels[y][x];
					}

					data.add(d);
				}
			}
		}

		final float[][] arraydata = data.toArray(new float[data.size()][ndims]);

		gauss = CachingMultivariateGaussian.estimate(arraydata);
	}

	@Override
	public SingleGaussianPixelModel clone() {
		final SingleGaussianPixelModel model = new SingleGaussianPixelModel(ndims);
		model.gauss = new CachingMultivariateGaussian(gauss.getMean().copy(), gauss.getCovariance().copy());

		return null;
	}
}
