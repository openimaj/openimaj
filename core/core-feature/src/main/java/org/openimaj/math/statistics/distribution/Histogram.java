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
package org.openimaj.math.statistics.distribution;

import org.openimaj.feature.DoubleFV;
import org.openimaj.util.array.ArrayUtils;

/**
 * Simple Histogram based on a DoubleFV.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class Histogram extends DoubleFV {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a histogram with the given number of bins.
	 * 
	 * @param nbins
	 *            number of bins
	 */
	public Histogram(int nbins) {
		super(nbins);
	}

	/**
	 * Construct a histogram by concatenating the given histograms
	 * 
	 * @param hs
	 *            histograms to concatenate
	 */
	public Histogram(DoubleFV... hs) {
		final double[][] hists = new double[hs.length][];
		for (int i = 0; i < hs.length; i++) {
			hists[i] = hs[i].values;
		}

		this.values = ArrayUtils.concatenate(hists);
	}

	/**
	 * Construct from values array and dimensions
	 * 
	 * @param data
	 *            the flat array of values
	 */
	public Histogram(double[] data) {
		super(data);
	}

	/**
	 * Normalise to unit area
	 */
	public void normalise() {
		double sum = 0;

		for (int i = 0; i < values.length; i++)
			sum += values[i];

		for (int i = 0; i < values.length; i++)
			values[i] /= sum;
	}

	/**
	 * l1 norm
	 */
	public void normaliseL1() {
		double sum = 0;

		for (int i = 0; i < values.length; i++)
			sum += Math.abs(values[i]);

		if (sum != 0)
			for (int i = 0; i < values.length; i++)
				values[i] /= sum;
	}

	/**
	 * l2 norm
	 */
	public void normaliseL2() {
		double sumsq = 0;

		for (int i = 0; i < values.length; i++)
			sumsq += values[i] * values[i];

		if (sumsq != 0)
			for (int i = 0; i < values.length; i++)
				values[i] /= Math.sqrt(sumsq);
	}

	/**
	 * Compute the maximum value in the histogram
	 * 
	 * @return the maximum value
	 */
	public double max()
	{
		double max = Double.MIN_VALUE;
		for (int i = 0; i < values.length; i++)
			max = Math.max(values[i], max);
		return max;
	}

	@Override
	public Histogram clone() {
		return (Histogram) super.clone();
	}

	/**
	 * Create a new histogram by concatenating this one with the given ones.
	 * 
	 * @param hs
	 *            histograms to concatenate
	 * @return new histogram that is the concatenation of the argument
	 *         histograms
	 */
	public Histogram combine(Histogram... hs) {
		final int hsLength = hs == null ? 0 : hs.length;
		final double[][] hists = new double[1 + hsLength][];
		hists[0] = this.values;

		for (int i = 0; i < hsLength; i++) {
			hists[i + 1] = hs[i].values;
		}

		return new Histogram(ArrayUtils.concatenate(hists));
	}
}
