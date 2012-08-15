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
package org.openimaj.demos.faces;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.openimaj.demos.Demo;
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
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 13 Oct 2011
 */
@Demo(
	author = "David Dupplaw", 
	description = "Demonstrates the KLT Tracker on faces, with the" +
			" OpenIMAJ FaceTracker component.", 
	keywords = { "face", "tracking", "video", "klt" }, 
	title = "Face Tracking"
)
public class KLTHaarFaceTrackerDemo
{
	/** The face tracker */
	private KLTHaarFaceTracker faceTracker = new KLTHaarFaceTracker( 40 );
	
	/** The video with faces in to track */
	private XuggleVideo video =null;
	
	int frameCounter = 0;
	
	/**
	 * 	Default contructor
	 * @throws MalformedURLException 
	 */
	public KLTHaarFaceTrackerDemo() throws MalformedURLException
	{
		// Load the video
		URL url = KLTHaarFaceTrackerDemo.class.getResource("/org/openimaj/demos/video/guy_goma.mp4");
		if(url == null){
			url = getAFile().toURI().toURL();
		}
		
		video = new XuggleVideo(url);
		
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
	
	private File getAFile() {
		JFileChooser c = new JFileChooser();
		// Demonstrate "Open" dialog:
		int rVal = c.showOpenDialog(new JFrame());
		if (rVal == JFileChooser.APPROVE_OPTION) {
			return c.getSelectedFile();
		}
		return null;
	}

	/**
	 * 	Default main
	 *  @param args Command-line arguments
	 *  @throws MalformedURLException If the URL for the video isn't right
	 */
	public static void main( String[] args ) throws MalformedURLException
    {
	    new KLTHaarFaceTrackerDemo();
    }
}
