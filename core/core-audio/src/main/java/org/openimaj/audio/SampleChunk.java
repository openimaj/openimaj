/**
 * 
 */
package org.openimaj.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.openimaj.time.Timecode;

/**
 *	Represents a chunk of an audio file and stores the raw audio data.
 *	The data is unnormalised - that is, it is stored in this class in its
 *	original format in the form of a byte array. This is for speed during
 *	audio playback. 
 *
 *	If you require normalised data (data as an integer array for example) 
 *	use the method {@link #getSamplesAsByteBuffer()} and use the 
 *	{@link ByteBuffer}'s methods asXXXBuffer (e.g. ByteBuffer#asShortBuffer) 
 *	to get the samples in a normalised form. 
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 8 Jun 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class SampleChunk extends Audio
{
	/** The samples in the chunk */
	private byte[] samples = null;
	
	/** The timecode of the start of the sample chunk */
	private Timecode startTimecode = null;
	
	/**
	 * 	Create a new SampleChunk buffer with the given
	 * 	audio format, but do not initialise the samples.
	 * 
	 *	@param af The audio format of the samples
	 */
	public SampleChunk( AudioFormat af )
	{
		this( null, af );
	}
	
	/**
	 * 	Create a new sample chunk using the given samples
	 * 	and the given audio format.
	 * 
	 *	@param samples The samples to initialise with
	 *	@param af The audio format of the samples
	 */
	public SampleChunk( byte[] samples, AudioFormat af )
	{
		this.setSamples( samples );
		super.format = af;
	}

	/**
	 * 	Set the samples in this sample chunk.
	 *	@param samples the samples in this sample chunk.
	 */
	public void setSamples( byte[] samples )
	{
		this.samples = samples;
	}

	/**
	 * 	Get the samples in this sample chunk
	 *	@return the samples in this sample chunk
	 */
	public byte[] getSamples()
	{
		return samples;
	}
	
	/**
	 * 	Returns a {@link ByteBuffer} that can be used to create
	 * 	views of the samples in the object. For example, to get short
	 * 	integers, you can get {@link #getSamplesAsByteBuffer()}.asShortBuffer()
	 * 
	 *	@return A {@link ByteBuffer}
	 */
	public ByteBuffer getSamplesAsByteBuffer()
	{
		ByteOrder bo = null;
		
		if( format.isBigEndian() )
				bo = ByteOrder.BIG_ENDIAN;
		else	bo = ByteOrder.LITTLE_ENDIAN;
		
		return ByteBuffer.wrap( samples ).order( bo );
	}
	
	/**
	 * 	For the implementation of {@link Audio#getSampleChunk()} this
	 * 	method returns itself.
	 * 
	 *	@inheritDoc
	 * 	@see org.openimaj.audio.Audio#getSampleChunk()
	 */
	public SampleChunk getSampleChunk()
	{
		return this;
	}

	/**
	 * 	Set the timecode at the start of this audio chunk.
	 *	@param startTimecode the timecode at the start of the chunk.
	 */
	public void setStartTimecode( Timecode startTimecode )
	{
		this.startTimecode = startTimecode;
	}

	/**
	 * 	Get the timecode at the start of this audio chunk.
	 *	@return the timecode at the start of the chunk.
	 */
	public Timecode getStartTimecode()
	{
		return startTimecode;
	}
}
