package org.openimaj.image.processing.algorithm;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 *	Implementation of the Hough Transform for lines as an image processor.
 * 	
 *  @author Jon Hare <jsh2@ecs.soton.ac.uk>
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@created 8 Aug 2011
 */
public class HoughLines implements ImageProcessor<FImage> 
{
	/** The accumulator images */
	private FImage accum = null;
	
	/** The number of segments in the accumulator space */
	private int nSegments = 360;
	
	/**
	 * 	Default constructor that creates an accumulator space for 360 degrees.
	 */
	public HoughLines()
	{
		this( 360 );
	}
	
	/**
	 * 	Constructor that creates an accumulator space for the given number
	 * 	of segments.
	 * 
	 *  @param nSegments The number of segments.
	 */
	public HoughLines( int nSegments )
	{
		this.nSegments = nSegments;
	}
	
	/**
	 *  @inheritDoc
	 *  @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image, org.openimaj.image.Image<?,?>[])
	 */
	@Override
	public void processImage(FImage image, Image<?,?>... otherimages) 
	{
		int amax = (int) round(sqrt((image.getHeight()*image.getHeight()) + (image.getWidth()*image.getWidth())));

		if (accum == null || accum.height != amax || accum.width != nSegments )
				accum = new FImage( nSegments, amax );
		else	accum.zero();
		
		for( int y = 0; y < image.getHeight(); y++ ) 
		{
			for( int x = 0; x < image.getWidth(); x++ ) 
			{
				if( image.getPixel(x,y) == 0 ) 
				{
					for( int m = 0; m < nSegments; m++ ) 
					{
						double mm = m * 360d/nSegments;
						int a = (int) round(
								x * cos(mm*PI / 180d) + 
								y * sin(mm*PI / 180d) );
						
						if( a < amax && a >= 0) 
							accum.pixels[a][m]++;
					}
				}
			}
		}
	}
	
	/**
	 * 	Returns the accumulator space.
	 *  @return The accumulator space {@link FImage}
	 */
	public FImage getAccumulator()
	{
		return accum;
	}
	
	/**
	 * 	Returns the top line in the accumulator space. 
	 * 	The end points of the line will have x coordinates at -2000 and 2000.
	 * 
	 *  @return The strongest line in the accumulator space
	 */
	public Line2d getBestLine()
	{
		FValuePixel p = accum.maxPixel();
		
		// Remember accumulator space is r,theta
		int theta = p.x;
		int dist  = p.y;
	
		return getLineFromParams( theta, dist, -2000, 2000 );
	}
	
	/**
	 * 	Returns the top n lines from the accumulator space.
	 * 	The end points of the lines will have x coordinates at -2000 and 2000.
	 * 
	 *  @param n The number of lines to return
	 *  @return A list of lines
	 */
	public List<Line2d> getBestLines( int n )
	{
		FImage accum2 = accum.clone();
		
		List<Line2d> lines = new ArrayList<Line2d>();
		for( int i = 0; i < n; i++ )
		{
			FValuePixel p = accum2.maxPixel();
			lines.add( getLineFromParams( p.x, p.y, -2000, 2000 ) );
			accum2.setPixel( p.x, p.y, 0f );
		}
		
		return lines;
	}
	
	/**
	 * 	From a r,theta parameterisation of a line, this returns a {@link Line2d}
	 * 	with endpoints at the given x coordinates. 	
	 * 
	 *  @param theta The angle of the line
	 *  @param dist The distance from the origin
	 *  @param x1 The x-coordinate of the start of the line
	 *  @param x2 The y-coordimate of the start of the line
	 *  @return A {@link Line2d}
	 */
	public Line2d getLineFromParams( int theta, int dist, int x1, int x2 )
	{
		double t = theta * (360d/nSegments) * Math.PI/180d; 
		return new Line2d( 
				new Point2dImpl(
					x1, (float)(x1*(-Math.cos(t)/Math.sin(t)) + (dist/Math.sin(t)) ) ),
				new Point2dImpl(
					x2, (float)(x2*(-Math.cos(t)/Math.sin(t)) + (dist/Math.sin(t)) )
				) );		
	}
}
