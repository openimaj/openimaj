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
package org.openimaj.math.geometry.shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.util.PolygonUtils;

import Jama.Matrix;

/**
 * A polygon, modelled as a list of vertices.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class Polygon implements Shape, Iterable<Point2d> 
{
	private static final long serialVersionUID = 1L;

	public List<Point2d> vertices = new ArrayList<Point2d>();

	/**
	 * Polygons can contain other polygons. If the polygon is
	 * representing a shape, then the inner polygons can represent
	 * holes in the polygon or other polygons within the polygon.
	 */
	private List<Polygon> innerPolygons = new ArrayList<Polygon>();
	
	/** If this polygon is a hole within another polygon, this is set to true */
	private boolean isHole = false;

	/**
	 * 	Constructs an empty polygon to which vertices may be added.
	 */
	public Polygon()
	{
		this( false );
	}
	
	/**
	 * 	Constructs an empty polygon to which vertices may be added.
	 * 	The boolean parameter determines whether this polygon will
	 * 	represent a hole (rather than a solid).
	 * 
	 *	@param representsHole Whether the polygon will represent a hole.
	 */
	public Polygon( boolean representsHole )
	{
		this.isHole = representsHole;
	}
	
	/**
	 * Construct a Polygon from vertices
	 * @param vertices the vertices
	 */
	public Polygon(Point2d... vertices) {
		for (Point2d v : vertices)
			this.vertices.add(v);
	}

	/**
	 * Construct a Polygon from vertices
	 * @param vertices the vertices
	 */
	public Polygon(Collection<? extends Point2d> vertices) {
		this( vertices, false );
	}

	/**
	 * Construct a Polygon from the vertices, possibly 
	 * copying the vertices first
	 * @param vertices the vertices
	 * @param copy should the vertices be copied
	 */
	public Polygon( Collection<? extends Point2d> vertices, boolean copy ) {
		if( !copy )
			this.vertices.addAll( vertices );
		else
		{
			for( Point2d p : vertices )
				this.vertices.add( new Point2dImpl( p.getX(), p.getY() ) );
		}
	}

	void rotate(Point2d point, Point2d origin, double angle) {
		double X = origin.getX() + ((point.getX() - origin.getX()) * Math.cos(angle) -
				(point.getY() - origin.getY()) * Math.sin(angle));

		double Y = origin.getY() + ((point.getX() - origin.getX()) * Math.sin(angle) +
				(point.getY() - origin.getY()) * Math.cos(angle));

		point.setX((float) X);
		point.setY((float) Y);
	}

	/**
	 * Rotate the polygon about the given origin with the given angle (in radians)
	 * @param origin the origin of the rotation
	 * @param angle the angle in radians
	 */
	public void rotate(Point2d origin, double angle) {
		for (Point2d p : vertices)
			rotate(p, origin, angle);
	}

	/**
	 * Rotate the polygon about (0,0) with the given angle (in radians)
	 * @param angle the angle in radians
	 */
	public void rotate(double angle) {
		this.rotate( new Point2dImpl(0,0), angle );
	}

	/**
	 * Get the vertices of the polygon
	 * @return the vertices
	 */
	public List<Point2d> getVertices() {
		return vertices;
	}

	/**
	 * Get the number of vertices
	 * @return the number of vertices
	 */
	public int nVertices() {
		if (isClosed())
			return vertices.size() - 1;
		return vertices.size();
	}

	/**
	 * Is the polygon closed (i.e. is the last vertex equal to the first)?
	 * @return true if closed; false if open
	 */
	public boolean isClosed() {
		if (vertices.size() > 0 && vertices.get(0) == vertices.get(vertices.size()-1))
			return true;
		return false;
	}

	/**
	 * Close the polygon if it's not already closed
	 */
	public void close() {
		if (!isClosed())
			vertices.add(vertices.get(0));
	}

	/**
	 * Open the polygon if it's closed
	 */
	public void open() {
		if (isClosed())
			vertices.remove(vertices.get(vertices.size()-1));
	}

	/**
	 * Test whether the point p is inside the polygon using the winding rule 
	 * algorithm. Also tests whether the point is in any of the inner polygons.
	 * If the inner polygon represents a hole and the point is within that
	 * polygon then the point is not within this polygon.
	 * 
	 * @param point the point to test
	 * @return true if the point is inside; false otherwise
	 */
	@Override
	public boolean isInside(Point2d point) {
		boolean isClosed = isClosed();
		if (!isClosed) close();

		boolean isOdd=false;

		for( int pp = 0; pp < getNumInnerPoly(); pp++ )
		{
			List<Point2d> v = getInnerPoly( pp ).getVertices();
			int j = v.size()-1 ;
			for (int i=0; i < v.size(); i++) {
				if (v.get(i).getY() < point.getY() && v.get(j).getY() >= point.getY() || 
					v.get(j).getY() < point.getY() && v.get(i).getY() >= point.getY()) {
					if (v.get(i).getX() + (point.getY()-v.get(i).getY()) / 
						(v.get(j).getY()-v.get(i).getY())*(v.get(j).getX()-v.get(i).getX()) < point.getX()) {
						isOdd=!isOdd; 
					}
				}
				j=i; 
			}
		}

		if (!isClosed) open();		
		return isOdd;
	}

	/**
	 * Compute the regular (oriented to the axes) bounding box
	 * of the polygon.
	 * 
	 * @return the regular bounding box as [x,y,width,height]
	 */
	@Override
	public Rectangle calculateRegularBoundingBox() {
		int xmin=Integer.MAX_VALUE, xmax=0, ymin=Integer.MAX_VALUE, ymax=0;

		for( int pp = 0; pp < getNumInnerPoly(); pp++ )
		{
			Polygon ppp = getInnerPoly( pp );
			for (Point2d p : ppp.getVertices() ) {
				if (p.getX() < xmin) xmin = (int) Math.floor(p.getX());
				if (p.getX() > xmax) xmax = (int) Math.ceil(p.getX());
				if (p.getY() < ymin) ymin = (int) Math.floor(p.getY());
				if (p.getY() > ymax) ymax = (int) Math.ceil(p.getY());
			}
		}

		return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
	}

	/**
	 * Translate the polygons position
	 *  
	 * @param x x-translation
	 * @param y y-translation
	 */
	@Override
	public void translate(float x, float y) {
		for( int pp = 0; pp < getNumInnerPoly(); pp++ )
		{
			Polygon ppp = getInnerPoly( pp );
			for (Point2d p : ppp.getVertices() ) {
				p.setX(p.getX() + x);
				p.setY(p.getY() + y);
			}
		}
	}

	/**
	 *	@inheritDoc
	 * 	@see java.lang.Object#clone()
	 */
	@Override
	public Polygon clone() {
		Polygon clone = new Polygon();
		clone.setIsHole( isHole );

		for (Point2d p : vertices)
			clone.vertices.add(p.copy());

		for( Polygon innerPoly: innerPolygons )
			clone.addInnerPolygon( innerPoly.clone() );
		
		return clone;
	}

	/**
	 * Scale the polygon by the given amount about (0,0). Scalefactors
	 * between 0 and 1 shrink the polygon. 
	 * @param sc the scale factor.
	 */
	@Override
	public void scale(float sc) {
		for( int pp = 0; pp < getNumInnerPoly(); pp++ )
		{
			Polygon ppp = getInnerPoly( pp );
			for (Point2d p : ppp.getVertices() ) {
				p.setX(p.getX() * sc);
				p.setY(p.getY() * sc);
			}
		}
	}
	
	/**
	 * 	Scale the polygon only in the x-direction by the given amount about
	 * 	(0,0). Scale factors between 0 and 1 will shrink the polygon 
	 *	@param sc The scale factor
	 */
	public Polygon scaleX( float sc )
	{
		for( int pp = 0; pp < getNumInnerPoly(); pp++ )
		{
			Polygon ppp = getInnerPoly( pp );
			for (Point2d p : ppp.getVertices() ) {
				p.setX(p.getX() * sc);
			}
		}
		return this;
	}

	/**
	 * 	Scale the polygon only in the y-direction by the given amount about
	 * 	(0,0). Scale factors between 0 and 1 will shrink the polygon 
	 *	@param sc The scale factor
	 */
	public Polygon scaleY( float sc )
	{
		for( int pp = 0; pp < getNumInnerPoly(); pp++ )
		{
			Polygon ppp = getInnerPoly( pp );
			for (Point2d p : ppp.getVertices() ) {
				p.setY(p.getY() * sc);
			}
		}
		return this;
	}
	
	/**
	 * Scale the polygon by the given amount about (0,0). Scale factors
	 * between 0 and 1 shrink the polygon. 
	 * @param sc the scale factor.
	 */
	public Polygon scaleXY( float scx, float scy )
	{
		for( int pp = 0; pp < getNumInnerPoly(); pp++ )
		{
			Polygon ppp = getInnerPoly( pp );
			for (Point2d p : ppp.getVertices() ) {
				p.setX(p.getX() * scx);
				p.setY(p.getY() * scy);
			}
		}		
		return this;
	}

	/**
	 * Scale the polygon by the given amount about the given point. 
	 * Scalefactors between 0 and 1 shrink the polygon.
	 * @param centre the centre of the scaling operation
	 * @param sc the scale factor
	 */
	@Override
	public void scale(Point2d centre, float sc) {
		this.translate( -centre.getX(), -centre.getY() );
		for( int pp = 0; pp < getNumInnerPoly(); pp++ )
		{
			Polygon ppp = getInnerPoly( pp );
			for (Point2d p : ppp.getVertices() ) {
				p.setX(p.getX() * sc);
				p.setY(p.getY() * sc);
			}
		}
		this.translate( centre.getX(), centre.getY() );
	}

	/**
	 * Scale the polygon about its centre of gravity.
	 * Scalefactors between 0 and 1 shrink the polygon.
	 * @param sc the scale factor
	 */
	@Override
	public void scaleCOG( float sc )
	{
		Point2d cog = getCOG();
		this.translate( -cog.getX(), -cog.getY() );
		this.scale( sc );
		this.translate( cog.getX(), cog.getY() );
	}

	/**
	 * Get the centre of gravity of the polygon
	 * @return the centre of gravity of the polygon
	 */
	@Override
	public Point2d getCOG()
	{
		float xSum = 0;
		float ySum = 0;

		int n = 0;
		for( int pp = 0; pp < getNumInnerPoly(); pp++ )
		{
			Polygon ppp = getInnerPoly( pp );
			for (Point2d p : ppp.getVertices() ) {
				xSum += p.getX();
				ySum += p.getY();
				n++;
			}
		}

		xSum /= n;
		ySum /= n;

		return new Point2dImpl( xSum, ySum );
	}

	/**
	 * 	Calculates the difference between two polygons
	 * 	and returns a new polygon. It assumes that the given
	 * 	polygon and this polygon have the same number of
	 * 	vertices.
	 *  @param p the polygon to subtract.
	 *  @return the difference polygon 
	 */
	public Polygon difference( Polygon p )
	{
		List<Point2d> v = new ArrayList<Point2d>();

		for( int i = 0; i < vertices.size(); i++ )
			v.add( new Point2dImpl( 
					vertices.get(i).getX() - p.getVertices().get(i).getX(),
					vertices.get(i).getY() - p.getVertices().get(i).getY() ) );
		
		Polygon p2 = new Polygon( v );
		for( int i = 0; i < innerPolygons.size(); i++ )
			p2.addInnerPolygon( innerPolygons.get(i).difference( 
					p2.getInnerPoly( i+1 ) ) );
		
		return p2;
	}

	/**
	 * Calculate the area of the polygon. This does not take into account
	 * holes in the polygon.
	 * @return the area of the polygon
	 */
	@Override
	public double calculateArea() {
		boolean closed = isClosed();
		double area = 0;

		if (!closed) close();

		// TODO: This does not take into account the winding 
		// rule and therefore holes
		for (int k=0; k<vertices.size()-1; k++) {
			float ik = vertices.get(k).getX();
			float jk = vertices.get(k).getY();
			float ik1 = vertices.get(k+1).getX();
			float jk1 = vertices.get(k+1).getY();

			area += ik*jk1 - ik1*jk;
		}

		if (!closed) open();

		return 0.5 * Math.abs(area);
	}

	/**
	 * @return the minimum x-ordinate of all vertices
	 */
	@Override
	public double minX() { return calculateRegularBoundingBox().x; }

	/**
	 * @return the minimum y-ordinate of all vertices
	 */
	@Override
	public double minY() { return calculateRegularBoundingBox().y; }

	/**
	 * @return the maximum x-ordinate of all vertices
	 */
	@Override
	public double maxX() { Rectangle r = calculateRegularBoundingBox(); return r.x+r.width; }

	/**
	 * @return the maximum y-ordinate of all vertices
	 */
	@Override
	public double maxY() { Rectangle r = calculateRegularBoundingBox(); return r.y+r.height; }

	/**
	 * @return the width of the regular bounding box 
	 */
	@Override
	public double getWidth() { return maxX()-minX(); }

	/**
	 * @return the height of the regular bounding box
	 */
	@Override
	public double getHeight() { return maxY()-minY(); }

	/**
	 * Apply a 3x3 transform matrix to a copy of the polygon
	 * and return it
	 * @param transform 3x3 transform matrix
	 * @return the transformed polygon
	 */
	@Override
	public Polygon transform(Matrix transform) {
		List<Point2d> newVertices = new ArrayList<Point2d>();

		for (Point2d p : vertices) {
			Matrix p1 = new Matrix(3,1);
			p1.set(0,0, p.getX());
			p1.set(1,0, p.getY());
			p1.set(2,0, 1);

			Matrix p2_est = transform.times(p1);

			Point2d out = new Point2dImpl((float)(p2_est.get(0,0) / p2_est.get(2, 0)), (float)(p2_est.get(1,0) / p2_est.get(2, 0)));

			newVertices.add(out);
		}

		Polygon p = new Polygon(newVertices);
		for( Polygon pp : innerPolygons )
			p.addInnerPolygon( pp.transform( transform ) );
		return p;
	}
	
	/**
	 * Calls {@link Polygon#intersectionArea(Polygon, double)} with 1 step per pixel dimension. Subsequently this 
	 * function returns the shared whole pixels of this polygon and that.
	 * @param that
	 * @return intersection area
	 */
	@Override
	public double intersectionArea(Shape that){
		return this.intersectionArea(that,1);
	}
	/**
	 * Return an estimate for the area of the intersection of this polygon and another polygon. For
	 * each pixel step 1 is added if the point is inside both polygons.
	 * For each pixel, perPixelPerDimention steps are taken. Subsequently the intersection is:
	 * 
	 * sumIntersections / (perPixelPerDimention * perPixelPerDimention)
	 * 
	 * @param that
	 * @param perPixelPerDimention
	 * @return normalised intersection area
	 */
	@Override
	public double intersectionArea(Shape that, int nStepsPerDimention) {
		Rectangle overlapping = this.calculateRegularBoundingBox().overlapping(that.calculateRegularBoundingBox());
		if(overlapping==null)
			return 0;
		double intersection = 0;
		double step = Math.max(overlapping.width, overlapping.height)/(double)nStepsPerDimention;
		double nReads = 0;
		for(float x = overlapping.x; x < overlapping.x + overlapping.width; x+=step){
			for(float y = overlapping.y; y < overlapping.y + overlapping.height; y+=step){
				boolean insideThis = this.isInside(new Point2dImpl(x,y));
				boolean insideThat = that.isInside(new Point2dImpl(x,y));
				nReads++;
				if(insideThis && insideThat) {
					intersection++;
				}
			}
		}
		
		return (intersection/nReads) * (overlapping.width * overlapping.height);
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.math.geometry.shape.Shape#asPolygon()
	 */
	@Override
	public Polygon asPolygon() {
		return this;
	}

	/**
	 *	@inheritDoc
	 * 	@see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Point2d> iterator()
	{
		return vertices.iterator();
	}
	
	/**
	 * Add a vertex to the polygon
	 * 
	 * @param x x-coordinate of the vertex
	 * @param y y-coordinate of the vertex
	 */
	public void addVertex(float x, float y) {
		vertices.add(new Point2dImpl(x, y));
	}

	/**
	 * Add a vertex to the polygon
	 * 
	 * @param pt coordinate of the vertex
	 */
	public void addVertex(Point2d pt) {
		vertices.add(pt);
	}
	
	/**
	 * 	Iterates through the vertices and rounds all vertices to
	 * 	round integers. Side-affects this polygon.
	 *  
	 *	@return this polygon 
	 */
	public Polygon roundVertices()
	{
		Iterator<Point2d> i = this.iterator();
		while( i.hasNext() )
		{
			Point2d p = i.next();
			Point2dImpl p2 = new Point2dImpl( (int)p.getX(), (int)p.getY() );
			
			int xx = -1;
			if( (xx = this.vertices.indexOf( p2 )) != -1 &&
				 this.vertices.get(xx) != p )
				i.remove();
			else
			{
				p.setX( p2.x );
				p.setY( p2.y );
			}
		}
		
		for( Polygon pp : innerPolygons )
			pp.roundVertices();
		
		return this;
	}
	
	/**
	 * 	Return whether this polygon has no vertices or not.
	 *	@return TRUE if this polygon has no vertices
	 */
	public boolean isEmpty()
	{
		return this.vertices.isEmpty() && innerPolygons.isEmpty();
	}
	
	/**
	 * 	Returns the number of inner polygons in this polygon including
	 * 	this polygon.
	 * 
	 *	@return the number of inner polygons in this polygon.
	 */
	public int getNumInnerPoly()
	{
		return innerPolygons.size()+1;
	}
	
	/**
	 * 	Get the inner polygon at the given index. Note that index 0
	 * 	will return this polygon, while index i+1 will return the
	 * 	inner polygon i.
	 * 
	 *	@param index the index of the polygon to get
	 *	@return The inner polygon at the given index.
	 */
	public Polygon getInnerPoly( int index )
	{
		if( index == 0 ) return this;
		return innerPolygons.get( index-1 );
	}
	
	/**
	 * 	Add an inner polygon to this polygon. If there is no main
	 * 	polygon defined (the number of vertices is zero) then the given
	 * 	inner polygon will become the main polygon if the <code>inferOuter</code>
	 * 	boolean is true. If this variable is false, the inner polygon will
	 * 	be added to the list of inner polygons for this polygon regardless
	 * 	of whether a main polygon exists. When the main polygon is inferred
	 * 	from the given polygon, the vertices are copied into this polygon's
	 * 	vertex list.
	 * 
	 *	@param p The inner polygon to add
	 *	@param inferOuter Whether to infer the outer polygon from this inner one
	 */
	public void addInnerPolygon( Polygon p, boolean inferOuter )
	{
		if( !inferOuter )
		{
			this.innerPolygons.add( p );
		}
		else
		{
			if( this.vertices.size() == 0 )
			{
				this.vertices.addAll( p.vertices );
				this.isHole = p.isHole;
			}
			else
			{
				this.addInnerPolygon( p, false );
			}
		}
	}
	
	/**
	 * 	Add an inner polygon to this polygon. If there is no main
	 * 	polygon defined (the number of vertices is zero) then the given
	 * 	inner polygon will become the main polygon.
	 * 
	 *	@param p The inner polygon to add
	 */
	public void addInnerPolygon( Polygon p )
	{
		this.addInnerPolygon( p, true );
	}
	
	/**
	 * 	Returns the list of inner polygons.
	 *	@return the list of inner polygons
	 */
	public List<Polygon> getInnerPolys()
	{
		return this.innerPolygons;
	}
	
	/**
	 * 	Set whether this polygon represents a hole in another polygon.
	 *	@param isHole Whether this polygon is a whole.
	 */
	public void setIsHole( boolean isHole )
	{
		this.isHole = isHole;
	}
	
	/**
	 * 	Returns whether this polygon is representing a hole in another polygon.
	 *	@return Whether this polygon is representing a hole in another polygon.
	 */
	public boolean isHole()
	{
		return this.isHole;
	}
	
	/**
	 *	@inheritDoc
	 * 	@see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj )
	{
		return 
			(obj instanceof Polygon) &&
			this.equals( (Polygon)obj ); 
	}
	
	/**
	 * 	Specific equals method for polygons where the polgyons are
	 * 	tested for the vertices being in the same order. If the vertices
	 * 	are not in the vertex list in the same manner but are in the same
	 * 	order (when wrapped around) the method will return true. So the
	 * 	triangles below will return true:
	 * 
	 * 		{[1,1],[2,2],[1,2]} and {[1,2],[1,1],[2,2]}
	 * 
	 *	@param p The polygon to test against
	 *	@return TRUE if the polygons are the same.
	 */
	public boolean equals( Polygon p )
	{
		if( isHole() != p.isHole() )
			return false;
		if( this.vertices.size() != p.vertices.size() )
			return false;
		if( this.isEmpty() && p.isEmpty() )
			return true;
		
		int i = this.vertices.indexOf( p.vertices.get( 0 ) );
		if( i == -1 ) 
			return false;
		
		int s = this.vertices.size();
		for( int n = 0; n < s; n++ )
		{
			if( !p.vertices.get(n).equals( this.vertices.get((n+i)%s) ) )
				return false;
		}
		
		return true;
	}
	
	/**
	 *	@inheritDoc
	 * 	@see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return vertices.hashCode() * (isHole()?-1:1);
	}
	
	/**
	 * 	Displays the complete list of vertices unless the number of vertices
	 * 	is greater than 10 - then a sublist is shown of 5 from the start and
	 * 	5 from the end separated by ellipses.
	 * 
	 *	@inheritDoc
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		if( isHole() ) sb.append( "H" );
		int len = 10;
		if( vertices.size() < len )
			sb.append( vertices.toString() );
		else
			sb.append( vertices.subList( 0, len/2 ).toString()+"..."+
				vertices.subList( vertices.size()-len/2, vertices.size() )
					.toString() );
		
		if( innerPolygons.size() > 0 )
		{
			sb.append( "\n    - "+innerPolygons.size()+" inner polygons:" );
			for( Polygon ip : innerPolygons )
				sb.append( "\n       + "+ip.toString() );
		}
		
		return sb.toString();
	}
	
	/**
	 * 	Returns the intersection of this polygon and the given polygon.
	 * 
	 *	@param p2 The polygon to intersect with.
	 *	@return The resulting polygon intersection
	 */
	public Polygon intersect( Polygon p2 )
	{
		return new PolygonUtils().intersection( this, p2 );
	}
	
	/**
	 * 	Returns the union of this polygon and the given polygon.
	 * 
	 *	@param p2 The polygon to union with.
	 *	@return The resulting polygon union
	 */
	public Polygon union( Polygon p2 )
	{
		return new PolygonUtils().union( this, p2 );
	}
	
	/**
	 * 	Returns the XOR of this polygon and the given polygon.
	 * 
	 *	@param p2 The polygon to XOR with.
	 *	@return The resulting polygon
	 */
	public Polygon xor( Polygon p2 )
	{
		return new PolygonUtils().xor(  this, p2 );
	}
	
	/**
	 * 
	 * @param dist
	 * @return
	 */
	public Polygon reduceVertices( double dist )
	{
		if( this.nVertices() < 3 )
			return this.clone();
		
		Polygon p = new Polygon();
		Iterator<Point2d> it = this.iterator();
		List<Point2d> points = new ArrayList<Point2d>();
		points.add( it.next() );
		points.add( it.next() );
		p.addVertex( points.get(0) );
		Point2d pp = null;
		while( it.hasNext() )
		{
			pp = it.next(); 
			points.add( pp );
			
			double maxDist = 0;
			Line2d l = new Line2d( points.get(0), pp );
			for( int i = 1; i < points.size()-1; i++ )
			{
				Point2d p1 = points.get(i);
				Line2d norm = l.getNormal( p1 );
				Point2d p2 = l.getIntersection( norm ).intersectionPoint;
				if( p2 != null )
					maxDist = Math.max( maxDist, Line2d.distance( p1, p2 ) );
			}
			
			// If the distance is too great....
			if( maxDist > dist )
			{
				// Add the PREVIOUS point to the output polygon.
				// We don't add this one, of course because it is the point
				// that caused the excessive distance.
				p.addVertex( points.get( points.size()-2 ) );
				points.clear();
				points.add( pp );
				p.addVertex( pp );
			}
		}
		
		p.addVertex( pp );
		
		for( Polygon ppp : innerPolygons )
			p.addInnerPolygon( ppp.reduceVertices(dist) );
		
		return p;
	}
}
