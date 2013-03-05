/**
 *
 */
package org.openimaj.audio.features;

import jAudioFeatureExtractor.AudioFeatures.MFCC;
import jAudioFeatureExtractor.AudioFeatures.MagnitudeSpectrum;

import org.openimaj.audio.samples.SampleBuffer;

/**
 *	A wrapper around the MFCC implementation of jAudio (which itself
 *	is a wrapper around the OrangeCow Volume implementation of FFT).
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 5 Mar 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class MFCCJAudio
{
	/** The jAudio MFCC feature extractor */
	private final MFCC mfccFeatureExtractor = new MFCC();

	/** The jAudio Magnitude Spectrum feature extractor */
	private final MagnitudeSpectrum magSpectrum = new MagnitudeSpectrum();

	/**
	 * 	Calculate the MFCCs for a multi-channel sample buffer. The MFCCs for each
	 * 	channel are calculated independently.
	 *
	 *	@param sb The sample buffer to process
	 *	@return The MFCCs
	 */
	public double[][] calculateMFCC( final SampleBuffer sb )
	{
		final double[][] chanSamples = sb.asDoubleChannelArray();
		final double[][] mfccs = new double[chanSamples.length][];
		for( int c = 0; c < sb.getFormat().getNumChannels(); c++ )
			mfccs[c] = this.calculateMFCC( chanSamples[c], sb.getFormat().getSampleRateKHz()*1000d );
		return mfccs;
	}

	/**
	 * 	Calculate the MFCCs for a multi-channel sample. The MFCCs for each
	 * 	channel are calculated independently.
	 *	@param samples The samples
	 *	@param sampleRate The sample rate
	 *	@return The MFCCs
	 */
	public double[][] calculateMFCC( final double[][] samples, final double sampleRate )
	{
		final double[][] mfccs = new double[samples.length][];
		for( int i = 0; i < samples.length; i++ )
			mfccs[i] = this.calculateMFCC( samples[i], sampleRate );
		return mfccs;
	}

	/**
	 * 	Calculate the MFCC of a set of (single channel) samples.
	 *	@param samples The samples to calculate the MFCC for
	 * 	@param sampleRate The sample rate of the audio
	 *	@return The MFCCs
	 */
	public double[] calculateMFCC( final double[] samples, final double sampleRate )
	{
		double[] mfccs = null;
		try
		{
			mfccs = this.mfccFeatureExtractor.extractFeature( samples, sampleRate,
					new double[][]{ this.magSpectrum.extractFeature( samples, 0, null ) } );
		}
		catch( final Exception e )
		{
			e.printStackTrace();
		}

		return mfccs;
	}
}
