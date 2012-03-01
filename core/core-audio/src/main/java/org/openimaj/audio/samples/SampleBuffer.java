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
package org.openimaj.audio.samples;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;

/**
 * 	This class provides a consistent API for access samples of different
 * 	sizes (e.g. 8-bit and 16-bit). Subclasses implement get and set methods 
 * 	for samples in a sample byte buffer. The getters and setters return and 
 * 	take floats in all cases, so it is up to the implementing class to decide
 * 	how best to convert the incoming value to an appropriate value for the
 * 	sample buffer. The values given and expected should be normalised between
 * 	{@link Integer#MAX_VALUE} and {@link Integer#MIN_VALUE}. Despite using
 * 	floats, this allows us to detect clipping. Return values are signed such
 * 	that no signal has sample value 0.
 * 
 * 	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@created 23rd November 2011
 */
public interface SampleBuffer
{
	/**
	 * 	Get the sample at the given index. The sample will be a float and have
	 * 	a value between {@link Integer#MAX_VALUE} and {@link Integer#MIN_VALUE}.
	 * 	That sample will be signed.
	 * 
	 * 	@param index The index of the sample to retrieve
	 * 	@return The sample value as an float
	 */
	public float get( int index );
	
	/**
	 * 	Set the sample value at the given index. The sample value should be
	 * 	scaled between {@link Integer#MAX_VALUE} and {@link Integer#MIN_VALUE}
	 * 	(i.e. it should be signed).
	 * 
	 * 	@param index The index of the sample to set.
	 * 	@param sample The sample value to set.
	 */
	public void set( int index, float sample );
	
	/**
	 * 	Returns the size of this buffer.
	 * 	@return the size of this buffer.
	 */
	public int size();
	
	/**
	 * 	Returns the audio format for this set of samples.
	 *	@return The {@link AudioFormat} for this set of samples.
	 */
	public AudioFormat getFormat();
	
	/**
	 * 	Return a sample chunk that contains the data from
	 * 	this sample buffer. Note that any timestamps will be
	 * 	unset in the new sample chunk.
	 * 
	 * 	@return A {@link SampleChunk} containing data in this buffer.
	 */
	public SampleChunk getSampleChunk();
	
	/**
	 * 	Return a sample chunk that contains the data from
	 * 	a specific channel in this sample buffer. The channel is
	 * 	numbered from 0 (so mono audio streams will only have 1 channel
	 * 	accessed with getSampleChunk(0)).
	 * 
	 * 	Note that any timestamps will be unset in the new sample chunk.
	 * 
	 *  @param channel The channel 
	 * 
	 * 	@return A {@link SampleChunk} containing data in this buffer.
	 */	
	public SampleChunk getSampleChunk( int channel );
}
