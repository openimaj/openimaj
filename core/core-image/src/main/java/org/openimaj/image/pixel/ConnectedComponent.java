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
package org.openimaj.image.pixel;

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;
import org.openimaj.io.ReadWriteable;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;

/**
 *	This class represents a connected region within an image and provides
 *	methods for accessing and manipulating that region.
 *	<p>
 *	Some of the methods within this class allow for pixels to be added to this
 *	{@link ConnectedComponent} that are not adjacent to other pixels within
 *	the connected component (i.e. unconnected).  It is important to realise that
 *	some methods may return unexpected results (e.g. boundary tracing), but the
 *	class continues to allows this mainly for performance.   
 *	
 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
public class ConnectedComponent implements Cloneable, ReadWriteable 
{
	/**
	 * 	For boundary representations of {@link ConnectedComponent}s, this
	 * 	enum determines and specifies how the boundary is calculated; either
	 * 	using a 4-connected rule, or an 8-connected rule.
	 * 
	 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public enum ConnectMode 
	{
		/** 4-connected edges in the boundary representation */
		CONNECT_4,
		/** 8-connected edges in the boundary representation */
		CONNECT_8;
	}
	
	/** The set of pixels within this connected component */
	public Set<Pixel> pixels = new HashSet<Pixel>();

	/**
	 * 	Default constructor. Has an empty implementation.
	 */
	public ConnectedComponent() 
	{
		// Intentionally left blank!
	}

	/**
	 * 	Construct a new connected component from the given shape. Pixels are created
	 * 	for the connected component that lie within the shape; but these pixels
	 * 	will not have specific values as they are not associated to any particular
	 * 	image.
	 * 
	 *	@param shape The shape from which to construct the connected component.
	 */
	public ConnectedComponent(Shape shape) 
	{
		int minx=(int) Math.round(shape.minX()); 
		int maxx=(int) Math.round(shape.maxX());
		int miny=(int) Math.round(shape.minY()); 
		int maxy=(int) Math.round(shape.maxY());
		
		for (int y=miny; y<=maxy; y++) {
			for (int x=minx; x<=maxx; x++) {
				Pixel p = new Pixel(x, y);
				if (shape.isInside(p)) addPixel(p);
			}
		}
	}
	
	/**
	 * 	Construct a rectangular {@link ConnectedComponent}. Pixels are created
	 * 	for the area within the rectangle but these will not have any specific
	 * 	value as they are not associated to any particular image.
	 * 
	 * 	@param x The top-left x-coordinate of the rectangle
	 * 	@param y The top-left y-coordinate of the rectangle
	 * 	@param w The width of the rectangle
	 * 	@param h The height of the rectangle
	 */
	public ConnectedComponent(int x, int y, int w, int h) 
	{
		this();
		
		for (int j=y; j<h+y; j++)
			for (int i=x; i<w+x; i++)
				pixels.add(new Pixel(i, j));
	}
	
	/**
	 * 	Constructs a connected component from a mask image. Pixels are created
	 * 	for areas within the mask image that are non-zero. Note that this may
	 * 	result in a connected component definition that is unconnected and
	 * 	some methods may not return expected results.
	 * 
	 *	@param img The mask image to construct a connected component from.
	 */
	public ConnectedComponent(int [][] img) 
	{
		for (int j=0; j<img.length; j++) {
			for (int i=0; i<img[j].length; i++) {
				if (img[j][i] > 0) pixels.add(new Pixel(i,j));
			}
		}
	}
	
	/**
	 * 	Constructs a connected component from a mask image. Pixels are created
	 * 	for areas within the mask image that are above the given threshold. 
	 * 	Note that this may result in a connected component definition that is 
	 * 	unconnected and some methods may not return expected results.
	 *  @param mask The mask image to construct a connected component from.
	 *  @param thresh the threshold value.
	 */
	public ConnectedComponent(FImage mask, float thresh) 
	{
		for (int j=0; j<mask.height; j++) {
			for (int i=0; i<mask.width; i++) {
				if (mask.pixels[j][i] >= thresh) pixels.add(new Pixel(i,j));
			}
		}
	}
	
	/**
	 * 	Construct a connected component from the given set of {@link Pixel}s. The pixels
	 * 	are shallow copied into the connected component. If the pixels do not
	 * 	form a connected component then some methods in this class may not return
	 * 	expected results.
	 *  
	 *	@param pixels A {@link Set} of {@link Pixel}s.
	 */
	public ConnectedComponent(Set<Pixel> pixels) 
	{
		this.pixels.addAll(pixels);
	}

	/**
	 * 	Add a pixel into this connected component. If the pixel is not adjacent
	 * 	to another pixel within this connected component, then some methods	
	 * 	may return unexpected results. Side-affects this object.
	 * 
	 *	@param x The x-coordinate of the pixel to add
	 *	@param y The y-coordinate of the pixel to add
	 */
	public void addPixel(int x, int y) {
		addPixel(new Pixel(x, y));
	}

	/**
	 * 	Add a pixel into this connected component. If the pixel is not adjacent
	 * 	to another pixel within this connected component, then some methods	
	 * 	may return unexpected results. Side-affects this object.
	 *
	 *	@param p The pixel to add
	 */
	public void addPixel(Pixel p) {
		pixels.add(p);
	}

	/**
	 * 	Returns the set of {@link Pixel}s that are within this component.
	 *
	 *	@return the set of {@link Pixel}s that are within this component.
	 */
	public Set<Pixel> getPixels() {
		return pixels;
	}

	/**
	 *	Shallow copies the pixels from the given {@link ConnectedComponent} into
	 *	this object. If the pixels that are added are not adjacent to other pixels
	 *	within the component some methods may return unexpected results. Side-affects
	 *	this object.
	 * 
	 *	@param c The {@link ConnectedComponent} to copy pixels from.
	 */
	public void merge(ConnectedComponent c) 
	{
		pixels.addAll(c.pixels);
	}

	/**
	 * 	Returns whether the given pixel is within this connected component.
	 * 	This is synonymous to 
	 *  <code>{@link #getPixels() getPixels}().contains(p)</code>.	
	 * 
	 *	@param p The pixel to find.
	 *	@return TRUE if the pixel is contained within this component; FALSE otherwise
	 */
	public boolean find(Pixel p) {
		return pixels.contains(p);
	}

	/**
	 * 	Returns whether the given coordinates are within this connected
	 * 	component. This is synonymous with 
	 *  <code>{@link #find(Pixel) find}( new Pixel(x,y) )</code>.
	 * 
	 *	@param x The x-coordinate of the pixel to find
	 *	@param y The y-coordinate of the pixel to find
	 *	@return TRUE if the pixel is contained within this component; FALSE otherwise
	 */
	public boolean find(int x, int y) 
	{
		return find( new Pixel(x, y) );
	}

	/**
	 * 	Calculate the area of this connected component in pixels. This is
	 * 	synonymous with <code>{@link #getPixels() getPixels}.size()</code>
	 * 
	 * 	@return the number of pixels covered by this connected component. 
	 */
	public int calculateArea() 
	{
		return pixels.size();
	}

	/**
	 * Calculate the pq moment, M<sub>pq</sub> around the given centroid.
	 * From Equation 6.43 in Sonka, Hlavac and Boyle.
	 *  
	 * @param p The P moment to calculate
	 * @param q The Q moment to calculate
	 * @param xc x-coordinate of centroid
	 * @param yc y-coordinate of centroid
	 * @return The pq moment, M<sub>pq</sub>. 
	 */
	public double calculateMoment(int p, int q, double xc, double yc) {
		if (p==0 && q==0) return calculateArea();

		double mpq = 0;
		for (Pixel pix : pixels) {
			mpq += Math.pow(pix.x-xc, p) * Math.pow(pix.y-yc, q); 
		}
		return mpq;
	}

	/**
	 * Calculate the pq moment, M<sub>pq</sub> for this region.
	 * From Equation 6.43 in Sonka, Hlavac and Boyle.
	 * 
	 * @param p The P moment to calculate
	 * @param q The Q moment to calculate
	 * @return The pq moment, M<sub>pq</sub>.
	 */
	public double calculateMoment(int p, int q) 
	{
		if (p==0 && q==0) return calculateArea();

		double [] centroid = calculateCentroid();

		double mpq = 0;
		for (Pixel pix : pixels) {
			mpq += Math.pow(pix.x-centroid[0], p) * Math.pow(pix.y-centroid[1], q); 
		}
		return mpq;
	}

	/**
	 * Calculate the normalized, unscaled, central moments for M<sub>pq</sub>. 
	 * From Equation 6.47 in Sonka, Hlavac and Boyle [1st Ed.].  Normalised
	 * central moments are invariant to scale and translation.
	 * 
	 * @param p The P moment to calculate
	 * @param q The Q moment to calculate
	 * @return The normalised, unscaled central moment, M<sub>pq</sub>.
	 */
	public double calculateMomentNormalised(int p, int q) 
	{
		double gamma = ((p+q)/2) + 1; 
		return calculateMoment(p, q) / Math.pow(pixels.size(), gamma);
	}

	/**
	 * 	Calculates the principle direction of the connected component. This is
	 * 	given by <code>0.5 * atan( (M<sub>20</sub>-M<sub>02</sub>) / 2 * M<sub>11</sub> )</code>
	 * 	so results in an angle between -PI and +PI.
	 * 
	 *	@return The principle direction (-PI/2 to +PI/2 radians) of the connected component.
	 */
	public double calculateDirection() 
	{
		double [] centroid = calculateCentroid();
		double u11 = calculateMoment(1, 1, centroid[0], centroid[1]);
		double u20 = calculateMoment(2, 0, centroid[0], centroid[1]);
		double u02 = calculateMoment(0, 2, centroid[0], centroid[1]);

		double theta = 0.5 * Math.atan2((2 * u11), (u20 - u02));

		return theta;
	}

	/**
	 * 	Calculate the centroid of the connected component. This is the average
	 * 	of all the pixel coordinates in the component. The result is returned
	 * 	in a double array where the the first index is the x-coordinate and 
	 * 	second is the y-coordinate.
	 *  
	 * 	@return The centroid point as a double array (x then y). 
	 */
	public double[] calculateCentroid() 
	{
		double [] centroid = new double[2];
		double m00 = calculateMoment(0,0,0,0);
		centroid[0] = calculateMoment(1,0,0,0) / m00;
		centroid[1] = calculateMoment(0,1,0,0) / m00;

		return centroid;
	}

	/**
	 * 	Calculates the centroid pixel of the connected component. That is, the
	 * 	centroid value is rounded to the nearest pixel.
	 * 
	 *	@return A {@link Pixel} at the centroid.
	 */
	public Pixel calculateCentroidPixel() 
	{
		double [] centroid = calculateCentroid();
		return new Pixel((int)Math.round(centroid[0]), (int)Math.round(centroid[1]));
	}

	/**
	 * 	Calculate the height and width of a box surrounding
	 * 	the component by averaging the distances of pixels
	 * 	above and below the centroid. The result is a double array where
	 * 	the first index is the height and the second is the width.  
	 * 
	 * 	@param centroid The centroid of the component.
	 * 	@return average height and width as a double array.
	 */
	public double [] calculateAverageHeightWidth(double [] centroid) 
	{
		double height, width, accumPosH = 0, accumNegH = 0, accumPosW = 0, accumNegW = 0;
		int nPosH = 0, nNegH = 0, nPosW = 0, nNegW = 0;

		for (Pixel p : pixels) {
			double x = p.getX() - centroid[0];
			double y = p.getY() - centroid[1];

			if (x >= 0) {
				accumPosW += x;
				nPosW++;
			} else {
				accumNegW += x;
				nNegW++;
			}

			if (y >= 0) {
				accumPosH += y;
				nPosH++;
			} else {
				accumNegH += y;
				nNegH++;
			}
		}
		height = 2 * ((accumPosH / nPosH) + Math.abs(accumNegH / nNegH));
		width = 2 * ((accumPosW / nPosW) + Math.abs(accumNegW / nNegW));

		return new double[] {height, width};
	}

	/**
	 * 	Calculate the height and width of a box surrounding
	 * 	the component by averaging the distances of pixels
	 * 	above and below the centroid. The result is a double array where
	 * 	the first index is the height and the second is the width.  
	 * 
	 * 	@return average height and width as a double array.
	 */
	public double [] calculateAverageHeightWidth() 
	{
		return calculateAverageHeightWidth(calculateCentroid());
	}

	/**
	 * 	Calculates the width and height of the bounding box that best fits 
	 * 	the component, at whatever angle that may be. The result is returned
	 * 	as a double array, where the first index is the height and the second
	 * 	index the width.
	 *  
	 *	@return A double array containing the height and width of the best
	 *		fitting bounding box.
	 */
	public double [] calculateOrientatedBoundingBoxHeightWidth() 
	{
		List<Pixel> bound = getInnerBoundary(ConnectMode.CONNECT_8);
		double theta = calculateDirection();

		double alphaMax=-Double.MAX_VALUE, alphaMin=Double.MAX_VALUE;
		double betaMax=-Double.MAX_VALUE, betaMin=Double.MAX_VALUE;
		double alpha, beta;

		for (Pixel p : bound) {
			alpha = p.x * Math.cos(theta) + p.y * Math.sin(theta);
			beta = -1.0 * p.x * Math.sin(theta) + p.y * Math.cos(theta);

			if (alpha > alphaMax) alphaMax = alpha;
			if (alpha < alphaMin) alphaMin = alpha;

			if (beta > betaMax) betaMax = beta;
			if (beta < betaMin) betaMin = beta;
		}

		return new double [] {betaMax - betaMin, alphaMax - alphaMin};
	}

	/**
	 *	Calculates the polygon that defines the bounding box that best fits
	 *	the connected component, at whatever angle that may be. 
	 *
	 *	@return A {@link Polygon} that defines the bounding box.
	 */
	public Polygon calculateOrientatedBoundingBox() 
	{
		double [] centroid = calculateCentroid();
		double [] hw = calculateOrientatedBoundingBoxHeightWidth();
		double theta = calculateDirection();

		Point2d p1 = new Point2dImpl((float)(centroid[0] - (hw[1] / 2.0)), (float)(centroid[1] - (hw[0] / 2.0)));
		Point2d p2 = new Point2dImpl((float)(centroid[0] + (hw[1] / 2.0)), (float)(centroid[1] - (hw[0] / 2.0)));
		Point2d p3 = new Point2dImpl((float)(centroid[0] + (hw[1] / 2.0)), (float)(centroid[1] + (hw[0] / 2.0)));
		Point2d p4 = new Point2dImpl((float)(centroid[0] - (hw[1] / 2.0)), (float)(centroid[1] + (hw[0] / 2.0)));

		Polygon p = new Polygon(p1, p4, p3, p2);
		Point2d origin = new Point2dImpl((float)centroid[0], (float)centroid[1]); 
		p.rotate(origin, theta);

		return p;
	}

	/**
	 *	Draws the oriented bounding box for this component into the given 
	 *	image in the given colour.
	 * 
	 *	@param image The {@link FImage} into which to draw the box
	 *	@param grey The colour in which to draw the box
	 */
	public void drawOrientatedBoundingBox(FImage image, float grey) 
	{
		image.createRenderer().drawPolygon(calculateOrientatedBoundingBox(), grey);
	}

	/**
	 * 	Calculates the distance from the centroid of every pixel on the 
	 * 	8-connected boundary of this component. Returns a {@link TFloatArrayList}
	 * 	that contains the list of distances (in order of the boundary).
	 *  
	 *	@return A list ({@link TFloatArrayList}) of distances of boundary
	 *		points to the centroid.
	 */
	public TFloatArrayList calculateBoundaryDistanceFromCentre() 
	{
		TFloatArrayList distances = new TFloatArrayList();
		List<Pixel> bound = getInnerBoundary(ConnectMode.CONNECT_8);
		double [] centroid = calculateCentroid();

		for (Pixel p : bound) {
			float dist = (float) Math.sqrt(((centroid[0]-p.x) * ((centroid[0]-p.x))) + 
					((centroid[1]-p.y) * ((centroid[1]-p.y))));
			distances.add(dist);
		}

		return distances;
	}

	/**
	 *	Estimates how many vertices are required to encode the boundary
	 *	with the given smoothness and window width. Basically it determines
	 *	how many strong peaks there are in the boundary, where the peak strength
	 *	is determined by the two parameters. The window width determines
	 *	how many boundary points are considered when iterating through the
	 *	binary, while the smoothness determines how smooth the resulting
	 *	boundary representation can be. 
	 * 
	 *	@param smoothWidth The smoothness of the resulting boundary
	 *	@param windowWidth The number of edge points to consider
	 *	@return The estimated number of vertices required to encode the boundary. 
	 */
	public int estimateNumberOfVertices(int smoothWidth, int windowWidth) 
	{
		TFloatArrayList distances = calculateBoundaryDistanceFromCentre();

		if (smoothWidth%2==0) smoothWidth++;
		if (windowWidth%2==0) windowWidth++;

		int n = distances.size();
		float [] kernel = new float[windowWidth];
		float [] response = new float[n];

		for (int i=0; i<n; i++) {
			float sum = 0;
			for (int j=0; j<smoothWidth; j++) {
				int k = i + j -(smoothWidth/2);

				if (k<0) {
					k = n+k;
				} else if (k>=n) {
					k = k - n;
				}
				sum += distances.get(k);
			}
			distances.set(i, sum / smoothWidth);
		}


		for (int i=0; i<windowWidth; i++) kernel[i] = -(windowWidth/2) + i;

		for (int i=0; i<n; i++) {
			float sum = 0;
			for (int j=0; j<windowWidth; j++) {
				int k = i + j -(windowWidth/2);

				if (k<0) {
					k = n+k;
				} else if (k>=n) {
					k = k - n;
				}
				sum += kernel[j] * distances.get(k);
			}
			response[i] = sum;
		}

		int peaks = 0;
		for (int i=1; i<n; i++) {
			if (response[i-1]>=0 && response[i]<0) peaks++;
		}
		if (response[n-1]>=0 && response[0]<0) peaks++;

		return peaks;
	}

	/**
	 * 
	 *	@param P0
	 *	@param P1
	 *	@param P2
	 *	@return
	 */
	protected int isLeft(Pixel P0, Pixel P1, Pixel P2) {
		return (P1.x - P0.x)*(P2.y - P0.y) - (P2.x - P0.x)*(P1.y - P0.y);
	}

	/**
	 * 	Calculates the convex hull polygon for this connected component
	 * 	using Andrew's montone algorithm.
	 * 
	 *	@return The polygon defining the convex hull shape for this component.
	 */
	public Polygon calculateConvexHull() {
		return calculateConvexHull_AndrewsMontone();
	}

	/**
	 * 	Calculate the ratio of the area of the given connected component to the
	 * 	area of the connected component. This does not consider whether
	 * 	the areas overlap.
	 * 
	 *	@param ch The connected component to test.
	 *	@return The area ratio of the given connected component to this
	 *		connected component.
	 */
	public double calculateAreaRatio(ConnectedComponent ch) 
	{
		return (double)calculateArea() / (double)ch.calculateArea();
	}

	/**
	 * 	Calculate the ratio of the area of the given polygon to the
	 * 	area of this connected component. This does not consider whether
	 * 	the areas overlap.
	 * 
	 *	@param ch The polygon to test again.
	 *	@return The area ratio of the given polygon to this
	 *		connected component.
	 */
	public double calculateAreaRatio(Polygon ch) 
	{
		return calculateAreaRatio(new ConnectedComponent(ch));
	}
	
	/**
	 * 	Calculate the ratio of the area of this component's convex hull to the
	 * 	actual area of this connected component. This gives an idea of how
	 * 	well the calculated convex hull fits the component. The value returned
	 * 	is a percentage (0-1).
	 * 
	 *	@return The area ratio of this component's convex hull its area.
	 */
	public double calculatePercentageConvexHullFit() 
	{
		return calculateAreaRatio(calculateConvexHull());
	}

	/**
	 * Calculate convex hull using Melkman's algorithm.
	 * Based on http://softsurfer.com/Archive/algorithm_0203/algorithm_0203.htm
	 * 
	 * // Copyright 2001, softSurfer (www.softsurfer.com)
	 * This code may be freely used and modified for any purpose
	 * providing that this copyright notice is included with it.
	 * SoftSurfer makes no warranty for this code, and cannot be held
	 * liable for any real or imagined damage resulting from its use.
	 * Users of this code must verify correctness for their application.
	 *  
	 * @param V List of pixels containing within the region
	 * @return A polygon defining the shape of the convex hull
	 */
	protected Polygon calculateConvexHull_Melkman(List<Pixel> V)
	{
		// initialize a deque D[] from bottom to top so that the
		// 1st three vertices of V[] are a counterclockwise triangle
		int n = V.size();
		Pixel [] D = new Pixel[2*n+1];
		int bot = n-2, top = bot+3;   // initial bottom and top deque indices
		D[bot] = D[top] = V.get(2);       // 3rd vertex is at both bot and top
		if (isLeft(V.get(0), V.get(1), V.get(2)) > 0) {
			D[bot+1] = V.get(0);
			D[bot+2] = V.get(1);          // ccw vertices are: 2,0,1,2
		}
		else {
			D[bot+1] = V.get(1);
			D[bot+2] = V.get(0);          // ccw vertices are: 2,1,0,2
		}

		// compute the hull on the deque D[]
		for (int i=3; i < n; i++) {   // process the rest of vertices
			// test if next vertex is inside the deque hull
			if ((isLeft(D[bot], D[bot+1], V.get(i)) > 0) &&
					(isLeft(D[top-1], D[top], V.get(i)) > 0) )
				continue;         // skip an interior vertex

			// incrementally add an exterior vertex to the deque hull
			// get the rightmost tangent at the deque bot
			while (isLeft(D[bot], D[bot+1], V.get(i)) <= 0)
				++bot;                // remove bot of deque
			D[--bot] = V.get(i);          // insert V[i] at bot of deque

			// get the leftmost tangent at the deque top
			while (isLeft(D[top-1], D[top], V.get(i)) <= 0)
				--top;                // pop top of deque
			D[++top] = V.get(i);          // push V[i] onto top of deque
		}

		// transcribe deque D[] to the output hull array H[]
		Polygon H = new Polygon();
		List<Point2d> vertices = H.getVertices(); 
		for (int h=0; h <= (top-bot); h++)
			vertices.add(D[bot + h]);

		return H;
	}

	/**
	 * 	Calculate the convex hull using Andrew's monotone chain 2D convex hull 
	 * 	algorithm.
	 * 
	 * 	@return A polygon defining the shape of the convex hull.
	 */
	protected Polygon calculateConvexHull_AndrewsMontone()
	{
		if (this.calculateArea() == 1) {
			return new Polygon(this.pixels.iterator().next());
		}

		List<Pixel> P = new ArrayList<Pixel>();

		//sort
		int minx=Integer.MAX_VALUE, maxx=Integer.MIN_VALUE, miny=Integer.MAX_VALUE, maxy=Integer.MIN_VALUE;

		for (Pixel p : pixels) {
			if (p.x < minx) minx = p.x;
			if (p.x > maxx) maxx = p.x;
			if (p.y < miny) miny = p.y;
			if (p.y > maxy) maxy = p.y;
		}

		for (int x=minx; x<=maxx; x++) {
			for (int y=miny; y<=maxy; y++) {
				Pixel p = new Pixel(x,y);
				if (pixels.contains(p)) 
					P.add(p);
			}
		}

		// the output array H[] will be used as the stack
		int bot=0, top=(-1);  // indices for bottom and top of the stack
		int i;                // array scan index
		int n = P.size();

		Polygon poly = new Polygon();
		Pixel [] H = new Pixel[P.size()];

		// Get the indices of points with min x-coord and min|max y-coord
		int minmin = 0, minmax;
		float xmin = P.get(0).x;
		for (i=1; i<n; i++)
			if (P.get(i).x != xmin) break;
		minmax = i-1;
		if (minmax == n-1) {       // degenerate case: all x-coords == xmin
			H[++top] = P.get(minmin);
			if (P.get(minmax).y != P.get(minmin).y) // a nontrivial segment
				H[++top] = P.get(minmax);
			H[++top] = P.get(minmin);           // add polygon endpoint

			for (int k=0; k<top+1; k++)
				poly.getVertices().add(H[k]);
			return poly;
		}

		// Get the indices of points with max x-coord and min|max y-coord
		int maxmin, maxmax = n-1;
		float xmax = P.get(n-1).x;
		for (i=n-2; i>=0; i--)
			if (P.get(i).x != xmax) break;
		maxmin = i+1;

		// Compute the lower hull on the stack H
		H[++top] = P.get(minmin);      // push minmin point onto stack
		i = minmax;
		while (++i <= maxmin)
		{
			// the lower line joins P[minmin] with P[maxmin]
			if (isLeft( P.get(minmin), P.get(maxmin), P.get(i)) >= 0 && i < maxmin)
				continue;          // ignore P[i] above or on the lower line

			while (top > 0)        // there are at least 2 points on the stack
			{
				// test if P[i] is left of the line at the stack top
				if (isLeft( H[top-1], H[top], P.get(i)) > 0)
					break;         // P[i] is a new hull vertex
				else
					top--;         // pop top point off stack
			}
			H[++top] = P.get(i);       // push P[i] onto stack
		}

		// Next, compute the upper hull on the stack H above the bottom hull
		if (maxmax != maxmin)      // if distinct xmax points
			H[++top] = P.get(maxmax);  // push maxmax point onto stack
		bot = top;                 // the bottom point of the upper hull stack
		i = maxmin;
		while (--i >= minmax)
		{
			// the upper line joins P[maxmax] with P[minmax]
			if (isLeft( P.get(maxmax), P.get(minmax), P.get(i)) >= 0 && i > minmax)
				continue;          // ignore P[i] below or on the upper line

			while (top > bot)    // at least 2 points on the upper stack
			{
				// test if P[i] is left of the line at the stack top
				if (isLeft( H[top-1], H[top], P.get(i)) > 0)
					break;         // P[i] is a new hull vertex
				else
					top--;         // pop top point off stack
			}
			H[++top] = P.get(i);       // push P[i] onto stack
		}
		if (minmax != minmin)
			H[++top] = P.get(minmin);  // push joining endpoint onto stack

		for (int k=0; k<top+1; k++)
			poly.getVertices().add(H[k]);
		return poly;
	}

	/**
	 * 	Calculate the regular bounding box of the region by calculating the
	 * 	maximum and minimum x and y coordinates of the pixels contained within
	 * 	the region. The result is an integer array containing the (x,y) coordinate
	 * 	of the top-left of the bounding box, and the width and height of the
	 * 	bounding box.
	 * 
	 * 	@return an {@link Rectangle} describing the bounds
	 */
	public Rectangle calculateRegularBoundingBox() 
	{
		int xmin=Integer.MAX_VALUE, xmax=0, ymin=Integer.MAX_VALUE, ymax=0;

		for (Pixel p : pixels) {
			if (p.x < xmin) xmin = p.x;
			if (p.x > xmax) xmax = p.x;
			if (p.y < ymin) ymin = p.y;
			if (p.y > ymax) ymax = p.y;
		}

		return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
	}

	/**
	 * 	Translates the region's pixels by x and y. This method side-affects
	 * 	the pixels in this object.
	 *  
	 * 	@param x The offset in the horizontal direction
	 * 	@param y The offset in the vertical direction.
	 */
	public void translate(int x, int y) 
	{
		//Note: changing the position changes the hashcode, so you need to rehash the set!
		Set<Pixel> newPixels = new HashSet<Pixel>(); 
		
		for (Pixel p : pixels) { 
			p.x += x;
			p.y += y;
			newPixels.add(p);
		}
		
		pixels = newPixels;
	}

	/**
	 * Gets the top-left most pixel within the connected component.
	 * 
	 * @return the top-left most pixel within the connected component.
	 */
	public Pixel topLeftMostPixel() 
	{
		int top = Integer.MAX_VALUE;
		Pixel pix = null;
		for (Pixel p : pixels) {
			if (p.y < top) {
				top = p.y;
				pix = p; 
			}
		}

		for (Pixel p : pixels) {
			if (p.y == top) {
				if (p.x < pix.x)
					pix = p;
			}
		}

		return pix;
	}

	/**
	 * 	Gets the bottom-right most pixel in the connected component.
	 * 
	 * 	@return the bottom-right most pixel in the connected component.
	 */
	public Pixel bottomRightMostPixel() 
	{
		int bottom = Integer.MIN_VALUE;
		Pixel pix = null;
		for (Pixel p : pixels) {
			if (p.y > bottom) {
				bottom = p.y;
				pix = p; 
			}
		}

		for (Pixel p : pixels) {
			if (p.y == bottom) {
				if (p.x > pix.x)
					pix = p;
			}
		}

		return pix;
	}

	/**
	 * 	Returns the next edge pixel when tracing a boundary
	 * 	in a 4-connected system.
	 * 
	 *	@param current The current pixel
	 *	@param lastdir The last direction traversed
	 *	@return The next pixel in the edge
	 */
	protected Pixel nextEdgePixelACW4(Pixel current, int lastdir) 
	{
		return nextEdgePixelACW4(current, lastdir, null);
	}

	/**
	 * 	Returns the next edge pixel when tracing a boundary
	 * 	in a 4-connected system.
	 * 
	 *	@param current The current pixel
	 *	@param lastdir The last direction traversed
	 *	@param outer A list to fill with the outer boundary
	 *	@return The next pixel in the edge
	 */
	protected Pixel nextEdgePixelACW4(Pixel current, int lastdir, List<Pixel> outer) 
	{
		int startdir = (lastdir+3) % 4;

		Pixel target[] = {
				new Pixel(current.x+1, current.y), //dir 0
				new Pixel(current.x, current.y-1), //dir 1
				new Pixel(current.x-1, current.y), //dir 2
				new Pixel(current.x, current.y+1)  //dir 3
		};

		for (int i=0; i<4; i++) {
			int dir = startdir + i;
			if (dir >= 4) dir -= 4;

			if (pixels.contains(target[dir])) 
				return target[dir];
			else
				if (outer != null) outer.add(target[dir]);
		}

		return current;
	}
	
	/**
	 * 	Returns the next edge pixel when tracing a boundary
	 * 	in an 8-connected system.
	 * 
	 *	@param current The current pixel
	 *	@param lastdir The last direction traversed
	 *	@return The next pixel in the edge
	 */
	protected Pixel nextEdgePixelACW8(Pixel current, int lastdir) {
		int startdir = (lastdir + 7 - (lastdir % 2)) % 8;

		Pixel target[] = {
				new Pixel(current.x+1, current.y  ), //dir 0
				new Pixel(current.x+1, current.y-1), //dir 1
				new Pixel(current.x  , current.y-1), //dir 2
				new Pixel(current.x-1, current.y-1), //dir 3
				new Pixel(current.x-1, current.y  ), //dir 4
				new Pixel(current.x-1, current.y+1), //dir 5
				new Pixel(current.x  , current.y+1), //dir 6
				new Pixel(current.x+1, current.y+1)  //dir 7
		};

		for (int i=0; i<8; i++) {
			int dir = startdir + i;
			if (dir >= 8) dir -= 8;

			if (pixels.contains(target[dir])) return target[dir];
		}

		return current;
	}

	/**
	 * 	For the two pixels, determines the 4-connected chain code that will
	 * 	move from the first pixel to the next. If the pixels are not adjacent
	 * 	the method returns -1.
	 * 
	 *	@param current The current pixel
	 *	@param next The next pixel
	 *	@return The Freeman 4-connected chain code 
	 */
	protected int code4(Pixel current, Pixel next) 
	{
		if (current.x-1 == next.x) return 2;
		if (current.y+1 == next.y) return 3;
		if (current.x+1 == next.x) return 0;
		if (current.y-1 == next.y) return 1;

		return -1;
	}

	/**
	 * 	For the two pixels, determines the 8-connected chain code that will
	 * 	move from the first pixel to the next. If the pixels are not adjacent
	 * 	or diagonal then the method returns -1.
	 * 
	 *	@param current The current pixel
	 *	@param next The next pixel
	 *	@return The Freeman 8-connected chain code 
	 */
	protected int code8(Pixel current, Pixel next) 
	{
		if (current.x+1 == next.x && current.y   == next.y) return 0;
		if (current.x+1 == next.x && current.y-1 == next.y) return 1;
		if (current.x   == next.x && current.y-1 == next.y) return 2;
		if (current.x-1 == next.x && current.y-1 == next.y) return 3;
		if (current.x-1 == next.x && current.y   == next.y) return 4;
		if (current.x-1 == next.x && current.y+1 == next.y) return 5;
		if (current.x   == next.x && current.y+1 == next.y) return 6;
		if (current.x+1 == next.x && current.y+1 == next.y) return 7;

		return -1;
	}

	/**
	 *	Converts this connected component into a {@link Polygon} representation
	 *	by performing a 4-connected boundary trace and converting the resulting
	 *	pixels into vertices.
	 * 
	 *	@return A {@link Polygon} representing the inner boundary of the component.
	 */
	public Polygon toPolygon() 
	{
		Polygon poly = new Polygon();

		for (Pixel p : getInnerBoundary(ConnectMode.CONNECT_4))
			poly.getVertices().add(p);

		return poly;
	}

	/**
	 * 	Returns an ordered list of pixels that are on the inner boundary of the
	 * 	shape. That means that the boundary points are all within the region.
	 * 	The list is ordered such that adjacent boundary pixels are adjacent
	 * 	in the list. The first pixel in the list should be the same as
	 * 	{@link #topLeftMostPixel()}.	
	 * 
	 *	@param mode The {@link ConnectMode} to use.
	 *	@return An ordered list of pixels defining the inner boundary
	 */
	public List<Pixel> getInnerBoundary(ConnectMode mode) 
	{
		List<Pixel> pset = new ArrayList<Pixel>();

		Pixel start = topLeftMostPixel();
		Pixel current = start;
		Pixel next;
		int dir;

		switch (mode) {
		case CONNECT_4:
			dir = 3;
			while (true) {
				next = nextEdgePixelACW4(current, dir);
				if (pset.size() >= 2 && next.equals(pset.get(1)) && current.equals(start)) {
					break;
				}

				dir = code4(current, next);
				pset.add(current);		
				current = next;
			}
			break;
		case CONNECT_8:
			dir = 7;
			while (true) {
				next = nextEdgePixelACW8(current, dir);

				if (pset.size() >= 2 && next.equals(pset.get(1)) && current.equals(start)) {
					break;
				}

				dir = code8(current, next);
				pset.add(current);
				current = next;
			}
			break;
		}

		return pset;
	}

	/**
	 * 	Returns an ordered list of pixels that are on the outer boundary of the
	 * 	shape. That means that the boundary points are all outside of the region.
	 * 	The list is ordered such that adjacent boundary pixels are adjacent
	 * 	in the list.	
	 * 
	 *	@return An ordered list of pixels defining the outer boundary
	 */
	public List<Pixel> getOuterBoundary() 
	{
		List<Pixel> pset = new ArrayList<Pixel>();
		List<Pixel> outer = new ArrayList<Pixel>();

		Pixel start = topLeftMostPixel();
		Pixel current = start;
		Pixel next;
		int dir = 3;

		while (true) {
			next = nextEdgePixelACW4(current, dir, outer);
			if (pset.size() >= 2 && next.equals(pset.get(1)) && current.equals(start)) {
				break;
			}

			dir = code4(current, next);
			pset.add(current);		
			current = next;
		}

		return outer;
	}

	/**
	 * 	Calculates the Freeman chaincode for this connected component. The
	 * 	chaincode is returned as a list of direction codes defining the
	 * 	path of the boundary. 
	 * 	<p>
	 * 	The Freeman chaincode is a means for encoding the paths between nodes
	 * 	on the boundary of a shape, thereby reducing the encoding of a shape
	 * 	to a single start coordinate and a list of direction codes. The Freeman
	 * 	direction codes are 0-4 for 4-connected boundaries and 0-7 for 
	 * 	8-connected boundaries.
	 * 
	 *	@param mode 4 or 8 connectivity
	 *	@return the chain code
	 */
	public TIntArrayList freemanChainCode(ConnectMode mode) 
	{
		TIntArrayList code = new TIntArrayList();

		Pixel start = topLeftMostPixel();
		Pixel current = start;
		Pixel next;
		int dir;

		switch (mode) {
		case CONNECT_8:
			dir = 7;
			while (!(next = nextEdgePixelACW8(current, dir)).equals(start)) {
				dir = code8(current, next);
				code.add(dir);
				current = next;
			}
			code.add(code8(current, next));
			break;
		case CONNECT_4:
			dir = 3;
			while (!(next = nextEdgePixelACW4(current, dir)).equals(start)) {
				dir = code4(current, next);
				code.add(dir);
				current = next;
			}
			code.add(code4(current, next));
			break;
		}

		return code;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConnectedComponent(" + "area=" + calculateArea() + ")";
	}

	/**
	 *	Process the given set of connected components with the given
	 *	{@link ConnectedComponentProcessor}.
	 * 
	 *	@param components The components to process
	 *	@param p The process to process the components with
	 */
	public static void process(Collection<ConnectedComponent> components, ConnectedComponentProcessor p) 
	{
		for (ConnectedComponent c : components)
			c.processInplace(p);
	}

	/**
	 * 	Process this connected component with the given {@link ConnectedComponentProcessor}
	 * 	and returns a new component containing the result.
	 * 
	 * 	@param p The processor to process this component with 
	 * 	@return A new component containing the result.
	 */
	public ConnectedComponent process(ConnectedComponentProcessor p) 
	{
		ConnectedComponent tmp = clone();
		p.process(tmp);
		return tmp;
	}

	/**
	 * 	Process a connected component with the given {@link ConnectedComponentProcessor}. 
	 * 	Side-affects this component.
	 * 
	 * 	@param p The processor to process this component with
	 * 	@return A reference to this connected component.
	 */
	public ConnectedComponent processInplace(ConnectedComponentProcessor p) 
	{
		p.process(this);
		return this;
	}

	/**
	 * 	Performs a flood fill on the given image starting at the given
	 * 	pixel. The result of the flood fill is returned as a {@link ConnectedComponent}.
	 * 
	 *	@param image The image on which to perform a flood fill
	 *	@param start The start pixel to begin the flood
	 *	@return A ConnectedComponent containing the resulting region.
	 */
	public static ConnectedComponent floodFill(FImage image, Pixel start) 
	{
		ConnectedComponent cc = new ConnectedComponent();
		float val = image.pixels[start.y][start.x];
		int [][] output = new int[image.height][image.width];

		//		Flood-fill (node, target-color, replacement-color):
		//			 1. Set Q to the empty queue.
		//Queue<Pixel> queue = new LinkedList<Pixel>();
		LinkedHashSet<Pixel> queue = new LinkedHashSet<Pixel>();

		//			 2. If the color of node is not equal to target-color, return.
		if (image.pixels[start.y][start.x] > val) return cc;

		//			 3. Add node to Q.
		queue.add(start);

		//			 4. For each element n of Q:
		while (queue.size() > 0) {
			//Pixel n = queue.poll();
			Pixel n = queue.iterator().next();
			queue.remove(n);

			//			 5.  If the color of n is equal to target-color:
			if (image.pixels[n.y][n.x] <= val) {
				//			 6.   Set w and e equal to n.
				int e = n.x, w=n.x;
				//			 7.   Move w to the west until the color of the node to the west of w no longer matches target-color.
				while (w>0 && image.pixels[n.y][w-1] <= val) w--;

				//			 8.   Move e to the east until the color of the node to the east of e no longer matches target-color.
				while (e<image.width-1 && image.pixels[n.y][e+1] <= val) e++;

				//			 9.   Set the color of nodes between w and e to replacement-color.
				for (int i=w; i<=e; i++) {
					output[n.y][i] = 1;
					cc.addPixel(i, n.y);

					//			10.   For each node n between w and e:
					int north = n.y - 1;
					int south = n.y + 1;
					//			11.    If the color of the node to the north of n is target-color, add that node to Q.
					if (north >= 0 && image.pixels[north][i] <=val && output[north][i] != 1) queue.add(new Pixel(i, north));
					//			       If the color of the node to the south of n is target-color, add that node to Q.
					if (south < image.height && image.pixels[south][i] <=val && output[south][i] != 1) queue.add(new Pixel(i, south));
				}
				//			12. Continue looping until Q is exhausted.
			}
		}
		//			13. Return.
		return cc;
	}

	/**
	 *	{@inheritDoc}
	 *
	 *	Performs a deep copy on the connected component; that is, all pixels
	 *	are also cloned.
	 */
	@Override
	public ConnectedComponent clone() {
		ConnectedComponent tmp;
		try {
			tmp = (ConnectedComponent) super.clone();
			tmp.pixels = new HashSet<Pixel>();
			
			for (Pixel p : pixels)
				tmp.pixels.add(p.clone());

			return tmp;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Extract a 1xarea image with all the pixels from the region in it.
	 * Useful for analysing the colour for example
	 * @param input input image to extract samples from
	 * @return new image with pixels set from samples 
	 */
	public MBFImage extractPixels1d(MBFImage input) 
	{
		MBFImage out = new MBFImage(pixels.size(), 1, input.numBands());

		int j=0;
		for (Pixel p : pixels) {
			for (int i=0; i<input.numBands(); i++) {
				out.setPixel(j, 0, input.getPixel(p.x, p.y));
			}
			j++;
		}

		return out;
	}

	/**
	 * Extract a 1 x area image with all the pixels from the region in it.
	 * Useful for analysing the colour for example
	 * @param input image to extract pixel values from
	 * @return new image filled with pixel values
	 */
	public FImage extractPixels1d(FImage input) {
		FImage out = new FImage(pixels.size(), 1);

		int j=0;
		for (Pixel p : pixels) {
			out.pixels[0][j] = input.pixels[p.y][p.x];
			j++;
		}

		return out;
	}
	
	/**
	 * 	Returns an image containing just the connected component
	 * 	cropped from the original image. Although that the result
	 * 	image is necessarily rectangular, the pixels which are not
	 * 	part of the connected component will be transparent.
	 * 
	 * 	@param input The input image from which to take the pixels
	 *  @param blackout Whether to blackout pixels that are not part of the region
	 *  	or whether to mark them as transparent
	 *	@return An image with the component's pixels cropped 
	 */
	public MBFImage crop( MBFImage input, boolean blackout )
	{
		Rectangle bb = this.calculateRegularBoundingBox();
		
		MBFImage output = new MBFImage( (int)bb.width, (int)bb.height, input.numBands() );
		
		Polygon p = null;
		
		// We only need to get the polygon if we're going to black
		// out the region
		if( blackout )
			p = this.toPolygon();
		
		for( int y = 0; y < (int)bb.height; y++ )
		{
			for( int x = 0; x < (int)bb.width; x++ )
			{
				for( int b = 0; b < input.numBands(); b++ )
				{
					if( !blackout || p.isInside( new Point2dImpl(x+(int)bb.x, y+(int)bb.y) ) )
							output.getBand( b ).setPixel( x, y, input.getBand(b).getPixel( x+(int)bb.x, y+(int)bb.y ) );
					else	output.getBand( b ).setPixel( x, y, 0f );
				}
			}
		}
		
		return output;
	}
	
	/**
	 * 	This is a convenience method that simply calls {@link #crop(MBFImage, boolean)}
	 * 
	 *	@param input The input image from which to take the pixels
	 *	@param blackout Whether to blackout pixels that are not part of
	 *		the region or whether to mark them as transparent
	 *	@return An image with the component's pixels cropped
	 */
	public MBFImage extractPixels2d( MBFImage input, boolean blackout )
	{
		return this.crop( input, blackout );
	}
	
	/**
	 * 	Returns an image where the connected component is masked in
	 * 	the image. The image is the same size as the image that is passed in.
	 * 
	 * 	@param input The input image from which to take the size.
	 *	@return An {@link FImage} containing a binary mask; pixels within the
	 *		connected component will have value 1, outside with have value 0
	 */
	public FImage calculateBinaryMask( Image<?,?> input )
	{
		FImage n = new FImage( input.getWidth(), input.getHeight() );
		
		for (Pixel p : pixels) n.pixels[p.y][p.x] = 1;
		
		return n;
	}

	/**
	 * 	Returns an ASCII representation of the connected component
	 * 	as a mask; where the output is "1" for a pixel within the mask
	 * 	and "0" for a pixel outside of the mask.
	 * 
	 *  @return An image string.
	 */
	public String toStringImage() {
		Rectangle bb = this.calculateRegularBoundingBox();
		
		String s = "";
		for (int j=(int)bb.y-1; j<=bb.y+bb.height+1; j++) {
			for (int i=(int)bb.x-1; i<=bb.x+bb.width+1; i++) {
				if (pixels.contains(new Pixel(i,j)))
					s += "1";
				else
					s += "0";
			}
			s += "\n";
		}
		return s;
	}
	
	/**
	 * 	Repositions the connected component so that its bounding box has 
	 * 	its origin at (0,0). Side-affects this connected component.
	 */
	public void reposition() 
	{
		Rectangle bb = this.calculateRegularBoundingBox();
		translate(-(int)bb.x, -(int)bb.y);
	}
	
	/**
	 * 	Returns a mask image for this connected component that will be the
	 * 	size of this component's bounding box. Pixels within the component
	 * 	will be set to value 1.0, while pixels outside of the component
	 * 	will retain their initial value.
	 * 
	 *  @return An {@link FImage} mask image
	 */
	public FImage toFImage() {
		Rectangle bb = this.calculateRegularBoundingBox();
		
		FImage img = new FImage((int)(bb.x+bb.width+1), (int)(bb.y+bb.height+1));
		
		for (Pixel p : pixels)
			img.pixels[p.y][p.x] = 1;
		
		return img;
	}
	
	/**
	 * 	Returns a mask image for this connected component that will be the
	 * 	size of this component's bounding box plus a border of the given amount
	 * 	of padding. Pixels within the component
	 * 	will be set to value 1.0, while pixels outside of the component
	 * 	will retain their initial value.
	 *
	 * 	@param padding The number of pixels padding to add around the outside
	 * 		of the mask.
	 *  @return An {@link FImage} mask image
	 */
	public FImage toFImage(int padding) {
		Rectangle bb = this.calculateRegularBoundingBox();
		
		FImage img = new FImage((int)(bb.x+bb.width+1+2*padding), (int)(bb.y+bb.height+1+2*padding));
		
		for (Pixel p : pixels)
			img.pixels[p.y + padding][p.x + padding] = 1;
		
		return img;
	}

	/**
	 * 	Affine transform the shape with the given transform matrix.
	 * 	Side-affects this component.
	 * 
	 *  @param transform The matrix containing the transform.
	 */
	public void transform(Matrix transform) {
		Matrix p1 = new Matrix(3,1);
		
		for (Pixel p : pixels) {
			p1.set(0,0, p.getX());
			p1.set(1,0, p.getY());
			p1.set(2,0, 1);

			Matrix p2_est = transform.times(p1);

			p.x = (int) Math.rint(p2_est.get(0,0));
			p.y = (int) Math.rint(p2_est.get(1,0));
		}
	}

	/**
	 * 	Returns a normalisation matrix for this component.
	 * 
	 *  @return A normalisation matrix.
	 */
	public Matrix normMatrix() {
		double u20 = calculateMoment(2, 0);
		double u02 = calculateMoment(0, 2);
		double u11 = -calculateMoment(1, 1);
		
		Matrix tf = new Matrix(3,3);
		tf.set(0, 0, u20);
		tf.set(1, 1, u02);
		tf.set(0, 1, u11);
		tf.set(1, 0, u11);
		tf.set(2, 2, 1);
		
		tf = tf.inverse();
		tf = tf.times(1 / Math.sqrt(tf.det()));
		tf = MatrixUtils.sqrt(tf);
		
		tf.set(2, 2, 1);

		return tf;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		int count = in.nextInt();
		
		for (int i=0; i<count; i++) {
			Pixel p = new Pixel();
			p.readASCII(in);
			pixels.add( p );
		}
	}

	@Override
	public String asciiHeader() {
		return "ConnectedComponent";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		int count = in.readInt();
		
		for (int i=0; i<count; i++) {
			Pixel p = new Pixel();
			p.readBinary(in);
			pixels.add( p );
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "CC".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(pixels.size());
		for (Pixel p : pixels) { 
			p.writeASCII(out);
			out.println();
		}
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(pixels.size());
		for (Pixel p : pixels) 
			p.writeBinary(out);
	}
}
