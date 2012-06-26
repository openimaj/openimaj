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
import org.openimaj.math.util.Interpolation;

/**
 * 	A sample rate conversion audio processing class. There is an enumerator
 * 	within the class that is publically available for determining the
 * 	algorithm for sample rate conversion. This defaults to 
 * 	{@link SampleRateConversionAlgorithm#LINEAR_INTERPOLATION}.
 * 	<p>
 * 	To use the class, instantiate using the default constructor or the
 * 	chainable constructor. Both of these constructors take the algorithm for
 * 	sample rate conversion as well as the output format. The output format
 * 	must have the same number of bits and same number of channels as the
 * 	expected input otherwise the {@link #process(SampleChunk)} method 
 * 	will throw {@link IllegalArgumentException}. The input format for the samples
 * 	is expected to be provided as part of the {@link SampleChunk}.
 * 	<p>
 * 	The class itself checks whether the output format and the input format
 * 	are the same (in which case the sample does not need to be resampled).
 * 	That means the algorithm implementation does not need to do this.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 18 Jun 2012
 */
public class SampleRateConverter extends AudioProcessor
{
	/**
	 * 	An enumerator of the different sample rate conversion algorithms
	 * 	available in this sample rate converter.
	 * 
	 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *	
	 *	@created 18 Jun 2012
	 */
	public enum SampleRateConversionAlgorithm
	{
		/**
		 * 	Performs linear interpolation between samples where the sample rate
		 * 	in the output format is greater than the sample rate of the input
		 * 	format. If the sample rate is less, then the nearest value
		 * 	of the input samples is used.
		 */
		LINEAR_INTERPOLATION
		{
			@Override
            public SampleChunk process( SampleChunk s, AudioFormat output)
            {
				AudioFormat input = s.getFormat();
				
				// Check to see if the input and output are the same
				if( input.getSampleRateKHz() == output.getSampleRateKHz() )
					return s;
				
				// Work out the size of the output sample chunk
				double scalar = input.getSampleRateKHz() / output.getSampleRateKHz();
				SampleBuffer sbin = s.getSampleBuffer();
				double size = sbin.size() * scalar;
				SampleBuffer sbout = SampleBufferFactory.createSampleBuffer( 
						output, (int)size );
				sbout.setFormat( output );
				
				// If the input format has a greater sample rate than the
				// output format - down sampling (scalar > 1)
				if( scalar > 1 )
				{
					System.out.println( "Scalar: "+scalar );
					for( int i = 0; i < sbout.size(); i++ )
					{
						int inputSampleX = (int)(i / scalar);
						sbout.set( i, sbin.get( inputSampleX ) );
					}
					return sbout.getSampleChunk();
				}
				// If the input format has a sample rate less than that
				// of the output - up sampling (scalar < 1)
				else
				{
					// Linear interpolate each sample value
					for( int i = 0; i < sbout.size()-1; i++ )
					{
						int inputSampleX = (int)(i / scalar);
						sbout.set( i, Interpolation.lerp( (float)(i/scalar), 
								inputSampleX, sbin.get(inputSampleX), 
								inputSampleX+1, sbin.get(inputSampleX+1) ) );
					}
					sbout.set( sbout.size()-1, sbin.get(sbin.size()-1) );
					return sbout.getSampleChunk();
				}
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
	 * Sample rate conversion defaults to 
	 * {@link SampleRateConversionAlgorithm#LINEAR_INTERPOLATION}
	 */
	private SampleRateConversionAlgorithm sampleConverter = 
		SampleRateConversionAlgorithm.LINEAR_INTERPOLATION;
	
	/** The output format to which sample chunks will be converted */
	private AudioFormat outputFormat = null;
	
	/**
	 * 	Default constructor that takes the input conversion
	 *  @param converter The converter to use
	 *  @param outputFormat The output format to convert to
	 */
	public SampleRateConverter( SampleRateConversionAlgorithm converter,
			AudioFormat outputFormat )
    {
		this.sampleConverter = converter;
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
	public SampleRateConverter( AudioStream as, SampleRateConversionAlgorithm converter,
			AudioFormat outputFormat )
	{
		super( as );
		this.sampleConverter = converter;
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
		if( sample.getFormat().getNBits() != outputFormat.getNBits() )
			throw new IllegalArgumentException( "The number of bits in the " +
					"output format is not the same as the sample chunk. Use a " +
					"resampling conversion first before using the sample-rate " +
					"converter." );

		if( sample.getFormat().getNumChannels() != outputFormat.getNumChannels() )
			throw new IllegalArgumentException( "The number of channels in the " +
					"output format is not the same as the sample chunk. Use a " +
					"channel converter first before using the sample-rate " +
					"converter." );
		
		if( sample.getFormat().getSampleRateKHz() == outputFormat.getSampleRateKHz() )
			return sample;
		
		return sampleConverter.process( sample, outputFormat );
	}
}
