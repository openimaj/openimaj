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
