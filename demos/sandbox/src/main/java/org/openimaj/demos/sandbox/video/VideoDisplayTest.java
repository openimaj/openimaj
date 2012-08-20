/**
 * 
 */
package org.openimaj.demos.sandbox.video;

import java.io.File;

import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 *	Simple video test that displays a video (with sound) in one window and
 *	a webcam (without sound) in another window.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 16 Aug 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class VideoDisplayTest
{
	/**
	 * 
	 *	@param args
	 *	@throws VideoCaptureException
	 */
	public static void main( final String[] args ) throws VideoCaptureException
	{
		final VideoCapture vc = new VideoCapture( 640,480 );
		
		String file = "video.m4v";
		if( args.length > 0 )
			file = args[0];
			
		final XuggleVideo xv = new XuggleVideo( new File( file ) );
		final XuggleAudio xa = new XuggleAudio( new File( file ) );
		
		VideoDisplay.createVideoDisplay( xv, xa );
		VideoDisplay.createVideoDisplay( vc );
	}
}
