/**
 *
 */
package org.openimaj.audio.features;


/**
 *	A wrapper around the JAudio magnitude spectrum feature extractor.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 23 May 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class MagnitudeSpectrum extends JAudioFeatureExtractor
{
	/**
	 *	Default constructor
	 */
	public MagnitudeSpectrum()
	{
		this.featureExtractor = new jAudioFeatureExtractor.AudioFeatures.MagnitudeSpectrum();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.features.JAudioFeatureExtractor#getExtraInputs(double[], double)
	 */
	@Override
	public double[][] getExtraInputs( final double[] samples, final double sampleRate )
	{
		return null;
	}
}
