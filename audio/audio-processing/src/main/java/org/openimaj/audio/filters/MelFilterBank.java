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
/**
 *
 */
package org.openimaj.audio.filters;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.util.AudioUtils;

/**
 *	Filter bank of Mel filters for applying to a frequency domain source. It
 *	is standard that the edges of each filter in the filter bank correspond
 *	to the centre of the neighbouring filter - so they overlap by half (in
 *	the Mel frequencies).
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 25 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MelFilterBank
{
	/** The lowest frequency covered by this filter bank */
	private double lowestFreq = 300;

	/** The highest frequency coverted by this filter bank */
	private double highestFreq = 5000;

	/** The number of filters in this filter bank */
	private int nFilters = 40;

	/** The list of filters */
	private ArrayList<TriangularFilter> filters = null;

	/**
	 * 	Construct a default MelFilterBank. The defaults are the lowest
	 * 	frequency covered is 300Hz, the highest 5000Hz covered by 40 Mel filters.
	 */
	public MelFilterBank()
	{
	}

	/**
	 * 	Default constructor to create a filter bank with the given number
	 * 	of filters between the two given frequencies.
	 *
	 *	@param nFilters The number of filters
	 *	@param lowFreq The lowest frequency covered by the bank
	 *	@param highFreq The highest frequency covered by the bank
	 */
	public MelFilterBank( final int nFilters, final double lowFreq, final double highFreq )
	{
		this.lowestFreq = lowFreq;
		this.highestFreq = highFreq;
		this.nFilters = nFilters;
	}

	/**
	 * 	Instantiate the filter bank, if it's not already instantiated.
	 */
	private void createFilterBank()
	{
		if( this.filters == null )
		{
			this.filters = new ArrayList<TriangularFilter>();

			// Convert the range of the filter banks (in Hz) to Mel frequencies
			final double lowFreqMel = AudioUtils.frequencyToMelFrequency( this.lowestFreq );
			final double highFreqMel = AudioUtils.frequencyToMelFrequency( this.highestFreq );
			final double melFreqRange = highFreqMel-lowFreqMel;

			// The filters are evenly distributed on the Mel Scale.
			final double melFreqPerFilter = 2*melFreqRange /(this.nFilters+1);

			// Create the Filters
			for( int filter = 0; filter < this.nFilters; filter++ )
			{
				// Centre frequency of the mel triangular filter
				final double lf = lowFreqMel + melFreqPerFilter/2 * filter;
				final double cf = lf + melFreqPerFilter/2;
				final double hf = lf + melFreqPerFilter;
				this.filters.add( new TriangularFilter(
						AudioUtils.melFrequencyToFrequency( lf ),
						AudioUtils.melFrequencyToFrequency( cf ),
						AudioUtils.melFrequencyToFrequency( hf )
				) );
			}
		}
	}

	/**
	 * 	Returns a list of filters in this filter bank
	 *	@return The filters
	 */
	public List<TriangularFilter> getFilters()
	{
		this.createFilterBank();
		return this.filters;
	}

	/**
	 * 	Process the input power spectrum with this filter bank. The output is
	 * 	a set of Mel Frequency Coefficients for each channel of the audio. The
	 * 	power spectrum is expected to be just the magnitudes for the real parts
	 * 	of a frequency spectrum.
	 *
	 *	@param spectrum The power spectrum
	 *	@param format The format of the original audio used to produce the
	 *		spectrum
	 *	@return The Mel frequency coefficients
	 */
	public float[][] process( final float[][] spectrum, final AudioFormat format )
	{
		// Make sure we've got some filters to apply
		this.createFilterBank();

		final float[][] output = new float[spectrum.length][this.filters.size()];

		for( int c = 0; c < spectrum.length; c++ )
			for( int i = 0; i < this.filters.size(); i++ )
				output[c][i] = (float)this.filters.get(i).process( spectrum[c], format );

		return output;
	}
}
