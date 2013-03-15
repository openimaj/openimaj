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
package org.openimaj.audio.filters;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;

/**
 * 	Applies a weighted window on top of the audio signal.
 *  Assumes that all incoming audio samples will be the same size (or smaller)
 *  than the initial sample. This is because the weights function is cached
 *  the first time that the function is called.
 *  <p>
 *  Just one method needs to be overridden in the implementing class and that
 *  is the method that generates the window of weights to be applied to the
 *  signal. The weighting is then applied automatically. The method will only
 *  be called once and lazily.
 *  
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@created 31 Oct 2011
 */
public abstract class WeightedWindowedAudioProcessor 
	extends FixedSizeSampleAudioProcessor
{
	/** A table of weights */
	protected double[] weightTable = null;
	
	/** Whether to apply the weights to the incoming signal */
	protected boolean useWeights = true;

	/**
	 * 	Default constructor for non chainable processing.
	 * 	@param sizeRequired Size of the window required 
	 */
	public WeightedWindowedAudioProcessor( final int sizeRequired )
	{
		super( sizeRequired );
	}
	
	/**
	 *	Chainable constructor. Takes the audio stream and the size required.
	 * 
	 *  @param stream The audio stream to process
	 *  @param sizeRequired The size of window required.
	 */
	public WeightedWindowedAudioProcessor( final AudioStream stream, final int sizeRequired )
    {
	    super( stream, sizeRequired );
    }

	/**
	 * 	Constructor that takes the size of the window and the number of samples
	 * 	overlap.
	 * 
	 *	@param nSamplesInWindow Samples in window
	 *	@param nSamplesOverlap Samples in window overlap
	 */
	public WeightedWindowedAudioProcessor( final int nSamplesInWindow,
			final int nSamplesOverlap )
	{
		super( nSamplesInWindow, nSamplesOverlap );
	}

	/**
	 * 	Chainable constructor that takes the size of the window and 
	 * 	the number of samples overlap.
	 * 
	 * 	@param as The chained audio stream
	 *	@param nSamplesInWindow Samples in window
	 *	@param nSamplesOverlap Samples in window overlap
	 */
	public WeightedWindowedAudioProcessor( final AudioStream as,
			final int nSamplesInWindow, final int nSamplesOverlap )
	{
		super( as, nSamplesInWindow, nSamplesOverlap );
	}

	/**
	 * 	Generate a cache of the COS function used for the Hanning.
	 *  @param sample An example of the sample that will have the Hanning
	 *  	function applied.
	 */
	private void generateWeightTableCache( final SampleChunk sample )
	{
		this.generateWeightTableCache( 
				sample.getNumberOfSamples()/sample.getFormat().getNumChannels(), 
				sample.getFormat().getNumChannels() );
	}
	
	/**
	 * 	Generate the cos table cache
	 *	@param length The length of the window to generate (per channel)
	 *	@param nc The number of channels for which to generate a weight table
	 */
	protected abstract void generateWeightTableCache( final int length, final int nc );
	
	/**
	 * 	Process the given sample chunk. Note that it is expected that the 
	 * 	sample will be the correct length (as given in the constructor). If it
	 * 	is not, the window will not be applied correctly.
	 * 
	 *  {@inheritDoc}
	 *  @see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	final public SampleChunk process( final SampleChunk sample )
	{
		if( sample == null ) return null;
		if( this.weightTable == null )
			this.generateWeightTableCache( sample );
		
		// Apply the Hanning weights
		this.process( sample.getSampleBuffer() );
		
		return this.processSamples( sample );
	}
	
	/**
	 * 	Process the given sample buffer with the Hanning window.
	 *	@param b The sample buffer
	 *	@return The sample buffer
	 */
	final public SampleBuffer process( final SampleBuffer b )
	{
		final int nc = b.getFormat().getNumChannels();
		if( this.weightTable == null )
			this.generateWeightTableCache( b.size()/nc, nc );

		for( int c = 0; c < nc; c++ )
		{
			for( int n = 0; n < b.size()/nc; n++ )
			{
				final float x = b.get(n*nc+c);
				float v = (float)(x * this.weightTable[n*nc+c]);
				if( !this.useWeights ) v = x;
				b.set( n*nc+c, v );
			}
		}
		
		return b;
	}
	
	/**
	 * 	Process the Hanning samples.
	 * 
	 *	@param sample The samples to process
	 *	@return The processed samples
	 */
	public SampleChunk processSamples( final SampleChunk sample )
	{
		return sample;
	}

	/**
	 * 	The sum of the Hanning window
	 * 	@param samples A representative sample 
	 *	@return The sum of the hanning window
	 */
	public double getWindowSum( final SampleChunk samples )
    {
		return this.getWindowSum( 
				samples.getNumberOfSamples() / samples.getFormat().getNumChannels(),  
				samples.getFormat().getNumChannels() );
    }
	
	/**
	 * 	Get the sum of the Hanning window for the given parameters
	 *	@param length The length of the window
	 *	@param numChannels The number of channels in the window
	 *	@return The sum of the window
	 */
	public double getWindowSum( final int length, final int numChannels )
	{
		if( this.weightTable == null )
			this.generateWeightTableCache( length, numChannels );
		
		double sum = 0;
		for( int i = 0; i < this.weightTable.length; i += numChannels )
			sum += this.weightTable[i];
		return sum;
    }
	
	/**
	 * 	Get the weights used.
	 *	@return The weights
	 */
	public double[] getWeights()
	{
		return this.weightTable;
	}
}
