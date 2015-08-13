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
package org.openimaj.image.renderer;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.renderer.ScanRasteriser.ScanLineListener;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.path.Path2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;

import com.caffeineowl.graphics.bezier.BezierUtils;
import com.caffeineowl.graphics.bezier.CubicSegmentConsumer;
import com.caffeineowl.graphics.bezier.QuadSegmentConsumer;
import com.caffeineowl.graphics.bezier.flatnessalgos.SimpleConvexHullSubdivCriterion;

/**
 * ImageRenderer is the abstract base class for all renderers capable of drawing
 * to images.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <Q>
 *            Pixel type
 * @param <I>
 *            Image type
 */
public abstract class ImageRenderer<Q, I extends Image<Q, I>> {
	protected RenderHints hints;
	protected I targetImage;

	/**
	 * Construct with given target image.
	 *
	 * @param targetImage
	 *            the target image.
	 */
	public ImageRenderer(final I targetImage) {
		this(targetImage, new RenderHints());
	}

	/**
	 * Construct with given target image and rendering hints.
	 *
	 * @param targetImage
	 *            the target image.
	 * @param hints
	 *            the render hints
	 */
	public ImageRenderer(final I targetImage, final RenderHints hints) {
		this.targetImage = targetImage;
		this.hints = hints;
	}

	/**
	 * Draw onto this image lines drawn with the given colour between the points
	 * given. No points are drawn.
	 *
	 * @param pts
	 *            The point list to draw onto this image.
	 * @param col
	 *            The colour to draw the lines
	 */
	public void drawConnectedPoints(final List<? extends Point2d> pts, final Q col) {
		Point2d p0 = pts.get(0);
		for (int i = 1; i < pts.size(); i++) {
			final Point2d p1 = pts.get(i);

			final int x0 = Math.round(p0.getX());
			final int y0 = Math.round(p0.getY());
			final int x1 = Math.round(p1.getX());
			final int y1 = Math.round(p1.getY());

			this.drawLine(x0, y0, x1, y1, col);

			p0 = p1;
		}
	}

	/**
	 * Draw into this image the provided image at the given coordinates. Parts
	 * of the image outside the bounds of this image will be ignored.
	 *
	 * @param image
	 *            The image to draw.
	 * @param x
	 *            The x-coordinate of the top-left of the image
	 * @param y
	 *            The y-coordinate of the top-left of the image
	 */
	public void drawImage(final I image, final int x, final int y) {
		final int stopx = Math.min(this.targetImage.getWidth(), x + image.getWidth());
		final int stopy = Math.min(this.targetImage.getHeight(), y + image.getHeight());
		final int startx = Math.max(0, x);
		final int starty = Math.max(0, y);

		for (int yy = starty; yy < stopy; yy++)
			for (int xx = startx; xx < stopx; xx++)
				this.targetImage.setPixel(xx, yy, image.getPixel(xx - x, yy - y));
	}

	/**
	 * Draw into this image the provided image at the given coordinates ignoring
	 * certain pixels. Parts of the image outside the bounds of this image will
	 * be ignored. Pixels in the ignore list will be stripped from the image to
	 * draw.
	 *
	 * @param image
	 *            The image to draw.
	 * @param x
	 *            The x-coordinate of the top-left of the image
	 * @param y
	 *            The y-coordinate of the top-left of the image
	 * @param ignoreList
	 *            The list of pixels to ignore when copying the image
	 */
	public void drawImage(final I image, final int x, final int y, @SuppressWarnings("unchecked") final Q... ignoreList) {
		final int stopx = Math.min(this.targetImage.getWidth(), x + image.getWidth());
		final int stopy = Math.min(this.targetImage.getHeight(), y + image.getHeight());
		final int startx = Math.max(0, x);
		final int starty = Math.max(0, y);

		for (int yy = starty; yy < stopy; yy++)
			for (int xx = startx; xx < stopx; xx++) {
				final Q val = image.getPixel(xx - x, yy - y);
				if (Arrays.binarySearch(ignoreList, val, this.targetImage.getPixelComparator()) < 0)
					this.targetImage.setPixel(xx, yy, val);
			}

	}

