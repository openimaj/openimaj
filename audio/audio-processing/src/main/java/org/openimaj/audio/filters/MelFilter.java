/**
 * 
 */
package org.openimaj.audio.filters;

import org.openimaj.audio.AudioFormat;

/**
 *	A Mel triangular filter for frequency spectrum.
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
	
	/** The slope of first half of the filter. Second half is -slope */
	private double slope = 1;
	
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

		this.startFrequency = startFreq;
		this.endFrequency = endFreq;

		this.centreFrequency = (endFrequency - startFrequency)/2;
		this.filterAmplitude = 2f / (endFrequency - startFrequency);
		this.slope = filterAmplitude / (centreFrequency - startFrequency);
	}
	
	/**
	 * 	Requires a power spectrum and produces an output for each of the
	 * 	Mel Frequencies.
	 * 
	 *	@param frequencySpectrum The power spectrum
	 * 	@param format The format of the samples used to create the spectrum 
	 *	@return The output power for the spectrum
	 */
	public double[][] process( float[][] frequencySpectrum, AudioFormat format )
	{
		double[][] output = new double[frequencySpectrum.length][];
		
		// The size of each bin in Hz (using the first channel as examplar)
		double binSize = (format.getSampleRateKHz()*1000) 
				/ (frequencySpectrum[0].length/2);

		int startBin = (int)(startFrequency / binSize);
		int endBin = (int)(endFrequency / binSize);
		int centreBin = (int)(centreFrequency / binSize);
		
		// Now apply the filter to the spectrum and accumulate the output
		for( int c = 0; c < frequencySpectrum.length; c++ )
		{
			output[c] = new double[frequencySpectrum[c].length];
			
			for( int x = startBin; x < endBin; x++ )
			{
				// Ensure we're within the bounds of the 
				if( x >= 0 && x < frequencySpectrum[c].length )
				{				
					double binFreq = binSize * x;
					double weight = 0;
					
					if( x < centreBin )
							weight = slope * (centreFrequency - binFreq);
					else	weight = filterAmplitude - slope * (binFreq - centreFrequency);
					
					output[c][x] = weight * frequencySpectrum[c][x];
				}
			}
		}
		
		return output;
	}
}
