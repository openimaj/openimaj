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
