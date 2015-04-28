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
import java.text.AttributedString;
import java.util.Comparator;
import java.util.List;

import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.analyser.PixelAnalyser;
import org.openimaj.image.combiner.AccumulatingImageCombiner;
import org.openimaj.image.combiner.ImageCombiner;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.GridProcessor;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.processor.Processor;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.math.geometry.path.Path2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;

import Jama.Matrix;

/**
 * Base class for representing and manipulating images. Images are typed by the
 * type of pixel at each coordinate and the concrete subclass type.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <Q>
 *            the pixel type
 * @param <I>
 *            the actual image of the concrete subclass
 */
public abstract class Image<Q, I extends Image<Q, I>> implements Cloneable, Serializable, ImageProvider<I> {
	/**
	 * Enumerator for representing the type of field interlacing operations.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public enum Field {
		/**
		 * Odd field
		 */
		ODD,
		/**
		 * Even field
		 */
		EVEN
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Accumulate this image the the given {@link AccumulatingImageCombiner}.
	 *
	 * @param combiner
	 *            the combiner
	 * @see AccumulatingImageCombiner#accumulate(Image)
	 */
	@SuppressWarnings("unchecked")
	public void accumulateWith(AccumulatingImageCombiner<I, ?> combiner) {
		combiner.accumulate((I) this);
	}

	/**
	 * Set all pixels to their absolute values, so that all pixel values in the
	 * image will be greater than zero.
	 *
	 * @return The image with absolute values
	 */
	public abstract I abs();

	/**
	 * Adds the given image to this image and return new image.
	 *
	 * @param im
	 *            The image to add
	 * @return A new image that is the sum of this image and the given image.
	 */
	public I add(Image<?, ?> im) {
		final I newImage = this.clone();
		newImage.addInplace(im);
		return newImage;
	}

	/**
	 * Add a value to each pixel and return new image.
	 *
	 * @param num
	 *            The value to add to each pixel
	 * @return A new image that is the sum of this image and the given value.
	 */
	public I add(Q num) {
		final I newImage = this.clone();
		newImage.addInplace(num);
		return newImage;
	}

	/**
	 * Add the given image to this image (side-affects this image).
	 *
	 * @param im
	 *            The image to add to this image
	 * @return A reference to this image.
	 */
	public abstract I addInplace(Image<?, ?> im);

	/**
	 * Add a scalar to each pixel in this image (side-affects this image).
	 *
	 * @param num
	 *            The value to add to every pixel in this image.
	 * @return A reference to this image.
	 */
	public abstract I addInplace(Q num);

	/**
	 * Analyse this image with an {@link ImageAnalyser}.
	 *
	 * @param analyser
	 *            The analyser to analyse with.
	 * @see ImageAnalyser#analyseImage(Image)
	 */
	@SuppressWarnings("unchecked")
	public void analyseWith(ImageAnalyser<I> analyser) {
		analyser.analyseImage((I) this);
	}

