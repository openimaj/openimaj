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
package org.openimaj.audio.features;

import java.util.Stack;

import org.openimaj.audio.AudioStream;

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
	private MagnitudeSpectrum magSpec = null;

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
		this.magSpec = new MagnitudeSpectrum();
	}

	/**
	 * 	Constructor that's chainable.
	 *	@param as The stream to chain to
	 */
	public SpectralFlux( final AudioStream as )
	{
		super( as );
		this.featureExtractor = new jAudioFeatureExtractor.AudioFeatures.SpectralFlux();
		this.magSpec = new MagnitudeSpectrum();
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

		double[] ms1 = null;
		if( this.lastSpec.size() == this.numberToStore )
			ms1 = this.lastSpec.pop();
		this.lastSpec.push( ms );

//		System.out.println( Arrays.toString(ms) );
//		System.out.println( Arrays.toString(ms1) );

		// If we don't have 2 spectra, we return empty otherwise we return both.
		return ms1 == null ? new double[][] {{0},{0}} : new double[][] {ms, ms1};
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
