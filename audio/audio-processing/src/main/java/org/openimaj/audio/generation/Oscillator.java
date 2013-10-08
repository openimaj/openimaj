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
package org.openimaj.audio.generation;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.generation.Synthesizer.FMOptions;
import org.openimaj.audio.generation.Synthesizer.OscillatorOptions;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.samples.SampleBufferFactory;

/**
 * 	The oscillator implementations for the synthesiser.
 *
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 *	@created 2 May 2012
 */
public interface Oscillator
{
	/**
	 * 	Oscillator that produces pure sine waves.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 21 Feb 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class SineOscillator implements Oscillator
	{
		@Override
        public SampleChunk getSampleChunk( final int length, final double time,
				final double freq, final int gain, final AudioFormat format )
        {
			// Work out how many samples per frequency wave
			final double samplesPerWave = format.getSampleRateKHz()*1000d/freq;

			// Phase offset in samples. (f*t)-floor(f*t) is the part number
			// of waves at this point (assuming the first wave starts at a
			// phase of zero).
			final double p = 2*Math.PI*((freq*time)-Math.floor(freq*time));

			// Create an appropriate sample buffer
			final SampleBuffer sb = SampleBufferFactory.createSampleBuffer(
					format, length );

			// Fill it with sin waves
			final double z = 2*Math.PI/samplesPerWave;
			for( int i = 0; i < length; i++ )
				sb.set( i, (float)(Math.sin( i*z+p )*gain) );

            return sb.getSampleChunk();
        }

		@Override
		public OscillatorOptions getOptions()
		{
			return null;
		}
	};

	/**
	 * 	Oscillator that produces pure square waves.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 21 Feb 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class SquareOscillator implements Oscillator
	{
		@Override
        public SampleChunk getSampleChunk( final int length, final double time,
				final double freq, final int gain, final AudioFormat format )
        {
			final SampleBuffer sb = SampleBufferFactory.createSampleBuffer(
					format, length );

			final double samplesPerWave = format.getSampleRateKHz()*1000d/freq;

			// phase offset in samples
			final int p = (int)( samplesPerWave *
				((freq*time)-Math.floor(freq*time)));

			for( int i = 0; i < length; i++ )
			{
				final int x = (i+p) % (int)samplesPerWave;
				if( x > samplesPerWave/2 )
						sb.set( i, gain );
				else	sb.set( i, -gain );
			}

            return sb.getSampleChunk();
        }

		@Override
		public OscillatorOptions getOptions()
		{
			return null;
		}
	};

	/**
	 * 	Oscillator that produces saw waves.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 21 Feb 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class SawOscillator implements Oscillator
	{
		@Override
        public SampleChunk getSampleChunk( final int length, final double time,
				final double freq, final int gain, final AudioFormat format )
        {
			final SampleBuffer sb = SampleBufferFactory.createSampleBuffer(
					format, length );

			final double samplesPerWave = format.getSampleRateKHz()*1000d/freq;

			// phase offset in samples
			final int p = (int)( samplesPerWave *
				((freq*time)-Math.floor(freq*time)));

			for( int i = 0; i < length; i++ )
			{
				final int x = (i+p) % (int)samplesPerWave;
				sb.set( i, (float)(x*(gain/samplesPerWave)) );
			}

            return sb.getSampleChunk();
        }

		@Override
		public OscillatorOptions getOptions()
		{
			return null;
		}
	};

	/**
	 * 	White noise generator
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 21 Feb 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class NoiseOscillator implements Oscillator
	{
		@Override
		public SampleChunk getSampleChunk( final int length, final double time,
				final double freq, final int gain, final AudioFormat format )
		{
			final SampleBuffer sb = SampleBufferFactory.createSampleBuffer(
					format, length );
			for( int i = 0; i < sb.size(); i++ )
				sb.set( i, (float)(Math.random() * Integer.MAX_VALUE) );
			return sb.getSampleChunk();
		}

		@Override
		public OscillatorOptions getOptions()
		{
			return null;
		}
	};

	/**
	 * 	Generates an empty sample chunk
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 21 Feb 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class DummyOscillator implements Oscillator
	{
		@Override
		public SampleChunk getSampleChunk( final int length, final double time,
				final double freq, final int gain, final AudioFormat format )
		{
			final SampleBuffer sb = SampleBufferFactory.createSampleBuffer(
					format, length );
			return sb.getSampleChunk();
		}

		@Override
		public OscillatorOptions getOptions()
		{
			return null;
		}
	};

	/**
	 * Frequency modulation of wave types. We use a carrier wave that generates a
	 * tone at the required frequency and modulate with the wave that is the
	 * modulation wave by altering the time (phase) offset of the carrier using
	 * the modulation wave.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 21 Feb 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class FrequencyModulatedOscillator implements Oscillator
	{
		/** The options for this synth */
		private final FMOptions options = new FMOptions();

		@Override
		public SampleChunk getSampleChunk(final int length, final double time,
				final double freq, final int gain, final AudioFormat format)
		{
//			System.out.println( "======================================================" );
//			System.out.println( "Getting signal at time "+time );

			// Get the modulator signal
			final SampleBuffer sb = this.getOptions().modulator.nextSampleChunk().getSampleBuffer();

			// work out the maximum and minimum time signal needed from the carrier
			// so that we can retrieve the appropriate time chunk from it
			double minTime = time;
			double maxTime = time + length;
			final double step = format.getSampleRateKHz();
			final double amp = this.getOptions().modulatorAmplitude;
			for( int i = 0; i < sb.size(); i++ )
			{
				final float f = sb.get(i);
				final double t = time + step*i + f*amp;
				minTime = Math.min( t, minTime );
				maxTime = Math.max( t, maxTime );
			}

			// The start and length of the carrier signal required
			final int cl = (int)(maxTime - minTime);
			final double ct = minTime;

//			System.out.println( "Carrier buffer: "+ct+" -> "+cl );

			// Get the carrier signal
			final SampleBuffer carrierBuffer = this.getOptions().carrier.getOscillator()
					.getSampleChunk( cl, ct, freq, gain, format ).getSampleBuffer();

			// We'll side-affect the modulator signal, as it's the right length already.
			for( int i = 0; i < sb.size(); i++ )
			{
				// The time to retrieve in the carrier signal
				final float f = sb.get(i);
				final double t = time + step*i + f*amp;

				// Convert the time back to an index into the carrier buffer
				final int index = (int)((t - minTime)/step);

				sb.set( i, carrierBuffer.get( index ) );

//				System.out.println( "------ "+i+" ------");
//				System.out.println( "Value in modulator: "+f+" (scaled to "+(f*amp)+")" );
//				System.out.println( "Time at position: "+(time + step*i) );
//				System.out.println( "Modulated time: "+t );
//				System.out.println( "Index into carrier: "+index );
//				System.out.println( "Value in carrier: "+carrierBuffer.get(index) );
			}

			return sb.getSampleChunk();
		}

		@Override
		public FMOptions getOptions()
		{
			return this.options;
		}
	};

	/**
	 *
	 *  @param length The length of the sample chunk to generate
	 *  @param time The time at which the sample chunk should start
	 *  @param freq The frequency of wave to generate
	 *  @param gain The gain of the wave to generate (0 <= gain <= MAX_INT)
	 *  @param format The format of the sample chunk
	 *  @return The sample chunk
	 */
	public abstract SampleChunk getSampleChunk( int length, double time,
			double freq, int gain, AudioFormat format );

	/**
	 * 	Returns the options for the particular oscillator type.
	 *	@return The options object
	 */
	public abstract OscillatorOptions getOptions();
}