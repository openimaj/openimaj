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
import org.openimaj.video.VideoDisplay.EndAction;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.processing.shotdetector.HistogramVideoShotDetector;
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
	private final int nFrameAvg = 1;
	
	/** Colours to draw each of the detected faces in */
	private final Float[][] colours = new Float[][]{
			RGBColour.YELLOW,
			RGBColour.RED,
			RGBColour.MAGENTA,
			RGBColour.CYAN
	};
	
	/**
	 * 
	 *	@param filename
	 */
	public FaceModelAcousticMatch( final String filename )
    {
		// Create a video object
		final XuggleVideo v = new XuggleVideo( new File( filename ) );
		final double fps = v.getFPS();
		
		// Create an audio object
		final XuggleAudio a = new XuggleAudio( new File( filename ) );
		
		// Create an audio processor where the frames will be the same length
		// (in time) as the video frames.
		final EffectiveSoundPressure esp = new EffectiveSoundPressure( a, 
				this.nFrameAvg * (int)(1000d/fps), 0 );
		
		// The face tracker
		final CLMFaceTracker faceTracker = new CLMFaceTracker();
		
		// The shot detector used to control the face tracker
		final HistogramVideoShotDetector shotDetector = new HistogramVideoShotDetector( fps );
		
		// An image to display the vis.
		final MBFImage visImage = new MBFImage( 800, 400, 3 );
		
		// Create a video display to show the video
		final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( v );
		vd.setEndAction( EndAction.STOP_AT_END );
		vd.addVideoListener( new VideoDisplayListener<MBFImage>()
		{
			private double lastPressure = 0;
			private int counter = 0;
			private final HashMap<TrackedFace,Integer> lastPos = 
					new HashMap<TrackedFace, Integer>();
			
			@Override
			public void beforeUpdate( final MBFImage frame )
			{
				// Check whether a shot boundary was found...
				shotDetector.processFrame( frame );
				
				// ...if so, reset the face tracker
				if( shotDetector.wasLastFrameBoundary() )
				{
					faceTracker.reset();
					this.lastPos.clear();
				}
				
				// Track the face
				faceTracker.track( frame );
				final List<TrackedFace> trackedFaces = faceTracker.getModelTracker().trackedFaces;
				
				// Draw any detected faces onto the image
				for( int i = 0; i < trackedFaces.size(); i++ )
				{
					final TrackedFace f = trackedFaces.get(i);
					CLMFaceTracker.drawFaceModel( frame, f, true, true, 
							true, true, true, faceTracker.getReferenceTriangles(), 
							faceTracker.getReferenceConnections(), 1f, 
							FaceModelAcousticMatch.this.colours[i], RGBColour.BLACK, RGBColour.WHITE, 
							RGBColour.RED );
				}
				
				// We aren't actually interested in the samples here, but this
				// will force the calculation of the pressure for the next
				// sample chunk - and the sample chunks should be the length
				// of one frame of video which is why we can do it once per
				// frame.
				double pressure = this.lastPressure;
				if( this.counter % FaceModelAcousticMatch.this.nFrameAvg == 0 )
				{
					esp.nextSampleChunk();
					pressure = esp.getEffectiveSoundPressure() / 20d;
				}
				
				// Move the vis along
				visImage.shiftLeftInplace();
				
				// Draw the audio pressure
				visImage.drawLine( visImage.getWidth()-2, (int)(visImage.getHeight() - this.lastPressure), 
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
						final TrackedFace f = trackedFaces.get(i);
						modelShape = f.clm._plocal.get(0,0);
					
						// Last position to draw from
						Integer lp = this.lastPos.get(f);
						if( lp == null )
							lp = 0;
						
						// Mouth speed (dy/dx of model shape)
						final int diff = (int)(Math.abs( modelShape-lastMs ) * 10d);
						
						// Draw the model shape
						visImage.drawLine( visImage.getWidth()-2, 
								(visImage.getHeight() - lp),
								visImage.getWidth()-1, 
								(visImage.getHeight() - diff),
								FaceModelAcousticMatch.this.colours[i%FaceModelAcousticMatch.this.colours.length] );
						
						this.lastPos.put( f, diff );
						lastMs = modelShape;
					}
				}
				
				// Show the window
				DisplayUtilities.displayName( visImage, "Pressure vs Shape", true );

				// Remember where we were last drawing (so we can draw lines)
				this.lastPressure = pressure;
				this.counter++;
			}
			
			@Override
			public void afterUpdate( final VideoDisplay<MBFImage> display )
			{
			}
		} );
    }

	/**
	 * 	Main
	 *	@param args CLAs
	 */
	public static void main( final String[] args )
    {
	    new FaceModelAcousticMatch( "heads1.mpeg" );
    }
}