	/**
	 * Draw a line from the coordinates specified by <code>(x1,y1)</code> at an
	 * angle of <code>theta</code> with the given length, thickness and colour.
	 *
	 * @param x1
	 *            The x-coordinate to start the line.
	 * @param y1
	 *            The y-coordinate to start the line.
	 * @param theta
	 *            The angle at which to draw the line.
	 * @param length
	 *            The length to draw the line.
	 * @param thickness
	 *            The thickness to draw the line.
	 * @param col
	 *            The colour to draw the line.
	 */
	public abstract void drawLine(int x1, int y1, double theta, int length, int thickness, Q col);

	/**
	 * Draw a line from the coordinates specified by <code>(x1,y1)</code> at an
	 * angle of <code>theta</code> with the given length and colour.
	 * Line-thickness will be 1.
	 *
	 * @param x1
	 *            The x-coordinate to start the line.
	 * @param y1
	 *            The y-coordinate to start the line.
	 * @param theta
	 *            The angle at which to draw the line.
	 * @param length
	 *            The length to draw the line.
	 * @param col
	 *            The colour to draw the line.
	 */
	public void drawLine(final int x1, final int y1, final double theta, final int length, final Q col) {
		this.drawLine(x1, y1, theta, length, 1, col);
	}

	/**
	 * Draw a line from the coordinates specified by <code>(x0,y0)</code> to the
	 * coordinates specified by <code>(x1,y1)</code> using the given color and
	 * thickness.
	 *
	 * @param x0
	 *            The x-coordinate at the start of the line.
	 * @param y0
	 *            The y-coordinate at the start of the line.
	 * @param x1
	 *            The x-coordinate at the end of the line.
	 * @param y1
	 *            The y-coordinate at the end of the line.
	 * @param thickness
	 *            The thickness which to draw the line.
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public abstract void drawLine(int x0, int y0, int x1, int y1, int thickness, Q col);

	/**
	 * Draw a line from the coordinates specified by <code>(x0,y0)</code> to the
	 * coordinates specified by <code>(x1,y1)</code> using the given color and
	 * thickness.
	 *
	 * @param x0
	 *            The x-coordinate at the start of the line.
	 * @param y0
	 *            The y-coordinate at the start of the line.
	 * @param x1
	 *            The x-coordinate at the end of the line.
	 * @param y1
	 *            The y-coordinate at the end of the line.
	 * @param thickness
	 *            The thickness which to draw the line.
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public abstract void drawLine(final float x0, final float y0, final float x1, final float y1, final int thickness,
			final Q col);

	/**
	 * Draw a line from the coordinates specified by <code>(x0,y0)</code> to
	 * <code>(x1,y1)</code> using the given colour. The line thickness will be 1
	 * pixel.
	 *
	 * @param x0
	 *            The x-coordinate at the start of the line.
	 * @param y0
	 *            The y-coordinate at the start of the line.
	 * @param x1
	 *            The x-coordinate at the end of the line.
	 * @param y1
	 *            The y-coordinate at the end of the line.
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public void drawLine(final int x0, final int y0, final int x1, final int y1, final Q col) {
		this.drawLine(x0, y0, x1, y1, 1, col);
	}

	/**
	 * Draw a line from the coordinates specified by <code>(x0,y0)</code> to
	 * <code>(x1,y1)</code> using the given colour. The line thickness will be 1
	 * pixel.
	 *
	 * @param p1
	 *            The coordinate of the start of the line.
	 * @param p2
	 *            The coordinate of the end of the line.
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public void drawLine(final Point2d p1, final Point2d p2, final Q col) {
		this.drawLine(Math.round(p1.getX()), Math.round(p1.getY()),
				Math.round(p2.getX()), Math.round(p2.getY()),
				1, col);
	}

	/**
	 * Draw a line from the coordinates specified by <code>(x0,y0)</code> to
	 * <code>(x1,y1)</code> using the given colour and thickness.
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
	public void drawLine(final Point2d p1, final Point2d p2, final int thickness, final Q col) {
		this.drawLine(Math.round(p1.getX()), Math.round(p1.getY()),
				Math.round(p2.getX()), Math.round(p2.getY()),
				thickness, col);
	}

	/**
	 * Draw a line from the specified {@link Path2d} object
	 *
	 * @param line
	 *            the line
	 * @param thickness
	 *            the stroke width
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public void drawLine(final Path2d line, final int thickness, final Q col) {
		drawPath(line, thickness, col);
	}

	/**
	 * Draw a path from the specified {@link Path2d} object
	 *
	 * @param path
	 *            the path
	 * @param thickness
	 *            the stroke width
	 * @param col
	 *            The colour in which to draw the line.
	 */
	public void drawPath(final Path2d path, final int thickness, final Q col) {
		final Iterator<Line2d> it = path.lineIterator();

		while (it.hasNext()) {
			final Line2d line = it.next();
			this.drawLine((int) line.begin.getX(), (int) line.begin.getY(), (int) line.end.getX(), (int) line.end.getY(),
					thickness, col);
		}
	}

