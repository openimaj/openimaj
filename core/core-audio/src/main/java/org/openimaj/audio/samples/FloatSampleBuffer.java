/**
 * 
 */
package org.openimaj.audio.samples;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;
import org.openimaj.util.array.ArrayUtils;

/**
 *	An implementation of a sample buffer that maintains the floating point
 *	precision values.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 27 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class FloatSampleBuffer implements SampleBuffer
{
	/** The samples */
	private float[] samples = null;
	
	/** The audio format */
	private AudioFormat format = null;
	
	/**
	 *	@param samples The samples to use
	 * 	@param af The audio format of the samples 
	 */
	public FloatSampleBuffer( float[] samples, AudioFormat af )
	{
		this.format = af.clone();
		this.format.setNBits( -1 );
		this.samples = samples;
	}
	
	/**
	 * 	
	 *	@param samples The samples to use
	 *	@param af The audio format
	 */
	public FloatSampleBuffer( double[] samples, AudioFormat af )
	{
		this( ArrayUtils.doubleToFloat( samples ), af );
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#get(int)
	 */
	@Override
	public float get( int index )
	{
		return samples[index];
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#set(int, float)
	 */
	@Override
	public void set( int index, float sample )
	{
		samples[index] = sample;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#size()
	 */
	@Override
	public int size()
	{
		return samples.length;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getFormat()
	 */
	@Override
	public AudioFormat getFormat()
	{
		return format;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#setFormat(org.openimaj.audio.AudioFormat)
	 */
	@Override
	public void setFormat( AudioFormat af )
	{
		format = af.clone();
		format.setNBits( -1 );
	}

	/**
	 * 	Returns NULL. If you need a sample chunk from this sample buffer, then
	 * 	you must instantiate the appropriate sample chunk first and fill it.
	 * 	It cannot be done from this class because this class no longer knows
	 * 	how many bits you would like the sample chunk to be created as.
	 * 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getSampleChunk()
	 */
	@Override
	public SampleChunk getSampleChunk()
	{
		return null;
	}

	/**
	 * 	Returns NULL. If you need a sample chunk from this sample buffer, then
	 * 	you must instantiate the appropriate sample chunk first and fill it.
	 * 	It cannot be done from this class because this class no longer knows
	 * 	how many bits you would like the sample chunk to be created as.
	 *
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getSampleChunk(int)
	 */
	@Override
	public SampleChunk getSampleChunk( int channel )
	{
		return null;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#asDoubleArray()
	 */
	@Override
	public double[] asDoubleArray()
	{
		return null;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getUnscaled(int)
	 */
	@Override
	public float getUnscaled( int index )
	{
		return get(index);
	}
	
	/**
	 * 	Multipy the samples by the given scalar
	 *	@param scalar The scalar
	 * 	@return this object 
	 */
	public FloatSampleBuffer multiply( double scalar )
	{
		for( int i = 0; i < samples.length; i++ )
			set( i, (float)(samples[i] * scalar) );
		return this;
	}
	
	/**
	 * 	Add the scalar to all the samples
	 *	@param scalar The scalar
	 * 	@return this object 
	 */
	public FloatSampleBuffer add( double scalar )
	{
		for( int i = 0; i < samples.length; i++ )
			set( i, (float)(samples[i] + scalar) );
		return this;
	}
}
