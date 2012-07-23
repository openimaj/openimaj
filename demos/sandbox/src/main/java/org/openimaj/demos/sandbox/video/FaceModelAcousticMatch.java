/**
 * 
 */
package org.openimaj.demos.sandbox.video;

import java.io.File;
import java.util.List;

import org.openimaj.audio.analysis.EffectiveSoundPressure;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 23 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class FaceModelAcousticMatch
{
	/** Number of frames average to take sound pressure over */
	private int nFrameAvg = 1;
	
	/**
	 * 
	 *	@param filename
	 */
	public FaceModelAcousticMatch( String filename )
    {
		// Create a video object
		XuggleVideo v = new XuggleVideo( new File( filename ) );
		double fps = v.getFPS();
		
		// Create an audio object
		XuggleAudio a = new XuggleAudio( new File( filename ) );
		
		// Create an audio processor where the frames will be the same length
		// (in time) as the video frames.
		final EffectiveSoundPressure esp = new EffectiveSoundPressure( a, 
				nFrameAvg * (int)(1000d/fps), 0 );
		
		// The face tracker
		final CLMFaceTracker faceTracker = new CLMFaceTracker();
		
		// The shot detector used to control the face tracker
		final VideoShotDetector shotDetector = new VideoShotDetector( fps );
		
		// An image to display the vis.
		final MBFImage visImage = new MBFImage( 800, 400, 3 );
		
		// Create a video display to show the video
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( v );
		vd.addVideoListener( new VideoDisplayListener<MBFImage>()
		{
			private double lastPressure = 0;
			private double lastModelShape = 0;
			private int counter = 0;
			
			@Override
			public void beforeUpdate( MBFImage frame )
			{
				// Check whether a shot boundary was found...
				shotDetector.processFrame( frame );
				
				// ...if so, reset the face tracker
				if( shotDetector.wasLastFrameBoundary() )
					faceTracker.reset();
				
				// Track the face
				faceTracker.track( frame );
				
				// Draw any detected faces onto the image
				faceTracker.drawModel( frame, true,true,true,true,true );
				
				// We aren't actually interested in the samples, but this
				// will force the calculation of the pressure for the next
				// sample chunk.
				double pressure = lastPressure;
				if( counter % nFrameAvg == 0 )
				{
					esp.nextSampleChunk();
					pressure = esp.getEffectiveSoundPressure() / 20d;
				}
				
				// Check the model shape at this frame
				double modelShape = 0;
				List<TrackedFace> trackedFaces = faceTracker.getModelTracker().trackedFaces;
				if( trackedFaces.size() > 0 )
					modelShape = (trackedFaces.get(0).clm._plocal.get( 0, 0 )+30d)*50d-1300;
				
				System.out.println( modelShape );

				// Move the vis along
				visImage.shiftLeftInplace();
				
				// Draw the audio pressure
				visImage.drawLine( visImage.getWidth()-2, (int)(visImage.getHeight() - lastPressure), 
						visImage.getWidth()-1, (int)(visImage.getHeight() - pressure), 
						RGBColour.GREEN );
				
				// Draw the model shape
				visImage.drawLine( visImage.getWidth()-2, (int)(visImage.getHeight() - lastModelShape),
						visImage.getWidth()-1, (int)(visImage.getHeight() - modelShape),
						RGBColour.YELLOW );
				
				// Show the window
				DisplayUtilities.displayName( visImage, "Pressure vs Shape", true );

				// Remember where we were last drawing (so we can draw lines)
				lastPressure = pressure;
				lastModelShape = modelShape;
				counter++;
			}
			
			@Override
			public void afterUpdate( VideoDisplay<MBFImage> display )
			{
			}
		} );
    }

	/**
	 * 	Main
	 *	@param args CLAs
	 */
	public static void main( String[] args )
    {
	    new FaceModelAcousticMatch( "/home/dd/mel.mp4" );
    }
}
