/**
 * 
 */
package org.openimaj.demos.sandbox.video;

import java.io.File;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.VideoPlayer;
import org.openimaj.video.capture.VideoCaptureException;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 *	Simple class that shows the video player class (like video display but
 *	with user controls).
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 14 Aug 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class VideoPlayerTest
{
	/**
	 *	@param args
	 * 	@throws VideoCaptureException 
	 */
	public static void main( final String[] args ) throws VideoCaptureException
	{
		String name = "heads1.mpeg";
		if( args.length > 0 )
			name = args[0];
		
		final XuggleVideo xv = new XuggleVideo( new File( name ) );
		final XuggleAudio xa = new XuggleAudio( new File( name ) );
		final VideoPlayer<MBFImage> vp = VideoPlayer.createVideoPlayer( xv, xa );
		vp.showFrame();
		vp.addVideoListener( new VideoDisplayListener<MBFImage>()
		{
//			private final CannyEdgeDetector2 cad = new CannyEdgeDetector2();
			
			@Override
			public void beforeUpdate( final MBFImage frame )
			{
//				frame.processInplace( this.cad );
			}
			
			@Override
			public void afterUpdate( final VideoDisplay<MBFImage> display )
			{
			}
		} );
	}
}
