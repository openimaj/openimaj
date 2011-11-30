package org.openimaj.audio;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;

/**
 * Calculate the effective sound pressure by calculating the
 * RMS of samples over a temporal window.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class EffectiveSoundPressure extends FixedSizeSampleAudioProcessor {
	private double rms = 0;
	
	public EffectiveSoundPressure()
	{
		super( 1 );
	}
	
	public EffectiveSoundPressure(AudioStream stream, int windowSizeMillis, int overlapMillis) {
		super(stream, (int) (stream.getFormat().getSampleRateKHz() * windowSizeMillis));
		this.setWindowStep((int)(stream.getFormat().getSampleRateKHz() * overlapMillis));
	}

	@Override
	public SampleChunk process(SampleChunk sample) throws Exception {
		long accum = 0;
		final int size;
		
		switch( sample.getFormat().getNBits() )
		{
			case 16:
			{
				ShortBuffer b = sample.getSamplesAsByteBuffer().asShortBuffer();
				size = b.limit();
				for( int x = 0; x < size; x++ )
					accum += b.get( x )*b.get( x );
				break;
			}
			case 8:
			{
				ByteBuffer b = sample.getSamplesAsByteBuffer();
				size = b.limit();
				for( int x = 0; x < size; x++ )
					accum += b.get( x )*b.get( x );
				break;
			}
			default:
				throw new Exception( "Unsupported Format" );
		}
		
		rms = Math.sqrt((double)accum / (double)size);
		
//		System.out.println(rms);
		
		return sample;
	}

	public double getEffectiveSoundPressure() {
		return rms;
	}
}
