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
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.util.array.ArrayUtils;

/**
 *	This class provides an OpenIMAJ wrapper for the JAudio library of feature extractors.
 *	It provides the marshalling of data from the OpenIMAJ audio streams into
 *	the data structures necessary for the jAudio FeatureExtractor interface.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 23 May 2013
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class JAudioFeatureExtractor extends AudioProcessor
	implements FeatureExtractor<DoubleFV, SampleChunk>
{
	/** The jaudio feature extractor */
	protected jAudioFeatureExtractor.AudioFeatures.FeatureExtractor featureExtractor;

	/** The feature that was last calculated */
	private double[][] lastCalculatedFeature;

	/**
	 * 	Default constructor for ad-hoc processing.
	 */
	public JAudioFeatureExtractor()
	{
	}

	/**
	 * 	Chainable constructor
	 *	@param as The audio stream to chain to.
	 */
	public JAudioFeatureExtractor( final AudioStream as )
	{
		super( as );
	}

	/**
	 * 	Process the given sample buffer.
	 *	@param sb The sample buffer
	 *	@return The sample buffer
	 */
	public SampleBuffer process( final SampleBuffer sb )
	{
		final double[][] chanSamples = sb.asDoubleChannelArray();
		this.lastCalculatedFeature = new double[chanSamples.length][];
		for( int c = 0; c < sb.getFormat().getNumChannels(); c++ )
			this.lastCalculatedFeature[c] = this.process( chanSamples[c], sb.getFormat().getSampleRateKHz()*1000d );
		return sb;
	}

	/**
	 * 	Process the given sample data.
	 * 	@param samples The samples
	 * 	@param sampleRate The sample rate of the data
	 *	@return The features
	 */
	public double[][] process( final double[][] samples, final double sampleRate )
	{
		final double[][] featureVectors = new double[samples.length][];
		for( int i = 0; i < samples.length; i++ )
			featureVectors[i] = this.process( samples[i], sampleRate );
		return featureVectors;
	}

	/**
	 * 	Process the given sample array for a single channel
	 *	@param samples The samples for a single channel
	 *	@param sampleRate The sample rate of the data
	 *	@return The feature for the single channel
	 */
	public double[] process( final double[] samples, final double sampleRate )
	{
		// Process the feature
		try
		{
			final double[] f = this.featureExtractor.extractFeature( samples, sampleRate,
					this.getExtraInputs( samples, sampleRate ) );
			return f;
		}
		catch( final Exception e )
		{
			e.printStackTrace();
		}

		// If an exception occurs we return null
		return null;
	}

	/**
	 * 	Returns the extra inputs required by a specific feature extractor
	 *	@param samples The samples for a single channel
	 *	@param sampleRate The sample rate of the data
	 *	@return The extra input
	 */
	public abstract double[][] getExtraInputs( double[] samples, double sampleRate );

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		this.process( sample.getSampleBuffer() );
		return sample;
	}

	/**
	 * 	Calculates the feature for each channel, then flattens the channel arrays
	 * 	into a single {@link DoubleFV}.
	 *
	 *	{@inheritDoc}
	 * 	@see org.openimaj.feature.FeatureExtractor#extractFeature(java.lang.Object)
	 */
	@Override
	public DoubleFV extractFeature( final SampleChunk sc )
	{
		// Calculate the feature vector for this frame.
		this.process( sc.getSampleBuffer() );
		return new DoubleFV( ArrayUtils.reshape( this.lastCalculatedFeature ) );
	}

	/**
	 *	@return the lastCalculatedFeature
	 */
	public double[][] getLastCalculatedFeature()
	{
		return this.lastCalculatedFeature;
	}

	/**
	 *	@param lastCalculatedFeature the lastCalculatedFeature to set
	 */
	public void setLastCalculatedFeature( final double[][] lastCalculatedFeature )
	{
		this.lastCalculatedFeature = lastCalculatedFeature;
	}

}
