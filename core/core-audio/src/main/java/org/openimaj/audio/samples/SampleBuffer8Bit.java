/**
 * 
 */
package org.openimaj.audio.samples;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;

/**
 * 	A {@link SampleBuffer} for 8 bit sample chunks.
 * 
 * 	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@created 23rd November 2011
 */
public class SampleBuffer8Bit implements SampleBuffer 
{
	/** The byte buffer */
	private byte[] byteBuffer = null;
	
	/** The audio format of the samples */
	private AudioFormat format = null;

	/**
	 * 	Create a new 8-bit sample buffer using the given
	 * 	samples and the given audio format.
	 * 
	 * 	@param samples The samples to buffer.
	 * 	@param af The audio format.
	 */
	public SampleBuffer8Bit( SampleChunk samples, AudioFormat af )
	{
		this.format = af;
		if( this.format == null || this.format.getNBits() != 8 )
			throw new IllegalArgumentException( "Number of bits " +
					"must be 8 if you're instantiating an 8 bit " +
					"sample buffer. However "+
					(this.format==null?"format object was null.":
					"number of bits in format was "+format.getNBits()));
		
		this.byteBuffer = samples.getSamples();
	}
	
	/**
	 * 	Create a new sample buffer with the given format and
	 * 	the given number of samples.
	 * 
	 * 	@param af The {@link AudioFormat} of the samples
	 * 	@param nSamples The number of samples
	 */
	public SampleBuffer8Bit( AudioFormat af, int nSamples )
	{
		this.format = af;
		if( this.format == null || this.format.getNBits() != 8 )
			throw new IllegalArgumentException( "Number of bits " +
					"must be 8 if you're instantiating an 8 bit " +
					"sample buffer. However "+
					(this.format==null?"format object was null.":
					"number of bits in format was "+format.getNBits()));

		byteBuffer = new byte[ nSamples * af.getNumChannels() ];
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getSampleChunk()
	 */
	@Override
	public SampleChunk getSampleChunk()
	{
		return new SampleChunk( byteBuffer, this.format );
	}
	
	/**
	 *	@inheritDoc
	 *
	 *	Note that because we cannot use native methods for copying parts of
	 *	an array, we must use Java methods so this will be considerably
	 *	slower than {@link #getSampleChunk()}.
	 *
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getSampleChunk(int)
	 */
	public SampleChunk getSampleChunk( int channel )
	{
		if( channel > format.getNumChannels() )
			throw new IllegalArgumentException( "Cannot generate sample chunk " +
					"for channel "+channel+" as sample only has " + 
					format.getNumChannels() + " channels." );
		
		if( channel == 0 && format.getNumChannels() == 1 )
			return getSampleChunk();
		
		byte[] newSamples = new byte[size()];
		for( int i = 0; i < size(); i++ )
			newSamples[i] = byteBuffer[i*format.getNumChannels() + channel];
		
		AudioFormat af = format.clone();
		af.setNumChannels( 1 );
		return new SampleChunk( newSamples, af );
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.audio.samples.SampleBuffer#get(int)
	 */
	@Override
	public float get( int index ) 
	{
		// Convert the byte to an integer
		return (byteBuffer[index]-128) << 24;
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.audio.samples.SampleBuffer#set(int, float)
	 */
	@Override
	public void set( int index, float sample ) 
	{
		byteBuffer[index] = (byte)((((int)sample)>>24)+128);		
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.audio.samples.SampleBuffer#size()
	 */
	@Override
	public int size() 
	{
		return byteBuffer.length;
	}
	
	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getFormat()
	 */
	@Override
	public AudioFormat getFormat()
	{
		return format;
	}
}
