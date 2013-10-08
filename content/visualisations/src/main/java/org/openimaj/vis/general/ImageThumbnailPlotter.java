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
			final AxesRenderer2D<Float[],MBFImage> renderer )
	{
		final MBFImage thumbnail = object.object.process( new ResizeProcessor( this.thumbnailSize ) );
		final Point2d p = renderer.calculatePosition( object.x, object.y );
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
