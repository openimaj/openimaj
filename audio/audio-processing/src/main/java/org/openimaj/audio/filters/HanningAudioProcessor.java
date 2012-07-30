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

import java.util.Arrays;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;

/**
 * 	Applies a Hanning window on top of the audio signal.
 *  Assumes that all incoming audio samples will be the same size (or smaller)
 *  than the initial sample. This is because the Hanning function is cached
 *  the first time that the function is called.
 *  
 * 	@see "http://cnx.org/content/m0505/latest/"
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@created 31 Oct 2011
 */
public class HanningAudioProcessor extends FixedSizeSampleAudioProcessor
{
	/** A table of cos */
	private double[] cosTable = null; 

	/**
	 * 	Default constructor for non chainable processing.
	 * 	@param sizeRequired Size of the window required 
	 */
	public HanningAudioProcessor( int sizeRequired )
	{
		super( sizeRequired );
	}
	
	/**
	 *	Chainable constructor. Takes the audio stream and the size required.
	 * 
	 *  @param stream The audio stream to process
	 *  @param sizeRequired The size of window required.
	 */
	public HanningAudioProcessor( AudioStream stream, int sizeRequired )
    {
	    super( stream, sizeRequired );
    }

	/**
	 * 	Generate a cache of the COS function used for the Hanning.
	 *  @param sample An example of the sample that will have the Hanning
	 *  	function applied.
	 */
	private void generateCosTableCache( final SampleChunk sample )
	{
		generateCosTableCache( 
				sample.getNumberOfSamples()/sample.getFormat().getNumChannels(), 
				sample.getFormat().getNumChannels() );
	}
	
	/**
	 * 	Generate the cos table cache
	 *	@param length The length of the window to generate (per channel)
	 *	@param nc The number of channels for which to generate a weight table
	 */
	private void generateCosTableCache( final int length, final int nc )
	{
		final int ns = length;
		cosTable = new double[ length ];
		for( int n = 0; n < ns; n++ )
			for( int c = 0; c < nc; c++ )
				cosTable[n*nc+c] = 0.5*(1-Math.cos((2*Math.PI*n)/ns));
	}
	
	/**
	 * 	The implementation in this class returns the sample as is.
	 * 
	 *  {@inheritDoc}
	 *  @see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	final public SampleChunk process( SampleChunk sample )
	{
		if( sample == null ) return null;
		if( cosTable == null )
			generateCosTableCache( sample );
		
		System.out.println( "Hanning Window: "+Arrays.toString( cosTable ) );
		
		process( sample.getSampleBuffer() );
		return processSamples( sample );
	}
	
	/**
	 * 	Process the given sample buffer with the Hanning window.
	 *	@param b The sample buffer
	 *	@return The sample buffer
	 */
	final public SampleBuffer process( SampleBuffer b )
	{
		final int nc = b.getFormat().getNumChannels();
		for( int n = 0; n < b.size()/nc; n++ )
			for( int c = 0; c < nc; c++ )
				b.set( n*nc+c, (float)(b.get(n*nc+c) * cosTable[n*nc+c]) );
		return b;
	}
	
	/**
	 * 	Process the Hanning samples.
	 * 
	 *	@param sample The samples to process
	 *	@return The processed samples
	 */
	public SampleChunk processSamples( SampleChunk sample )
	{
		return sample;
	}

	/**
	 * 	The sum of the Hanning window
	 * 	@param samples A representative sample 
	 *	@return The sum of the hanning window
	 */
	public double getWindowSum( SampleChunk samples )
    {
		return getWindowSum( 
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
		if( cosTable == null )
			generateCosTableCache( length, numChannels );
		
		double sum = 0;
		for( int i = 0; i < cosTable.length; i += numChannels )
			sum += cosTable[i];
		return sum;
    }
}
