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
	 * 	@return A {@link SampleChunk} containing data in this buffer.
	 */	
	public SampleChunk getSampleChunk( int channel );
}
