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
	/** The start frequency of the filter */
	private double startFrequency = 0;
	
	/** The end frequency of the filter */
	private double endFrequency = 44100;
	
	/** The centre frequency */
	private double centreFrequency = (endFrequency - startFrequency)/2; 
	
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
	public MelFilter( double startFreq, double endFreq )
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
		
		this.filterAmplitude = 2f / (endFrequency - startFrequency);
		this.lowSlope = filterAmplitude / (centreFrequency - startFrequency);
		this.highSlope = filterAmplitude / (endFrequency - centreFrequency);
	}
	
	/**
	 * 	Requires a power spectrum and produces an output for each of the
	 * 	Mel Frequencies.
	 * 
	 *	@param frequencySpectrum The power spectrum
	 * 	@param format The format of the samples used to create the spectrum 
	 *	@return The output power for the filter
	 */
	public double process( float[][] frequencySpectrum, AudioFormat format )
	{
		double output = 0d;
		
		// The size of each bin in Hz (using the first channel as examplar)
		double binSize = (format.getSampleRateKHz()*1000) 
				/ (frequencySpectrum[0].length/2);

		int startBin = (int)(startFrequency / binSize);
		int endBin = (int)(endFrequency / binSize);
		
		// Now apply the filter to the spectrum and accumulate the output
		for( int c = 0; c < frequencySpectrum.length; c++ )
		{
			for( int x = startBin; x < endBin; x++ )
			{
				// Ensure we're within the bounds of the spectrum
				if( x >= 0 && x < frequencySpectrum[c].length )
				{				
					double binFreq = binSize * x;
					double weight = 0;
					
					// Up or down slope depending on whether we're left or
					// right of the centre frequency
					if( binFreq < centreFrequency )
							weight = lowSlope * (centreFrequency - binFreq);
					else	weight = filterAmplitude - highSlope * (binFreq - centreFrequency);
					
					output += weight * frequencySpectrum[c][x];
				}
			}
		}
		
		return output;
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
}
