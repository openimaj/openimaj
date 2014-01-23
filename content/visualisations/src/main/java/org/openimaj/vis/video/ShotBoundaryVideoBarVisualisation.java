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

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.image.typography.hershey.HersheyFontStyle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.time.Timecode;
import org.openimaj.video.Video;
import org.openimaj.video.processing.shotdetector.HistogramVideoShotDetector;
import org.openimaj.video.processing.shotdetector.ShotBoundary;

/**
 *	Will display a video in a timeline with shot detections marked on it.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class ShotBoundaryVideoBarVisualisation extends VideoBarVisualisation
{
	/** */
	private static final long serialVersionUID = 1L;

	/** Shot detector */
	private HistogramVideoShotDetector shotDetector = null;

	/**
	 * 	To avoid constantly resampling, we cache the resampled images against
	 * 	the hash code of the original image.
	 */
	private final HashMap<Integer, MBFImage> imageCache = new HashMap<Integer,MBFImage>();

	/**
	 *	Default constructor that takes the video to visualise
	 *	@param video The video to visualise
	 */
	public ShotBoundaryVideoBarVisualisation( final Video<MBFImage> video )
	{
		super( video );

		/// Use a HistogramVideoShotDetector and store the key frames
		this.shotDetector = new HistogramVideoShotDetector( video );
		this.shotDetector.setFindKeyframes( true );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.video.VideoBarVisualisation#processFrame(org.openimaj.image.MBFImage, org.openimaj.time.Timecode)
	 */
	@Override
	public void processFrame( final MBFImage frame, final Timecode t )
	{
		// Look for shot boundaries
		this.shotDetector.processFrame( frame );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.video.VideoBarVisualisation#update()
	 */
	@Override
	public void update()
	{
		// Set the colour of the bar
		this.visImage.fill( this.barColour );

		// Get all the shot boundaries we currently have
		final List<ShotBoundary<MBFImage>> sbs = new ArrayList<ShotBoundary<MBFImage>>(
				this.shotDetector.getShotBoundaries() );

		// Store the list of bounding boxes of labels
		final List<Rectangle> timecodeLabelBounds = new ArrayList<Rectangle>();

		// Now loop through the shot boundaries.
		for( final ShotBoundary<MBFImage> sb : sbs )
		{
			// Try to get the resized image that's stored for a boundary image
			final int hash = sb.getKeyframe().imageAtBoundary.hashCode();
			MBFImage img = this.imageCache.get( hash );

			// We'll cache the resized image if it's not already there
			if( img == null )
				this.imageCache.put( hash, img = sb.getKeyframe().imageAtBoundary
					.process( new ResizeProcessor( 100, 100 ) ) );

			// Now draw the image into the visualisation
			if( img != null )
			{
				// Find the time position of a given timecode.
				final int x = (int)this.getTimePosition( sb.getTimecode() );
//				System.out.println( "Drawing image: "+sb.getTimecode()+" -> "+x );
				try
				{
					// Draw the image and its timecode
					final MBFImageRenderer r = this.visImage.createRenderer();
					final HersheyFont f = HersheyFont.TIMES_BOLD;
					final HersheyFontStyle<Float[]> fs = f.createStyle( r );
					fs.setFontSize( 12 );
					final String string = sb.getTimecode().toString();
					final Rectangle bounds = f.getRenderer( r ).getSize( string, fs );
					bounds.translate( x, img.getHeight()+bounds.height );

					// Move the bounds until the text isn't overlapping other text
					boolean overlapping = true;
					while( overlapping )
					{
						overlapping = false;
						for( final Rectangle rect : timecodeLabelBounds )
						{
							if( bounds.isOverlapping( rect ) )
							{
								bounds.translate( 0, rect.height );
								overlapping = true;
								break;
							}
						}
					}

					// Store the bounds of the timecode label we're above to draw
					bounds.width += 8;
					timecodeLabelBounds.add( bounds );

					// Draw the thumbnail image
					r.drawImage( img, x, 0 );

					// Draw the line at which the boundary occurred
					r.drawLine( x, 0, x, this.visImage.getHeight(), 2, RGBColour.BLACK );

					// Draw a box behind the text
					r.drawShapeFilled( bounds, new Float[]{0f,0f,0f,0.3f} );

					// Draw the text
					r.drawText( string, (int)bounds.x+4, (int)(bounds.y+bounds.height), f, 12, RGBColour.WHITE );
				}
				catch( final Exception e )
				{
					e.printStackTrace();
					System.out.println( "Image was: "+img );
					System.out.println( "    - Size: "+img.getWidth()+"x"+img.getHeight() );
					System.out.println( "    - Num Bands: "+img.numBands() );
					System.out.println( "Being drawn to: "+this.visImage );
					System.out.println( "    - Size: "+this.visImage.getWidth()+"x"+this.visImage.getHeight() );
					System.out.println( "    - Num Bands: "+this.visImage.numBands() );
					System.out.println( "    - At Position: "+x+",0 ");

					DisplayUtilities.display( img, "img" );
					DisplayUtilities.display( this.visImage, "Vis" );

					try
					{
						Thread.sleep( 100000000 );
					}
					catch( final InterruptedException e1 )
					{
						e1.printStackTrace();
					}
				}
			}
		}

		super.update();
	}
}
