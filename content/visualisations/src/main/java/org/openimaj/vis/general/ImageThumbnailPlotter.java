/**
 *
 */
package org.openimaj.vis.general;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.vis.general.XYPlotVisualisation.LocatedObject;

/**
 *	An item plotter that is able to plot images into a visualisation at a given
 *	position and size. The thumbnail size is initially 100 pixels, but if you need
 *	to alter this based on the size of the visualisation, you can use the
 *	{@link #setThumbnailSize(int)} method to set the maximum dimension.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jun 2013
 */
public class ImageThumbnailPlotter implements ItemPlotter<MBFImage,Float[],MBFImage>
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
			final LocatedObject<MBFImage> object,
			final AxesRenderer<Float[],MBFImage> renderer )
	{
		final MBFImage thumbnail = object.object.process( new ResizeProcessor( this.thumbnailSize ) );
		final Point2d p = renderer.calculatePosition( visImage, object.x, object.y );
		visImage.createRenderer().drawImage( thumbnail, (int)p.getX(), (int)p.getY() );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.ItemPlotter#renderRestarting()
	 */
	@Override
	public void renderRestarting()
	{
	}
}
