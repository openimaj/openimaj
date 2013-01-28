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
package org.openimaj.vis.video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openimaj.audio.AudioStream;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.time.Timecode;
import org.openimaj.video.Video;
import org.openimaj.video.processing.shotdetector.ShotBoundary;
import org.openimaj.video.processing.shotdetector.HistogramVideoShotDetector;

/**
 *	Will display a video in a timeline with shot detections marked on it.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class ShotBoundaryVideoBarVisualisation extends VideoBarVisualisation
{
	/** Shot detector */
	private HistogramVideoShotDetector shotDetector = null;
	
	/** 
	 * 	To avoid constantly resampling, we cache the resampled images against
	 * 	the hash code of the original image.
	 */
	private HashMap<Integer, MBFImage> imageCache = new HashMap<Integer,MBFImage>();
	
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 *	@param video
	 */
	public ShotBoundaryVideoBarVisualisation( Video<MBFImage> video )
	{
		this( video, null );
	}

	/**
	 * 
	 *	@param video
	 *	@param audio
	 */
	public ShotBoundaryVideoBarVisualisation( Video<MBFImage> video, AudioStream audio )
	{
		super( video );
		this.shotDetector = new HistogramVideoShotDetector( video );
		this.shotDetector.setFindKeyframes( true );
	}
	
	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.video.VideoBarVisualisation#processFrame(org.openimaj.image.MBFImage, org.openimaj.time.Timecode)
	 */
	@Override
	public void processFrame( MBFImage frame, Timecode t )
	{
		this.shotDetector.processFrame( frame );
	}

	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.video.VideoBarVisualisation#updateVis(org.openimaj.image.MBFImage)
	 */
	@Override
	public void updateVis( MBFImage vis )
	{
		List<ShotBoundary<MBFImage>> sbs = new ArrayList<ShotBoundary<MBFImage>>(
				this.shotDetector.getShotBoundaries() );
		for( ShotBoundary<MBFImage> sb : sbs )
		{
			int hash = sb.getKeyframe().imageAtBoundary.hashCode();
			MBFImage img = imageCache.get( hash );
			if( img == null )
				imageCache.put( hash, img = sb.getKeyframe().imageAtBoundary
					.process( new ResizeProcessor( 100, 100 ) ) );
			
			if( img != null )
			{
				int x = (int)getTimePosition( sb.getTimecode() );
				try
				{
					vis.createRenderer().drawImage( img, x, 0 );
				}
				catch( Exception e )
				{
					e.printStackTrace();
					System.out.println( "Image was: "+img );
					System.out.println( "    - Size: "+img.getWidth()+"x"+img.getHeight() );
					System.out.println( "    - Num Bands: "+img.numBands() );
					System.out.println( "Being drawn to: "+vis );
					System.out.println( "    - Size: "+vis.getWidth()+"x"+vis.getHeight() );
					System.out.println( "    - Num Bands: "+vis.numBands() );
					System.out.println( "    - At Position: "+x+",0 ");
					
					DisplayUtilities.display( img, "img" );
					DisplayUtilities.display( vis, "Vis" );
					
					try
					{
						Thread.sleep( 100000000 );
					}
					catch( InterruptedException e1 )
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
	//			vis.drawLine( x, 0, x, vis.getHeight(), 2, RGBColour.BLACK );
			}
		}
	}
}
