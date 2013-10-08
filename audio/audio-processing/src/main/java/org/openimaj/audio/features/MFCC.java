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
