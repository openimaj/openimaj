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
package org.openimaj.image;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.GridProcessor;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.processor.connectedcomponent.render.BlobRenderer;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;

import Jama.Matrix;

/**
 * Base class for representing and manipulating images.  Images are typed
 * by the type of pixel at each coordinate and the concrete subclass type. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <Q> the pixel type
 * @param <I> the actual image of the concrete subclass
 */
public abstract class Image<Q, I extends Image<Q, I>> implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *	Enumerator for representing the type of field 
	 *	interlacing operations.	
	 *	@author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public enum Field 
	{
		/**
		 * Odd field
		 */
		ODD,
		/**
		 * Even field
		 */
		EVEN
	}
	
	/**
	 * Set all pixels to their absolute values, so that all
	 * pixel values in the image will be greater than zero.
	 * 
	 * @return The image with absolute values
	 */
	public abstract I abs();
	
	
	
	/**
	 *	Get bounding box of non-zero-valued pixels around the outside of
	 *	the image. Used by {@link #trim()}.
	 * 
	 *  @return A  rectangle of the boundaries of the non-zero-valued image
	 */
	public abstract Rectangle getContentArea();
	
	/**
	 * Get a rectangle representing the image, with the top-left
	 * at 0,0 and the bottom-right at width,height
	 * @return the bounding rectangle of the image
	 */
	public Rectangle getBounds(){
		return new Rectangle(0,0,this.getWidth(),this.getHeight());
	}

	/**
	 *	Removes zero-valued pixels from around the outside of
	 *	the image. Analagous to {@link String#trim()}.
	 * 
	 *  @return A new image containing the trimmed image.
	 */
	public I trim() {
		Rectangle rect = this.getContentArea();
		return this.extractROI((int)rect.minX(), (int)rect.minY(), (int)(rect.getWidth()), (int)(rect.getHeight()));
	}
	
	/**
	 * Adds the given image to this image and return new image.
	 * 
	 * @param im The image to add 
	 * @return A new image that is the sum of this image and the given image.
	 */
	public I add(Image<?,?> im) {
		I newImage = this.clone();
		newImage.addInline(im);
		return newImage;
	}
	
	/**
	 * Add a value to each pixel and return new image.
	 * 
	 * @param num The value to add to each pixel
	 * @return A new image that is the sum of this image and the given value.
	 */
	public I add(Q num) {
		I newImage = this.clone();
		newImage.addInline(num);
		return newImage;
	}

	/**
	 * Add the given image to this image (side-affects this image).
	 * 
	 * @param im The image to add to this image 
	 * @return A reference to this image.
	 */
	public abstract I addInline(Image<?,?> im);
	
	/**
	 * Add a scalar to each pixel in this image (side-affects this image).
	 * 
	 * @param num The value to add to every pixel in this image.
	 * @return A reference to this image.
	 */
	public abstract I addInline(Q num);
	
	/**
	 * Sets any pixels that are below <code>min</code> or above <code>max</code>
	 * to zero. This method may side-affect this image.
	 * 
	 * @param min The minimum value
	 * @param max The maximum value
	 * @return The clipped image.
	 */
	public abstract I clip(Q min, Q max);

	/**
	 * Set all values greater than the given value to zero. This method
	 * may side-affect this image.
	 * 
	 * @param thresh The value over which pixels are clipped to zero.
	 * @return The clipped image.
	 */
	public abstract I clipMax(Q thresh);

	/**
	 * Set all values less than the given value to zero. This method may
	 * side-affect this image.
	 * 
	 * @param thresh The value below which pixels are clipped to zero.
	 * @return The clipped image.
	 */
	public abstract I clipMin(Q thresh);
	
	/**
	 * Deep copy of an image (internal image buffers copied).
	 * @return A copy of this image.
	 */
	@Override
	public abstract I clone();

	/**
	 * Divide each pixel of the image by corresponding pixel in the given
	 * image. This method should return a new image.
	 * 
	 * @param im image The image to divide this image by.
	 * @return A new image containing the result.
	 */
	public I divide(Image<?,?> im) {
		I newImage = this.clone();
		newImage.divideInline(im);
		return newImage;
	}
	
	/**
	 * Divide each pixel of the image by the given scalar value. This method
	 * should return a new image.  
	 * 
	 * @param val The value to divide the pixels in this image by.
	 * @return A new image containing the result.
	 */
	public I divide(Q val) {
		I newImage = this.clone();
		newImage.divideInline(val);
		return newImage;
	}
	
	/**
	 * Divide each pixel in this image by the corresponding pixel value in
	 * the given image. This method should side-affect this image.
	 *   
	 * @param im image The image to divide this image by.
	 * @return A reference to this image containing the result.
	 */
	public abstract I divideInline(Image<?,?> im);
	
	/**
	 * Divide each pixel of the image by the given scalar value. This method
	 * should side-affect this image. 
	 * 
	 * @param val The value to divide each pixel by.
	 * @return A reference to this image containing the result.
	 */
	public abstract I divideInline(Q val);
	
	/**
	 * Draw onto this image lines drawn with the given colour between the
	 * points given. No points are drawn. Side-affects this image.
	 *  
	 * @param pts The point list to draw onto this image.
	 * @param col The colour to draw the lines
	 */
	public void drawConnectedPoints(List<? extends Point2d> pts, Q col) {
		Point2d p0 = pts.get(0);  
		for (int i=1; i<pts.size(); i++) {
			Point2d p1 = pts.get(i);
			
			int x0 = Math.round(p0.getX());
			int y0 = Math.round(p0.getY());
			int x1 = Math.round(p1.getX());
			int y1 = Math.round(p1.getY());
			
			drawLine(x0, y0, x1, y1,col);
			
			p0 = p1;
		}
	}

	/**
	 * Draw into this image the provided image at the given coordinates.
	 * Parts of the image outside the bounds of this image
	 * will be ignored. Side-affects this image.
	 * 
	 * @param image The image to draw. 
	 * @param x The x-coordinate of the top-left of the image
	 * @param y The y-coordinate of the top-left of the image
	 */
	public void drawImage(I image, int x, int y) {
		int stopx = Math.min(getWidth(), x + image.getWidth());
		int stopy = Math.min(getHeight(), y + image.getHeight());
		int startx = Math.max(0, x);
		int starty = Math.max(0, y);
		
		for (int yy=starty; yy<stopy; yy++)
			for (int xx=startx; xx<stopx; xx++)
				setPixel(xx, yy, image.getPixel(xx-x,yy-y));
	}
	
	/**
	 * Draw into this image the provided image at the given coordinates ignoring
	 * certain pixels. Parts of the image outside the bounds of this image will 
	 * be ignored. Side-affects this image. Pixels in the ignore list will be
	 * stripped from the image to draw.
	 * 
	 * @param image The image to draw. 
	 * @param x The x-coordinate of the top-left of the image
	 * @param y The y-coordinate of the top-left of the image
	 * @param ignoreList The list of pixels to ignore when copying the image
	 */
	public void drawImage(I image, int x, int y, Q ... ignoreList) {
		int stopx = Math.min(getWidth(), x + image.getWidth());
		int stopy = Math.min(getHeight(), y + image.getHeight());
		int startx = Math.max(0, x);
		int starty = Math.max(0, y);
		
		for (int yy=starty; yy<stopy; yy++)
			for (int xx=startx; xx<stopx; xx++)
			{
				Q val = image.getPixel(xx-x, yy-y);
				if(Arrays.binarySearch(ignoreList, val, getPixelComparator())<0)
					setPixel(xx, yy, val);
			}
				
	}

	/**
	 *	Returns a pixel comparator that is able to compare equality of pixels
	 *	in the given image type.
	 * 
	 *	@return A {@link Comparator} that compares pixels.
	 */
	public abstract Comparator<? super Q> getPixelComparator() ;

	/**
	 * Draw a line from the coordinates specified by <code>(x1,y1)</code> 
	 * at an angle of <code>theta</code> with the given length, thickness
	 * and colour. Side-affects this image.
	 * 
	 * @param x1 The x-coordinate to start the line.
	 * @param y1 The y-coordinate to start the line.
	 * @param theta The angle at which to draw the line.
	 * @param length The length to draw the line.
	 * @param thickness The thickness to draw the line.
	 * @param col The colour to draw the line.
	 */
	public abstract void drawLine(int x1, int y1, double theta, int length, int thickness, Q col);
	
	/**
	 * Draw a line from the coordinates specified by <code>(x1,y1)</code> 
	 * at an angle of <code>theta</code> with the given length and colour.
	 * Line-thickness will be 1. Side-affects this image.
	 * 
	 * @param x1 The x-coordinate to start the line.
	 * @param y1 The y-coordinate to start the line.
	 * @param theta The angle at which to draw the line.
	 * @param length The length to draw the line.
	 * @param col The colour to draw the line.
	 */
	public void drawLine(int x1, int y1, double theta, int length, Q col) {
		drawLine(x1, y1, theta, length, 1, col);
	}
	
	/**
	 * Draw a line from the coordinates specified by <code>(x0,y0)</code> to 
	 * the coordinates specified by <code>(x1,y1)</code> using the given 
	 * color and thickness. Side-affects this image.
	 * 
	 * @param x0 The x-coordinate at the start of the line.
	 * @param y0 The y-coordinate at the start of the line. 
	 * @param x1 The x-coordinate at the end of the line.
	 * @param y1 The y-coordinate at the end of the line.
	 * @param thickness The thickness which to draw the line.
	 * @param col The colour in which to draw the line.
	 */
	public abstract void drawLine(int x0, int y0, int x1, int y1, int thickness, Q col);

	/**
	 * Draw a line from the coordinates specified by <code>(x0,y0)</code> to 
	 * <code>(x1,y1)</code> using the given colour. The line thickness will
	 * be 1 pixel. Side-affects this image.
	 * 
	 * @param x0 The x-coordinate at the start of the line.
	 * @param y0 The y-coordinate at the start of the line. 
	 * @param x1 The x-coordinate at the end of the line.
	 * @param y1 The y-coordinate at the end of the line.
	 * @param col The colour in which to draw the line.
	 */
	public void drawLine(int x0, int y0, int x1, int y1, Q col) {
		drawLine(x0, y0, x1, y1, 1, col);
	}
	
	/**
	 * Draw a line from the specified Line2d object
	 * 
	 * @param line
	 * @param col The colour in which to draw the line.
	 */
	public void drawLine(Line2d line, int thickness, Q col) {
		drawLine((int)line.begin.getX(), (int)line.begin.getY(), (int)line.end.getX(), (int)line.end.getY(), thickness, col);
	}

	/**
	 * Draw a dot centered on the given location (rounded to nearest integer 
	 * location) at the given size and with the given color. 
	 * Side-affects this image. 
	 * 
	 * @param p The coordinates at which to draw the point 
	 * @param col The colour to draw the point
	 * @param size The size at which to draw the point.
	 */
	public abstract void drawPoint(Point2d p, Q col, int size);

	/**
	 * Draw the given list of points using {@link #drawPoint(Point2d, Object, int)}
	 * with the given colour and size. Side-affects this image.
	 * 
	 * @param pts The list of points to draw.
	 * @param col The colour to draw each point.
	 * @param size The size to draw each point.
	 */
	public void drawPoints(Iterable<? extends Point2d> pts, Q col, int size) {
		for (Point2d p : pts)
			drawPoint(p, col, size);
	}
	
	/**
	 * Draw the given polygon in the specified colour with the given thickness lines.
	 * Side-affects this image.
	 * 
	 * @param p The polygon to draw.
	 * @param thickness The thickness of the lines to use
	 * @param col The colour to draw the lines in
	 */
	public abstract void drawPolygon(Polygon p, int thickness, Q col);

	/**
	 * Draw the given polygon in the specified colour. Uses
	 * {@link #drawPolygon(Polygon, int, Object)} with line thickness 1.
	 * Side-affects this image.
	 * 
	 * @param p The polygon to draw.
	 * @param col The colour to draw the polygon in.
	 */
	public void drawPolygon(Polygon p, Q col) {
		drawPolygon(p, 1, col);
	}

	/**
	 * Draw the given polygon, filled with the specified colour.
	 * Side-affects this image.
	 * 
	 * @param p The polygon to draw.
	 * @param col The colour to fill the polygon with.
	 */
	public void drawPolygonFilled(Polygon p, Q col) {
		drawPolygon(p, col);
		
		ConnectedComponent cc = new ConnectedComponent(p);
		cc.process(new BlobRenderer<Q>(this, col));
	}
	
	/**
	 * Draw the given shape in the specified colour with the given thickness lines.
	 * Side-affects this image.
	 * 
	 * @param s The shape to draw.
	 * @param thickness The thickness of the lines to use
	 * @param col The colour to draw the lines in
	 */
	public void drawShape(Shape s, int thickness, Q col) {
		drawPolygon(s.asPolygon(), thickness, col);
	}

	/**
	 * Draw the given shape in the specified colour. Uses
	 * {@link #drawPolygon(Polygon, int, Object)} with line thickness 1.
	 * Side-affects this image.
	 * 
	 * @param p The shape to draw.
	 * @param col The colour to draw the polygon in.
	 */
	public void drawShape(Shape p, Q col) {
		drawShape(p, 1, col);
	}

	/**
	 * Draw the given shape, filled with the specified colour. 
	 * Side-affects this image.
	 * 
	 * @param s The shape to draw.
	 * @param col The colour to fill the polygon with.
	 */
	public void drawShapeFilled(Shape s, Q col) {
		drawShape(s, col);
		
		ConnectedComponent cc = new ConnectedComponent(s);
		cc.process(new BlobRenderer<Q>(this, col));
	}

	/**
	 * 	Calculates straight line segments along a Bezier curve.	
	 * 
	 *	@param nPoints The number of points to divide the bezier curve into
	 *	@param p1 The point at the end of the line
	 *	@param p2 The point at the other end of the line
	 *	@param c1 The control point associated with p1
	 *	@param c2 The control point associated with p2
	 *	@return An array of points representing points along the curve
	 */
	protected Point2d[] computeBezierPoints( int nPoints, Point2d p1, 
			Point2d p2, Point2d c1, Point2d c2 )
	{
		Point2d[] bezier = new Point2d[nPoints];
		
		float inc = (1f/nPoints);
		float t = 0;
		
		for( int j = 0; j < nPoints; j++ )
		{
			float t1 = 1 - t;
			
			float t13 = t1*t1*t1;
			float t13a = 3*t*t1*t1;
			float t13b = 3*t*t*t1;
			float t13c = t*t*t;
			
			float x = t13 * p1.getX();
			x += t13a * c1.getX();
			x += t13b * c2.getX();
			x += t13c * p2.getX();
			
			float y = t13 * p1.getY();
			y += t13a * c1.getY();
			y += t13b * c2.getY();
			y += t13c * p2.getY();
			
			bezier[j] = new Point2dImpl( x, y );
			
			t += inc;
		}
		
		return bezier;
	}

	/**
	 * 	Draw a cubic Bezier curve into the image with 100 point accuracy.
	 * 
	 *	@param p1 One end point of the line
	 *	@param p2 The other end point of the line
	 *	@param c1 The control point associated with p1
	 *	@param c2 The control point associated with p2
	 *	@param thickness The thickness to draw the line
	 *	@param col The colour to draw the line
	 *	@return The points along the bezier curve
	 */
	public Point2d[] drawCubicBezier( Point2d p1, Point2d p2, 
			Point2d c1, Point2d c2, int thickness, Q col )
	{
		return drawCubicBezier( p1, p2, c1, c2, thickness, col, 100 );
	}
	
	/**
	 * 	Draw a cubic Bezier curve into the image with the given accuracy
	 * 
	 *	@param p1 One end point of the line
	 *	@param p2 The other end point of the line
	 *	@param c1 The control point associated with p1
	 *	@param c2 The control point associated with p2
	 *	@param thickness The thickness to draw the line
	 *	@param col The colour to draw the line
	 *	@param nPoints The number of points to divide the curve into
	 *	@return The points along the bezier curve
	 */
	public Point2d[] drawCubicBezier( Point2d p1, Point2d p2, 
			Point2d c1, Point2d c2, int thickness, Q col, int nPoints )
	{
		Point2d[] b = computeBezierPoints( nPoints, p1, p2, c1, c2 );
		
		Point2d last = null;
		for( Point2d p : b )
		{
			if( last != null )
				drawLine( (int)last.getX(), (int)last.getY(), 
						  (int)p.getX(), (int)p.getY(), 3, col );
			last = p;
		}
		
		return b;
	}
	
	/**
	 * Extract a rectangular region about the centre of the image with
	 * the given width and height. The method will return a box that
	 * extends <code>width/2</code> and <code>height/2</code> from the
	 * centre point so that the centre point of the extracted box is
	 * also the centre point of the image.
	 * 
	 * @param w The width of the box to extract
	 * @param h The height of the box to extract 
	 * @return A new image centred around the centre of the image.
	 */
	public I extractCenter(int w, int h){
		int selectedX = (int) Math.floor(this.getWidth() / 2.0);
		int selectedY = (int) Math.floor(this.getHeight() / 2.0);
		
		return this.extractCenter(selectedX, selectedY, w, h);
	}

	/**
	 * Extract a rectangular region centred on a given point. The method
	 * will return a box that extends <code>width/2</code> and <code>height/2</code>
	 * from the given point <code>(x,y)</code> such that the centre point of
	 * the extracted box is the same as the point <code>(x,y)</code> in this
	 * image.
	 * 
	 * @param x Center point of the rectangle to extract
	 * @param y center point of the rectangle to extract
	 * @param w The width of the rectangle to extract
	 * @param h The height of the rectangle to extract
	 * @return A new image centred around the centre of the image.
	 */
	public I extractCenter(int x, int y, int w, int h){
		if(w % 2 == 0 ) w+=1;
		if(h % 2 == 0 ) h+=1;
		
		int roiX = Math.max(0,x-(int)(w/2.0));
		int roiY = Math.max(0,y-(int)(h/2.0));
		
		int newWidth = (int)(w / 2.0) + 1 + (x - roiX);
		int newHeight = (int)(h / 2.0) + 1 + (y - roiY);
		
		int roiW = roiX + newWidth > this.getWidth() ? this.getWidth() - roiX : newWidth;
		int roiH = roiY + newHeight > this.getHeight() ? this.getHeight() - roiY : newHeight;
		
		return this.extractROI(roiX, roiY, roiW, roiH);
	}

	/**
	 * Extract a rectangular region of interest from this image and put
	 * it in the given image. Coordinate <code>(0,0)</code> is the top-left corner.
	 * The width and height of the extracted image should be determined
	 * from the given image's width and height. Returns a new image.
	 * 
	 * @param x The leftmost coordinate of the rectangle to extract 
	 * @param y The topmost coordinate of the rectangle to extract
	 * @param img The destination image
	 * @return A reference to the destination image containing the result
	 */
	public abstract I extractROI(int x, int y, I img);

	/**
	 * Extract a rectangular region of interest of the given width and height.
	 * Coordinate <code>(0,0)</code> is the top-left corner. Returns a new image.
	 * 
	 * @param x The leftmost coordinate of the rectangle to extract
	 * @param y The topmost coordinate of the rectangle to extract
	 * @param w The width of the rectangle to extract
	 * @param h The height of the rectangle to extract
	 * @return A new image representing the selected region
	 */
	public abstract I extractROI(int x, int y, int w, int h);
	
	/**
	 * Extract a rectangular region of interest of the given width and height.
	 * Coordinate <code>(0,0)</code> is the top-left corner. Returns a new image.
	 * 
	 * @param x The leftmost coordinate of the rectangle to extract
	 * @param y The topmost coordinate of the rectangle to extract
	 * @param w The width of the rectangle to extract
	 * @param h The height of the rectangle to extract
	 * @return A new image representing the selected region
	 */
	public I extractROI(Rectangle r) {
		return extractROI((int)r.x, (int)r.y, (int)r.width, (int)r.height);
	}

	/**
	 * Fill this image with the given colour. Should overwrite all other
	 * data stored in this image. Side-affects this image.
	 * 
	 * @param colour the colour to fill the image with
	 * @return A reference to this image.
	 */
	public abstract I fill(Q colour);

	/**
	 * Get the width (number of columns) in this image.
	 *  
	 * @return the image width
	 */
	public abstract int getWidth();

	/**
	 * Get the given field of this image. Used for deinterlacing video, this
	 * should return a new image containing the deinterlaced image. The returned 
	 * image will be half the height of this image.
	 * 
	 * @param f The {@link Field} to extract from this image
	 * @return An image containing only the odd or even fields.
	 */
	public abstract I getField(Field f);
	
	/**
	 * Get the given field of this image, maintaining the image's aspect ratio
	 * by doubling the fields. Used for deinterlacing video, this should return
	 * a new image containing the deinterlaced image. The returned image should
	 * be the same size as this image.
	 * 
	 * @param f The {@link Field} to extract from this image
	 * @return An image containing the odd or even fields doubled.
	 */
	public abstract I getFieldCopy(Field f);
	
	/**
	 * Get the given field of this image, maintaining the image's aspect
	 * ratio by interpolating between the fields. Used for deinterlacing
	 * video, this should return a new image containing the detinterlaced image.
	 * The returned image should be the same size as this image.
	 * 
	 * @param f The {@link Field} to extract from this image.
	 * @return An image containing the odd or even fields with interpolated
	 * 		rows between.
	 */
	public abstract I getFieldInterpolate(Field f);

	/**
	 * Get the height of this image. This is a syntactic sugar method for
	 * {@link #getHeight()}.
	 * 
	 * @return The image height in pixels.
	 */
	public int getRows() {
		return getHeight();
	}
	
	/**
	 * Get the value of the pixel at coordinate <code>(x, y)</code>.
	 * 
	 * @param x The x-coordinate to get
	 * @param y The y-coordinate to get
	 * 
	 * @return The pixel value at (x, y)
	 */
	public abstract Q getPixel(int x, int y);
	
	/**
	 * Get the value of a sub-pixel using linear-interpolation.
	 * 
	 * @param x The x-coordinate to get
	 * @param y The y-coordinate to get
	 * @return The value of the interpolated point at <code>(x,y)</code>
	 */
	public abstract Q getPixelInterp(double x, double y);
	
	/**
	 * Get the value of a sub-pixel using linear-interpolation. Also specify the colour of the background (for interpolation at the edge)
	 * 
	 * @param x The x-coordinate to get.
	 * @param y The y-coordinate to get.
	 * @param backgroundColour The colour of the background pixel.
	 * @return The value of the interpolated point at <code>(x,y)</code>
	 */
	public abstract Q getPixelInterp(double x, double y, Q backgroundColour);
	
	/**
	 * 	Returns the pixels in this image as a vector (an array of the pixel
	 * 	type).
	 *  
	 *  @param f The array into which to place the data
	 *  @return The pixels in the image as a vector (a reference to the given array).
	 */
	public Q[] getPixelVector( Q[] f )
	{
		for( int y = 0; y < getHeight(); y++ )
			for( int x = 0; x < getWidth(); x++ )
				f[x+y*getWidth()] = getPixel(x,y);
		
		return f;
	}
	
	/**
	 * Returns the image height in pixels.
	 * 
	 * @return The image height in pixels.
	 */
	public abstract int getHeight();
	
	/**
	 * Get the image width in pixels. This is syntactic 
	 * sugar for {@link #getWidth()};
	 * 
	 * @return The image width in pixels.
	 */
	public int getCols() {
		return getWidth();
	}
	
	/**
	 * Copy pixels from given ARGB buffer image into this image. Side-affects this
	 * image.
	 * 
	 * @param pixelData buffer of ARGB packed integer pixels 
	 * @param width the width of the buffer
	 * @param height the height of the buffer  
	 * 
	 * @return A reference to this image.
	 */
	public abstract I internalAssign(int [] pixelData, int width, int height);
	
	/**
	 * Copy the internal state from another image of the same type.
	 * 
	 * @param im The source image to make a copy of.
	 * @return A reference to this image.
	 */
	public abstract I internalAssign(I im);

	/**
	 * Invert the image pixels by finding the maximum value and subtracting
	 * each pixel value from that maximum. 
	 * 
	 * @return A reference to this image.
	 */
	public abstract I inverse();
	
	/**
	 * Find the maximum pixel value.
	 * 
	 * @return The maximum pixel value
	 */
	public abstract Q max();
	
	/**
	 * Find the minimum pixel value.
	 * 
	 * @return The minimum pixel value
	 */
	public abstract Q min();
	
	/**
	 * Multiply the pixel values in this image with the corresponding pixel
	 * values in the given image. This method returns a new image.
	 * 
	 * @param im The image to multiply with this one
	 * @return A new image containing the result.
	 */
	public I multiply(Image<?,?> im) {
		I newImage = this.clone();
		newImage.multiplyInline(im);
		return newImage;
	}
	
	/**
	 * Multiply each pixel of this by the given scalar and return new image.
	 * 
	 * @param num The scalar which to multiply the image by 
	 * @return A new image containing the result
	 */
	public I multiply(Q num) {
		I newImage = this.clone();
		newImage.multiplyInline(num);
		return newImage;
	}
	
	/**
	 * Multiply each pixel in this image by the corresponding pixel in
	 * the given image. This method side-affects this image.
	 * 
	 * @param im The image to multiply with this image.
	 * @return A reference to this image.
	 */
	public abstract I multiplyInline(Image<?,?> im);
	
	/**
	 * Multiply each pixel of this by the given scalar. This method side-affects
	 * this image.
	 * 
	 * @param num The scalar to multiply this image by.
	 * @return A reference to this image.
	 */
	public abstract I multiplyInline(Q num);
	
	/**
	 * Create a new instance of this image subclass with given dimensions.
	 * @param width The image width
	 * @param height The image height
	 * 
	 * @return A new instance of an image of type <code>I</code>
	 */
	public abstract I newInstance(int width, int height);
	
	/**
	 * Normalise all pixel values to fall within the range 0.0 - 1.0. This should
	 * be scaled by both the maximum and minimum values. This method side-affects
	 * this image.
	 * 
	 * @return A reference to this image.
	 */
	public abstract I normalise();
	
	/**
	 * Process this image with the given {@link GridProcessor} and return new 
	 * image containing the result.
	 * 
	 * @param p {@link GridProcessor} to apply to this image.
	 * @return A new image containing the result.
	 */
	public I process(GridProcessor<Q,I> p) {
		int height = p.getVericalGridElements();
		int width = p.getHorizontalGridElements();
		I newImage = this.newInstance( width, height );
		newImage.zero();
		
		int gridWidth = getWidth()/width;
		int gridHeight = getHeight()/height;
		for( int y = 0; y < height; y++ )
			for( int x = 0; x < width; x++ )
				newImage.setPixel( x, y, p.processGridElement( this.extractROI( gridWidth*x, gridHeight*y, gridWidth, gridHeight ) ) );
		
		return newImage;
	}
	
	/**
	 * Process this image with an {@link ImageProcessor} and return new image
	 * containing the result.
	 * 
	 * @param p The {@link ImageProcessor} to apply to this image.
	 * @return A new image containing the result.
	 */
	public I process(ImageProcessor<I> p)  {
		I newImage = this.clone();
		newImage.processInline(p);
		return newImage;
	}
	
	/**
	 * Process this image with an {@link ImageProcessor} and an optional set
	 * of extra images, and return new image containing the result.
	 * 
	 * @param p The {@link ImageProcessor} to apply.
	 * @param images Extra images that are passed to the processor.
	 * @return A new image containing the result.
	 */
	public I process(ImageProcessor<I> p, Image<?,?>... images) {
		I newImage = this.clone();
		newImage.processInline(p, images);
		return newImage;
	}

	/**
	 * Process this image with the given {@link KernelProcessor} and 
	 * return new image containing the result.
	 * 
	 * @param p The {@link KernelProcessor} to apply.
	 * @return A new image containing the result.
	 */
	public I process(KernelProcessor<Q,I> p) {
		return process(p, false);
	}
	
	/**
	 * Process this image with the given {@link KernelProcessor} and 
	 * return new image containing the result.
	 * 
	 * @param p The {@link KernelProcessor} to apply.
	 * @param pad Should the image be zero padded so the 
	 * 	kernel reaches the edges of the output
	 * @return A new image containing the result.
	 */ 
	public I process(KernelProcessor<Q,I> p, boolean pad) {
		I newImage = this.clone();
		newImage.zero();
		
		int kh = p.getKernelHeight();
		int kw = p.getKernelWidth();
		
		int hh = p.getKernelHeight() / 2;
		int hw = p.getKernelWidth() / 2;
		
		I tmp = newInstance( kw, kh );
		
		if (!pad) {
			for( int y = hh; y < getHeight() - (kh - hh); y++ ) {
				for( int x = hw; x < getWidth() - (kw - hw); x++ ) {
					newImage.setPixel(x, y, p.processKernel( this.extractROI( x - hw, y - hh, tmp ) ));
				}
			}
		} else {
			for( int y = 0; y < getHeight(); y++ ) {
				for( int x = 0; x < getWidth(); x++ ) {
					newImage.setPixel(x, y, p.processKernel( this.extractROI( x - hw, y - hh, tmp ) ));
				}
			}
		}
		
		return newImage;
	}

	/**
	 * 	Process this image with the given {@link PixelProcessor} and return
	 * 	a new image containing the result.
	 * 
	 *  @param p The {@link PixelProcessor} to apply.
	 *  @return A new image containing the result.
	 */
	public I process(PixelProcessor<Q> p) {
		I newImage = this.clone();
		newImage.processInline(p);
		return newImage;
	}
	
	/**
	 * 	Process this image with the given {@link PixelProcessor} with an optional
	 * 	set of images and return a new image containing the result.
	 * 
	 *  @param p The {@link PixelProcessor} to apply.
	 *  @param images Extra set of images to pass to the processor.
	 *  @return A new image containing the result.
	 */
	public I process(PixelProcessor<Q> p, Image<?,?>... images) {
		I newImage = this.clone();
		newImage.processInline(p, images);
		return newImage;
	}
	
	/**
	 * 	Process this image with the given {@link ImageProcessor} side-affecting
	 * 	this image.
	 * 
	 *  @param p The {@link ImageProcessor} to apply.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInline(ImageProcessor<I> p) {
		p.processImage((I)this);
		return (I)this;
	}

	/**
	 *	Process this image with the given {@link ImageProcessor} and an optional
	 *	set of images, side-affecting this image.
	 * 
	 *  @param p The {@link ImageProcessor} to apply.
	 *  @param images Extra set of image to pass to the processor.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInline(ImageProcessor<I> p, Image<?,?>... images) {
		p.processImage((I)this, images);
		return (I)this;
	}
	
	/**
	 * 	Process this image with the given {@link KernelProcessor} side-affecting
	 * 	this image.
	 * 
	 *  @param p The {@link KernelProcessor} to apply.
	 *  @return A reference to this image containing the result.
	 */
	public I processInline(KernelProcessor<Q,I> p) {
		return processInline(p, false);
	}
	
	/**
	 * 	Process this image with the given {@link KernelProcessor} side-affecting
	 * 	this image.
	 * 
	 *  @param p The {@link KernelProcessor} to apply.
	 *  @param pad Should the image be zero padded so the 
	 * 	  kernel reaches the edges of the output
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInline(KernelProcessor<Q,I> p, boolean pad) {
		I newImage = process(p, pad);
		this.internalAssign(newImage);
		return (I)this;
	}
	
	/**
	 * 	Process this image with the given {@link PixelProcessor} side-affecting
	 * 	this image.
	 * 
	 *  @param p The {@link PixelProcessor} to apply.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInline(PixelProcessor<Q> p) {
		for (int y=0; y<getHeight(); y++) {
			for (int x=0; x<getWidth(); x++) {
				setPixel(x, y, p.processPixel(getPixel(x,y)));
			}
		}
		
		return (I)this;
	}
	
	/**
	 * 	Process this image with the given {@link PixelProcessor} and an optional
	 * 	set of images, side-affecting this image.
	 * 
	 *  @param p The {@link PixelProcessor} to apply.
	 *  @param images Extra set of images that are passed to the processor.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInline(PixelProcessor<Q>p, Image<?,?>... images) {
		Number[] otherpixels = new Number[images.length];
		for (int y=0; y<getHeight(); y++) {
			for (int x=0; x<getWidth(); x++) {
				for (int i=0; i<images.length; i++) otherpixels[i] = (Number)images[i].getPixel(x, y);
				setPixel(x, y, p.processPixel(getPixel(x,y), otherpixels));
			}
		}
		return (I)this;
	}
	
	/**
	 * 	Process this image with the given {@link PixelProcessor} only affecting
	 * 	those pixels where the mask is non-zero. Returns a new image.
	 * 
	 *  @param mask The mask to apply to the processing.
	 *  @param p The {@link PixelProcessor} to apply.
	 *  @return A new image containing the result.
	 */
	public I processMasked(FImage mask, PixelProcessor<Q> p) {
		I newImage = this.clone();
		newImage.processMaskedInline(mask, p);
		return newImage;
	}
	
	/**
	 * 	Process this image with the given {@link PixelProcessor} and an extra
	 * 	set of optional images, only affecting those pixels where the mask is 
	 * 	non-zero. Returns a new image.
	 * 
	 *  @param mask The mask to apply to the processor.
	 *  @param p The {@link PixelProcessor} to apply.
	 *  @param images An optional set of extra images passed to the processor.
	 *  @return A new image containing the result.
	 */
	public I processMasked(FImage mask, PixelProcessor<Q> p, Image<?,?>... images) {
		I newImage = this.clone();
		newImage.processMaskedInline(mask, p, images);
		return newImage;
	}
	
	/**
	 * 	Process this image with the given {@link PixelProcessor} and an extra
	 * 	set of optional images, only affecting those pixels where the mask is 
	 * 	non-zero. Side-affects this image.
	 * 
	 *  @param mask The mask to apply to the processor.
	 *  @param p The {@link PixelProcessor} to apply.
	 *  @param images An optional set of extra images passed to the processor.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processMaskedInline(FImage mask, PixelProcessor<Q>p, Image<?,?>... images) {
		Number[] otherpixels = new Number[images.length];
		for (int y=0; y<getHeight(); y++) {
			for (int x=0; x<getWidth(); x++) {
				if (mask.pixels[y][x] == 0) continue;
				for (int i=0; i<images.length; i++) otherpixels[i] = (Number)images[i].getPixel(x, y);
				setPixel(x, y, p.processPixel(getPixel(x,y), otherpixels));
			}
		}
		return (I)this;
	}
	
	
	/**
	 * Sets the pixel at <code>(x,y)</code> to the given value. Side-affects
	 * this image.
	 * 
	 * @param x The x-coordinate of the pixel to set
	 * @param y The y-coordinate of the pixel to set
	 * @param val The value to set the pixel to.
	 */
	public abstract void setPixel(int x, int y, Q val);
	
	/**
	 * Subtract the corresponding pixel value from the given image from the
	 * pixel values in this image. Returns a new image.
	 * 
	 * @param im The image to subtract from this image.
	 * @return A new image containing the result.
	 */
	public I subtract(Image<?,?> im) {
		I newImage = this.clone();
		newImage.subtractInline(im);
		return newImage;
	}
	
	/**
	 * Subtract a scalar from every pixel value in this image and return 
	 * new image.
	 * 
	 * @param num A value to subtract from each pixel. 
	 * @return A new image containing the result.
	 */
	public I subtract(Q num) {
		I newImage = this.clone();
		newImage.subtractInline(num);
		return newImage;
	}
	
	/**
	 * Subtract the corresponding pixel value from the given image from the
	 * pixel values in this image. Side-affects this image.
	 * 
	 * @param im The image to subtract from this image.
	 * @return A reference to this containing the result.
	 */
	public abstract I subtractInline(Image<?,?> im);
	
	/**
	 * Subtract a scalar from every pixel value in this image. Side-affects
	 * this image.
	 * 
	 * @param num A value to subtract from each pixel. 
	 * @return A reference to this image containing the result.
	 */
	public abstract I subtractInline(Q num);
	
	/**
	 * Set all values less than the given threshold to 0 and all others to 1.
	 * Side-affects this image.
	 * 
	 * @param thresh The threshold value 
	 * @return A reference to this image containing the result.
	 */
	public abstract I threshold(Q thresh);

	/**
	 * 	Returns a 1D array representation of this image with each pixel
	 *  represented as a packed ARGB integer.
	 * 
	 *  @return An array of ARGB pixels.
	 */
	public abstract int [] toPackedARGBPixels();
	
	/**
	 * Convert the image to a byte representation suitable for writing to a pnm
	 * type format. Each byte should represent a single pixel. Multiband images 
	 * should interleave the data; e.g. [R1,G1,B1,R2,G2,B2...etc.]
	 * 
	 * @return This image as a byte array
	 */
	public abstract byte[] toByteImage();
	
	/**
	 * Apply a transform matrix to the image and returns the
	 * result as a new image.
	 * 
	 * @param transform The transform matrix to apply.
	 * @return A new image containing the result.
	 */
	public I transform(Matrix transform) {
		boolean unset = true;
		double minX=0,minY=0,maxX=0,maxY=0;
		double[][][] extrema = new double[][][]{
				{{0},{0},{1}},
				{{0},{this.getHeight()},{1}},
				{{this.getWidth()},{0},{1}},
				{{this.getWidth()},{this.getHeight()},{1}},
		};
		for(double[][] ext : extrema){
			Matrix tmp = transform.times(Matrix.constructWithCopy(ext));
			if(unset)
			{
				minX = maxX = tmp.get(0, 0);
				maxY = minY = tmp.get(1, 0);
				unset = false;
			}
			else{
				if(tmp.get(0, 0) > maxX) maxX = tmp.get(0, 0);
				if(tmp.get(1, 0) > maxY) maxY = tmp.get(1, 0);
				if(tmp.get(0, 0) < minX) minX = tmp.get(0, 0);
				if(tmp.get(1, 0) < minY) minY = tmp.get(1, 0);
			}
		}
		I output = this.newInstance((int)(Math.abs(maxX - minX)),(int)(Math.abs(maxY-minY)));
		Matrix invTrans = transform.inverse();
		for(int x = 0; x < output.getWidth(); x++){
			for(int y = 0; y < output.getHeight(); y++){
				
				Matrix pre = Matrix.constructWithCopy(
						new double[][]{{x},{y},{1.0}}
				);
				Matrix post = invTrans.times(pre);
				double oldx = post.get(0, 0);
				double oldy = post.get(1, 0);
				if(oldx < 0 || oldx >= this.getWidth() || oldy < 0 || oldy >= this.getHeight()) continue;
				output.setPixel(x, y, this.getPixelInterp(oldx, oldy));
			}
		}
		return output;
	}
	
	/**
	 * Set all pixels in the image to zero. Side-affects this image.
	 * 
	 * @return A reference to this image containing the result.
	 */
	public abstract I zero();
	
	/**
	 * Adds padding as in {@link FImage#padding}. The padding colour is the colour of the closest border pixel.
	 * @param paddingWidth
	 * @param paddingHeight
	 * @return
	 */
	public I padding(int paddingWidth, int paddingHeight) {
		return this.padding(paddingWidth, paddingHeight,null);
	}
	
	/**
	 * Adds this many pixels to both sides of the image such that the new image width = padding + width + padding
	 * with the original image in the middle
	 * @param paddingWidth left and right padding width
	 * @param paddingHeight top and bottom padding width
	 * @param paddingColour colour of padding, if null the closes border pixel is used
	 */
	// 
	public I padding(int paddingWidth, int paddingHeight, Q paddingColour) {
		I out = this.newInstance(paddingWidth + this.getWidth() + paddingWidth, paddingHeight + this.getHeight() + paddingHeight);
		I clone = this.clone();
		out.drawImage(clone, paddingWidth, paddingHeight);
		int rightLimit = paddingWidth+this.getWidth();
		int bottomLimit = paddingHeight+this.getHeight();
		// Fill the padding with a colour if it isn't null
		if(paddingColour != null)
			for(int y = 0;y<out.getHeight();y++){
				for(int x = 0;x<out.getWidth();x++){
					if(x>=paddingWidth&&x<rightLimit&&y>=paddingHeight&&y<bottomLimit) continue;
					out.setPixel(x, y, paddingColour);
				}
			}
		else
			for(int y = 0;y<out.getHeight();y++){
				for(int x = 0;x<out.getWidth();x++){
					if(x>=paddingWidth&&x<rightLimit&&y>=paddingHeight&&y<bottomLimit) continue;
					if(x < paddingWidth && y < paddingHeight) 
						out.setPixel(x, y, this.getPixel(0, 0)); // Top Left
					else if(x < paddingWidth && y >= bottomLimit) 
						out.setPixel(x, y, this.getPixel(0, this.getHeight()-1)); // Bottom Left
					else if(x >= rightLimit && y < paddingHeight) 
						out.setPixel(x, y, this.getPixel(this.getWidth()-1, 0)); // Top Right
					else if(x >= rightLimit && y >= bottomLimit) 
						out.setPixel(x, y, this.getPixel(this.getWidth()-1, this.getHeight()-1)); // Bottom Right
					else{
						if(x < paddingWidth) out.setPixel(x, y, this.getPixel(0, y-paddingHeight)); // Left
						else if(x >= rightLimit) out.setPixel(x, y, this.getPixel(this.getWidth()-1, y-paddingHeight)); // Right
						else if(y < paddingHeight) out.setPixel(x, y, this.getPixel(x-paddingWidth, 0)); // Top
						else if(y >= bottomLimit) out.setPixel(x, y, this.getPixel(x-paddingWidth, this.getHeight()-1)); // Bottom
					}
				}
			}
			
		return out;
	}
}

