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

import org.openimaj.audio.AudioFormat;

/**
 *	A default non-isoceles triangular filter definition, where the low, mid
 *	and top frequencies are defined.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Dec 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class TriangularFilter
{
	/** The lowest frequency of the filter */
	protected double lowFrequency = 0;

	/** The highest frequency of the filter */
	protected double highFrequency = 20000;

	/** The centre (peak) frequency of the filter */
	protected double centreFrequency = 10000;

	/** The height of the filter */
	protected double filterAmplitude;

	/** The slope of first half of the filter. */
	protected double lowSlope;

	/** The slope of the second half of the filter */
	protected double highSlope;

	/**
	 *
	 *	@param low
	 *	@param centre
	 *	@param high
	 */
	public TriangularFilter( final double low, final double centre, final double high )
    {
		// Ensure the end frequency is greater than the start frequency
		if( high <= low )
			throw new IllegalArgumentException( "Triangular Filter start and end " +
					"frequencies are incorrect: "+high+" <= "+low );

		this.lowFrequency = low;
		this.highFrequency = high;
		this.centreFrequency = centre;

		this.filterAmplitude = 2f / (this.highFrequency - this.lowFrequency);
		this.lowSlope = this.filterAmplitude / (this.centreFrequency - this.lowFrequency);
		this.highSlope = this.filterAmplitude / (this.highFrequency - this.centreFrequency);

    }

	/**
	 * 	Returns the lowest frequency of the filter
	 *	@return The low frequency of the filter
	 */
	public double getLowFrequency()
	{
		return this.lowFrequency;
	}

	/**
	 * 	Sets the low frequency
	 *	@param lowFrequency The new low frequency
	 */
	public void setLowFrequency( final double lowFrequency )
	{
		this.lowFrequency = lowFrequency;
		if( this.highFrequency <= this.lowFrequency )
			throw new IllegalArgumentException( "Triangular Filter start and end " +
					"frequencies are incorrect: "+this.highFrequency+" <= "+this.lowFrequency );
	}

	/**
	 * 	Gets the high frequency
	 *	@return The high frequency
	 */
	public double getHighFrequency()
	{
		return this.highFrequency;
	}

	/**
	 * 	Set the high frequency of the filter
	 *	@param highFrequency The new high frequency
	 */
	public void setHighFrequency( final double highFrequency )
	{
		this.highFrequency = highFrequency;
		if( this.highFrequency <= this.lowFrequency )
			throw new IllegalArgumentException( "Triangular Filter start and end " +
					"frequencies are incorrect: "+highFrequency+" <= "+this.lowFrequency );
	}

	/**
	 * 	Get the centre frequency
	 *	@return The centre frequency
	 */
	public double getCentreFrequency()
	{
		return this.centreFrequency;
	}

	/**
	 * 	Set the new centre frequency
	 *	@param centreFrequency The new centre frequency
	 */
	public void setCentreFrequency( final double centreFrequency )
	{
		this.centreFrequency = centreFrequency;
		if( this.centreFrequency <= this.lowFrequency || this.centreFrequency >= this.highFrequency )
			throw new IllegalArgumentException( "Triangular Filter start and end " +
					"frequencies are incorrect: centre frequency "+centreFrequency );
	}

	/**
	 * 	Requires a power spectrum and produces an output the filter
	 *
	 *	@param frequencySpectrum The power spectrum
	 * 	@param format The format of the samples used to create the spectrum
	 *	@return The output power for the filter
	 */
	public double process( final float[] frequencySpectrum, final AudioFormat format )
	{
		double output = 0d;

		// The size of each bin in Hz (using the first channel as examplar)
		final double binSize = (format.getSampleRateKHz()*1000)
				/ (frequencySpectrum.length/2);

		final int startBin = (int)(this.lowFrequency / binSize);
		final int endBin = (int)(this.highFrequency / binSize);

		// Now apply the filter to the spectrum and accumulate the output
		for( int x = startBin; x < endBin; x++ )
		{
			// Ensure we're within the bounds of the spectrum
			if( x >= 0 && x < frequencySpectrum.length )
			{
				final double binFreq = binSize * x;
				final double weight = this.getWeightAt(binFreq);
				output += weight * frequencySpectrum[x];
			}
		}

		return output;
	}

	/**
	 * 	Returns a set of values that represent the response of this filter
	 * 	when the linear frequency is split in the given number of bins. The
	 * 	result will have <code>nSpectrumBins</code> length.
	 *
	 * 	@param nSpectrumBins The number of bins in a spectrum.
	 * 	@param maxFreq The maximum frequency (sample rate)
	 *	@return The response curve.
	 */
	public double[] getResponseCurve( final int nSpectrumBins, final double maxFreq )
	{
		final double[] curve = new double[nSpectrumBins];
		final double binSize = maxFreq / nSpectrumBins;

		for( int x = 0; x < nSpectrumBins; x++ )
			curve[x] = this.getWeightAt( binSize * x);

		return curve;
	}

	/**
	 * 	Returns the weighting provided by this filter at the given frequency (Hz).
	 *	@param frequency The frequency (Hz) to get the weight for
	 *	@return The weight at the given frequency (Hz) for this filter
	 */
	public double getWeightAt( final double frequency )
	{
		// Up or down slope depending on whether we're left or
		// right of the centre frequency
		double weight = 0;
		if( frequency < this.centreFrequency )
				weight = this.filterAmplitude - this.lowSlope * (this.centreFrequency - frequency);
		else	weight = this.filterAmplitude - this.highSlope * (frequency - this.centreFrequency);

		if( weight < 0 ) weight = 0;

		return weight;
	}

	/**
	 * 	Returns the filter amplitude
	 *	@return The filter amplitude
	 */
	public double getFilterAmplitude()
	{
		return this.filterAmplitude;
	}
	
	/**
	 * 	Set the filter amplitude
	 *	@param fa The filter amplitude
	 */
	public void setFilterAmplitude( final double fa )
	{
		this.filterAmplitude = fa;
		this.lowSlope = this.filterAmplitude / (this.centreFrequency - this.lowFrequency);
		this.highSlope = this.filterAmplitude / (this.highFrequency - this.centreFrequency);
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
//		return ""+this.centreFrequency;
		return "tf{"+this.lowFrequency+"->"+this.centreFrequency+"->"+this.highFrequency+"}";
	}

}
