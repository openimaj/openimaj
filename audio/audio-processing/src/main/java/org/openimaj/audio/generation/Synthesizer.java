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
package org.openimaj.audio.generation;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
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
				// Work out how many samples per frequency wave
				double samplesPerWave = format.getSampleRateKHz()*1000d/freq;
				
				// Phase offset in samples. (f*t)-floor(f*t) is the part number
				// of waves at this point (assuming the first wave starts at a 
				// phase of zero). 
				double p = 2*Math.PI*((freq*time)-Math.floor(freq*time));
				
				// Create an appropriate sample buffer
				SampleBuffer sb = SampleBufferFactory.createSampleBuffer( 
						format, length );
				
				// Fill it with sin waves
				double z = 2*Math.PI/samplesPerWave;
				for( int i = 0; i < length; i++ )
					sb.set( i, (float)(Math.sin( i*z+p )*Integer.MAX_VALUE) );
				
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

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#getLength()
	 */
	@Override
	public long getLength()
	{
		return -1;
	}
}
