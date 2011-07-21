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
import org.openimaj.video.processing.shotdetector.VideoKeyframe;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
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
		int threshold = 8000;
		VideoShotDetector<MBFImage> vsd = new VideoShotDetector<MBFImage>( new XuggleVideo(new File( "/Users/ss/dwhelper/OldNeil.mpg") ), true );
		vsd.setStoreAllDifferentials( true );
		vsd.setFindKeyframes( true );
		vsd.setThreshold( threshold );
		vsd.process();

		System.out.println( "Found these boundaries: "+vsd.getShotBoundaries() );

		// Calculate the various variables required to draw the visualisation.
		DoubleFV dfv = vsd.getDifferentials();
		double max = Double.MIN_VALUE;
		for( int x = 0; x < dfv.length(); x++ )
			max = Math.max( max, dfv.get(x) );
		int th = 64;
		int tw = 64;
		int h = 200;
		int w = Toolkit.getDefaultToolkit().getScreenSize().width - tw;
		MBFImage m = new MBFImage( w+tw, h, 3 );

		// Draw all the keyframes found onto the image
		ResizeProcessor rp = new ResizeProcessor( tw, th, true );
		for( VideoKeyframe<MBFImage> kf : vsd.getKeyframes() )
		{
			int fn = kf.getTimecode().getFrameNumber();
			int x = fn * w / dfv.length();
			
			// We draw the keyframes along the top of the visualisation.
			// So we draw a line to the frame to match it up to the differential
			m.drawLine( x, h, x, 0, new Float[]{0.3f,0.3f,0.3f} );			
			m.drawImage( kf.getImage().process( rp ), x+1, 0);
		}

		// This is the threshold line drawn onto the image.
		m.drawLine( 0, (int)(h - h/max*threshold), w, (int)(h - h/max*threshold), RGBColour.RED );
		
		// Now draw all the differentials
		int x = 0;
		for( int z = 0; z < dfv.length(); z++ )
		{
			x = z * w/dfv.length();
			m.drawLine( x, h, x, (int)(h - h/max*dfv.get(z)), RGBColour.WHITE );
		}

		// Display the visualisation
		DisplayUtilities.display( m );

		// System.out.println( "Keyframes: "+keyframes );
		// DisplayUtilities.display( "Keyframes: ", keyframes.toArray( new Image<?,?>[0] ) );
	}
}