	/**
	 * Draw the given list of lines using {@link #drawLine(Path2d, int, Object)}
	 * with the given colour and thickness.
	 *
	 * @param lines
	 *            The list of lines to draw.
	 * @param thickness
	 *            the stroke width
	 * @param col
	 *            The colour to draw each point.
	 */
	public void drawLines(final Iterable<? extends Path2d> lines, final int thickness, final Q col) {
		drawPaths(lines, thickness, col);
	}

	/**
	 * Draw the given list of lines using {@link #drawLine(Path2d, int, Object)}
	 * with the given colour and thickness.
	 *
	 * @param paths
	 *            The list of paths to draw.
	 * @param thickness
	 *            the stroke width
	 * @param col
	 *            The colour to draw each point.
	 */
	public void drawPaths(final Iterable<? extends Path2d> paths, final int thickness, final Q col) {
		for (final Path2d path : paths)
			this.drawLine(path, thickness, col);
	}

	/**
	 * Draw a dot centered on the given location (rounded to nearest integer
	 * location) at the given size and with the given color.
	 *
	 *
	 * @param p
	 *            The coordinates at which to draw the point
	 * @param col
	 *            The colour to draw the point
	 * @param size
	 *            The size at which to draw the point.
	 */
	public abstract void drawPoint(Point2d p, Q col, int size);

	/**
	 * Draw the given list of points using
	 * {@link #drawPoint(Point2d, Object, int)} with the given colour and size.
	 *
	 * @param pts
	 *            The list of points to draw.
	 * @param col
	 *            The colour to draw each point.
	 * @param size
	 *            The size to draw each point.
	 */
	public void drawPoints(final Iterable<? extends Point2d> pts, final Q col, final int size) {
		for (final Point2d p : pts)
			this.drawPoint(p, col, size);
	}

	/**
	 * Draw the given polygon in the specified colour with the given thickness
	 * lines.
	 *
	 *
	 * @param p
	 *            The polygon to draw.
	 * @param thickness
	 *            The thickness of the lines to use
	 * @param col
	 *            The colour to draw the lines in
	 */
	public abstract void drawPolygon(Polygon p, int thickness, Q col);

	/**
	 * Draw the given polygon in the specified colour. Uses
	 * {@link #drawPolygon(Polygon, int, Object)} with line thickness 1.
	 *
	 *
	 * @param p
	 *            The polygon to draw.
	 * @param col
	 *            The colour to draw the polygon in.
	 */
	public void drawPolygon(final Polygon p, final Q col) {
		this.drawPolygon(p, 1, col);
	}

	/**
	 * Draw a horizontal line with the specified colour.
	 *
	 * @param x1
	 *            starting x (inclusive)
	 * @param x2
	 *            ending x (inclusive)
	 * @param y
	 *            y
	 * @param col
	 *            the colour
	 */
	protected abstract void drawHorizLine(int x1, int x2, int y, Q col);

	/**
	 * Draw the given polygon, filled with the specified colour.
	 *
	 * @param p
	 *            The polygon to draw.
	 * @param col
	 *            The colour to fill the polygon with.
	 */
	public void drawPolygonFilled(Polygon p, final Q col) {
		// clip to the frame
		p = p.intersect(this.targetImage.getBounds().asPolygon());

		this.drawPolygon(p, col);

		if (p.getNumInnerPoly() == 1) {
			ScanRasteriser.scanFill(p.points, new ScanLineListener() {
				@Override
				public void process(final int x1, final int x2, final int y) {
					ImageRenderer.this.drawHorizLine(x1, x2, y, col);
				}
			});
		} else {
			// final ConnectedComponent cc = new ConnectedComponent(p);
			// cc.process(new BlobRenderer<Q>(this.targetImage, col));

			final int minx = Math.max(0, (int) Math.round(p.minX()));
			final int maxx = Math.min((int) Math.round(p.maxX()), targetImage.getWidth() - 1);
			final int miny = Math.max(0, (int) Math.round(p.minY()));
			final int maxy = Math.min((int) Math.round(p.maxY()), targetImage.getHeight() - 1);

			final Pixel tmp = new Pixel();
			for (tmp.y = miny; tmp.y <= maxy; tmp.y++) {
				for (tmp.x = minx; tmp.x <= maxx; tmp.x++) {
					if (p.isInside(tmp))
						this.targetImage.setPixel(tmp.x, tmp.y, col);
				}
			}
		}
	}

