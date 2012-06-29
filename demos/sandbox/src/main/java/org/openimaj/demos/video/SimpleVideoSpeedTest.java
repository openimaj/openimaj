/**
 * 
 */
package org.openimaj.demos.video;

import java.io.IOException;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

/**
 *	
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 20 Jun 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class SimpleVideoSpeedTest
{
	public static void main( String[] args ) throws IOException
	{
		VideoCapture vc = new VideoCapture( 1280, 960 );
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( vc );
		vd.addVideoListener( new VideoDisplayListener<MBFImage>()
		{
			long lastTime = 0;
			
			@Override
			public void afterUpdate( VideoDisplay<MBFImage> display )
			{
			}

			@Override
			public void beforeUpdate( MBFImage frame )
			{
				long thisTime = System.currentTimeMillis();
				if( lastTime != 0 )
				{
					System.out.println( 1000/(System.currentTimeMillis()-lastTime) );
				}
				
				lastTime = thisTime;
			}
		} );
	}
}
