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

import java.io.File;

import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.filters.EQFilter;
import org.openimaj.audio.filters.EQFilter.EQType;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	Demonstration of a band-pass filter using a high-pass filter and a low-pass
 *	filter to create the band.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class BandPassFilterDemo
{
	/**
	 * 	Main
	 *	@param args CLAs
	 */
	public static void main( String[] args )
	{
		double fc = 1000; // mid-point 1000Hz
		double q = 1600;  // HPF @ 200Hz, LPF @ 1800Hz
		
		XuggleAudio s = new XuggleAudio( new File("videoplayback.mp4") );
		EQFilter lpf = new EQFilter( s, EQType.LPF, fc+q/2 );
		EQFilter hpf = new EQFilter( lpf, EQType.HPF, fc-q/2 );
		
//		AudioSpectragram as = new AudioSpectragram( hpf );
//		as.addListener( new SpectragramCompleteListener()
//		{
//			@Override
//			public void spectragramComplete( AudioSpectragram as )
//			{
//				DisplayUtilities.display( as.getLastGeneratedView() );
//			}
//		} );
//		as.processStream();
		
		AudioPlayer ap = new AudioPlayer( hpf );
		ap.run();
	}
}
