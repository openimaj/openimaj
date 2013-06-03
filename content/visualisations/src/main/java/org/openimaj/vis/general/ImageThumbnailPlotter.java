/**
 *
 */
package org.openimaj.vis.general;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.vis.general.XYPlotVisualisation.LocatedObject;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jun 2013
 */
public class ImageThumbnailPlotter implements ItemPlotter<MBFImage,Float[]>
{
	/** The maximum size of the thumbnail */
	private int thumbnailSize = 100;

	/**
	 *	@return the thumbnailSize
	 */
	public int getThumbnailSize()
	{
		return this.thumbnailSize;
	}

	/**
	 *	@param thumbnailSize the thumbnailSize to set
	 */
	public void setThumbnailSize( final int thumbnailSize )
	{
		this.thumbnailSize = thumbnailSize;
	}

	@Override
	public void plotObject(
			final MBFImage visImage,
			final LocatedObject<MBFImage> object, final AxesRenderer<Float[]> renderer )
	{
		final MBFImage thumbnail = object.object.process( new ResizeProcessor( this.thumbnailSize ) );
		final Point2d p = renderer.calculatePosition( visImage, object.x, object.y );
		visImage.createRenderer().drawImage( thumbnail, (int)p.getX(), (int)p.getY() );
	}
}