	/**
	 * Draw the given shape in the specified colour with the given thickness
	 * lines.
	 *
	 * @param s
	 *            The shape to draw.
	 * @param thickness
	 *            The thickness of the lines to use
	 * @param col
	 *            The colour to draw the lines in
	 */
	public void drawShape(final Shape s, final int thickness, final Q col) {
		this.drawPolygon(s.asPolygon(), thickness, col);
	}

	/**
	 * Draw the given shape in the specified colour. Uses
	 * {@link #drawPolygon(Polygon, int, Object)} with line thickness 1.
	 *
	 * @param p
	 *            The shape to draw.
	 * @param col
	 *            The colour to draw the polygon in.
	 */
	public void drawShape(final Shape p, final Q col) {
		this.drawShape(p, 1, col);
	}

	/**
	 * Draw the given shape, filled with the specified colour.
	 *
	 * @param s
	 *            The shape to draw.
	 * @param col
	 *            The colour to fill the polygon with.
	 */
	public void drawShapeFilled(final Shape s, Q col) {
		col = this.sanitise(col);
		if (s instanceof Polygon) {
			this.drawPolygonFilled((Polygon) s, col);
		} else {
			this.drawShape(s, col);

			final int minx = (int) Math.max(0, Math.round(s.minX()));
			final int maxx = (int) Math.min(this.targetImage.getWidth(), Math.round(s.maxX()));
			final int miny = (int) Math.max(0, Math.round(s.minY()));
			final int maxy = (int) Math.min(this.targetImage.getHeight(), Math.round(s.maxY()));

			for (int y = miny; y <= maxy; y++) {
				for (int x = minx; x <= maxx; x++) {
					final Pixel p = new Pixel(x, y);
					if (s.isInside(p))
						this.targetImage.setPixel(p.x, p.y, col);
				}
			}
		}
	}

	/**
	 * Render the text in the given font with the default style.
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
	public <F extends Font<F>> void drawText(final String text, final int x, final int y, final F f, final int sz) {
		final FontStyle<Q> sty = f.createStyle(this);
		sty.setFontSize(sz);
		f.getRenderer(this).renderText(this, text, x, y, sty);
	}

	/**
	 * Render the text in the given font in the given colour with the default
	 * style.
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
	public <F extends Font<F>> void drawText(final String text, final int x, final int y, final F f, final int sz,
			final Q col)
	{
		final FontStyle<Q> sty = f.createStyle(this);
		sty.setFontSize(sz);
		sty.setColour(col);
		f.getRenderer(this).renderText(this, text, x, y, sty);
	}

	/**
	 * Render the text in the given font with the default style.
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
	public <F extends Font<F>> void drawText(final String text, final Point2d pt, final F f, final int sz) {
		final FontStyle<Q> sty = f.createStyle(this);
		sty.setFontSize(sz);
		f.getRenderer(this).renderText(this, text, (int) pt.getX(), (int) pt.getY(), sty);
	}

	/**
	 * Render the text in the given font in the given colour with the default
	 * style.
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
	public <F extends Font<F>> void drawText(final String text, final Point2d pt, final F f, final int sz, final Q col) {
		final FontStyle<Q> sty = f.createStyle(this);
		sty.setFontSize(sz);
		sty.setColour(col);
		f.getRenderer(this).renderText(this, text, (int) pt.getX(), (int) pt.getY(), sty);
	}

	/**
	 * Render the text with the given {@link FontStyle}.
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
	public void drawText(final String text, final int x, final int y, final FontStyle<Q> f) {
		f.getRenderer(this).renderText(this, text, x, y, f);
	}

	/**
	 * Render the text with the given {@link FontStyle}.
	 *
	 * @param text
	 *            the text
	 * @param pt
	 *            the coordinate to render at
	 * @param f
	 *            the font style
	 */
	public void drawText(final String text, final Point2d pt, final FontStyle<Q> f) {
		f.getRenderer(this).renderText(this, text, (int) pt.getX(), (int) pt.getY(), f);
	}

