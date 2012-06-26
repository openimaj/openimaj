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
package org.openimaj.image.analysis.algorithm;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 *	Implementation of the Hough Transform for lines as an {@link ImageAnalyser}.
 *	The input image should have the lines to detect zeroed in the image (black).
 *	All other values will be ignored. That means you usually need to invert 
 *	images created with edge detectors.
 *	<p><pre>
 *	{@code
 *		CannyEdgeDetector2 ced = new CannyEdgeDetector2();
 *		FImage i = ced.process( ImageUtilities.readF( new File( 'test.jpg' ) );
 *		
 *		HoughLines hl = new HoughLines();
 *		i.inverse().analyse( hl );
 *		double d = hl.calculatePrevailingAngle();
 *	}
 *	</pre>
 *	<p>
 *	The analyser is iterable over the lines that are found within the
 *	accumulator space. Iterated lines will be returned in strength order.
 *	Once an iterator has been created, the object contains a copy of the 
 *	accumulator space until {@link #clearIterator()} is called.
 *	You can use the Java 5 for construct:
 *	<pre>
 *		int maxLines = 20;
 *		for( Line2d line: hl ) {
 *			System.out.println( "Line: "+line );
 *			if( --maxLines == 0 )
 *				break;
 *		}
 *		hl.clearIterator();
 *	</pre>
 *	<p>
 *	To convert a bin into a degree, use bin*360d/{@link #getNumberOfSegments()}.
 *	To convert a degree into a bin, use degree/360d/{@link #getNumberOfSegments()}.
 * 	
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@created 8 Aug 2011
 */
public class HoughLines implements 
	ImageAnalyser<FImage>, 
	Iterable<Line2d>,
	Iterator<Line2d>
{
	/** The accumulator images */
	private FImage accum = null;
	
	/** The number of segments in the accumulator space */
	private int numberOfSegments = 360;

	/** 
	 * 	When the iterator is being used, this contains the accumulator
	 * 	that is being iterated over. This accumulator will have the lines
	 * 	that have already been returned via the iterator set to zero.
	 */
	private FImage iteratorAccum = null;
	
	/** The current accumulator pixel (line) in the iterator */ 
	private FValuePixel iteratorCurrentPix = null;

	private float onValue;
	
	/**
	 * 	Default constructor that creates an accumulator space for 360 degrees with a "on value" of 0.0f
	 */
	public HoughLines()
	{
		this( 360 , 0f);
	}
	
	/**
	 * 	Constructor that creates a default accumulator space with 
	 * 	a specified value for pixels considered to be "on"
	 * @param onValue value of pixels considered on
	 */
	public HoughLines(float onValue)
	{
		this( 360 , onValue);
	}
	
	/**
	 * 	Constructor that creates an accumulator space for the given number
	 * 	of segments.
	 * 
	 *  @param nSegments The number of segments.
	 * @param onValue value of pixels considered on
	 */
	public HoughLines( int nSegments , float onValue)
	{
		this.setNumberOfSegments( nSegments );
		this.onValue = onValue;
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) 
	{
		int amax = (int) round(sqrt((image.getHeight()*image.getHeight()) + (image.getWidth()*image.getWidth())));

		if( accum == null || 
			accum.height != amax || 
			accum.width != getNumberOfSegments() )
				accum = new FImage( getNumberOfSegments(), amax );
		else	accum.zero();
		
		for( int y = 0; y < image.getHeight(); y++ ) 
		{
			for( int x = 0; x < image.getWidth(); x++ ) 
			{
				if( image.getPixel(x,y) == onValue ) 
				{
					for( int m = 0; m < getNumberOfSegments(); m++ ) 
					{
//						double mm = PI*m*360d/getNumberOfSegments()/180d;
						double mm = ((double)m / (double)getNumberOfSegments()) * (2 * PI);
						int a = (int) round( x * cos(mm) +	y * sin(mm) );
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
	 * 	Calculates a projection across the accumulator space. 
	 * 	Returns an image that has width {@link #getNumberOfSegments()}
	 * 	and height of 1. Effectively sums across the distances from origin
	 * 	in the space such that you end up with a representation that gives you
	 * 	the strength of the angles in the image irrespective of where those
	 * 	lines occur. 
	 * 
	 *	@return A horizontal projection on the accumulator space as an
	 *		FImage with dimensions {@link #getNumberOfSegments()} x 1 
	 */
	public FImage calculateHorizontalProjection()
	{
		return calculateHorizontalProjection( accum );
	}
		
	/**
	 * 	Calculates a projection across the given accumulator space. 
	 *  Returns an image that has width the same as the input
	 * 	and height of 1. Effectively sums across the distances from origin
	 * 	in the space such that you end up with a representation that gives you
	 * 	the strength of the angles in the image irrespective of where those
	 * 	lines occur. 
	 *
	 * 	@param accum The accumulator space to project
	 *	@return A horizontal projection on the accumulator space as an
	 *		FImage with same width as input image but only 1 pixel high
	 */
	public FImage calculateHorizontalProjection( FImage accum )
	{
		FImage proj = new FImage( accum.getWidth(), 1 );
		
		for( int x = 0; x < accum.getWidth(); x++ )
		{
			float acc = 0;
			for( int y = 0; y < accum.getHeight(); y++ )
				acc += accum.getPixel(x,y)*accum.getPixel(x,y);
			proj.setPixel(x,0, (float)Math.sqrt(acc) );
		}

		return proj;
	}
	
	/**
	 * 	Returns the most frequent angle that occurs within the accumulator
	 * 	space by calculating a horizontal projection over the accumulator
	 * 	space and returning the angle with the most votes. The prevailing
	 * 	angle is given in degrees. If it is less than zero, then no angle
	 * 	could be extracted (there was no local maxima in the accumulator).
	 * 
	 *	@return The prevailing angle (degrees) in the accumulator space; or 
	 *		Double.MIN_VALUE if the value cannot be calculated
	 */
	public double calculatePrevailingAngle()
	{
		return calculatePrevailingAngle( accum, 0, 360 );
	}
	
	/**
	 * 	Returns the most frequent angle that occurs within the given accumulator
	 * 	space by calculating a horizontal projection over the accumulator
	 * 	space and returning the angle with the most votes. The prevailing
	 * 	angle is given in degrees. If it is less than zero, then no angle
	 * 	could be extracted (there was no local maxima in the accumulator).
	 *
	 * 	@param accum The accumulator space to use
	 * 	@param offset The offset into the accumulator of the 0 degree bin
	 * 	@param nDegrees The number of degrees covered by the accumulator space
	 *	@return The prevailing angle (degrees) in the accumulator space; or 
	 *		Double.MIN_VALUE if there is no prevailing angle
	 */
	public double calculatePrevailingAngle( FImage accum, int offset, double nDegrees )
	{
		FValuePixel maxpix = calculateHorizontalProjection(accum).maxPixel();
		if( maxpix.x == -1 && maxpix.y == -1 )
			return Double.MIN_VALUE;
		return (maxpix.x+offset) *
		   (nDegrees/accum.getWidth());
	}
	
	/**
	 * 	Returns the most frequent angle that occurs within the given accumulator
	 * 	space with a given range of angles (specified in degrees)
	 * 	by calculating a horizontal projection over the given accumulator
	 * 	space and returning the angle with the most votes. The prevailing
	 * 	angle is given in degrees. If it is less than zero, then no angle
	 * 	could be extracted (there was no local maxima in the accumulator).
	 * 
	 *	@param minTheta The minimum angle (degrees)
	 *	@param maxTheta The maximum angle (degrees) 
	 *	@return The prevailing angle within the given range; or Double.MIN_VALUE
	 *		if the value cannot be calculated
	 */
	public double calculatePrevailingAngle( float minTheta, float maxTheta )
	{
		// Swap if some numpty puts (50,40)
		if( minTheta > maxTheta )
		{
			float tmp = minTheta;
			minTheta = maxTheta;
			maxTheta = tmp;
		}
			
		if( minTheta >= 0 )
		{
			int mt = (int)(minTheta / (360d/getNumberOfSegments()));
			int xt = (int)(maxTheta / (360d/getNumberOfSegments()));
			FImage f = accum.extractROI( mt, 0, xt-mt, accum.getHeight() );
			return calculatePrevailingAngle( f, mt, (xt-mt)*(360d/getNumberOfSegments()) );
		}
		else
		{
			// If minTheta < maxTheta, the assumption is that someone has
			// entered something like (-10,10) - between -10 and +10 degrees.
			
			// Create an accumulator space that's shifted left by the right number of bins
			int mt = (int)(minTheta / (360d/getNumberOfSegments()));
			int xt = (int)(maxTheta / (360d/getNumberOfSegments()));
			FImage a = accum.shiftRight( -mt ).extractROI(0,0,(xt-mt),accum.getHeight());
			return calculatePrevailingAngle( a, mt, (xt-mt)*(360d/getNumberOfSegments()) );
		}
	}
	
	/**
	 * 	Returns the top line in the accumulator space. 
	 * 	The end points of the line will have x coordinates at -2000 and 2000.
	 * 
	 *  @return The strongest line in the accumulator space
	 */
	public Line2d getBestLine()
	{
		return getBestLine( accum, 0 );
	}
	
	/**
	 * 	Returns the top line in the given accumulator space. 
	 * 	The end points of the line will have x coordinates at -2000 and 2000.
	 * 
	 * 	@param accumulatorSpace The accumulator space to look within
	 * 	@param offset The number of bins offset from zero degrees
	 *  @return The strongest line in the accumulator space
	 */
	public Line2d getBestLine( FImage accumulatorSpace, int offset )
	{
		FValuePixel p = accumulatorSpace.maxPixel();
		
		// Remember accumulator space is r,theta
		int theta = p.x + offset;
		int dist  = p.y;
	
		return getLineFromParams( theta, dist, -2000, 2000 );		
	}
	
	/**
	 * 	Returns the best line within a certain angular range. Angles
	 * 	should be provided in degrees (0..359). If the best line has
	 * 	the maximum or minimum angles it will be returned.
	 * 
	 *	@param minTheta Minimum angle of the best line
	 *	@param maxTheta Maximum angle of the best line
	 *	@return The best line that has an angle within the given range.
	 */
	public Line2d getBestLine( float minTheta, float maxTheta )
	{
		// Swap if some numpty puts (50,40)
		if( minTheta > maxTheta )
		{
			float tmp = minTheta;
			minTheta = maxTheta;
			maxTheta = tmp;
		}
			
		if( minTheta >= 0)
		{
			int mt = (int)(minTheta / (360d/getNumberOfSegments()));
			int xt = (int)(maxTheta / (360d/getNumberOfSegments()));
			FImage f = accum.extractROI( mt, 0, xt-mt, accum.getHeight() );
			return getBestLine( f, mt );
		}
		else
		{
			// If minTheta < maxTheta, the assumption is that someone has
			// entered something like (-10,10) - between -10 and +10 degrees.
			
			// Create an accumulator space that's shifted left by the right number of bins
			int mt = (int)(minTheta / (360d/getNumberOfSegments()));
			int xt = (int)(maxTheta / (360d/getNumberOfSegments()));
			FImage a = accum.shiftRight( -mt ).extractROI(0,0,(xt-mt),accum.getHeight());
			return getBestLine( a, mt );
		}			
	}
	
	/**
	 * 	Returns the top n lines from the given accumulator space within the range.
	 * 	The end points of the lines will have x coordinates at -2000 and 2000.
	 * 
	 *  @param n The number of lines to return
	 *  @param minTheta The minimum angle (degrees)
	 *  @param maxTheta The maximum angle (degrees)
	 *  @return A list of lines
	 */
	public List<Line2d> getBestLines( int n, float minTheta, float maxTheta )
	{
		// Swap if some numpty puts (50,40)
		if( minTheta > maxTheta )
		{
			float tmp = minTheta;
			minTheta = maxTheta;
			maxTheta = tmp;
		}
			
		if( minTheta >= 0)
		{
			int mt = (int)(minTheta / (360d/getNumberOfSegments()));
			int xt = (int)(maxTheta / (360d/getNumberOfSegments()));
			FImage f = accum.extractROI( mt, 0, xt-mt, accum.getHeight() );
			return getBestLines( n, f, mt );
		}
		else
		{
			// If minTheta < maxTheta, the assumption is that someone has
			// entered something like (-10,10) - between -10 and +10 degrees.
			
			// Create an accumulator space that's shifted left by the right number of bins
			int mt = (int)(minTheta / (360d/getNumberOfSegments()));
			int xt = (int)(maxTheta / (360d/getNumberOfSegments()));
			FImage a = accum.shiftRight( -mt ).extractROI(0,0,(xt-mt),accum.getHeight());
			return getBestLines( n, a, mt );
		}
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
		return getBestLines( n, accum, 0 );
	}
	
	/**
	 * 	Returns the top n lines from the given accumulator space.
	 * 	The end points of the lines will have x coordinates at -2000 and 2000.
	 * 
	 *  @param n The number of lines to return
	 *  @param accumulatorSpace The space to look within
	 *  @param offset The offset into the accumulator of 0 in this space
	 *  @return A list of lines
	 */
	public List<Line2d> getBestLines( int n, FImage accumulatorSpace, int offset )
	{
		FImage accum2 = accumulatorSpace.clone();
		List<Line2d> lines = new ArrayList<Line2d>();
		for( int i = 0; i < n; i++ )
		{
			FValuePixel p = accum2.maxPixel();
			lines.add( getLineFromParams( p.x+offset, p.y, -2000, 2000 ) );
			accum2.setPixel( p.x, p.y, 0f );
		}
		
		return lines;
	}
	
	/**
	 * 	From a r,theta parameterisation of a line, this returns a {@link Line2d}
	 * 	with endpoints at the given x coordinates. If theta is 0 this will return
	 * 	a vertical line between -2000 and 2000 with the x-coordinate the appopriate
	 * 	distance from the origin. 	
	 * 
	 *  @param theta The angle bin in which the line lies (x in the accumulator space)
	 *  @param dist The distance bin in which the line lies (y in the accumulator space)
	 *  @param x1 The x-coordinate of the start of the line
	 *  @param x2 The x-coordinate of the end of the line
	 *  @return A {@link Line2d}
	 */
	public Line2d getLineFromParams( int theta, int dist, int x1, int x2 )
	{
		if( theta == 0 )
		{
			return new Line2d(
					new Point2dImpl( dist, -2000 ),
					new Point2dImpl( dist, 2000 )
			);
		}
		
		double t = theta * (360d/getNumberOfSegments()) * Math.PI/180d; 
		return new Line2d( 
				new Point2dImpl(
					x1, (float)(x1*(-Math.cos(t)/Math.sin(t)) + (dist/Math.sin(t)) ) ),
				new Point2dImpl(
					x2, (float)(x2*(-Math.cos(t)/Math.sin(t)) + (dist/Math.sin(t)) )
				) );		
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Line2d> iterator()
	{
		clearIterator();
		checkIteratorSetup();
		return this;
	}

	/**
	 * 	Used to clone the accumulator space when an iterator
	 * 	function is used.
	 */
	private void checkIteratorSetup()
	{
		if( iteratorAccum == null )
			iteratorAccum = accum.clone();
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return iteratorAccum.maxPixel().value > 0f;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.util.Iterator#next()
	 */
	@Override
	public Line2d next()
	{
		iteratorCurrentPix = iteratorAccum.maxPixel();
		Line2d l = getBestLine( iteratorAccum, 0 );
		iteratorAccum.setPixel( iteratorCurrentPix.x, iteratorCurrentPix.y, 0f );
		return l;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		iteratorAccum.setPixel( iteratorCurrentPix.x, iteratorCurrentPix.y, 0f );
	}
	
	/**
	 *	Remove the temporary objects created during iteration. 
	 */
	public void clearIterator()
	{
		this.iteratorAccum = null;
		this.iteratorCurrentPix = null;
	}

	/**
	 * 	Set the number of segments used in the accumulator space. By default
	 * 	this value is 360 (one accumulator bin per degree). However, if you
	 * 	require greater accuracy then this can be changed. It is suggested
	 * 	that it is a round multiple of 360.
	 * 
	 *	@param numberOfSegments Set the number of directional bins in 
	 *		the accumulator space
	 */
	public void setNumberOfSegments( int numberOfSegments )
	{
		this.numberOfSegments = numberOfSegments;
	}

	/**
	 * 	Get the number of directional accumulator bins.
	 * 
	 *	@return the number of directional bins in the accumulator space.
	 */
	public int getNumberOfSegments()
	{
		return numberOfSegments;
	}
}
