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
package org.openimaj.ml.timeseries.series;

import java.util.Iterator;
import java.util.Map;

import org.openimaj.ml.timeseries.IncompatibleTimeSeriesException;
import org.openimaj.ml.timeseries.collection.SynchronisedTimeSeriesCollection;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DoubleSynchronisedTimeSeriesCollection
		extends
			SynchronisedTimeSeriesCollection<double[], Double, DoubleSynchronisedTimeSeriesCollection, DoubleTimeSeries>
{

	/**
	 * basic constructor
	 */
	public DoubleSynchronisedTimeSeriesCollection() {
	}

	/**
	 * create a synchronised series from a bunch of pairs
	 * 
	 * @param series
	 * @throws IncompatibleTimeSeriesException
	 */
	@SafeVarargs
	public DoubleSynchronisedTimeSeriesCollection(IndependentPair<String, DoubleTimeSeries>... series)
			throws IncompatibleTimeSeriesException
	{
		for (final IndependentPair<String, DoubleTimeSeries> dts : series) {
			this.addTimeSeries(dts.firstObject(), dts.secondObject());
		}
	}

	@Override
	public DoubleTimeSeries internalNewInstance() {
		return new DoubleTimeSeries();
	}

	@Override
	public DoubleSynchronisedTimeSeriesCollection newInstance() {
		return new DoubleSynchronisedTimeSeriesCollection();
	}

	@Override
	public double[] flatten() {

		final int tlength = this.getTimes().length;
		final int nseries = this.nSeries();
		final double[] flattened = new double[tlength * nseries];
		int seriesi = 0;
		for (final DoubleTimeSeries series : this.allseries()) {
			final double[] toCopy = series.getData();
			for (int timej = 0; timej < tlength; timej++) {
				flattened[seriesi + timej * nseries] = toCopy[timej];
			}
			seriesi++;
		}
		return flattened;
	}

	@Override
	public Iterator<IndependentPair<Long, Map<String, Double>>> iterator() {
		// return new Iterator<IndependentPair<Long,Map<String,Double>>>() {
		// };
		return null;
	}
}
