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
import org.openimaj.audio.util.AudioUtils;

/**
 *	A Mel triangular filter for frequency spectrum. The class is constructed
 *	using linear frequencies (Hz) to give the start and end points of the filter.
 *	These are converted into Mel frequencies (non-linear) so that the triangle
 *	which is Isosceles in Mel frequency is non-linear in the linear frequency
 *	(stretched towards the higher frequencies). 
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 25 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MelFilter
{
	/** The start frequency of the filter (Hz) */
	private double startFrequency = 0;
	
	/** The end frequency of the filter (Hz) */
	private double endFrequency = 44100;
	
	/** The centre frequency (HZ) */
	private double centreFrequency = (this.endFrequency - this.startFrequency)/2; 
	
	/** The height of the filter */
	private double filterAmplitude = 1;
	
	/** The slope of first half of the filter. */
	private double lowSlope = 1;
	
	/** The slope of the second half of the filter */
	private double highSlope = 1;
	
	/**
	 * 	The Mel Filter default constructor.
	 * 
	 *	@param startFreq The start frequency of the filter
	 *	@param endFreq The end frequency of the filter
	 */
	public MelFilter( final double startFreq, final double endFreq )
	{
		// Ensure the end frequency is greater than the start frequency
		if( endFreq <= startFreq )
			throw new IllegalArgumentException( "Mel Filter start and end " +
					"frequencies are incorrect." );

		// These are linear frequencies
		this.startFrequency = startFreq;
		this.endFrequency = endFreq;
		
		// We need to work out the centre frequency in Mel terms. We work out the
		// centre frequency in the Mel scale then convert back to linear frequency.
		this.centreFrequency = AudioUtils.melFrequencyToFrequency(
				(AudioUtils.frequencyToMelFrequency( endFreq ) +
				 AudioUtils.frequencyToMelFrequency( startFreq )) /2d 
			);
		
		this.filterAmplitude = 2f / (this.endFrequency - this.startFrequency);
		this.lowSlope = this.filterAmplitude / (this.centreFrequency - this.startFrequency);
		this.highSlope = this.filterAmplitude / (this.endFrequency - this.centreFrequency);
	}
	
	/**
	 * 	Requires a power spectrum and produces an output for each of the
	 * 	Mel Frequencies.
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

		final int startBin = (int)(this.startFrequency / binSize);
		final int endBin = (int)(this.endFrequency / binSize);
		
		// Now apply the filter to the spectrum and accumulate the output
		for( int x = startBin; x < endBin; x++ )
		{
			// Ensure we're within the bounds of the spectrum
			if( x >= 0 && x < frequencySpectrum.length )
			{				
				final double binFreq = binSize * x;
				final double weight = this.getWeightAt(binFreq);
//				System.out.println( "Weight at bin "+x+" ("+binFreq+"Hz) is "+weight );
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
	 * 	Get the start frequency of this filter.
	 *	@return The start frequency in Hz
	 */
	public double getStartFrequency()
	{
		return this.startFrequency;
	}
	
	/**
	 * 	Get the end frequency of this filter
	 *	@return the end frequency in Hz
	 */
	public double getEndFrequency()
	{
		return this.endFrequency;
	}
	
	/**
	 * 	Get the centre frequency of this filter. Due to the non-linear scale
	 * 	of the Mel frequency scale, this will not be (end-start)/2.
	 * 	
	 *	@return The centre frequency in Hz.
	 */
	public double getCentreFrequency()
	{
		return this.centreFrequency;
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
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "mf{"+this.startFrequency+"->"+this.endFrequency+"}";
	}
}
