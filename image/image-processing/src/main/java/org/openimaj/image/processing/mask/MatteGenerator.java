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
package org.openimaj.image.processing.mask;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 *	Generator for grayscale mattes.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 31 Jan 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class MatteGenerator
{
	/**
	 *	An enumerator for various matte algorithms.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 31 Jan 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public enum MatteType
	{
		/**
		 *	A basic linear vertical gradient. The argument should be boolean which, if TRUE,
		 *	determines that the gradient will be black at the top and white at the bottom.
		 *	If non-existent, or FALSE, the gradient will be white at the top and black at the bottom.
		 */
		LINEAR_VERTICAL_GRADIENT
		{
            @Override
            public void generateMatte( final FImage img, final Object... args )
            {
            	boolean whiteAtTop = false;
            	if( args.length == 0 || ((args[0] instanceof Boolean) && !((Boolean)args[0]).booleanValue()) )
            			whiteAtTop = true;

            	final double g = (whiteAtTop?1:0);
            	final double scalar = (whiteAtTop?-1d/img.getHeight():1d/img.getHeight());

            	for( int y = 0; y < img.getHeight(); y++ )
            		for( int x = 0; x < img.getWidth(); x++ )
            			img.pixels[y][x] = (float)(g + y*scalar);
            }
		},
		/**
		 *	A basic linear horizontal gradient. The argument should be boolean which, if TRUE,
		 *	determines that the gradient will be black at the left and white at the right.
		 *	If non-existent, or FALSE, the gradient will be white at the left and black at the right.
		 */
		LINEAR_HORIZONTAL_GRADIENT
		{
            @Override
            public void generateMatte( final FImage img, final Object... args )
            {
            	boolean whiteAtLeft = false;
            	if( args.length == 0 || ((args[0] instanceof Boolean) && !((Boolean)args[0]).booleanValue()) )
            			whiteAtLeft = true;

            	final double g = (whiteAtLeft?1:0);
            	final double scalar = (whiteAtLeft?-1d/img.getWidth():1d/img.getWidth());

            	for( int y = 0; y < img.getHeight(); y++ )
            		for( int x = 0; x < img.getWidth(); x++ )
            			img.pixels[y][x] = (float)(g + x*scalar);
            }
		},
		/**
		 * 	Basic radial gradient centred on the middle of the matte. The argument should be
		 * 	boolean which, if TRUE, determines whether the gradient will be black or white in
		 * 	the middle (TRUE for white).
		 */
		RADIAL_GRADIENT
		{
			@Override
            public void generateMatte( final FImage img, final Object... args )
            {
				boolean whiteInCentre = false;
				if( args.length > 0 && args[0] instanceof Boolean && ((Boolean)args[0]).booleanValue())
					whiteInCentre = true;

				// Centre coordinates
				final int cx = img.getWidth()  /2;
				final int cy = img.getHeight() /2;

				// Outside edge of radial
				final int maxDist = Math.max(
						Math.max( img.getWidth()  - cx, cx ),
						Math.max( img.getHeight() - cy, cy )
				);
				final double scale = maxDist;

				for( int y = 0; y < img.getHeight(); y++ )
					for( int x = 0; x < img.getWidth(); x++ )
						img.pixels[y][x] = whiteInCentre?
								1f-(float)this.distanceFromCentre( cx, cy, x, y, scale ):
								   (float)this.distanceFromCentre( cx, cy, x, y, scale );
            }

			/**
			 * 	Calculates the distance from the centre, scaled to the maximum distance
			 * 	with clipping bounds 0 <= d <= 1.
			 *
			 *	@param cx Centre x-coordinate
			 *	@param cy Centre y-coordinate
			 *	@param x X position of point to find distance
			 *	@param y Y position of point to find distance
			 *	@param scale maximum distance
			 *	@return Scaled, clipped distance measure
			 */
			private double distanceFromCentre( final int cx, final int cy, final int x, final int y,
                    final double scale )
            {
				final double b = cx - x;
				final double c = cy - y;
	            double v = Math.abs( Math.sqrt( b*b + c*c ) )/scale;
	            if( v > 1 ) v = 1;
	            if( v < 0 ) v = 0;
	            return v;
            }
		},
		/**
		 * 	Generates linear gradients that can be angled to any angle. The angle of the
		 * 	gradient is given in the first argument as a double. The position of the point
		 * 	at which the gradient is rotated, is given in the next two arguments, also as
		 * 	doubles. If any are missing, they are considered 0. To invert the gradient,
		 * 	invert the resulting FImage.
		 */
		ANGLED_LINEAR_GRADIENT
		{

			@Override
            public void generateMatte( final FImage img, final Object... args )
            {
				// Angle and position of gradient axis
				double angle = 0;
				double lx = 0;
				double ly = 0;

				// Get any arguments
				if( args.length > 0 && args[0] instanceof Double )
					angle = ((Double)args[0]).doubleValue();
				if( args.length > 1 && args[1] instanceof Double )
					lx = ((Double)args[1]).doubleValue();
				if( args.length > 2 && args[2] instanceof Double )
					ly = ((Double)args[2]).doubleValue();

				// Outside edge of radial
				final double scalar = Math.max(
						Math.max( img.getWidth()  - lx, lx ),
						Math.max( img.getHeight() - ly, ly )
				);

				for( int y = 0; y < img.getHeight(); y++ )
					for( int x = 0; x < img.getWidth(); x++ )
						img.pixels[y][x] = (float)this.distanceFromAxis( lx, ly, angle, x, y, scalar );
            }

			/**
			 * 	calculate the distance from the gradient axis.
			 *	@param lx A point on the gradient axis - x coordinate
			 *	@param ly A point on the gradient axis - y coordinate
			 *	@param angle The angle of the axis
			 *	@param x The x position to find the distance
			 *	@param y The y position to find the distance
			 *	@param scalar The scalar for the final vector
			 *	@return
			 */
			private double distanceFromAxis( final double lx, final double ly, final double angle,
                    final double x, final double y, final double scalar )
            {
				// See http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
				final Line2d line = Line2d.lineFromRotation( (int)lx, (int)ly, angle, 1 );
				final Point2d A = line.begin;
				final Point2d B = line.end;
				final Point2dImpl P = new Point2dImpl( (float)x, (float)y );
				final double normalLength = Math.hypot(B.getX() - A.getX(), B.getY() - A.getY());
				double grad = Math.abs((P.x - A.getX()) * (B.getY() - A.getY()) - (P.y - A.getY()) *
						(B.getX() - A.getX())) / normalLength / scalar;
				if( grad < 0 ) grad = 0;
				if( grad > 1 ) grad = 1;
				return grad;
            }
		}
		;

		/**
		 * 	Generate the matte into the given image.
		 *	@param img The image to generate the matte into
		 *	@param args The arguments for the matte generator
		 */
		public abstract void generateMatte( FImage img, Object... args );
	}

	/**
	 *	Generate a matte into the given image on the given band.
	 *
	 *	@param image The image to write the matte into
	 *	@param band The band of the image to write the matte into
	 *	@param type The type of the matte to draw
	 *	@param args The arguments for the matte generator
	 * 	@return The input image (for chaining) 
	 */
	public static FImage generateMatte( final MBFImage image, final int band,
			final MatteType type, final Object... args )
	{
		return MatteGenerator.generateMatte( image.getBand( band ), type, args );
	}

	/**
	 * 	Generate a matte into the given {@link FImage}.
	 *
	 *	@param image The image to write the matte into
	 *	@param type The type of the matte to draw
	 *	@param args The arguments for the matte generator
	 * 	@return The input image (for chaining) 
	 */
	public static FImage generateMatte( final FImage image, final MatteType type, final Object... args )
	{
		type.generateMatte( image, args );
		return image;
	}
}
