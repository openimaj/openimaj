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
package org.openimaj.demos;

import java.awt.Toolkit;
import java.io.File;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.processing.shotdetector.ShotBoundary;
import org.openimaj.video.processing.shotdetector.ShotDetectedListener;
import org.openimaj.video.processing.shotdetector.VideoKeyframe;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
import org.openimaj.video.timecode.VideoTimecode;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public class VideoShotDetectorVisualisation
{
	/**
	 * 	Testing code.
	 *  @param args
	 */
	public static void main( String[] args )
	{
		DisplayUtilities.displayName( new MBFImage(100,100,3), "vsd", true );
		
		final int th = 64;
		final int tw = 64;
		final int h = 200;
		final int w = Toolkit.getDefaultToolkit().getScreenSize().width - tw;
		final MBFImage m = new MBFImage( w+tw, h, 3 );
		final MBFImageRenderer renderer = m.createRenderer();
		final ResizeProcessor rp = new ResizeProcessor( tw, th, true );
		
		final int threshold = 8000;
		final VideoShotDetector<MBFImage> vsd = new VideoShotDetector<MBFImage>( 
				new XuggleVideo(new File( "src/test/resources/rttr1.mpg") ), false );
		vsd.setStoreAllDifferentials( true );
		vsd.setFindKeyframes( true );
		vsd.setThreshold( threshold );
		vsd.addShotDetectedListener( new ShotDetectedListener<MBFImage>()
		{
			private double lastMax = 10000;
			
			@Override
			public void shotDetected( ShotBoundary sb, VideoKeyframe<MBFImage> vk )
			{
				// Reset the image
				m.zero();
				
				// Calculate the various variables required to draw the visualisation.
				DoubleFV dfv = vsd.getDifferentials();
				double max = Double.MIN_VALUE;
				for( int x = 0; x < dfv.length(); x++ )
					max = Math.max( max, dfv.get(x) );
				if( max > 50 ) lastMax = max;
				
				// Draw all the keyframes found onto the image
				for( VideoKeyframe<MBFImage> kf : vsd.getKeyframes() )
				{
					int fn = kf.getTimecode().getFrameNumber();
					int x = fn * w / dfv.length();
					
					// We draw the keyframes along the top of the visualisation.
					// So we draw a line to the frame to match it up to the differential
					renderer.drawLine( x, h, x, 0, new Float[]{0.3f,0.3f,0.3f} );			
					renderer.drawImage( kf.getImage().process( rp ), x+1, 0);
				}

				// This is the threshold line drawn onto the image.
				renderer.drawLine( 0, (int)(h - h/max*threshold), w, 
						(int)(h - h/max*threshold), RGBColour.RED );
				
				// Now draw all the differentials
				int x = 0;
				for( int z = 0; z < dfv.length(); z++ )
				{
					x = z * w/dfv.length();
					renderer.drawLine( x, h, x, (int)(h - h/max*dfv.get(z)), 
							RGBColour.WHITE );
				}

				// Display the visualisation
				DisplayUtilities.updateNamed( "vsd", m, "Video Shot Detector" );

				// System.out.println( "Keyframes: "+keyframes );
				// DisplayUtilities.display( "Keyframes: ", keyframes.toArray( new Image<?,?>[0] ) );				
			}

			@Override
            public void differentialCalculated( VideoTimecode vt, double d )
			{	
				renderer.drawShapeFilled( new Rectangle(w,th,10,h-th), RGBColour.BLACK );
				renderer.drawLine( w+5, h, w+5, (int)(h - h/lastMax*d), 10,
						RGBColour.RED );

				// Display the visualisation
				DisplayUtilities.updateNamed( "vsd", m, "Video Shot Detector" );
            }
		});

		vsd.process();
	}
}
