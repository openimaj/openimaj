/**
 * 
 */
package org.openimaj.audio.samples;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;

/**
 * 	A {@link SampleBuffer} for 16-bit sample chunks.
 * 
 * 	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@created 23rd November 2011
 */
public class SampleBuffer16Bit implements SampleBuffer
{
	/** The underlying byte array we're wrapping */
	private byte[] samples = null;
	
	/** The short buffer that we're wrapping */
	private ShortBuffer shortBuffer = null;
	
	/** The audio format of the samples */
	private AudioFormat format;

	/**
	 * 	Create a new 16-bit sample buffer using the given
	 * 	samples and the given audio format.
	 * 
	 * 	@param samples The samples to buffer.
	 * 	@param af The audio format.
	 */
	public SampleBuffer16Bit( SampleChunk samples, AudioFormat af )
	{
		this.format = af;
		this.shortBuffer = samples.getSamplesAsByteBuffer().asShortBuffer();
		this.samples = samples.getSamples();
	}
	
	/**
	 * 	Create a new 16-bit sample buffer using the given
	 * 	sample format at the given size.
	 * 
	 * 	@param af The audio format of the samples
	 * 	@param nSamples The number of samples
	 */
	public SampleBuffer16Bit( AudioFormat af, int nSamples )
	{
		this.format = af;
		this.samples = new byte[ nSamples * af.getNumChannels() * 2 ];
		this.shortBuffer = new SampleChunk(this.samples,this.format)
			.getSamplesAsByteBuffer().asShortBuffer();
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getSampleChunk()
	 */
	@Override
	public SampleChunk getSampleChunk()
	{
		return new SampleChunk( this.samples, this.format );
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
	@Override
	public SampleChunk getSampleChunk( int channel )
	{
		if( channel > format.getNumChannels() )
			throw new IllegalArgumentException( "Cannot generate sample chunk " +
					"for channel "+channel+" as sample only has " + 
					format.getNumChannels() + " channels." );
		
		if( channel == 0 && format.getNumChannels() == 1 )
			return getSampleChunk();
		
		byte[] newSamples = new byte[size()*2];
		ShortBuffer sb = ByteBuffer.wrap( newSamples ).order(
			format.isBigEndian()?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN ).
			asShortBuffer();
		for( int i = 0; i < size()/format.getNumChannels(); i++ )
			sb.put( i, shortBuffer.get( i*format.getNumChannels() + channel ) );
		
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
		if( index >= shortBuffer.limit() )
			return 0;
		
		// Convert the short to an integer
		return shortBuffer.get(index) << 16;
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.audio.samples.SampleBuffer#set(int, float)
	 */
	@Override
	public void set( int index, float sample ) 
	{
		shortBuffer.put( index, (short)(((int)sample)>>16) );
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.audio.samples.SampleBuffer#size()
	 */
	@Override
	public int size() 
	{
		return shortBuffer.limit();
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
