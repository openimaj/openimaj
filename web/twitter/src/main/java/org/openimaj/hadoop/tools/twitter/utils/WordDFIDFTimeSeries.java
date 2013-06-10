/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.hadoop.tools.twitter.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.io.ReadWriteableASCII;
import org.openimaj.ml.timeseries.TimeSeriesArithmaticOperator;
import org.openimaj.ml.timeseries.series.ConcreteTimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeriesProvider;
import org.openimaj.util.pair.IndependentPair;

/**
 * A time series of WordDFIDF instances
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class WordDFIDFTimeSeries
		extends ConcreteTimeSeries<WordDFIDF, WordDFIDFTimeSeries>
		implements
		TimeSeriesArithmaticOperator<WordDFIDF, WordDFIDFTimeSeries>,
		DoubleTimeSeriesProvider,
		ReadWriteableASCII
{

	@Override
	public WordDFIDFTimeSeries newInstance() {
		return new WordDFIDFTimeSeries();
	}

	@Override
	public WordDFIDF zero() {
		return new WordDFIDF();
	}

	/**
	 * An explicit assumption is made that {@link WordDFIDF} instances all come
	 * from the same period of time and therefore have the same total number of
	 * tweets and total number of word instances across time (i.e.
	 * {@link WordDFIDF#Ttf} and {@link WordDFIDF#Twf} remain untouched)
	 */
	@Override
	public WordDFIDF sum() {
		final WordDFIDF ret = zero();
		for (final WordDFIDF time : this.getData()) {
			ret.tf += time.tf;
			ret.wf += time.wf;
			ret.Ttf = time.Ttf;
			ret.Twf = time.Twf;
		}
		return ret;
	}

	@Override
	public DoubleTimeSeries doubleTimeSeries() {
		final long[] times = this.getTimes();
		final double[] values = new double[times.length];
		final WordDFIDF[] current = this.getData();
		int i = 0;
		for (final WordDFIDF wordDFIDF : current) {
			values[i++] = wordDFIDF.dfidf();
		}
		return new DoubleTimeSeries(times, values);
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		final int count = in.nextInt();
		for (int i = 0; i < count; i++) {
			final WordDFIDF instance = new WordDFIDF();
			instance.readASCII(in);
			this.add(instance.timeperiod, instance);
		}
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.print(this.size() + " ");
		for (final IndependentPair<Long, WordDFIDF> i : this) {
			i.secondObject().writeASCII(out);
			out.print(" ");
		}
	}

}
