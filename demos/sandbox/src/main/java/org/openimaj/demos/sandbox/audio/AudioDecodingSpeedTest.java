/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import org.openimaj.audio.AudioMixer;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	SampleChunk is going to be updated to use system.arraycopy. Just wondering
 *	how much faster it will be afterwards.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 27 Nov 2011
 */
public class AudioDecodingSpeedTest
{
	public AudioDecodingSpeedTest()
	{
		XuggleAudio xa1 = new XuggleAudio(
				AudioMixer.class.getResource("/org/openimaj/demos/audio/140bpm-Arp.mp3") );
		
		FixedSizeSampleAudioProcessor f = new FixedSizeSampleAudioProcessor( xa1, 256 )
		{
			@Override
			public SampleChunk process( SampleChunk sample ) throws Exception
			{
				return sample;
			}
		};

		long start = System.currentTimeMillis();
		
		while( f.nextSampleChunk() != null );
		
		long end = System.currentTimeMillis();
		
		long diff = end-start;
		System.out.println( (diff/1000d) + " seconds");
		
		// Rougly 0.62 to 0.7 seconds before System.arraycopy() used.
		// Rougly 0.52 to 0.6 seconds after System.arraycopy() used.
	}
	
	public static void main( String[] args )
	{
		new AudioDecodingSpeedTest();
	}
}