	/**
	 * Analyse this image with a {@link PixelAnalyser}.
	 *
	 * @param analyser
	 *            The analyser to analyse with.
	 * @see PixelAnalyser#analysePixel(Object)
	 */
	public void analyseWith(PixelAnalyser<Q> analyser) {
		analyser.reset();

		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				analyser.analysePixel(getPixel(x, y));
			}
		}
	}

	/**
	 * Analyse this image with the given {@link PixelAnalyser}, only analysing
	 * those pixels where the mask is non-zero.
	 *
	 * @param mask
	 *            The mask to apply to the analyser.
	 * @param analyser
	 *            The {@link PixelProcessor} to apply.
	 *
	 * @see PixelAnalyser#analysePixel(Object)
	 */
	public void analyseWithMasked(FImage mask, PixelAnalyser<Q> analyser) {
		analyser.reset();

		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				if (mask.pixels[y][x] == 0)
					continue;
				analyser.analysePixel(getPixel(x, y));
			}
		}
	}

	/**
	 * Sets any pixels that are below <code>min</code> to zero or above
	 * <code>max</code> to the highest normal value that the image allows
	 * (usually 1 for floating-point images). This method may side-affect this
	 * image.
	 *
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 * @return The clipped image.
	 */
	public abstract I clip(Q min, Q max);

	/**
	 * Set all values greater than the given value to the highest normal value
	 * that the image allows (usually 1 for floating-point images). This method
	 * may side-affect this image.
	 *
	 * @param thresh
	 *            The value over which pixels are clipped to zero.
	 * @return The clipped image.
	 */
	public abstract I clipMax(Q thresh);

	/**
	 * Set all values less than the given value to zero. This method may
	 * side-affect this image.
	 *
	 * @param thresh
	 *            The value below which pixels are clipped to zero.
	 * @return The clipped image.
	 */
	public abstract I clipMin(Q thresh);

	/**
	 * Deep copy of an image (internal image buffers copied).
	 *
	 * @return A copy of this image.
	 */
	@Override
	public abstract I clone();

	/**
	 * Create a {@link ImageRenderer} capable of drawing into this image.
	 *
	 * @return the renderer
	 */
	public abstract ImageRenderer<Q, I> createRenderer();

	/**
	 * Create a {@link ImageRenderer} capable of drawing into this image.
	 *
	 * @param options
	 *            Options for the renderer
	 * @return the renderer
	 */
	public abstract ImageRenderer<Q, I> createRenderer(RenderHints options);

	/**
	 * Combine this image with another using an {@link ImageCombiner}.
	 *
	 * @param <OUT>
	 *            The output {@link Image} type.
	 * @param <OTHER>
	 *            The type of the other {@link Image} being combined.
	 * @param combiner
	 *            The combiner.
	 * @param other
	 *            The image to combine with this
	 * @return The combined output.
	 */
	@SuppressWarnings("unchecked")
	public <OUT extends Image<?, OUT>, OTHER extends Image<?, OTHER>> OUT combineWith(
			ImageCombiner<I, OTHER, OUT> combiner, OTHER other)
	{
		return combiner.combine((I) this, other);
	}

	/**
	 * Get the default foreground colour.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @return the default foreground colour.
	 */
	public Q defaultBackgroundColour() {
		return createRenderer().defaultBackgroundColour();
	}

	/**
	 * Get the default foreground colour.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @return the default foreground colour.
	 */
	public Q defaultForegroundColour() {
		return createRenderer().defaultForegroundColour();
	}

	/**
	 * Divide each pixel of the image by corresponding pixel in the given image.
	 * This method should return a new image.
	 *
	 * @param im
	 *            image The image to divide this image by.
	 * @return A new image containing the result.
	 */
	public I divide(Image<?, ?> im) {
		final I newImage = this.clone();
		newImage.divideInplace(im);
		return newImage;
	}

	/**
	 * Divide each pixel of the image by the given scalar value. This method
	 * should return a new image.
	 *
	 * @param val
	 *            The value to divide the pixels in this image by.
	 * @return A new image containing the result.
	 */
	public I divide(Q val) {
		final I newImage = this.clone();
		newImage.divideInplace(val);
		return newImage;
	}

	/**
	 * Divide each pixel in this image by the corresponding pixel value in the
	 * given image. This method should side-affect this image.
	 *
	 * @param im
	 *            image The image to divide this image by.
	 * @return A reference to this image containing the result.
	 */
	public abstract I divideInplace(Image<?, ?> im);

	/**
	 * Divide each pixel of the image by the given scalar value. This method
	 * should side-affect this image.
	 *
	 * @param val
	 *            The value to divide each pixel by.
	 * @return A reference to this image containing the result.
	 */
	public abstract I divideInplace(Q val);

	/**
	 * Draw onto this image lines drawn with the given colour between the points
	 * given. No points are drawn. Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param pts
	 *            The point list to draw onto this image.
	 * @param col
	 *            The colour to draw the lines
	 */
	public void drawConnectedPoints(List<? extends Point2d> pts, Q col) {
		createRenderer().drawConnectedPoints(pts, col);
	}

	/**
	 * Draw a cubic Bezier curve into the image with 100 point accuracy.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param p1
	 *            One end point of the line
	 * @param p2
	 *            The other end point of the line
	 * @param c1
	 *            The control point associated with p1
	 * @param c2
	 *            The control point associated with p2
	 * @param thickness
	 *            The thickness to draw the line
	 * @param col
	 *            The colour to draw the line
	 * @return The points along the bezier curve
	 */
	public Point2d[] drawCubicBezier(Point2d p1, Point2d p2,
			Point2d c1, Point2d c2, int thickness, Q col)
	{
		return createRenderer().drawCubicBezier(p1, p2, c1, c2, thickness, col);
	}

	/**
	 * Draw into this image the provided image at the given coordinates. Parts
	 * of the image outside the bounds of this image will be ignored.
	 * Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param image
	 *            The image to draw.
	 * @param x
	 *            The x-ordinate of the top-left of the image
	 * @param y
	 *            The y-ordinate of the top-left of the image
	 */
	public void drawImage(I image, int x, int y) {
		createRenderer().drawImage(image, x, y);
	}

	/**
	 * Draw into this image the provided image at the given coordinates. Parts
	 * of the image outside the bounds of this image will be ignored.
	 * Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param image
	 *            The image to draw.
	 * @param pt
	 *            the coordinate at which to draw
	 */
	public void drawImage(I image, Point2d pt) {
		createRenderer().drawImage(image, (int) pt.getX(), (int) pt.getY());
	}

	/**
	 * Draw into this image the provided image at the given coordinates ignoring
	 * certain pixels. Parts of the image outside the bounds of this image will
	 * be ignored. Side-affects this image. Pixels in the ignore list will be
	 * stripped from the image to draw.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param image
	 *            The image to draw.
	 * @param x
	 *            The x-ordinate of the top-left of the image
	 * @param y
	 *            The y-ordinate of the top-left of the image
	 * @param ignoreList
	 *            The list of pixels to ignore when copying the image
	 */
	public void drawImage(I image, int x, int y, @SuppressWarnings("unchecked") Q... ignoreList) {
		createRenderer().drawImage(image, x, y, ignoreList);
	}

	/**
	 * Draw a line from the coordinates specified by <code>(x1,y1)</code> at an
	 * angle of <code>theta</code> with the given length, thickness and colour.
	 * Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param x1
	 *            The x-ordinate to start the line.
	 * @param y1
	 *            The y-ordinate to start the line.
	 * @param theta
	 *            The angle at which to draw the line.
	 * @param length
	 *            The length to draw the line.
	 * @param thickness
	 *            The thickness to draw the line.
	 * @param col
	 *            The colour to draw the line.
	 */
	public void drawLine(int x1, int y1, double theta, int length, int thickness, Q col) {
		createRenderer().drawLine(x1, y1, theta, length, thickness, col);
	}

	/**
	 * Draw a line from the coordinates specified by <code>(x1,y1)</code> at an
	 * angle of <code>theta</code> with the given length and colour.
	 * Line-thickness will be 1. Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param x1
	 *            The x-ordinate to start the line.
	 * @param y1
	 *            The y-ordinate to start the line.
	 * @param theta
	 *            The angle at which to draw the line.
	 * @param length
	 *            The length to draw the line.
	 * @param col
	 *            The colour to draw the line.
	 */
	public void drawLine(int x1, int y1, double theta, int length, Q col) {
		createRenderer().drawLine(x1, y1, theta, length, 1, col);
	}

	/**
	 * Draw a line from the coordinates specified by <code>(x0,y0)</code> to the
	 * coordinates specified by <code>(x1,y1)</code> using the given color and
	 * thickness. Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param x0
	 *            The x-ordinate at the start of the line.
	 * @param y0
	 *            The y-ordinate at the start of the line.
	 * @param x1
	 *            The x-ordinate at the end of the line.
	 * @param y1
	 *            The y-ordinate at the end of the line.
	 * @param thickness
	 *            The thickness which to draw the line.
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public void drawLine(int x0, int y0, int x1, int y1, int thickness, Q col) {
		createRenderer().drawLine(x0, y0, x1, y1, thickness, col);
	}

	/**
	 * Draw a line from the coordinates specified by <code>(x0,y0)</code> to
	 * <code>(x1,y1)</code> using the given colour. The line thickness will be 1
	 * pixel. Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param x0
	 *            The x-ordinate at the start of the line.
	 * @param y0
	 *            The y-ordinate at the start of the line.
	 * @param x1
	 *            The x-ordinate at the end of the line.
	 * @param y1
	 *            The y-ordinate at the end of the line.
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public void drawLine(int x0, int y0, int x1, int y1, Q col) {
		createRenderer().drawLine(x0, y0, x1, y1, 1, col);
	}

	/**
	 * Draw a line from the coordinates specified using the given colour. The
	 * line thickness will be 1 pixel. Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param p1
	 *            The coordinate of the start of the line.
	 * @param p2
	 *            The coordinate of the end of the line.
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public void drawLine(Point2d p1, Point2d p2, Q col) {
		createRenderer().drawLine(p1, p2, col);
	}

	/**
	 * Draw a line from the coordinates specified using the given colour and
	 * thickness. Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param p1
	 *            The coordinate of the start of the line.
	 * @param p2
	 *            The coordinate of the end of the line.
	 * @param thickness
	 *            the stroke width
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public void drawLine(Point2d p1, Point2d p2, int thickness, Q col) {
		createRenderer().drawLine(p1, p2, thickness, col);
	}

	/**
	 * Draw a line from the specified Path2d object
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param line
	 *            the line
	 * @param thickness
	 *            the stroke width
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public void drawLine(Path2d line, int thickness, Q col) {
		createRenderer().drawLine(line, thickness, col);
	}

	/**
	 * Draw a line from the specified Path2d object
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param line
	 *            the line
	 * @param thickness
	 *            the stroke width
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public void drawPath(Path2d line, int thickness, Q col) {
		createRenderer().drawPath(line, thickness, col);
	}

	/**
	 * Draw the given list of lines using {@link #drawLine(Path2d, int, Object)}
	 * with the given colour and thickness. Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param lines
	 *            The list of lines to draw.
	 * @param thickness
	 *            the stroke width
	 * @param col
	 *            The colour to draw each point.
	 */
	public void drawLines(Iterable<? extends Path2d> lines, int thickness, Q col) {
		createRenderer().drawLines(lines, thickness, col);
	}

	/**
	 * Draw the given list of paths using {@link #drawLine(Path2d, int, Object)}
	 * with the given colour and thickness. Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param lines
	 *            The list of lines to draw.
	 * @param thickness
	 *            the stroke width
	 * @param col
	 *            The colour to draw each point.
	 */
	public void drawPaths(Iterable<? extends Path2d> lines, int thickness, Q col) {
		createRenderer().drawPaths(lines, thickness, col);
	}

	/**
	 * Draw a dot centered on the given location (rounded to nearest integer
	 * location) at the given size and with the given color. Side-affects this
	 * image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param p
	 *            The coordinates at which to draw the point
	 * @param col
	 *            The colour to draw the point
	 * @param size
	 *            The size at which to draw the point.
	 */
	public void drawPoint(Point2d p, Q col, int size) {
		createRenderer().drawPoint(p, col, size);
	}

	/**
	 * Draw the given list of points using
	 * {@link #drawPoint(Point2d, Object, int)} with the given colour and size.
	 * Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param pts
	 *            The list of points to draw.
	 * @param col
	 *            The colour to draw each point.
	 * @param size
	 *            The size to draw each point.
	 */
	public void drawPoints(Iterable<? extends Point2d> pts, Q col, int size) {
		createRenderer().drawPoints(pts, col, size);
	}

	/**
	 * Draw the given polygon in the specified colour with the given thickness
	 * lines. Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param p
	 *            The polygon to draw.
	 * @param thickness
	 *            The thickness of the lines to use
	 * @param col
	 *            The colour to draw the lines in
	 */
	public void drawPolygon(Polygon p, int thickness, Q col) {
		createRenderer().drawPolygon(p, thickness, col);
	}

	/**
	 * Draw the given polygon in the specified colour. Uses
	 * {@link #drawPolygon(Polygon, int, Object)} with line thickness 1.
	 * Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param p
	 *            The polygon to draw.
	 * @param col
	 *            The colour to draw the polygon in.
	 */
	public void drawPolygon(Polygon p, Q col) {
		createRenderer().drawPolygon(p, col);
	}

	/**
	 * Draw the given polygon, filled with the specified colour. Side-affects
	 * this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param p
	 *            The polygon to draw.
	 * @param col
	 *            The colour to fill the polygon with.
	 */
	public void drawPolygonFilled(Polygon p, Q col) {
		createRenderer().drawPolygonFilled(p, col);
	}

	/**
	 * Draw the given shape in the specified colour with the given thickness
	 * lines. Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param s
	 *            The shape to draw.
	 * @param thickness
	 *            The thickness of the lines to use
	 * @param col
	 *            The colour to draw the lines in
	 */
	public void drawShape(Shape s, int thickness, Q col) {
		createRenderer().drawShape(s, thickness, col);
	}

	/**
	 * Draw the given shape in the specified colour. Uses
	 * {@link #drawPolygon(Polygon, int, Object)} with line thickness 1.
	 * Side-affects this image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param p
	 *            The shape to draw.
	 * @param col
	 *            The colour to draw the polygon in.
	 */
	public void drawShape(Shape p, Q col) {
		createRenderer().drawShape(p, col);
	}

	/**
	 * Draw the given shape, filled with the specified colour. Side-affects this
	 * image.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param s
	 *            The shape to draw.
	 * @param col
	 *            The colour to fill the polygon with.
	 */
	public void drawShapeFilled(Shape s, Q col) {
		createRenderer().drawShapeFilled(s, col);
	}

	/**
	 * Render the text using its attributes.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param text
	 *            the text
	 * @param x
	 *            the x-ordinate
	 * @param y
	 *            the y-ordinate
	 */
	public void drawText(AttributedString text, int x, int y) {
		createRenderer().drawText(text, x, y);
	}

	/**
	 * Render the text using its attributes.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param text
	 *            the text
	 * @param pt
	 *            the coordinate to render at
	 */
	public void drawText(AttributedString text, Point2d pt) {
		createRenderer().drawText(text, pt);
	}

	/**
	 * Render the text in the given font with the default style.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param <F>
	 *            the font
	 * @param text
	 *            the text
	 * @param x
	 *            the x-ordinate
	 * @param y
	 *            the y-ordinate
	 * @param f
	 *            the font
	 * @param sz
	 *            the size
	 */
	public <F extends Font<F>> void drawText(String text, int x, int y, F f, int sz) {
		createRenderer().drawText(text, x, y, f, sz);
	}

	/**
	 * Render the text in the given font in the given colour with the default
	 * style.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param <F>
	 *            the font
	 * @param text
	 *            the text
	 * @param x
	 *            the x-ordinate
	 * @param y
	 *            the y-ordinate
	 * @param f
	 *            the font
	 * @param sz
	 *            the size
	 * @param col
	 *            the font color
	 */
	public <F extends Font<F>> void drawText(String text, int x, int y, F f, int sz, Q col) {
		createRenderer().drawText(text, x, y, f, sz, col);
	}

	/**
	 * Render the text with the given {@link FontStyle}.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param text
	 *            the text
	 * @param x
	 *            the x-ordinate
	 * @param y
	 *            the y-ordinate
	 * @param f
	 *            the font style
	 */
	public void drawText(String text, int x, int y, FontStyle<Q> f) {
		createRenderer().drawText(text, x, y, f);
	}

	/**
	 * Render the text in the given font with the default style.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param <F>
	 *            the font
	 * @param text
	 *            the text
	 * @param pt
	 *            the coordinate to render at
	 * @param f
	 *            the font
	 * @param sz
	 *            the size
	 */
	public <F extends Font<F>> void drawText(String text, Point2d pt, F f, int sz) {
		createRenderer().drawText(text, pt, f, sz);
	}

	/**
	 * Render the text in the given font in the given colour with the default
	 * style.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param <F>
	 *            the font
	 * @param text
	 *            the text
	 * @param pt
	 *            the coordinate to render at
	 * @param f
	 *            the font
	 * @param sz
	 *            the size
	 * @param col
	 *            the font colour
	 */
	public <F extends Font<F>> void drawText(String text, Point2d pt, F f, int sz, Q col) {
		createRenderer().drawText(text, pt, f, sz, col);
	}

	/**
	 * Render the text with the given {@link FontStyle}.
	 *
	 * <p>
	 * This is a convenience method that calls {@link #createRenderer()} to get
	 * the default renderer to do the actual drawing. Create the renderer
	 * yourself and use it to draw if you need more control.
	 * </p>
	 *
	 * @param text
	 *            the text
	 * @param pt
	 *            the coordinate to render at
	 * @param f
	 *            the font style
	 */
	public void drawText(String text, Point2d pt, FontStyle<Q> f) {
		createRenderer().drawText(text, pt, f);
	}

	/**
	 * Extract a rectangular region about the centre of the image with the given
	 * width and height. The method will return a box that extends
	 * <code>width/2</code> and <code>height/2</code> from the centre point so
	 * that the centre point of the extracted box is also the centre point of
	 * the image.
	 *
	 * @param w
	 *            The width of the box to extract
	 * @param h
	 *            The height of the box to extract
	 * @return A new image centred around the centre of the image.
	 */
	public I extractCenter(int w, int h) {
		final int sw = (int) Math.floor((this.getWidth() - w) / 2);
		final int sh = (int) Math.floor((this.getHeight() - h) / 2);

		return this.extractROI(sw, sh, w, h);
	}

	/**
	 * Extract a rectangular region centred on a given point. The method will
	 * return a box that extends <code>width/2</code> and <code>height/2</code>
	 * from the given point <code>(x,y)</code> such that the centre point of the
	 * extracted box is the same as the point <code>(x,y)</code> in this image.
	 *
	 * @param x
	 *            Center point of the rectangle to extract
	 * @param y
	 *            center point of the rectangle to extract
	 * @param w
	 *            The width of the rectangle to extract
	 * @param h
	 *            The height of the rectangle to extract
	 * @return A new image centred around the centre of the image.
	 */
	public I extractCenter(int x, int y, int w, int h) {
		final int sw = (int) Math.floor(x - (w / 2));
		final int sh = (int) Math.floor(y - (h / 2));

		return this.extractROI(sw, sh, w, h);
	}

	/**
	 * Extract a rectangular region of interest from this image and put it in
	 * the given image. Coordinate <code>(0,0)</code> is the top-left corner.
	 * The width and height of the extracted image should be determined from the
	 * given image's width and height.
	 *
	 * @param x
	 *            The leftmost coordinate of the rectangle to extract
	 * @param y
	 *            The topmost coordinate of the rectangle to extract
	 * @param img
	 *            The destination image
	 * @return A reference to the destination image containing the result
	 */
	public abstract I extractROI(int x, int y, I img);

	/**
	 * Extract a rectangular region of interest of the given width and height.
	 * Coordinate <code>(0,0)</code> is the top-left corner. Returns a new
	 * image.
	 *
	 * @param x
	 *            The leftmost coordinate of the rectangle to extract
	 * @param y
	 *            The topmost coordinate of the rectangle to extract
	 * @param w
	 *            The width of the rectangle to extract
	 * @param h
	 *            The height of the rectangle to extract
	 * @return A new image representing the selected region
	 */
	public abstract I extractROI(int x, int y, int w, int h);

	/**
	 * Extract a rectangular region of interest of the given width and height.
	 * Coordinate <code>(0,0)</code> is the top-left corner. Returns a new
	 * image.
	 *
	 * @param r
	 *            the rectangle
	 * @return A new image representing the selected region
	 */
	public I extractROI(Rectangle r) {
		return extractROI((int) r.x, (int) r.y, (int) r.width, (int) r.height);
	}

	/**
	 * Fill this image with the given colour. Should overwrite all other data
	 * stored in this image. Side-affects this image.
	 *
	 * @param colour
	 *            the colour to fill the image with
	 * @return A reference to this image.
	 */
	public abstract I fill(Q colour);

	/**
	 * Flips the content horizontally. Side-affects this image.
	 *
	 * @return A reference to this image.
	 */
	public abstract I flipX();

	/**
	 * Flips the content vertically. Side-affects this image.
	 *
	 * @return A reference to this image.
	 */
	public abstract I flipY();

	/**
	 * Get a rectangle representing the image, with the top-left at 0,0 and the
	 * bottom-right at width,height
	 *
	 * @return the bounding rectangle of the image
	 */
	public Rectangle getBounds() {
		return new Rectangle(0, 0, this.getWidth(), this.getHeight());
	}

	/**
	 * Get the image width in pixels. This is syntactic sugar for
	 * {@link #getWidth()};
	 *
	 * @return The image width in pixels.
	 */
	public int getCols() {
		return getWidth();
	}

	/**
	 * Get bounding box of non-zero-valued pixels around the outside of the
	 * image. Used by {@link #trim()}.
	 *
	 * @return A rectangle of the boundaries of the non-zero-valued image
	 */
	public abstract Rectangle getContentArea();

	/**
	 * Get the given field of this image. Used for deinterlacing video, this
	 * should return a new image containing the deinterlaced image. The returned
	 * image will be half the height of this image.
	 *
	 * @param f
	 *            The {@link Field} to extract from this image
	 * @return An image containing only the odd or even fields.
	 */
	public abstract I getField(Field f);

	/**
	 * Get the given field of this image, maintaining the image's aspect ratio
	 * by doubling the fields. Used for deinterlacing video, this should return
	 * a new image containing the deinterlaced image. The returned image should
	 * be the same size as this image.
	 *
	 * @param f
	 *            The {@link Field} to extract from this image
	 * @return An image containing the odd or even fields doubled.
	 */
	public abstract I getFieldCopy(Field f);

	/**
	 * Get the given field of this image, maintaining the image's aspect ratio
	 * by interpolating between the fields. Used for deinterlacing video, this
	 * should return a new image containing the detinterlaced image. The
	 * returned image should be the same size as this image.
	 *
	 * @param f
	 *            The {@link Field} to extract from this image.
	 * @return An image containing the odd or even fields with interpolated rows
	 *         between.
	 */
	public abstract I getFieldInterpolate(Field f);

	/**
	 * Returns the image height in pixels.
	 *
	 * @return The image height in pixels.
	 */
	public abstract int getHeight();

	/**
	 * Get the value of the pixel at coordinate <code>(x, y)</code>.
	 *
	 * @param x
	 *            The x-ordinate to get
	 * @param y
	 *            The y-ordinate to get
	 *
	 * @return The pixel value at (x, y)
	 */
	public abstract Q getPixel(int x, int y);

	/**
	 * Get the value of the pixel at coordinate p
	 *
	 * @param p
	 *            The coordinate to get
	 *
	 * @return The pixel value at (x, y)
	 */
	public Q getPixel(Pixel p) {
		return getPixel(p.x, p.y);
	}

	/**
	 * Returns a pixel comparator that is able to compare equality of pixels in
	 * the given image type.
	 *
	 * @return A {@link Comparator} that compares pixels.
	 */
	public abstract Comparator<? super Q> getPixelComparator();

	/**
	 * Get the value of a sub-pixel using linear-interpolation.
	 *
	 * @param x
	 *            The x-ordinate to get
	 * @param y
	 *            The y-ordinate to get
	 * @return The value of the interpolated point at <code>(x,y)</code>
	 */
	public abstract Q getPixelInterp(double x, double y);

	/**
	 * Get the value of a sub-pixel using linear-interpolation. Also specify the
	 * colour of the background (for interpolation at the edge)
	 *
	 * @param x
	 *            The x-ordinate to get.
	 * @param y
	 *            The y-ordinate to get.
	 * @param backgroundColour
	 *            The colour of the background pixel.
	 * @return The value of the interpolated point at <code>(x,y)</code>
	 */
	public abstract Q getPixelInterp(double x, double y, Q backgroundColour);

	/**
	 * Returns the pixels in this image as a vector (an array of the pixel
	 * type).
	 *
	 * @param f
	 *            The array into which to place the data
	 * @return The pixels in the image as a vector (a reference to the given
	 *         array).
	 */
	public Q[] getPixelVector(Q[] f) {
		for (int y = 0; y < getHeight(); y++)
			for (int x = 0; x < getWidth(); x++)
				f[x + y * getWidth()] = getPixel(x, y);

		return f;
	}

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
	 * Get the width (number of columns) in this image.
	 *
	 * @return the image width
	 */
	public abstract int getWidth();

	/**
	 * Copy the internal state from another image of the same type. This method
	 * is designed to be FAST. This means that bounds checking WILL NOT be
	 * performed, so it is important that the images are the SAME size.
	 *
	 * @param im
	 *            The source image to make a copy of.
	 * @return A reference to this image.
	 */
	public abstract I internalCopy(I im);

	/**
	 * Assign the internal state from another image of the same type.
	 *
	 * @param im
	 *            The source image to make a copy of.
	 * @return A reference to this image.
	 */
	public abstract I internalAssign(I im);

	/**
	 * Copy pixels from given ARGB buffer image into this image. Side-affects
	 * this image.
	 *
	 * @param pixelData
	 *            buffer of ARGB packed integer pixels
	 * @param width
	 *            the width of the buffer
	 * @param height
	 *            the height of the buffer
	 *
	 * @return A reference to this image.
	 */
	public abstract I internalAssign(int[] pixelData, int width, int height);

	/**
	 * Invert the image pixels by finding the maximum value and subtracting each
	 * pixel value from that maximum.
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
	 * @param im
	 *            The image to multiply with this one
	 * @return A new image containing the result.
	 */
	public I multiply(Image<?, ?> im) {
		final I newImage = this.clone();
		newImage.multiplyInplace(im);
		return newImage;
	}

	/**
	 * Multiply each pixel of this by the given scalar and return new image.
	 *
	 * @param num
	 *            The scalar which to multiply the image by
	 * @return A new image containing the result
	 */
	public I multiply(Q num) {
		final I newImage = this.clone();
		newImage.multiplyInplace(num);
		return newImage;
	}

	/**
	 * Multiply each pixel in this image by the corresponding pixel in the given
	 * image. This method side-affects this image.
	 *
	 * @param im
	 *            The image to multiply with this image.
	 * @return A reference to this image.
	 */
	public abstract I multiplyInplace(Image<?, ?> im);

	/**
	 * Multiply each pixel of this by the given scalar. This method side-affects
	 * this image.
	 *
	 * @param num
	 *            The scalar to multiply this image by.
	 * @return A reference to this image.
	 */
	public abstract I multiplyInplace(Q num);

	/**
	 * Create a new instance of this image subclass with given dimensions.
	 *
	 * @param width
	 *            The image width
	 * @param height
	 *            The image height
	 *
	 * @return A new instance of an image of type <code>I</code>
	 */
	public abstract I newInstance(int width, int height);

	/**
	 * Normalise all pixel values to fall within the range 0.0 - 1.0. This
	 * should be scaled by both the maximum and minimum values. This method
	 * side-affects this image.
	 *
	 * @return A reference to this image.
	 */
	public abstract I normalise();

	/**
	 * Adds padding as in {@link Image#padding(int, int, Object)}. The padding
	 * colour is the colour of the closest border pixel.
	 *
	 * @param paddingWidth
	 *            padding in the x direction
	 * @param paddingHeight
	 *            padding in the y direction
	 * @return padded image
	 */
	public I padding(int paddingWidth, int paddingHeight) {
		return this.padding(paddingWidth, paddingHeight, null);
	}

	/**
	 * Adds this many pixels to both sides of the image such that the new image
	 * width = padding + width + padding with the original image in the middle
	 *
	 * @param paddingWidth
	 *            left and right padding width
	 * @param paddingHeight
	 *            top and bottom padding width
	 * @param paddingColour
	 *            colour of padding, if null the closes border pixel is used
	 * @return padded image
	 */
	@SuppressWarnings("unchecked")
	public I padding(int paddingWidth, int paddingHeight, Q paddingColour) {
		final I out = this.newInstance(paddingWidth + this.getWidth() + paddingWidth, paddingHeight + this.getHeight()
				+ paddingHeight);

		out.createRenderer().drawImage((I) this, paddingWidth, paddingHeight);
		final int rightLimit = paddingWidth + this.getWidth();
		final int bottomLimit = paddingHeight + this.getHeight();
		// Fill the padding with a colour if it isn't null
		if (paddingColour != null)
			for (int y = 0; y < out.getHeight(); y++) {
				for (int x = 0; x < out.getWidth(); x++) {
					if (x >= paddingWidth && x < rightLimit && y >= paddingHeight && y < bottomLimit)
						continue;
					out.setPixel(x, y, paddingColour);
				}
			}
		else
			for (int y = 0; y < out.getHeight(); y++) {
				for (int x = 0; x < out.getWidth(); x++) {
					if (x >= paddingWidth && x < rightLimit && y >= paddingHeight && y < bottomLimit)
						continue;
					if (x < paddingWidth && y < paddingHeight)
						out.setPixel(x, y, this.getPixel(0, 0)); // Top Left
					else if (x < paddingWidth && y >= bottomLimit)
						out.setPixel(x, y, this.getPixel(0, this.getHeight() - 1)); // Bottom
					// Left
					else if (x >= rightLimit && y < paddingHeight)
						out.setPixel(x, y, this.getPixel(this.getWidth() - 1, 0)); // Top
					// Right
					else if (x >= rightLimit && y >= bottomLimit)
						out.setPixel(x, y, this.getPixel(this.getWidth() - 1, this.getHeight() - 1)); // Bottom
					// Right
					else {
						if (x < paddingWidth)
							out.setPixel(x, y, this.getPixel(0, y - paddingHeight)); // Left
						else if (x >= rightLimit)
							out.setPixel(x, y, this.getPixel(this.getWidth() - 1, y - paddingHeight)); // Right
						else if (y < paddingHeight)
							out.setPixel(x, y, this.getPixel(x - paddingWidth, 0)); // Top
						else if (y >= bottomLimit)
							out.setPixel(x, y, this.getPixel(x - paddingWidth, this.getHeight() - 1)); // Bottom
					}
				}
			}

		return out;
	}

	/**
	 * Adds pixels to around the image such that the new image width =
	 * paddingLeft + width + paddingRight with the original image in the middle.
	 * The values of the padding pixels are formed from repeated symmetric
	 * reflections of the original image.
	 *
	 * @param paddingLeft
	 *            left padding width
	 * @param paddingRight
	 *            right padding width
	 * @param paddingTop
	 *            top padding width
	 * @param paddingBottom
	 *            bottom padding width
	 * @return padded image
	 */
	public I paddingSymmetric(int paddingLeft, int paddingRight, int paddingTop, int paddingBottom) {
		final I out = this.newInstance(paddingLeft + this.getWidth() + paddingRight, paddingTop + this.getHeight()
				+ paddingBottom);
		final I clone = this.clone();
		final I hflip = clone.clone().flipX();

		final ImageRenderer<Q, I> rend = out.createRenderer();
		rend.drawImage(clone, paddingLeft, paddingTop);

		// left
		for (int i = paddingLeft - this.getWidth(), c = 0; i > -this.getWidth(); i -= this.getWidth(), c++) {
			if (c % 2 == 0) {
				rend.drawImage(hflip, i, paddingTop);
			} else {
				rend.drawImage(clone, i, paddingTop);
			}
		}

		// right
		for (int i = paddingLeft + this.getWidth(), c = 0; i < paddingLeft + paddingRight + this.getWidth(); i += this
				.getWidth(), c++)
		{
			if (c % 2 == 0) {
				rend.drawImage(hflip, i, paddingTop);
			} else {
				rend.drawImage(clone, i, paddingTop);
			}
		}

		final I centre = out.extractROI(0, paddingTop, paddingLeft + this.getWidth() + paddingRight, this.getHeight());
		final I yflip = centre.clone().flipY();

		// up
		for (int i = paddingTop - this.getHeight(), c = 0; i > -this.getHeight(); i -= this.getHeight(), c++) {
			if (c % 2 == 0) {
				rend.drawImage(yflip, 0, i);
			} else {
				rend.drawImage(centre, 0, i);
			}
		}

		// down
		for (int i = paddingTop + this.getHeight(), c = 0; i < paddingTop + paddingBottom + this.getHeight(); i += this
				.getHeight(), c++)
		{
			if (c % 2 == 0) {
				rend.drawImage(yflip, 0, i);
			} else {
				rend.drawImage(centre, 0, i);
			}
		}

		return out;
	}

	/**
	 * Process this image with the given {@link GridProcessor} and return new
	 * image containing the result.
	 *
	 * @param p
	 *            {@link GridProcessor} to apply to this image.
	 * @return A new image containing the result.
	 */
	public I process(GridProcessor<Q, I> p) {
		final int height = p.getVerticalGridElements();
		final int width = p.getHorizontalGridElements();
		final I newImage = this.newInstance(width, height);
		newImage.zero();

		final int gridWidth = getWidth() / width;
		final int gridHeight = getHeight() / height;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				newImage.setPixel(x, y,
						p.processGridElement(this.extractROI(gridWidth * x, gridHeight * y, gridWidth, gridHeight)));

		return newImage;
	}

	/**
	 * Process this image with an {@link ImageProcessor} and return new image
	 * containing the result.
	 *
	 * @param p
	 *            The {@link ImageProcessor} to apply to this image.
	 * @return A new image containing the result.
	 */
	public I process(ImageProcessor<I> p) {
		final I newImage = this.clone();
		newImage.processInplace(p);
		return newImage;
	}

	/**
	 * Process this image with the given {@link KernelProcessor} and return new
	 * image containing the result.
	 *
	 * @param p
	 *            The {@link KernelProcessor} to apply.
	 * @return A new image containing the result.
	 */
	public I process(KernelProcessor<Q, I> p) {
		return process(p, false);
	}

	/**
	 * Process this image with the given {@link KernelProcessor} and return new
	 * image containing the result.
	 *
	 * @param p
	 *            The {@link KernelProcessor} to apply.
	 * @param pad
	 *            Should the image be zero padded so the kernel reaches the
	 *            edges of the output
	 * @return A new image containing the result.
	 */
	public I process(KernelProcessor<Q, I> p, boolean pad) {
		final I newImage = this.clone();
		newImage.zero();

		final int kh = p.getKernelHeight();
		final int kw = p.getKernelWidth();

		final int hh = p.getKernelHeight() / 2;
		final int hw = p.getKernelWidth() / 2;

		final I tmp = newInstance(kw, kh);

		if (!pad) {
			for (int y = hh; y < getHeight() - (kh - hh); y++) {
				for (int x = hw; x < getWidth() - (kw - hw); x++) {
					newImage.setPixel(x, y, p.processKernel(this.extractROI(x - hw, y - hh, tmp)));
				}
			}
		} else {
			for (int y = 0; y < getHeight(); y++) {
				for (int x = 0; x < getWidth(); x++) {
					newImage.setPixel(x, y, p.processKernel(this.extractROI(x - hw, y - hh, tmp)));
				}
			}
		}

		return newImage;
	}

	/**
	 * Process this image with the given {@link PixelProcessor} and return a new
	 * image containing the result.
	 *
	 * @param p
	 *            The {@link PixelProcessor} to apply.
	 * @return A new image containing the result.
	 */
	public I process(PixelProcessor<Q> p) {
		final I newImage = this.clone();
		newImage.processInplace(p);
		return newImage;
	}

	/**
	 * Process this image with an {@link Processor} and return new image
	 * containing the result.
	 *
	 * @param p
	 *            The {@link Processor} to apply to this image.
	 * @return A new image containing the result.
	 */
	public I process(Processor<I> p) {
		final I newImage = this.clone();
		newImage.processInplace(p);
		return newImage;
	}

	/**
	 * Process this image with the given {@link Processor} side-affecting this
	 * image.
	 *
	 * @param p
	 *            The {@link Processor} to apply.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInplace(Processor<I> p) {
		if (p == null)
			return (I) this;
		if (p instanceof ImageProcessor)
			return processInplace((ImageProcessor<I>) p);
		if (p instanceof KernelProcessor)
			return processInplace((KernelProcessor<Q, I>) p);
		if (p instanceof PixelProcessor)
			return processInplace((PixelProcessor<Q>) p);

		throw new UnsupportedOperationException("Unsupported Processor type");
	}

	/**
	 * Process this image with the given {@link ImageProcessor} side-affecting
	 * this image.
	 *
	 * @param p
	 *            The {@link ImageProcessor} to apply.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInplace(ImageProcessor<I> p) {
		p.processImage((I) this);
		return (I) this;
	}

	/**
	 * Process this image with the given {@link KernelProcessor} side-affecting
	 * this image.
	 *
	 * @param p
	 *            The {@link KernelProcessor} to apply.
	 * @return A reference to this image containing the result.
	 */
	public I processInplace(KernelProcessor<Q, I> p) {
		return processInplace(p, false);
	}

	/**
	 * Process this image with the given {@link KernelProcessor} side-affecting
	 * this image.
	 *
	 * @param p
	 *            The {@link KernelProcessor} to apply.
	 * @param pad
	 *            Should the image be zero padded so the kernel reaches the
	 *            edges of the output
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInplace(KernelProcessor<Q, I> p, boolean pad) {
		final I newImage = process(p, pad);
		this.internalAssign(newImage);
		return (I) this;
	}

	/**
	 * Process this image with the given {@link PixelProcessor} side-affecting
	 * this image.
	 *
	 * @param p
	 *            The {@link PixelProcessor} to apply.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInplace(PixelProcessor<Q> p) {
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				setPixel(x, y, p.processPixel(getPixel(x, y)));
			}
		}

		return (I) this;
	}

	/**
	 * Process this image with the given {@link PixelProcessor} only affecting
	 * those pixels where the mask is non-zero. Returns a new image.
	 *
	 * @param mask
	 *            The mask to apply to the processing.
	 * @param p
	 *            The {@link PixelProcessor} to apply.
	 * @return A new image containing the result.
	 */
	public I processMasked(FImage mask, PixelProcessor<Q> p) {
		final I newImage = this.clone();
		newImage.processMaskedInplace(mask, p);
		return newImage;
	}

	/**
	 * Process this image with the given {@link PixelProcessor}, only affecting
	 * those pixels where the mask is non-zero. Side-affects this image.
	 *
	 * @param mask
	 *            The mask to apply to the processor.
	 * @param p
	 *            The {@link PixelProcessor} to apply.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processMaskedInplace(FImage mask, PixelProcessor<Q> p) {
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				if (mask.pixels[y][x] == 0)
					continue;
				setPixel(x, y, p.processPixel(getPixel(x, y)));
			}
		}
		return (I) this;
	}

	/**
	 * Sets the pixel at <code>(x,y)</code> to the given value. Side-affects
	 * this image.
	 *
	 * @param x
	 *            The x-ordinate of the pixel to set
	 * @param y
	 *            The y-ordinate of the pixel to set
	 * @param val
	 *            The value to set the pixel to.
	 */
	public abstract void setPixel(int x, int y, Q val);

	/**
	 * Subtract the corresponding pixel value from the given image from the
	 * pixel values in this image. Returns a new image.
	 *
	 * @param im
	 *            The image to subtract from this image.
	 * @return A new image containing the result.
	 */
	public I subtract(Image<?, ?> im) {
		final I newImage = this.clone();
		newImage.subtractInplace(im);
		return newImage;
	}

	/**
	 * Subtract a scalar from every pixel value in this image and return new
	 * image.
	 *
	 * @param num
	 *            A value to subtract from each pixel.
	 * @return A new image containing the result.
	 */
	public I subtract(Q num) {
		final I newImage = this.clone();
		newImage.subtractInplace(num);
		return newImage;
	}

	/**
	 * Subtract the corresponding pixel value from the given image from the
	 * pixel values in this image. Side-affects this image.
	 *
	 * @param im
	 *            The image to subtract from this image.
	 * @return A reference to this containing the result.
	 */
	public abstract I subtractInplace(Image<?, ?> im);

	/**
	 * Subtract a scalar from every pixel value in this image. Side-affects this
	 * image.
	 *
	 * @param num
	 *            A value to subtract from each pixel.
	 * @return A reference to this image containing the result.
	 */
	public abstract I subtractInplace(Q num);

	/**
	 * Set all values less than the given threshold to 0 and all others to 1.
	 * Side-affects this image.
	 *
	 * @param thresh
	 *            The threshold value
	 * @return A reference to this image containing the result.
	 */
	public abstract I threshold(Q thresh);

	/**
	 * Convert the image to a byte representation suitable for writing to a pnm
	 * type format. Each byte should represent a single pixel. Multiband images
	 * should interleave the data; e.g. [R1,G1,B1,R2,G2,B2...etc.]
	 *
	 * @return This image as a byte array
	 */
	public abstract byte[] toByteImage();

	/**
	 * Returns a 1D array representation of this image with each pixel
	 * represented as a packed ARGB integer.
	 *
	 * @return An array of ARGB pixels.
	 */
	public abstract int[] toPackedARGBPixels();

	/**
	 * Apply a transform matrix to the image and returns the result as a new
	 * image.
	 *
	 * @param transform
	 *            The transform matrix to apply.
	 * @return A new image containing the result.
	 */
	public I transform(Matrix transform) {
		boolean unset = true;
		double minX = 0, minY = 0, maxX = 0, maxY = 0;
		final double[][][] extrema = new double[][][] {
				{ { 0 }, { 0 }, { 1 } },
				{ { 0 }, { this.getHeight() }, { 1 } },
				{ { this.getWidth() }, { 0 }, { 1 } },
				{ { this.getWidth() }, { this.getHeight() }, { 1 } },
		};
		for (final double[][] ext : extrema) {
			final Matrix tmp = transform.times(Matrix.constructWithCopy(ext));
			if (unset) {
				minX = maxX = tmp.get(0, 0);
				maxY = minY = tmp.get(1, 0);
				unset = false;
			} else {
				if (tmp.get(0, 0) > maxX)
					maxX = tmp.get(0, 0);
				if (tmp.get(1, 0) > maxY)
					maxY = tmp.get(1, 0);
				if (tmp.get(0, 0) < minX)
					minX = tmp.get(0, 0);
				if (tmp.get(1, 0) < minY)
					minY = tmp.get(1, 0);
			}
		}
		final I output = this.newInstance((int) (Math.abs(maxX - minX)), (int) (Math.abs(maxY - minY)));
		final Matrix invTrans = transform.inverse();
		final double[][] invTransData = invTrans.getArray();

		for (int x = 0; x < output.getWidth(); x++) {
			for (int y = 0; y < output.getHeight(); y++) {
				double oldx = invTransData[0][0] * x + invTransData[0][1] * y + invTransData[0][2];
				double oldy = invTransData[1][0] * x + invTransData[1][1] * y + invTransData[1][2];
				final double norm = invTransData[2][0] * x + invTransData[2][1] * y + invTransData[2][2];

				oldx /= norm;
				oldy /= norm;

				if (oldx < 0 || oldx >= this.getWidth() || oldy < 0 || oldy >= this.getHeight())
					continue;

				output.setPixel(x, y, this.getPixelInterp(oldx, oldy));
			}
		}
		return output;
	}

	/**
	 * Removes zero-valued pixels from around the outside of the image.
	 * Analagous to {@link String#trim()}.
	 *
	 * @return A new image containing the trimmed image.
	 */
	public I trim() {
		final Rectangle rect = this.getContentArea();
		return this.extractROI((int) rect.minX(), (int) rect.minY(), (int) (rect.getWidth()), (int) (rect.getHeight()));
	}

	/**
	 * Set all pixels in the image to zero. Side-affects this image.
	 *
	 * @return A reference to this image containing the result.
	 */
	public abstract I zero();

	/**
	 * Shifts all the pixels to the left by one pixel
	 *
	 * @return A reference to this image.
	 */
	public I shiftLeftInplace() {
		return shiftLeftInplace(1);
	}

	/**
	 * Shifts all the pixels to the right by one pixel
	 *
	 * @return A reference to this image.
	 */
	public I shiftRightInplace() {
		return shiftRightInplace(1);
	}

	/**
	 * Shifts all the pixels to the left by count pixel
	 *
	 * @param count
	 *            The number of pixels
	 *
	 * @return A reference to this image.
	 */
	public I shiftLeftInplace(int count) {
		return this.internalAssign(this.shiftLeft(count));
	}

	/**
	 * Shifts all the pixels to the right by count pixel
	 *
	 * @param count
	 *            The number of pixels
	 *
	 * @return A reference to this image.
	 */
	public I shiftRightInplace(int count) {
		return this.internalAssign(this.shiftRight(count));
	}

	/**
	 * Returns a new image that is it shifted around the x-ordinates by one
	 * pixel
	 *
	 * @return A new image shifted around to the left by one pixel
	 */
	public I shiftLeft() {
		return shiftLeft(1);
	}

	/**
	 * Returns a new image that is it shifted around the x-ordinates by the
	 * number of pixels given.
	 *
	 * @param nPixels
	 *            The number of pixels
	 *
	 * @return A new image shifted around to the left by the number of pixels
	 */
	public I shiftLeft(int nPixels) {
		final I output = this.newInstance(getWidth(), getHeight());
		final I img = this.extractROI(0, 0, nPixels, getHeight());
		output.createRenderer().drawImage(
				this.extractROI(nPixels, 0, getWidth() - nPixels, getHeight()), 0, 0);
		output.createRenderer().drawImage(img, getWidth() - nPixels, 0);
		return output;
	}

	/**
	 * Returns a new image that is it shifted around the x-ordinates by one
	 * pixel
	 *
	 * @return A new image shifted around to the right by one pixel
	 */
	public I shiftRight() {
		return shiftRight(1);
	}

	/**
	 * Returns a new image that is it shifted around the x-ordinates by the
	 * number of pixels given.
	 *
	 * @param nPixels
	 *            the number of pixels
	 *
	 * @return A new image shifted around to the right by the number of pixels
	 */
	public I shiftRight(int nPixels) {
		final I output = this.newInstance(getWidth(), getHeight());
		final I img = this.extractROI(getWidth() - nPixels, 0, nPixels, getHeight());
		output.createRenderer().drawImage(
				this.extractROI(0, 0, getWidth() - nPixels, getHeight()), nPixels, 0);
		output.createRenderer().drawImage(img, 0, 0);
		return output;
	}

	/**
	 * Shifts all the pixels up by one pixel
	 *
	 * @return A reference to this image.
	 */
	public I shiftUpInplace() {
		return shiftUpInplace(1);
	}

	/**
	 * Shifts all the pixels down by one pixels
	 *
	 * @return A reference to this image.
	 */
	public I shiftDownInplace() {
		return shiftDownInplace(1);
	}

	/**
	 * Shifts all the pixels up by count pixels
	 *
	 * @param count
	 *            The number of pixels
	 *
	 * @return A reference to this image.
	 */
	public I shiftUpInplace(int count) {
		return this.internalAssign(this.shiftUp(count));
	}

	/**
	 * Shifts all the pixels down by count pixels
	 *
	 * @param count
	 *            The number of pixels
	 *
	 * @return A reference to this image.
	 */
	public I shiftDownInplace(int count) {
		return this.internalAssign(this.shiftDown(count));
	}

	/**
	 * Returns a new image that is it shifted around the x-ordinates by one
	 * pixel
	 *
	 * @return A new image shifted around up by one pixel
	 */
	public I shiftUp() {
		return shiftUp(1);
	}

	/**
	 * Returns a new image that is it shifted around the x-ordinates by the
	 * number of pixels given.
	 *
	 * @param nPixels
	 *            The number of pixels
	 *
	 * @return A new image shifted around up by the number of pixels
	 */
	public I shiftUp(int nPixels) {
		final I output = this.newInstance(getWidth(), getHeight());
		final I img = this.extractROI(0, 0, getWidth(), nPixels);
		output.createRenderer().drawImage(
				this.extractROI(0, nPixels, getWidth(), getHeight() - nPixels), 0, 0);
		output.createRenderer().drawImage(img, 0, getHeight() - nPixels);
		return output;
	}

	/**
	 * Returns a new image that is it shifted around the x-ordinates by one
	 * pixel
	 *
	 * @return A new image shifted around down by one pixel
	 */
	public I shiftDown() {
		return shiftDown(1);
	}

	/**
	 * Returns a new image that is it shifted around the x-ordinates by the
	 * number of pixels given.
	 *
	 * @param nPixels
	 *            the number of pixels
	 *
	 * @return A new image shifted around down by the number of pixels
	 */
	public I shiftDown(int nPixels) {
		final I output = this.newInstance(getWidth(), getHeight());
		final I img = this.extractROI(0, getHeight() - nPixels, getWidth(), nPixels);
		output.createRenderer().drawImage(
				this.extractROI(0, 0, getWidth(), getHeight() - nPixels), 0, nPixels);
		output.createRenderer().drawImage(img, 0, 0);
		return output;
	}

	/**
	 * Overlays the given image on this image and returns a new image containing
	 * the result.
	 *
	 * @param image
	 *            The image to overlay on this image.
	 * @param x
	 *            The location at which to overlay the image
	 * @param y
	 *            The location at which to overlay the image
	 * @return A new image containing the result
	 */
	public I overlay(I image, int x, int y) {
		final I img = this.clone();
		img.overlayInplace(image, x, y);
		return img;
	}

	/**
	 * Overlays the given image on this image directly. The method returns this
	 * image for chaining.
	 *
	 * @param image
	 *            The image to overlay
	 * @param x
	 *            The location at which to overlay the image
	 * @param y
	 *            The location at which to overlay the image
	 * @return Returns this image
	 */
	public abstract I overlayInplace(I image, int x, int y);

	@SuppressWarnings("unchecked")
	@Override
	public I getImage() {
		return (I) this;
	}

	/**
	 * Replace pixels of a certain colour with another colour. Side-affects this
	 * image.
	 *
	 * @param target
	 *            the colour to fill the image with
	 * @param replacement
	 *            the colour to fill the image with
	 * @return A reference to this image.
	 */
	public abstract I replace(Q target, Q replacement);

	/**
	 * Sub-pixel sampling of a centred rectangular region such that
	 * <code>dst(x, y) = src(x + center.x   (width(dst)   1) ⇤ 0.5, y + center.y   (height(dst)   1) ⇤ 0.5)</code>
	 * . Sub-pixels values are estimated using bilinear interpolation.
	 *
	 * @see #getPixelInterp(double, double)
	 *
	 * @param centre
	 *            the centre
	 * @param width
	 *            the region width
	 * @param height
	 *            the region height
	 * @return the extracted sub-pixel region
	 */
	public I extractCentreSubPix(Point2d centre, int width, int height) {
		return extractCentreSubPix(centre.getX(), centre.getY(), width, height);
	}

	/**
	 * Sub-pixel sampling of a centred rectangular region such that
	 * <code>dst(x, y) = src(x + center.x   (width(dst)   1) ⇤ 0.5, y + center.y   (height(dst)   1) ⇤ 0.5)</code>
	 * . Sub-pixels values are estimated using bilinear interpolation.
	 *
	 * @see #getPixelInterp(double, double)
	 * @param cx
	 *            the x-ordinate of the centre
	 * @param cy
	 *            the y-ordinate of the centre
	 * @param width
	 *            the region width
	 * @param height
	 *            the region height
	 * @return the extracted sub-pixel region
	 */
	public I extractCentreSubPix(float cx, float cy, int width, int height) {
		final I out = newInstance(width, height);
		return extractCentreSubPix(cx, cy, out);
	}

	/**
	 * Sub-pixel sampling of a centred rectangular region such that
	 * <code>dst(x, y) = src(x + center.x   (width(dst)   1) ⇤ 0.5, y + center.y   (height(dst)   1) ⇤ 0.5)</code>
	 * . Sub-pixels values are estimated using bilinear interpolation.
	 *
	 * @see #getPixelInterp(double, double)
	 *
	 * @param centre
	 *            the centre
	 * @param out
	 *            the output image (also defines the size of the extracted
	 *            region)
	 * @return <code>out</code>
	 */
	public I extractCentreSubPix(Point2d centre, I out) {
		return extractCentreSubPix(centre.getX(), centre.getY(), out);
	}

	/**
	 * Sub-pixel sampling of a centred rectangular region such that
	 * <code>dst(x, y) = src(x + center.x   (width(dst)   1) ⇤ 0.5, y + center.y   (height(dst)   1) ⇤ 0.5)</code>
	 * . Sub-pixels values are estimated using bilinear interpolation.
	 *
	 * @see #getPixelInterp(double, double)
	 * @param cx
	 *            the x-ordinate of the centre
	 * @param cy
	 *            the y-ordinate of the centre
	 * @param out
	 *            the output image (also defines the size of the extracted
	 *            region)
	 * @return <code>out</code>
	 */
	public abstract I extractCentreSubPix(float cx, float cy, I out);
}
