/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import java.io.File;

import org.openimaj.audio.AudioPlayer;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	Simple example of the AudioPlayer being paused, started and restarted.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 17 Aug 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioPlayerTest
{
	/**
	 *	@param args
	 * 	@throws InterruptedException InterruptedException
	 */
	public static void main( final String[] args ) throws InterruptedException
	{
		final File file = new File( "heads1.mpeg" );
		final XuggleAudio audio = new XuggleAudio( file );
		final AudioPlayer ap = new AudioPlayer( audio );
		
		new Thread(ap).start();
		
		Thread.sleep( 5000 );
		
		System.out.println( "Pause");
		ap.pause();
		System.out.println( ap.getTime() );
		Thread.sleep( 2000 );
		
		System.out.println( "Run");
		ap.run();
		System.out.println( ap.getTime() );
		Thread.sleep( 1500 );
		System.out.println( ap.getTime() );
		Thread.sleep( 1500 );
		
		System.out.println( "Reset");
		ap.reset();
		System.out.println( ap.getTime() );
		Thread.sleep( 10000 );
	}
}
