/**
 * 
 */
package org.openimaj.audio.conversion;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.samples.SampleBufferFactory;

/**
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 18 Jun 2012
 */
public class BitDepthConverter extends AudioProcessor
{
	/**
	 * 	An enumerator of the different bit-depth conversion algorithms
	 * 	available.
	 * 
	 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *	
	 *	@created 18 Jun 2012
	 */
	public enum BitDepthConversionAlgorithm
	{
		/**
		 * 	Performs a basic nearest value rounding bit-depth conversion. It
		 * 	does this by utilising the sample buffer conversion routines.
		 */
		NEAREST
		{
			@Override
            public SampleChunk process( SampleChunk s, AudioFormat output)
            {
				SampleBuffer sbin = s.getSampleBuffer();
				SampleBuffer sbout = SampleBufferFactory.createSampleBuffer( 
						output, sbin.size() );
				
				// The sample buffer will do the conversion
				for( int i = 0; i < sbin.size(); i++ )
					sbout.set( i, sbin.get(i) );
				
				return sbout.getSampleChunk();
            }			
		};
		
		/**
		 * 	Process a sample chunk and output a sample chunk in the given
		 * 	output format.
		 * 
		 *  @param s The input sample chunk
		 *  @param output The output format
		 *  @return A resampled sample chunk.
		 */
		public abstract SampleChunk process( SampleChunk s, AudioFormat output );
	}
	
	/** 
	 * Bit depth conversion defaults to 
	 * {@link BitDepthConversionAlgorithm#NEAREST}
	 */
	private BitDepthConversionAlgorithm bitDepthConverter = 
		BitDepthConversionAlgorithm.NEAREST;
	
	/** The output format to which sample chunks will be converted */
	private AudioFormat outputFormat = null;
	
	/**
	 * 	Default constructor that takes the input conversion
	 *  @param converter The converter to use
	 *  @param outputFormat The output format to convert to
	 */
	public BitDepthConverter( BitDepthConversionAlgorithm converter,
			AudioFormat outputFormat )
    {
		this.bitDepthConverter = converter;
		this.outputFormat = outputFormat;
		this.setFormat( outputFormat );
    }
	
	/**
	 * 	Chainable constructor.
	 * 
	 *  @param as The audio stream to process
	 *  @param converter The converter to use
	 *  @param outputFormat The output format to convert to
	 */
	public BitDepthConverter( AudioStream as, BitDepthConversionAlgorithm converter,
			AudioFormat outputFormat )
	{
		super( as );
		this.bitDepthConverter = converter;
		this.outputFormat = outputFormat;
		this.setFormat( outputFormat );
	}
	
	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( SampleChunk sample ) throws Exception
	{
		if( sample.getFormat().getSampleRateKHz() != outputFormat.getSampleRateKHz() )
			throw new IllegalArgumentException( "The sample rate of the " +
					"output format is not the same as the sample chunk. Use a " +
					"sample rate converter first before using the bit depth" +
					"converter." );

		if( sample.getFormat().getNumChannels() != outputFormat.getNumChannels() )
			throw new IllegalArgumentException( "The number of channels in the " +
					"output format is not the same as the sample chunk. Use a " +
					"channel converter first before using the bit-depth " +
					"converter." );
		
		if( sample.getFormat().getNBits() == outputFormat.getNBits() )
			return sample;
		
		return bitDepthConverter.process( sample, outputFormat );
	}
}
