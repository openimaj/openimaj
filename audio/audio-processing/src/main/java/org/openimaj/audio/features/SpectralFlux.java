/**
 *
 */
package org.openimaj.audio.features;

import java.util.Stack;

/**
 *	Wrapper around the jAudio implementation of Spectral flux.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 23 May 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class SpectralFlux extends JAudioFeatureExtractor
{
	/** The mag spec processor */
	private final MagnitudeSpectrum magSpec = null;

	/** The last n spectral flux values */
	private final Stack<double[]> lastSpec = new Stack<double[]>();

	/** The number of previous spectral flux to store */
	private int numberToStore = 2;

	/**
	 *	Default constructor
	 */
	public SpectralFlux()
	{
		this.featureExtractor = new jAudioFeatureExtractor.AudioFeatures.SpectralFlux();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.features.JAudioFeatureExtractor#getExtraInputs(double[], double)
	 */
	@Override
	public double[][] getExtraInputs( final double[] samples, final double sampleRate )
	{
		// Calculate the mag spec for this sample array
		final double[] ms = this.magSpec.process( samples, sampleRate );
		this.lastSpec.push( ms );

		double[] ms1 = null;
		if( this.lastSpec.size() == this.numberToStore )
			ms1 = this.lastSpec.pop();

		// If we don't have 2 spectra, we return empty otherwise we return both.
		return ms1 == null ? new double[0][0] : new double[][] {ms, ms1};
	}

	/**
	 * 	Get the number of spectral flux values to store in the feature
	 *	@return the numberToStore The number to store
	 */
	public int getNumberToStore()
	{
		return this.numberToStore;
	}

	/**
	 * 	Set the number of spectral flux values to store in the feature
	 *	@param numberToStore The number of values to store
	 */
	public void setNumberToStore( final int numberToStore )
	{
		this.numberToStore = numberToStore;
	}
}
