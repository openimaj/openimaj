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
import java.util.Map.Entry;
import java.util.Scanner;

import org.openimaj.io.ReadWriteableASCII;
import org.openimaj.ml.timeseries.collection.TimeSeriesCollection;

/**
 * A time series collection of {@link WordDFIDFTimeSeries} instances
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WordDFIDFTimeSeriesCollection 
	extends TimeSeriesCollection<WordDFIDF[], WordDFIDF, WordDFIDFTimeSeriesCollection, WordDFIDFTimeSeries>
	implements ReadWriteableASCII
{

	@Override
	public WordDFIDFTimeSeries internalNewInstance() {
		return new WordDFIDFTimeSeries();
	}

	

	@Override
	public WordDFIDFTimeSeriesCollection newInstance() {
		return new WordDFIDFTimeSeriesCollection();
	}

	@Override
	public void internalAssign(WordDFIDFTimeSeriesCollection interpolate) {
		this.timeSeriesHolder = interpolate.timeSeriesHolder;
	}
	
	@Override
	public String toString() {
		String retstr = "A set time series: " + this.timeSeriesHolder.size() + "\n";
		for (Entry<String, WordDFIDFTimeSeries> dts : this.timeSeriesHolder.entrySet()) {
			retstr += dts.getKey() + "\n";
			retstr += dts.getValue() + "\n";
		}
		return retstr;
	}
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		
		while(in.hasNext()){
			String name = in.next();
			WordDFIDFTimeSeries v = internalNewInstance();
			v.readASCII(in);
			in.nextLine();
			this.timeSeriesHolder.put(name,v);
		}
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		for (Entry<String, WordDFIDFTimeSeries> es : this.timeSeriesHolder.entrySet()) {
			out.println(es.getKey());
			es.getValue().writeASCII(out);
		}
	}
}
