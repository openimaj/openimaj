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
package org.openimaj.ml.timeseries.converter;

import org.openimaj.ml.timeseries.TimeSeries;
import org.openimaj.ml.timeseries.processor.TimeSeriesProcessor;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeriesProvider;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <INPUTALL>
 * @param <INPUTSINGLE>
 * @param <INPUTTS>
 * 
 * Given a double provider, convert and process returning a {@link DoubleTimeSeries}
 */
public class DoubleProviderTimeSeriesConverter<
			INPUTALL,
			INPUTSINGLE,
			INPUTTS extends TimeSeries<INPUTALL,INPUTSINGLE,INPUTTS> & DoubleTimeSeriesProvider 
		>
		implements
		TimeSeriesConverter<INPUTALL, INPUTSINGLE, INPUTTS, double[], Double, DoubleTimeSeries> {

	@Override
	public DoubleTimeSeries convert(INPUTTS series) {
		return series.doubleTimeSeries();
	}

	@Override
	public DoubleTimeSeries convert(INPUTTS series,TimeSeriesProcessor<double[], Double, DoubleTimeSeries> processor) {
		DoubleTimeSeries r = series.doubleTimeSeries();
		processor.process(r);
		return r;
	}

}
