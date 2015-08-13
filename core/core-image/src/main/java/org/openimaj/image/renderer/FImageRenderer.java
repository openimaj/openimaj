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

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.renderer.ScanRasteriser.ScanLineListener;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;

/**
 * {@link ImageRenderer} for {@link FImage} images. Supports both anti-aliased
 * and fast rendering.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FImageRenderer extends ImageRenderer<Float, FImage> {

	/**
	 * Construct with given target image.
	 *
	 * @param targetImage
	 *            the target image.
	 */
	public FImageRenderer(final FImage targetImage) {
		super(targetImage);
	}

	/**
	 * Construct with given target image and rendering hints.
	 *
	 * @param targetImage
	 *            the target image.
	 * @param hints
	 *            the render hints
	 */
	public FImageRenderer(final FImage targetImage, final RenderHints hints) {
		super(targetImage, hints);
	}

	@Override
	public Float defaultForegroundColour() {
		return 1f;
	}

	@Override
	public Float defaultBackgroundColour() {
		return 0f;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.renderer.ImageRenderer#drawLine(int, int, double,
	 *      int, int, java.lang.Object)
	 */
	@Override
	public void drawLine(final int x1, final int y1, final double theta, final int length, final int thickness,
			final Float grey)
	{
		final int x2 = x1 + (int) Math.round(Math.cos(theta) * length);
		final int y2 = y1 + (int) Math.round(Math.sin(theta) * length);

		this.drawLine(x1, y1, x2, y2, thickness, grey);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.renderer.ImageRenderer#drawLine(int, int, int,
	 *      int, int, java.lang.Object)
	 */
	@Override
	public void drawLine(final int x0, final int y0, final int x1, final int y1, final int thickness, final Float grey) {
		this.drawLine((float) x0, (float) y0, (float) x1, (float) y1, thickness, grey);
	}

	@Override
	public void drawLine(final float x0, final float y0, final float x1, final float y1, final int thickness,
			final Float grey)
	{
		switch (this.hints.drawingAlgorithm) {
		case ANTI_ALIASED:
			if (thickness <= 1) {
				this.drawLineXiaolinWu(x0, y0, x1, y1, grey);
			} else {
				final double theta = Math.atan2(y1 - y0, x1 - x0);
				final double t = thickness / 2;
				final double sin = t * Math.sin(theta);
				final double cos = t * Math.cos(theta);

				final Polygon p = new Polygon();
				p.addVertex(new Point2dImpl((float) (x0 - sin), (float) (y0 + cos)));
				p.addVertex(new Point2dImpl((float) (x0 + sin), (float) (y0 - cos)));
				p.addVertex(new Point2dImpl((float) (x1 + sin), (float) (y1 - cos)));
				p.addVertex(new Point2dImpl((float) (x1 - sin), (float) (y1 + cos)));

				this.drawPolygonFilled(p, grey);
			}
			break;
		default:
			this.drawLineBresenham(Math.round(x0), Math.round(y0), Math.round(x1), Math.round(y1), thickness, grey);
		}
	}

	private float fpart(final float f) {
		return f - (int) f;
	}

	private float rfpart(final float f) {
		return 1 - this.fpart(f);
	}

	private void plot(final int a, final int b, final float c, final float grey, final boolean reversed) {
		int x, y;
		if (reversed) {
			y = a;
			x = b;
		} else {
			x = a;
			y = b;
		}

		if (x >= 0 && x < this.targetImage.width && y >= 0 && y < this.targetImage.height && !Float.isNaN(c)) {
			this.targetImage.pixels[y][x] = c * grey + (1 - c) * this.targetImage.pixels[y][x];
		}
	}

	/*
	 * Implementation of Xiaolin Wu's anti-aliased line drawing algorithm. Based
	 * on the wikipedia article:
	 * http://en.wikipedia.org/wiki/Xiaolin_Wu's_line_algorithm
	 */
	protected void drawLineXiaolinWu(float x1, float y1, float x2, float y2, final Float grey) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		boolean reversed = false;

		if (Math.abs(dx) < Math.abs(dy)) {
			float tmp;
			tmp = x1;
			x1 = y1;
			y1 = tmp;
			tmp = x2;
			x2 = y2;
			y2 = tmp;
			tmp = dx;
			dx = dy;
			dy = tmp;
			reversed = true;
		}

		if (x2 < x1) {
			float tmp;
			tmp = x1;
			x1 = x2;
			x2 = tmp;
			tmp = y1;
			y1 = y2;
			y2 = tmp;
		}

		final float gradient = dy / dx;

		// handle first endpoint
		int xend = Math.round(x1);
		float yend = y1 + gradient * (xend - x1);
		float xgap = this.rfpart(x1 + 0.5f);
		final int xpxl1 = xend; // this will be used in the main loop
		final int ypxl1 = (int) (yend);
		this.plot(xpxl1, ypxl1, this.rfpart(yend) * xgap, grey, reversed);
		this.plot(xpxl1, ypxl1 + 1, this.fpart(yend) * xgap, grey, reversed);
		float intery = yend + gradient; // first y-intersection for the main
		// loop

		// handle second endpoint
		xend = Math.round(x2);
		yend = y2 + gradient * (xend - x2);
		xgap = this.fpart(x2 + 0.5f);
		final int xpxl2 = xend; // this will be used in the main loop
		final int ypxl2 = (int) (yend);
		this.plot(xpxl2, ypxl2, this.rfpart(yend) * xgap, grey, reversed);
		this.plot(xpxl2, ypxl2 + 1, this.fpart(yend) * xgap, grey, reversed);

		// main loop
		for (int x = xpxl1 + 1; x <= xpxl2 - 1; x++) {
			this.plot(x, (int) (intery), this.rfpart(intery), grey, reversed);
			this.plot(x, (int) (intery) + 1, this.fpart(intery), grey, reversed);
			intery += gradient;
		}
	}

	/*
	 * Implementation of Bresenham's fast line drawing algorithm. Based on the
	 * wikipedia article:
	 * http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
	 */
	protected void drawLineBresenham(int x0, int y0, int x1, int y1, int thickness, final Float grey) {
		final Line2d line = new Line2d(new Point2dImpl(x0, y0), new Point2dImpl(x1, y1))
				.lineWithinSquare(this.targetImage
						.getBounds());
		if (line == null)
			return;

		x0 = (int) line.begin.getX();
		y0 = (int) line.begin.getY();
		x1 = (int) line.end.getX();
		y1 = (int) line.end.getY();

		final double theta = Math.atan2(y1 - y0, x1 - x0);
		thickness = (int) Math.round(thickness * Math.max(Math.abs(Math.cos(theta)), Math.abs(Math.sin(theta))));

		final int offset = thickness / 2;
		final int extra = thickness % 2;

		// implementation of Bresenham's algorithm from Wikipedia.
		int Dx = x1 - x0;
		int Dy = y1 - y0;
		final boolean steep = (Math.abs(Dy) >= Math.abs(Dx));
		if (steep) {
			int tmp;
			// SWAP(x0, y0);
			tmp = x0;
			x0 = y0;
			y0 = tmp;
			// SWAP(x1, y1);
			tmp = x1;
			x1 = y1;
			y1 = tmp;

			// recompute Dx, Dy after swap
			Dx = x1 - x0;
			Dy = y1 - y0;
		}
		int xstep = 1;
		if (Dx < 0) {
			xstep = -1;
			Dx = -Dx;
		}
		int ystep = 1;
		if (Dy < 0) {
			ystep = -1;
			Dy = -Dy;
		}
		final int TwoDy = 2 * Dy;
		final int TwoDyTwoDx = TwoDy - 2 * Dx; // 2*Dy - 2*Dx
		int E = TwoDy - Dx; // 2*Dy - Dx
		int y = y0;
		int xDraw, yDraw;
		for (int x = x0; x != x1; x += xstep) {
			if (steep) {
				xDraw = y;
				yDraw = x;
			} else {
				xDraw = x;
				yDraw = y;
			}
			// plot
			if (xDraw >= 0 && xDraw < this.targetImage.width && yDraw >= 0 && yDraw < this.targetImage.height) {
				if (thickness == 1) {
					this.targetImage.pixels[yDraw][xDraw] = grey;
				} else if (thickness > 1) {
					for (int yy = yDraw - offset; yy < yDraw + offset + extra; yy++)
						for (int xx = xDraw - offset; xx < xDraw + offset + extra; xx++)
							if (xx >= 0 && yy >= 0 && xx < this.targetImage.width && yy < this.targetImage.height)
								this.targetImage.pixels[yy][xx] = grey;
				}
			}

			// next
			if (E > 0) {
				E += TwoDyTwoDx; // E += 2*Dy - 2*Dx;
				y = y + ystep;
			} else {
				E += TwoDy; // E += 2*Dy;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.renderer.ImageRenderer#drawPoint(org.openimaj.math.geometry.point.Point2d,
	 *      java.lang.Object, int)
	 */
	@Override
	public void drawPoint(final Point2d p, final Float grey, final int size) {

		if (!this.targetImage.getBounds().isInside(p))
			return;
		final int halfsize = (size + 1) / 2; // 3 == 2, 4 = 2, 5 = 3, 6 = 3 etc.
		// TODO anti-aliased point rendering
		final int x = Math.round(p.getX());
		final int y = Math.round(p.getY());
		final int startx = Math.max(0, x - (halfsize - 1));
		final int starty = Math.max(0, y - (halfsize - 1));
		final int endx = Math.min(this.targetImage.width, x + halfsize);
		final int endy = Math.min(this.targetImage.height, y + halfsize);

		for (int j = starty; j < endy; j++) {
			for (int i = startx; i < endx; i++) {
				this.targetImage.pixels[j][i] = grey;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.renderer.ImageRenderer#drawPolygon(org.openimaj.math.geometry.shape.Polygon,
	 *      int, java.lang.Object)
	 */
	@Override
	public void drawPolygon(final Polygon p, final int thickness, final Float grey) {
		if (p.nVertices() < 2)
			return;

		Point2d p1, p2;
		for (int i = 0; i < p.nVertices() - 1; i++) {
			p1 = p.getVertices().get(i);
			p2 = p.getVertices().get(i + 1);
			this.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), thickness, grey);
		}

		p1 = p.getVertices().get(p.nVertices() - 1);
		p2 = p.getVertices().get(0);
		this.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), thickness, grey);

		for (final Polygon i : p.getInnerPolys())
			drawPolygon(i, thickness, grey);
	}

	@Override
	protected void drawHorizLine(final int x1, final int x2, final int y, final Float col) {
		if (y < 0 || y > this.targetImage.getHeight() - 1)
			return;

		final int startx = Math.max(0, Math.min(x1, x2));
		final int stopx = Math.min(Math.max(x1, x2), this.targetImage.getWidth() - 1);
		final float[][] img = this.targetImage.pixels;
		final float c = col;

		for (int x = startx; x <= stopx; x++) {
			img[y][x] = c;
		}
	}

	@Override
	protected Float sanitise(final Float colour)
	{
		return colour;
	}

	@Override
	public void drawPolygonFilled(Polygon p, final Float col) {
		// clip to the frame
		p = p.intersect(this.targetImage.getBounds().asPolygon());

		this.drawPolygon(p, col);

		if (p.getNumInnerPoly() == 1) {
			ScanRasteriser.scanFill(p.points, new ScanLineListener() {
				@Override
				public void process(final int x1, final int x2, final int y) {
					FImageRenderer.this.drawHorizLine(x1, x2, y, col);
				}
			});
		} else {
			// final ConnectedComponent cc = new ConnectedComponent(p);
			// cc.process(new BlobRenderer<Q>(this.targetImage, col));

			final int minx = Math.max(0, (int) Math.round(p.minX()));
			final int maxx = Math.min((int) Math.round(p.maxX()), targetImage.width - 1);
			final int miny = Math.max(0, (int) Math.round(p.minY()));
			final int maxy = Math.min((int) Math.round(p.maxY()), targetImage.height - 1);

			final Pixel tmp = new Pixel();
			for (tmp.y = miny; tmp.y <= maxy; tmp.y++) {
				for (tmp.x = minx; tmp.x <= maxx; tmp.x++) {
					if (p.isInside(tmp))
						this.targetImage.pixels[tmp.y][tmp.x] = col;
				}
			}
		}
	}
}
