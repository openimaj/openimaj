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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.SVGImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * {@link ImageRenderer} for {@link FImage} images. Supports both anti-aliased
 * and fast rendering.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class SVGRenderer extends ImageRenderer<Float[], SVGImage> {

	private SVGGraphics2D svgGen;

	/**
	 * Construct with given target image.
	 *
	 * @param targetImage
	 *            the target image.
	 */
	public SVGRenderer(final SVGImage targetImage) {
		super(targetImage);
		prepareSVG();
	}

	/**
	 * Construct with given target image and rendering hints.
	 *
	 * @param targetImage
	 *            the target image.
	 * @param hints
	 *            the render hints
	 */
	public SVGRenderer(final SVGImage targetImage, final SVGRenderHints hints) {
		super(targetImage, hints);
		prepareSVG();
	}

	/**
	 * Construct with given target image and rendering hints.
	 *
	 * @param targetImage
	 *            the target image.
	 * @param hints
	 *            the render hints
	 */
	public SVGRenderer(final SVGRenderHints hints) {
		super(null, hints);
		prepareSVG();
	}

	/**
	 * @param create
	 */
	public SVGRenderer(SVGImage img, Graphics create) {
		super(img);
		this.svgGen = (SVGGraphics2D) create;
	}

	public SVGRenderer(SVGImage img, RenderHints renderHints, Graphics create) {
		super(img, renderHints);
		this.svgGen = (SVGGraphics2D) create;
	}

	private void prepareSVG() {
		final DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
		final String svgNS = "http://www.w3.org/2000/svg";
		final Document doc = impl.createDocument(svgNS, "svg", null);
		// Create an instance of the SVG Generator
		final SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(doc);

		// Reuse our embedded base64-encoded image data.
		// GenericImageHandler ihandler = new CachedImageHandlerBase64Encoder();
		// ctx.setGenericImageHandler(ihandler);
		this.svgGen = new SVGGraphics2D(ctx, false);

		if (this.targetImage != null) {
			final int w = targetImage.getWidth();
			final int h = targetImage.getHeight();
			this.svgGen.setSVGCanvasSize(new Dimension(w, h));
		} else if (this.hints != null && hints instanceof SVGRenderHints) {
			final int w = ((SVGRenderHints) hints).width;
			final int h = ((SVGRenderHints) hints).height;
			this.svgGen.setSVGCanvasSize(new Dimension(w, h));
		}

	}

	@Override
	public void drawLine(int x1, int y1, double theta, int length, int thickness, Float[] col) {
		final int x2 = x1 + (int) Math.round(Math.cos(theta) * length);
		final int y2 = y1 + (int) Math.round(Math.sin(theta) * length);

		this.drawLine(x1, y1, x2, y2, thickness, col);
	}

	@Override
	public void drawLine(int x0, int y0, int x1, int y1, int thickness, Float[] col) {
		if (thickness <= 1) {
			this.svgGen.setColor(colorFromFloats(col));
			this.svgGen.drawLine(x0, y0, x1, y1);
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

			this.drawPolygonFilled(p, col);
		}
	}

	private Color colorFromFloats(Float[] col) {
		final Color ret = new Color(col[0], col[1], col[2]);
		return ret;
	}

	@Override
	public void drawLine(float x0, float y0, float x1, float y1, int thickness, Float[] col) {
		this.drawLine((int) x0, (int) y0, (int) x1, (int) y1, thickness, col);
	}

	@Override
	public void drawPoint(Point2d p, Float[] col, int size) {
		this.svgGen.setColor(colorFromFloats(col));
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(p.getX(), p.getY());
		path.lineTo(p.getX(), p.getY());
		this.svgGen.draw(path);
	}

	@Override
	public void drawPolygon(Polygon p, int thickness, Float[] col) {
		this.svgGen.setColor(colorFromFloats(col));
		final List<Area> a = jPolyFromPolygon(p);
		for (final Area polygon : a) {
			this.svgGen.draw(polygon);
		}

	}

	@Override
	public void drawPolygonFilled(Polygon p, Float[] col) {
		this.svgGen.setColor(colorFromFloats(col));
		final List<Area> a = jPolyFromPolygon(p);
		for (final Area polygon : a) {
			this.svgGen.fill(polygon);
		}
	}

	@Override
	public void drawShapeFilled(Shape s, Float[] col) {
		super.drawPolygonFilled(s.asPolygon(), col);
	}

	private List<java.awt.geom.Area> jPolyFromPolygon(Polygon p) {
		final List<java.awt.geom.Area> ret = new ArrayList<java.awt.geom.Area>();
		final Path2D path = new Path2D.Double();
		boolean start = true;
		for (final Point2d p2d : p.getVertices()) {
			if (start) {
				path.moveTo(p2d.getX(), p2d.getY());
				start = false;
			}
			else {
				path.lineTo(p2d.getX(), p2d.getY());
			}

		}
		// final java.awt.geom.Area pp = new java.awt.geom.Area();
		ret.add(new java.awt.geom.Area(path));
		return ret;
	}

	@Override
	protected void drawHorizLine(int x1, int x2, int y, Float[] col) {
		this.drawLine(x1, y, x2, y, 1, col);
	}

	@Override
	public Float[] defaultForegroundColour() {
		return RGBColour.BLACK;
	}

	@Override
	public Float[] defaultBackgroundColour() {
		return RGBColour.WHITE;
	}

	@Override
	protected Float[] sanitise(Float[] colour) {
		return colour;
	}

	/**
	 * @param out
	 * @throws SVGGraphics2DIOException
	 */
	public void write(Writer out) throws SVGGraphics2DIOException {
		this.svgGen.stream(out, true);
	}

	public void drawOIImage(Image<?, ?> im) {
		final BufferedImage createBufferedImage = ImageUtilities.createBufferedImage(im);
		this.svgGen.drawImage(createBufferedImage, 0, 0, this.colorFromFloats(this.defaultBackgroundColour()),
				new ImageObserver() {

					@Override
					public boolean imageUpdate(java.awt.Image img, int infoflags, int x, int y, int width, int height) {
						return true;
					}
				});
	}

	@Override
	public SVGRenderer clone() {
		final SVGRenderer ret = new SVGRenderer((SVGRenderHints) this.getRenderHints());
		return ret;
	}

	@Override
	public void drawImage(SVGImage image, int x, int y) {
		// TODO: fix this...
		// throw new UnsupportedOperationException();
		// Element root = this.svgGen.getRoot();
		// System.out.println(root);
		// Node node =
		// this.svgGen.getDOMFactory().importNode(image.createRenderer().svgGen.getRoot(),
		// true);
		// image.createRenderer().svgGen.getRoot((Element) node);
		// root.appendChild(node);
	}

	public SVGGraphics2D getGraphics2D() {
		return this.svgGen;
	}

	public Document getDocument() {
		final Element root = svgGen.getRoot();
		//
		// Enforce that the default and xlink namespace
		// declarations appear on the root element
		//

		// final Document doc = null;
		try {
			//
			// Enforce that the default and xlink namespace
			// declarations appear on the root element
			//
			root.setAttributeNS(SVGGraphics2D.XMLNS_NAMESPACE_URI,
					SVGGraphics2D.XMLNS_PREFIX,
					SVGGraphics2D.SVG_NAMESPACE_URI);

			root.setAttributeNS(SVGGraphics2D.XMLNS_NAMESPACE_URI,
					SVGGraphics2D.XMLNS_PREFIX + ":" + SVGGraphics2D.XLINK_PREFIX,
					SVGGraphics2D.XLINK_NAMESPACE_URI);
			// DOMImplementation impl =
			// GenericDOMImplementation.getDOMImplementation();
			// String svgNS = "http://www.w3.org/2000/svg";
			// doc = impl.createDocument(svgNS, "svg", null);
			// doc.appendChild(root);
		} catch (final Exception e) {

		}
		return root.getOwnerDocument();

	}
}
