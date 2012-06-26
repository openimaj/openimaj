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

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * A time series converter goes from one type of {@link TimeSeries} to another given a processor.
 * 
 * @param <INPUTALL> The input all data type
 * @param <INPUTSINGLE> the input single data type
 * @param <INPUTTS> the input time series
 * @param <OUTPUTALL> the output all data type
 * @param <OUTPUTSINGLE> the output single data type
 * @param <OUTPUTTS> the output time series type
 */
public interface TimeSeriesConverter<
	INPUTALL,INPUTSINGLE,INPUTTS extends TimeSeries<INPUTALL,INPUTSINGLE,INPUTTS>,
	OUTPUTALL,OUTPUTSINGLE,OUTPUTTS extends TimeSeries<OUTPUTALL,OUTPUTSINGLE,OUTPUTTS>
> {
	/**
	 * @param series convert the series
	 * @return the converted series
	 */
	public OUTPUTTS convert(INPUTTS series);
	
	/**
	 * convert and process a time series
	 * @param series the input series
	 * @param processor the processor to alter the converted input
	 * @return the processed converted input
	 */
	public OUTPUTTS convert(INPUTTS series, TimeSeriesProcessor<OUTPUTALL, OUTPUTSINGLE, OUTPUTTS> processor);
}
