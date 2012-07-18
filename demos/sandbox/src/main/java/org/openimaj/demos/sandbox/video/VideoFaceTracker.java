package org.openimaj.demos.sandbox.video;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 *  Tracks faces in video using the CLM Tracker.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@version $Author$, $Revision$, $Date$
 *	@created 4 Jul 2012
 */
public class VideoFaceTracker implements VideoDisplayListener<MBFImage>
{
	/** The shot detector to use */
	private VideoShotDetector shotDetector = null; 

	/** The face tracker we're going to use */
	private CLMFaceTracker tracker = new CLMFaceTracker();

	/** Frame counter for FPS display */
	private int fnum = 0;
	
	/** Cache for the FPS text generated every 10 frames */
	private String fpsText = "";

	/** Timers for the FPS calculation */
	private long t1, t0 = System.currentTimeMillis();

	/** Whether to show the FPS on the view */
	private boolean showFPS = true;

	/** Whether to draw triangles on the video */
	private boolean drawTriangles = false;

	/** Whether to draw connection on the video */
	private boolean drawConnections = true;

	/** Whether to draw points on the video */
	private boolean drawPoints = true;
	
	/** Whether to draw the face bounds */
	private boolean drawBounds = true;
	
	/** Whether to draw the template match search area */
	private boolean drawSearchArea = true;

	/**
	 * 	Default constructor
	 * 	@param v The video to track faces in 
	 *	@throws IOException
	 */
	public VideoFaceTracker( Video<MBFImage> v ) throws IOException
	{
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( v );
		vd.addVideoListener( this );
		shotDetector = new VideoShotDetector( v.getFPS() );
//		shotDetector.setThreshold( 500 );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayListener#beforeUpdate(org.openimaj.image.Image)
	 */
	@Override
	public void beforeUpdate( MBFImage frame )
	{
		// Process the frame.
		shotDetector.processFrame( frame );
		if( shotDetector.wasLastFrameBoundary() )
		{
			System.out.println( "Shot detected. Forcing redetect on face tracker.");
			tracker.reset();
		}

		// Track faces in the image
		tracker.track( frame );
		
		// Draw the tracked model to the image
		tracker.drawModel( frame, drawTriangles, drawConnections, drawPoints,
				drawSearchArea, drawBounds );
	
		// Whether to show FPS
		if( showFPS )
		{
			// Draw framerate on display image (average of 10 frames)
			if( fnum >= 9 )
			{
				t1 = System.currentTimeMillis();
				double fps = 10 / ((double)(t1 - t0) / 1000.0);
				t0 = t1;
				fnum = 0;
				fpsText = String.format( "%d frames/sec", (int)Math.round( fps ) );
			}
			else
			{
				fnum++;
			}

			frame.drawText( fpsText, 10, 20, HersheyFont.ROMAN_SIMPLEX, 20,
			        RGBColour.GREEN );
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayListener#afterUpdate(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void afterUpdate( VideoDisplay<MBFImage> display )
	{
		// No implementation
	}

	/**
	 *	@param args
	 *	@throws Exception
	 */
	public static void main( String[] args ) throws Exception
	{
		new VideoFaceTracker( new XuggleVideo( new File( "/home/dd/rt20111114.mp4" ) ) );
	}
}
