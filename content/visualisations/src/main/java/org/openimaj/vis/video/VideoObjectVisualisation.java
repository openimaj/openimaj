/**
 * 
 */
package org.openimaj.vis.video;

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointList;
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
	
	/** The width of the video frame */
	private int frameWidth  = 0;
	
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
		
		this.frameWidth  = video.getWidth();
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
