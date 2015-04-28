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

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.time.Timecode;
import org.openimaj.video.Video;

/**
 *	A visualisation that is designed to show the position of an object over
 *	the course of the video. The trackObject method is called during processing
 *	so that subclasses can track whatever object it is that they wish to track.
 *	This should return a set of object coordinates that can be used to update
 *	the visualisation later. The type of position visualisation can be chosen.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 8 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class VideoObjectVisualisation extends VideoBarVisualisation
{
	/**
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 8 Feb 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static enum DrawType
	{
		/** Draw the point list as a point cloud */
		DOTS
		{
			@Override
			public void draw( final MBFImage vis, final int xoffset,
					final int yoffset, final PointList pl )
			{
				for( final Point2d p : pl )
				{
					final Point2d pp = new Point2dImpl( p );
					p.translate( xoffset, yoffset );
					vis.drawPoint( pp, RGBColour.RED, 2 );
				}
			}
		};

		/**
		 * 	Draw the point list into the given visualisation at the given offset.
		 *	@param vis The visualisation
		 *	@param xoffset The offset
		 *	@param yoffset The offset
		 *	@param pl The pointlist
		 */
		public abstract void draw( final MBFImage vis,
				final int xoffset, int yoffset, PointList pl );
	}

	/** We store the positions of all the objects as we go so we can redraw them */
	private final List<PointList> objectPositions = null;

	/** The height of the video frame */
	private int frameHeight = 0;

	/** The type of point drawing to use */
	private final DrawType drawType = DrawType.DOTS;

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 *	@param video
	 */
	protected VideoObjectVisualisation( final Video<MBFImage> video )
	{
		super( video );

		this.frameHeight = video.getHeight();
	}

	/**
	 * 	Tracks an object within the frame and returns a set of points
	 * 	that somehow delineate the object or give its position. If there is no
	 * 	object to track in the given frame return null and this will be dealt
	 * 	with correctly by the visualisation. The point positions should be
	 * 	in terms of the frame size and they will be resized to fit within
	 * 	the visualisation.
	 *
	 *	@param frame The frame
	 *	@return A {@link PointList} representing the object position
	 */
	public abstract PointList trackObject( final MBFImage frame );

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.video.VideoBarVisualisation#processFrame(org.openimaj.image.MBFImage, org.openimaj.time.Timecode)
	 */
	@Override
	public void processFrame( final MBFImage frame, final Timecode t )
	{
		this.objectPositions.add( this.trackObject( frame ) );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.video.VideoBarVisualisation#updateVis(org.openimaj.image.MBFImage)
	 */
	@Override
	public void updateVis( final MBFImage vis )
	{
		final float scalar = vis.getHeight() / this.frameHeight;

		// Redraw each of the positions.
		int frame = 0;
		for( final PointList pos : this.objectPositions )
		{
			final PointList pp = new PointList( pos.points, true );
			pp.scale( scalar );
			this.drawType.draw( vis, (int)this.getTimePosition( frame ), 0, pp );
			frame++;
		}
	}
}