	/**
	 * Render the text using its attributes.
	 *
	 * @param text
	 *            the text
	 * @param x
	 *            the x-ordinate
	 * @param y
	 *            the y-ordinate
	 */
	public void drawText(final AttributedString text, final int x, final int y) {
		FontRenderer.renderText(this, text, x, y);
	}

	/**
	 * Render the text using its attributes.
	 *
	 * @param text
	 *            the text
	 * @param pt
	 *            the coordinate to render at
	 */
	public void drawText(final AttributedString text, final Point2d pt) {
		FontRenderer.renderText(this, text, (int) pt.getX(), (int) pt.getY());
	}

	/**
	 * Draw a cubic Bezier curve into the image with 100 point accuracy.
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
	public Point2d[] drawCubicBezier(final Point2d p1, final Point2d p2,
			final Point2d c1, final Point2d c2, final int thickness, final Q col)
	{
		final List<Point2d> points = new ArrayList<Point2d>();

		final CubicCurve2D c = new CubicCurve2D.Double(
				p1.getX(), p1.getY(), c1.getX(), c1.getY(),
				c2.getX(), c2.getY(), p2.getX(), p2.getY());
		BezierUtils.adaptiveHalving(c, new SimpleConvexHullSubdivCriterion(),
				new CubicSegmentConsumer()
		{
			@Override
			public void processSegment(final CubicCurve2D segment,
					final double startT, final double endT)
			{
				if (0.0 == startT)
					points.add(new Point2dImpl(
							(float) segment.getX1(), (float) segment.getY1()));

				points.add(new Point2dImpl(
						(float) segment.getX2(), (float) segment.getY2()));
			}
		}
				);

		Point2d last = null;
		for (final Point2d p : points) {
			if (last != null)
				this.drawLine((int) last.getX(), (int) last.getY(),
						(int) p.getX(), (int) p.getY(), thickness, col);
			last = p;
		}

		return points.toArray(new Point2d[1]);
	}

	/**
	 * Draw a Quadratic Bezier curve
	 *
	 * @param p1
	 * @param p2
	 * @param c1
	 * @param thickness
	 * @param colour
	 * @return a set of points on the curve
	 */
	public Point2d[] drawQuadBezier(final Point2d p1, final Point2d p2, final Point2d c1,
			final int thickness, final Q colour)
	{
		final List<Point2d> points = new ArrayList<Point2d>();

		final QuadCurve2D c = new QuadCurve2D.Double(
				p1.getX(), p1.getY(), c1.getX(), c1.getY(), p2.getX(), p2.getY());
		BezierUtils.adaptiveHalving(c, new SimpleConvexHullSubdivCriterion(),
				new QuadSegmentConsumer()
		{
			@Override
			public void processSegment(final QuadCurve2D segment, final double startT, final double endT)
			{
				if (0.0 == startT)
					points.add(new Point2dImpl(
							(float) segment.getX1(), (float) segment.getY1()));

				points.add(new Point2dImpl(
						(float) segment.getX2(), (float) segment.getY2()));
			}
		}
				);

		Point2d last = null;
		for (final Point2d p : points) {
			if (last != null)
				this.drawLine((int) last.getX(), (int) last.getY(),
						(int) p.getX(), (int) p.getY(), thickness, colour);
			last = p;
		}

		return points.toArray(new Point2d[1]);

	}

	/**
	 * Get the default foreground colour.
	 *
	 * @return the default foreground colour.
	 */
	public abstract Q defaultForegroundColour();

	/**
	 * Get the default foreground colour.
	 *
	 * @return the default foreground colour.
	 */
	public abstract Q defaultBackgroundColour();

	/**
	 * Get the target image
	 *
	 * @return the image
	 */
	public I getImage() {
		return this.targetImage;
	}

	/**
	 * Change the target image of this renderer.
	 *
	 * @param image
	 *            new target
	 */
	public void setImage(final I image) {
		this.targetImage = image;
	}

	/**
	 * Get the render hints object associated with this renderer
	 *
	 * @return the render hints
	 */
	public RenderHints getRenderHints() {
		return this.hints;
	}

	/**
	 * Set the render hints associated with this renderer
	 *
	 * @param hints
	 *            the new hints
	 */
	public void setRenderHints(final RenderHints hints) {
		this.hints = hints;
	}

	/**
	 * Sanitize the colour given to fit this image's pixel type.
	 *
	 * @param size
	 *            The colour to sanitize
	 * @return The array
	 */
	protected abstract Q sanitise(Q colour);
}
