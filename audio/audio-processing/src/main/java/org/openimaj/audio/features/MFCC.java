/**
 *
 */
package org.openimaj.audio.features;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.samples.SampleBuffer;

/**
 *	A wrapper around the MFCC implementation of jAudio (which itself
 *	is a wrapper around the OrangeCow Volume implementation of FFT).
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 5 Mar 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class MFCC extends JAudioFeatureExtractor
{
	/** Processor for magnitude spectrum */
	private MagnitudeSpectrum magSpec = null;

	/**
	 * 	Default constructor
	 */
	public MFCC()
	{
		this.featureExtractor = new jAudioFeatureExtractor.AudioFeatures.MFCC();
		this.magSpec = new MagnitudeSpectrum();
	}

	/**
	 * 	Constructor for chaining to a stream
	 *	@param as The audio stream
	 */
	public MFCC( final AudioStream as )
	{
		super( as );
		this.featureExtractor = new jAudioFeatureExtractor.AudioFeatures.MFCC();
		this.magSpec = new MagnitudeSpectrum();
	}

	/**
	 * 	Calculate the MFCCs for the given sample buffer.
	 *	@param sb The sample buffer
	 *	@return The MFCCs
	 */
	public double[][] calculateMFCC( final SampleBuffer sb )
	{
		this.process( sb );
		return this.getLastCalculatedFeature();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.features.JAudioFeatureExtractor#getExtraInputs(double[], double)
	 */
	@Override
	public double[][] getExtraInputs( final double[] samples, final double sampleRate )
	{
		return new double[][] { this.magSpec.process( samples, sampleRate ) };
	}

	/**
	 * 	Returns the MFCCs with the first coefficient set to zero.
	 *	@return The MFCCs with the first set to zero.
	 */
	public double[][] getLastCalculatedFeatureWithoutFirst()
	{
		final double[][] d = this.getLastCalculatedFeature();
		for( final double[] dd : d )
			dd[0] = 0;
		return d;
	}
}
