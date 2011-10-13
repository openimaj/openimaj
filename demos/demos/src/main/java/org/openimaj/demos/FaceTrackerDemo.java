package org.openimaj.demos;

import java.io.File;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.tracking.KLTHaarFaceTracker;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 * 	A demo of the KLT/HaarCascade face tracker
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 13 Oct 2011
 */
public class FaceTrackerDemo
{
	/** The face tracker */
	private KLTHaarFaceTracker faceTracker = new KLTHaarFaceTracker( 40 );
	
	/** The video with faces in to track */
	private XuggleVideo video = new XuggleVideo( new File( "src/test/resources/rttr1.mpg" ) );
	
	int frameCounter = 0;
	
	/**
	 * 	Default contructor
	 */
	public FaceTrackerDemo()
	{
		// Jump into the video to a place where there are faces.
		video.setCurrentFrameIndex( 10 );
		
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( video );
		vd.addVideoListener( new VideoDisplayListener<MBFImage>()
		{
			@Override
			public void beforeUpdate( MBFImage frame )
			{
				// Pass the image to our face tracker
				List<DetectedFace> faces = faceTracker.trackFace( frame.flatten() );
				
				System.out.println( "Frame: "+(frameCounter++)+", "+faces.size()+" faces " );
				
				for( DetectedFace face: faces )
				{
					frame.drawShape( face.getBounds(), RGBColour.RED );
				}
			}
			
			@Override
			public void afterUpdate( VideoDisplay<MBFImage> display )
			{
			}
		});
	}
	
	public static void main( String[] args )
    {
	    new FaceTrackerDemo();
    }
}
