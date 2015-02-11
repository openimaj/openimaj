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
package org.openimaj.ml.timeseries.processor;

import org.openimaj.ml.timeseries.processor.interpolation.LinearInterpolationProcessor;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

/**
 * Calculates a moving average over a specified window in the past such that
 *
 * data[t_n] = sum^{m}_{i=1}{data[t_{n-i}}
 *
 * This processor returns a value for each time in the underlying time series.
 * For sensible results, consider interpolating a consistent time span using an
 * {@link LinearInterpolationProcessor} followed by this processor.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GaussianTimeSeriesProcessor implements TimeSeriesProcessor<double[], Double, DoubleTimeSeries>
{
	private double[] kernel;
	/**
	 * The default number of sigmas at which the Gaussian function is truncated
	 * when building a kernel
	 */
	public static final double DEFAULT_GAUSS_TRUNCATE = 4.0d;

	/**
	 * @param sigma
	 *            the sigma of the guassian function
	 */
	public GaussianTimeSeriesProcessor(double sigma) {
		this.kernel = makeKernel(sigma, DEFAULT_GAUSS_TRUNCATE);
	}

	/**
	 * Construct a zero-mean Gaussian with the specified standard deviation.
	 * 
	 * @param sigma
	 *            the standard deviation of the Gaussian
	 * @param truncate
	 *            the number of sigmas from the centre at which to truncate the
	 *            Gaussian
	 * @return an array representing a Gaussian function
	 */
	public static double[] makeKernel(double sigma, double truncate) {
		if (sigma == 0)
			return new double[] { 1f };
		// The kernel is truncated at truncate sigmas from center.
		int ksize = (int) (2.0f * truncate * sigma + 1.0f);
		// ksize = Math.max(1, ksize); // size must be at least 3
		if (ksize % 2 == 0)
			ksize++; // size must be odd

		final double[] kernel = new double[ksize];

		// build kernel
		float sum = 0.0f;
		for (int i = 0; i < ksize; i++) {
			final float x = i - ksize / 2;
			kernel[i] = (float) Math.exp(-x * x / (2.0 * sigma * sigma));
			sum += kernel[i];
		}

		// normalise area to 1
		for (int i = 0; i < ksize; i++) {
			kernel[i] /= sum;
		}

		return kernel;
	}

	/**
	 * Convolve a double array
	 *
	 * @param data
	 *            the image to convolve.
	 * @param kernel
	 *            the convolution kernel.
	 */
	public static void convolveHorizontal(double[] data, double[] kernel) {
		final int halfsize = kernel.length / 2;

		final double buffer[] = new double[data.length + kernel.length];

		for (int i = 0; i < halfsize; i++)
			buffer[i] = data[0];
		for (int i = 0; i < data.length; i++)
			buffer[halfsize + i] = data[i];

		for (int i = 0; i < halfsize; i++)
			buffer[halfsize + data.length + i] = data[data.length - 1];

		// convolveBuffer(buffer, kernel);
		final int l = buffer.length - kernel.length;
		for (int i = 0; i < l; i++) {
			float sum = 0.0f;

			for (int j = 0, jj = kernel.length - 1; j < kernel.length; j++, jj--)
				sum += buffer[i + j] * kernel[jj];

			buffer[i] = sum;
		}
		// end convolveBuffer(buffer, kernel);

		for (int c = 0; c < data.length; c++)
			data[c] = buffer[c];
	}

	@Override
	public void process(DoubleTimeSeries series) {
		convolveHorizontal(series.getData(), this.kernel);
	}
}
