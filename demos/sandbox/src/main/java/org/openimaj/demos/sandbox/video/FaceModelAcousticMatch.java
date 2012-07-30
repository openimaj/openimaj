/**
 * 
 */
package org.openimaj.demos.sandbox.video;

import java.io.File;
import java.util.HashMap;
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
	
	/** Colours to draw each of the detected faces in */
	private Float[][] colours = new Float[][]{
			RGBColour.YELLOW,
			RGBColour.RED,
			RGBColour.MAGENTA,
			RGBColour.CYAN
	};
	
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
		vd.setStopOnVideoEnd( true );
		vd.addVideoListener( new VideoDisplayListener<MBFImage>()
		{
			private double lastPressure = 0;
			private int counter = 0;
			private HashMap<TrackedFace,Integer> lastPos = 
					new HashMap<TrackedFace, Integer>();
			
			@Override
			public void beforeUpdate( MBFImage frame )
			{
				// Check whether a shot boundary was found...
				shotDetector.processFrame( frame );
				
				// ...if so, reset the face tracker
				if( shotDetector.wasLastFrameBoundary() )
				{
					faceTracker.reset();
					lastPos.clear();
				}
				
				// Track the face
				faceTracker.track( frame );
				List<TrackedFace> trackedFaces = faceTracker.getModelTracker().trackedFaces;
				
				// Draw any detected faces onto the image
				for( int i = 0; i < trackedFaces.size(); i++ )
				{
					TrackedFace f = trackedFaces.get(i);
					CLMFaceTracker.drawFaceModel( frame, f, true, true, 
							true, true, true, faceTracker.getReferenceTriangles(), 
							faceTracker.getReferenceConnections(), 1f, 
							colours[i], RGBColour.BLACK, RGBColour.WHITE, 
							RGBColour.RED );
				}
				
				// We aren't actually interested in the samples here, but this
				// will force the calculation of the pressure for the next
				// sample chunk - and the sample chunks should be the length
				// of one frame of video which is why we can do it once per
				// frame.
				double pressure = lastPressure;
				if( counter % nFrameAvg == 0 )
				{
					esp.nextSampleChunk();
					pressure = esp.getEffectiveSoundPressure() / 20d;
				}
				
				// Move the vis along
				visImage.shiftLeftInplace();
				
				// Draw the audio pressure
				visImage.drawLine( visImage.getWidth()-2, (int)(visImage.getHeight() - lastPressure), 
						visImage.getWidth()-1, (int)(visImage.getHeight() - pressure), 
						RGBColour.GREEN );

				// Check the model shape at this frame
				double modelShape = 0;

				if( trackedFaces.size() > 0 )
				{
					double lastMs = 0;
					for( int i = 0; i < trackedFaces.size(); i++ )
					{
						// Get the face model
						TrackedFace f = trackedFaces.get(i);
						modelShape = f.clm._plocal.get(0,0);
					
						// Last position to draw from
						Integer lp = lastPos.get(f);
						if( lp == null )
							lp = 0;
						
						// Mouth speed (dy/dx of model shape)
						int diff = (int)(Math.abs( modelShape-lastMs ) * 10d);
						
						// Draw the model shape
						visImage.drawLine( visImage.getWidth()-2, 
								(int)(visImage.getHeight() - lp),
								visImage.getWidth()-1, 
								(int)(visImage.getHeight() - diff),
								colours[i%colours.length] );
						
						lastPos.put( f, diff );
						lastMs = modelShape;
					}
				}
				
				// Show the window
				DisplayUtilities.displayName( visImage, "Pressure vs Shape", true );

				// Remember where we were last drawing (so we can draw lines)
				lastPressure = pressure;
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
	    new FaceModelAcousticMatch( "heads1.mpeg" );
    }
}
