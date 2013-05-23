/**
 *
 */
package org.openimaj.audio.features;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.analysis.FourierTransform;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FloatFV;

/**
 *	Spectral flux is the RMS difference between subsequent spectra.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 17 May 2013
 */
public class SpectralFlux2 extends AudioProcessor
{
	/** The FFT processor */
	private final FourierTransform fft = new FourierTransform();

	/** The last processed FFT */
	private DoubleFV[] lastFFTFV;

	/** The last calcualted Spectral Flux */
	private double[] lastSpectralFlux = null;

	/**
	 * 	Constructor for ad-hoc processing
	 */
	public SpectralFlux2()
	{

	}

	/**
	 * 	Default constructor that takes the stream to chain to.
	 *	@param as The stream to chain to.
	 */
	public SpectralFlux2( final AudioStream as )
	{
		super( as );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		// Get the spectrum
		this.fft.process( sample );
		final float[][] thisFFT = this.fft.getMagnitudes();

		// Convert into FloatFVs
		final DoubleFV[] thisFFTFV = new DoubleFV[thisFFT.length];
		for( int c = 0; c < thisFFT.length; c++ )
			thisFFTFV[c] = new FloatFV( thisFFT[c] ).normaliseFV();

		// Lazily instantiate the spectral flux double array
		if( this.lastSpectralFlux == null )
			this.lastSpectralFlux = new double[ thisFFTFV.length ];

		if( this.lastFFTFV != null )
		{
			// Loop over the channels and calculate spectral distance
			for( int c = 0; c < thisFFT.length; c++ )
				this.lastSpectralFlux[c] = DoubleFVComparison.EUCLIDEAN
					.compare( thisFFTFV[c], this.lastFFTFV[c] );
		}

		this.lastFFTFV = thisFFTFV;
		return sample;
	}

	/**
	 * 	Get the last calculated spectral flux
	 *	@return The last calcualted spectral flux
	 */
	public double[] getLastSpectralFlux()
	{
		return this.lastSpectralFlux;
	}
}
