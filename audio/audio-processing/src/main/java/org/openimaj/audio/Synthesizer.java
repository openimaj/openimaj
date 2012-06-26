/**
 * 
 */
package org.openimaj.audio;

import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.samples.SampleBufferFactory;

/**
 * 	Really really basic synthesizer. Useful for doing tests by running the
 * 	synth as an audio source through filters or whatever.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 2 May 2012
 */
public class Synthesizer extends AudioStream
{
	/**
	 * 	The oscillator implementations for the synthesiser.
	 * 	
	 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *	
	 *	@created 2 May 2012
	 */
	public enum WaveType
	{
		/**
		 * 	Oscillator that produces pure sine waves.
		 */
		SINE
		{
			@Override
            protected SampleChunk getSampleChunk( int length, double time,
    				double freq, AudioFormat format )
            {
				double samplesPerWave = format.getSampleRateKHz()*1000d/freq;
				
				// phase offset in samples
				int p = (int)( samplesPerWave *
					((freq*time)-Math.floor(freq*time)));
				
				SampleBuffer sb = SampleBufferFactory.createSampleBuffer( 
						format, length );
				
				for( int i = 0; i < length; i++ )
					sb.set( i, (float)(Math.sin( (i+p)*2*Math.PI/samplesPerWave )
							)*Integer.MAX_VALUE );
				
	            return sb.getSampleChunk();
            }
		},
		
		/**
		 * 	Oscillator that produces pure square waves.
		 */
		SQUARE
		{
			@Override
            protected SampleChunk getSampleChunk( int length, double time,
    				double freq, AudioFormat format )
            {
				SampleBuffer sb = SampleBufferFactory.createSampleBuffer( 
						format, length );

				double samplesPerWave = format.getSampleRateKHz()*1000d/freq;
				
				// phase offset in samples
				int p = (int)( samplesPerWave *
					((freq*time)-Math.floor(freq*time)));
								
				for( int i = 0; i < length; i++ )
				{
					int x = (i+p) % (int)samplesPerWave;
					if( x > samplesPerWave/2 )
							sb.set( i, Integer.MAX_VALUE );
					else	sb.set( i, Integer.MIN_VALUE );
				}
				
	            return sb.getSampleChunk();
            }
		},
		
		/**
		 * 	Oscillator that produces saw waves.
		 */
		SAW
		{
			@Override
            protected SampleChunk getSampleChunk( int length, double time,
    				double freq, AudioFormat format )
            {
				SampleBuffer sb = SampleBufferFactory.createSampleBuffer( 
						format, length );

				double samplesPerWave = format.getSampleRateKHz()*1000d/freq;
				
				// phase offset in samples
				int p = (int)( samplesPerWave *
					((freq*time)-Math.floor(freq*time)));
								
				for( int i = 0; i < length; i++ )
				{
					int x = (i+p) % (int)samplesPerWave;
					sb.set( i, (float)(x*(Integer.MAX_VALUE/samplesPerWave)) );
				}
				
	            return sb.getSampleChunk();
            }
		};

		/**
		 * 
		 *  @param length The length of the sample chunk to generate
		 *  @param time The time at which the sample chunk should start
		 *  @param freq The frequency of wave to generate
		 *  @param format The format of the sample chunk
		 *  @return The sample chunk
		 */
		protected abstract SampleChunk getSampleChunk( int length, double time, 
				double freq, AudioFormat format );
	}

	/** The current time position of the synth */
	private double currentTime = 0;
	
	/** The oscillator used to generate the wave */
	private WaveType oscillator = WaveType.SINE;
	
	/** Default sample chunk length is 1024 bytes */
	private int sampleChunkLength = 1024;
	
	/** Default frequency is the standard A4 (440Hz) tuning pitch */
	private double frequency = 440;
	
	/**
	 * 
	 */
	public Synthesizer()
    {
		setFormat( new AudioFormat( 16, 44.1, 1 ) );
    }

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#nextSampleChunk()
	 */
	@Override
    public SampleChunk nextSampleChunk()
    {
	    SampleChunk x = oscillator.getSampleChunk( sampleChunkLength, 
	    		currentTime, frequency, format );
	    
	    currentTime += x.getSampleBuffer().size() / 
	    	(format.getSampleRateKHz()*1000d);
	    
	    return x;
    }

	/**
	 * 	Set the frequency at which the synth will generate tones.
	 *  @param f The frequency
	 */
	public void setFrequency( double f )
	{
		this.frequency = f;
	}
	
	/**
	 * 	Set the type of oscillator used to generate tones.
	 *  @param t The type of oscillator.
	 */
	public void setOscillatorType( WaveType t )
	{
		this.oscillator = t;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#reset()
	 */
	@Override
    public void reset()
    {
		currentTime = 0;
    }
}
