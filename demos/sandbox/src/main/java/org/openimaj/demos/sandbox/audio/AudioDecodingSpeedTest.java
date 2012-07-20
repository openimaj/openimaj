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
package org.openimaj.demos.sandbox.audio;

import org.openimaj.audio.AudioMixer;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	SampleChunk is going to be updated to use system.arraycopy. Just wondering
 *	how much faster it will be afterwards.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 27 Nov 2011
 */
public class AudioDecodingSpeedTest
{
	/**
	 *	
	 */
	public AudioDecodingSpeedTest()
	{
		XuggleAudio xa1 = new XuggleAudio(
				AudioMixer.class.getResource("/org/openimaj/demos/audio/140bpm-Arp.mp3") );
		
		FixedSizeSampleAudioProcessor f = new FixedSizeSampleAudioProcessor( xa1, 256 );

		long start = System.currentTimeMillis();
		
		while( f.nextSampleChunk() != null );
		
		long end = System.currentTimeMillis();
		
		long diff = end-start;
		System.out.println( (diff/1000d) + " seconds");
		
		// ------------ Results --------------
		// Rougly 0.62 to 0.7 seconds before System.arraycopy() used.
		// Rougly 0.52 to 0.6 seconds after System.arraycopy() used.
	}
	
	/**
	 *	@param args
	 */
	public static void main( String[] args )
	{
		new AudioDecodingSpeedTest();
	}
}
