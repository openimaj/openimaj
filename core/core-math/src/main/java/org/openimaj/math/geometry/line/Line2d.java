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
package org.openimaj.math.geometry.line;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * A line in two-dimensional space.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 * @author David Dupplaw <dpd@ecs.soton.ac.uk>
 */
public class Line2d {
	/**
	 * Start point of line 
	 */
	public Point2d begin;
	
	/**
	 * End point of line 
	 */
	public Point2d end;
	
	/**
	 * Construct a line 
	 */
	public Line2d() {
	}
	
	/**
	 * Construct a line
	 * 
	 * @param begin start point
	 * @param end end point
	 */
	public Line2d(Point2d begin, Point2d end) {
		this.begin = begin;
		this.end = end;
	}
	
	/**
	 * Set the start point
	 * @param begin start point
	 */
	public void setBeginPoint(Point2d begin) {
		this.begin = begin;
	}
	
	/**
	 * Set the end point
	 * @param end end point
	 */
	public void setEndPoint(Point2d end) {
		this.end = end;
	}
	
	/**
	 * Get the start point
	 * @return the start point
	 */
	public Point2d getBeginPoint() {
		return begin;
	}
	
	/**
	 * Get the end point
	 * @return the end point
	 */
	public Point2d setEndPoint() {
		return end;
	}
	
	/**
	 * 	The type of a result of a line intersection
	 * 
	 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
	 */
	static public enum IntersectionType
	{
		/**
		 * Intersecting line (lines that cross)
		 */
		INTERSECTING, 
		/**
		 * Parallel line
		 */
		PARALLEL, 
		/**
		 * Co-incident line (on top of each other)
		 */
		COINCIDENT, 
		/**
		 * non-intersecting line
		 */
		NOT_INTERESECTING
	}
	
	/**
	 * 	The result of a line intersection.
	 * 
	 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
	 */
	static public class IntersectionResult
	{
		/**
		 * The type of intersection 
		 */
		public IntersectionType type;
		
		/**
		 * The point at which the lines intersect (if the type is INTERSECTING)
		 */
		public Point2d intersectionPoint;
		
		/**
		 * Construct the IntersectionResult with the given type
		 * @param type the type
		 */
		public IntersectionResult( IntersectionType type ) { this.type = type; }
		
		/**
		 * Construct the IntersectionResult with the given intersection point
		 * @param point the intersection point
		 */
		public IntersectionResult( Point2d point ) { this.type = IntersectionType.INTERSECTING; this.intersectionPoint = point; }
	}
	
	/**
	 * 	Calculates the intersection point of this line and another line
	 *
	 * 	@param otherLine The other line segment
	 *  @return a {@link IntersectionResult}
	 *  @see "http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/"
	 */
	public IntersectionResult getIntersection( Line2d otherLine ) {
        double denom = ((otherLine.end.getY() - otherLine.begin.getY())*(end.getX() - begin.getX())) -
                      ((otherLine.end.getX() - otherLine.begin.getX())*(end.getY() - begin.getY()));

        double numea = ((otherLine.end.getX() - otherLine.begin.getX())*(begin.getY() - otherLine.begin.getY())) -
         			  ((otherLine.end.getY() - otherLine.begin.getY())*(begin.getX() - otherLine.begin.getX()));

        double numeb = ((end.getX() - begin.getX())*(begin.getY() - otherLine.begin.getY())) -
        			  ((end.getY() - begin.getY())*(begin.getX() - otherLine.begin.getX()));

        if( denom == 0.0 )
        {
        	if( numea == 0.0 && numeb == 0.0 )
        	{
        		return new IntersectionResult( IntersectionType.COINCIDENT );
        	}
        	
        	return new IntersectionResult( IntersectionType.PARALLEL );
        }

        double ua = numea / denom;
        double ub = numeb / denom;

        if( ua >= 0.0 && ua <= 1.0 && ub >= 0.0 && ub <= 1.0 )
        {
        	// Get the intersection point.
        	double intX = begin.getX() + ua*(end.getX() - begin.getX() );
        	double intY = begin.getY() + ua*(end.getY() - begin.getY() );

        	return new IntersectionResult( new Point2dImpl( (float)intX, (float)intY ) );
        }

        return new IntersectionResult( IntersectionType.NOT_INTERESECTING );
	}
	
	/**
	 * 	Reflects a point around a this line.
	 * 
	 *  @param pointToReflect The point to reflect
	 *  @return The reflected point
	 *  
	 *  @see "http://algorithmist.wordpress.com/2009/09/15/reflecting-a-point-about-a-line/"
	 */
	public Point2d reflectAroundLine( Point2d pointToReflect ) {
		double nx = end.getX() - begin.getX();
		double ny = end.getY() - begin.getY();
		double d  = Math.sqrt(nx*nx + ny*ny);
		nx /= d;
		ny /= d;

		double px = pointToReflect.getX() - begin.getX();
		double py = pointToReflect.getY() - begin.getY();
		double w  = nx*px + ny*py;
		double rx = 2*begin.getX() - pointToReflect.getX() + 2*w*nx;
		double ry = 2*begin.getY() - pointToReflect.getY() + 2*w*ny;
		return new Point2dImpl( (float)rx, (float)ry );		
	}
}
