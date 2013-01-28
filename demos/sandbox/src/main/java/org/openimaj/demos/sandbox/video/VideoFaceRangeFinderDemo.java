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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.Video;
import org.openimaj.video.processing.shotdetector.ShotBoundary;
import org.openimaj.video.processing.shotdetector.HistogramVideoShotDetector;
import org.openimaj.video.processing.timefinder.ObjectTimeFinder;
import org.openimaj.video.processing.tracking.BasicMBFImageObjectTracker;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
import org.openimaj.video.timecode.VideoTimecode;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 *	Uses Jon's face keyframes (or at least the frame number from them) to
 *	make this visualisation. Requires the video from which they came too.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 14 Oct 2011
 */
@Demo(
	author = "David Dupplaw", 
	description = "Demonstrates the face range finder component which " +
			"splits a video into shots, detects faces from the keyframe at the " +
			"centre of the shot then tracks those faces " +
			"through the video (back and forward) to give the timecodes between which " +
			"faces appear in the video.", 
	keywords = { "face", "video", "tracking", "shot boundary" }, 
	title = "Face Range Finder"
)
public class VideoFaceRangeFinderDemo 
{
	/** The video we'll be reading from */
	private Video<MBFImage> video = null;
	
	/** Set this to false if you simply want the face ranges to be printed */
	private boolean doNiceVis = true;
	
	/** The number of frames in the video */
	private long nFrames = 0;

	/** The shot boundaries found in the video, if we're looking */
	private List<ShotBoundary<MBFImage>> shotBoundaries = null;

	/** The visualisation image */
	private MBFImage outputImage = null;
	
	private int tnSize = 64;
	
	/**
	 * 
	 */
	public VideoFaceRangeFinderDemo()
	{
		// The video we're going to use
		video = new XuggleVideo( new File(
			"src/main/resources/org/openimaj/demos/rttr/07161859-rttr-16k-news10-rttr-16k.mpg" ) );
	
		// If we're going to do a nice visualisation, we'll do a shot boundary
		// detection so that we can plot the timeline of the video
		if( doNiceVis )
		{
			// Work out the number of frames so that we can do a display
			nFrames = video.countFrames();
			
			// Work out the shot boundaries (just for visualisation)
			HistogramVideoShotDetector sd = new HistogramVideoShotDetector( video );
			sd.process();
			shotBoundaries = sd.getShotBoundaries();

			// Create the output image
			int w = 1500;
			int h = 150;
			outputImage  = new MBFImage( w, h, 3 );
			
			// Draw boxes into the output image to represent shots
			Float[][] colours = new Float[][]{
					{0.7f,0.7f,0.8f},
					{0.5f,0.5f,0.6f}
			};
			
			int i = 0;
			ShotBoundary<MBFImage> last = null;
			for( ShotBoundary<MBFImage> kf : shotBoundaries )
			{
				if( last != null )
				{
					int x  = (int)(last.getTimecode().getFrameNumber() / (double)nFrames * w);
					int xw = (int)(kf.getTimecode().getFrameNumber() / (double)nFrames * w);
					outputImage.drawShapeFilled( 
						new Rectangle( x, 0, xw-x, h ), 
						colours[i++%colours.length]);
				}
				
				last = kf;
			}
			
			// Display the shots
			DisplayUtilities.displayName( outputImage, "vis", true );
		}
		
		// Get the frames in which we think there are faces
		List<VideoTimecode> startFrames = getStartFrames();
		
		// Initialise our object trackers
		ObjectTimeFinder of = new ObjectTimeFinder();
		BasicMBFImageObjectTracker objectTracker = new BasicMBFImageObjectTracker();
		
		// For each of the keyframes in which we think there are faces...
		for( VideoTimecode keyframeTime : startFrames )
		{
			// Skip to the keyframe time and get the current frame
			System.out.println( "Seeking to frame "+keyframeTime );
			video.setCurrentFrameIndex( keyframeTime.getFrameNumber() );
			MBFImage image = video.getCurrentFrame();
			
			// Detect faces in this frame
			HaarCascadeDetector faceDetector = new HaarCascadeDetector( 40 );
			List<DetectedFace> faces = faceDetector.detectFaces( image.flatten() );
			
			System.out.println( "In frame "+keyframeTime+" found "+faces.size()+" faces.");

			// TODO: Check faces against keyframe
			DetectedFace df = faces.get(0);
			
			if( doNiceVis )
			{
				int x  = (int)(keyframeTime.getFrameNumber() / 
						(double)nFrames * outputImage.getWidth() );

				// Extract the face patch and draw it into the visualisation
				MBFImage facePatch = image.extractROI( df.getBounds() );
				outputImage.drawImage( facePatch.process( 
						new ResizeProcessor(tnSize, tnSize, true) ), x-(tnSize/2), 0 );
				
				// Update the display
				DisplayUtilities.displayName( outputImage, "vis", true );
			}
			
			// Track the face
			System.out.println( "Tracking face..." );
			IndependentPair<VideoTimecode, VideoTimecode> times = 
				of.trackObject( objectTracker, video, keyframeTime, df.getBounds(), null );
			
			System.out.println( "Got times: "+times );
			
			if( doNiceVis )
			{
				int x  = (int)(keyframeTime.getFrameNumber() / 
						(double)nFrames * outputImage.getWidth() );
				int x1  = (int)(times.firstObject().getFrameNumber() / 
						(double)nFrames * outputImage.getWidth() );
				int x2  = (int)(times.secondObject().getFrameNumber() / 
						(double)nFrames * outputImage.getWidth() );
				
				outputImage.drawLine(  x,   tnSize,  x, tnSize+4, 2, RGBColour.RED );
				outputImage.drawLine( x1, tnSize+4, x2, tnSize+4, 2, RGBColour.RED );
				outputImage.drawLine( x1, tnSize+2, x1, tnSize+6, 1, RGBColour.RED );
				outputImage.drawLine( x2, tnSize+2, x2, tnSize+6, 1, RGBColour.RED );

				// Update the display
				DisplayUtilities.displayName( outputImage, "vis", true );
			}
		}
	}
	
	/**
	 * 	Returns the keyframes in which we think there are faces.
	 *	@return
	 */
	private List<VideoTimecode> getStartFrames()
	{
		List<VideoTimecode> timecodes = new ArrayList<VideoTimecode>();
		
		// We're just going to read these from disk.
		File[] files = new File( "src/main/resources/org/openimaj/demos/rttr/shots" ).listFiles();

		for( File f : files )
		{
			String frameNumStr = f.getName().substring( 
					f.getName().lastIndexOf( '#' )+1,
					f.getName().lastIndexOf( '.' ) );
			int frameNum = Integer.parseInt( frameNumStr );
			
			HrsMinSecFrameTimecode t = new HrsMinSecFrameTimecode( frameNum, 
					video.getFPS() );
			timecodes.add( t );
		}
		
		Collections.sort( timecodes );
		
		return timecodes;
	}
	
	/**
	 * 	Default main
	 *  @param args Command-line arguments
	 */
	public static void main( String[] args )
	{
		new VideoFaceRangeFinderDemo();
	}
}
