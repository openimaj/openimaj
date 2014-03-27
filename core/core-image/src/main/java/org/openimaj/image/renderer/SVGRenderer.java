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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * {@link ImageRenderer} for {@link FImage} images. Supports both anti-aliased
 * and fast rendering.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class SVGRenderer extends ImageRenderer<Float[], MBFImage> {

	private SVGGraphics2D svgGen;

	/**
	 * Construct with given target image.
	 * 
	 * @param targetImage
	 *            the target image.
	 */
	public SVGRenderer(final MBFImage targetImage) {
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
	public SVGRenderer(final MBFImage targetImage, final SVGRenderHints hints) {
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

	private void prepareSVG() {
		DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document myFactory = impl.createDocument(svgNS, "svg", null);
		this.svgGen = new SVGGraphics2D(myFactory);
		
		if(this.targetImage != null){
			int w = targetImage.getWidth();
			int h = targetImage.getHeight();
			this.svgGen.setSVGCanvasSize(new Dimension(w, h));
		} else if(this.hints!=null && hints instanceof SVGRenderHints){
			int w = ((SVGRenderHints)hints).width;
			int h = ((SVGRenderHints)hints).height;
			this.svgGen.setSVGCanvasSize(new Dimension(w, h));
		}
		
	}

	@Override
	public void drawLine(int x1, int y1, double theta, int length,int thickness, Float[] col) {
		final int x2 = x1 + (int) Math.round(Math.cos(theta) * length);
		final int y2 = y1 + (int) Math.round(Math.sin(theta) * length);

		this.drawLine(x1, y1, x2, y2, thickness, col);
	}

	@Override
	public void drawLine(int x0, int y0, int x1, int y1, int thickness,Float[] col) {
		if(thickness <= 1){
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
		Color ret = new Color(col[0], col[1], col[2]);
		return ret;
	}

	@Override
	public void drawLine(float x0, float y0, float x1, float y1, int thickness,Float[] col) {
		this.drawLine((int)x0, (int)y0, (int)x1, (int)y1, thickness, col);
	}

	@Override
	public void drawPoint(Point2d p, Float[] col, int size) {
		this.svgGen.setColor(colorFromFloats(col));
		this.svgGen.drawOval((int)p.getX(), (int)p.getY(), size, size);
	}

	@Override
	public void drawPolygon(Polygon p, int thickness, Float[] col) {
		this.svgGen.setColor(colorFromFloats(col));
		List<java.awt.Polygon> a = jPolyFromPolygon(p);
		for (java.awt.Polygon polygon : a) {
			this.svgGen.drawPolygon(polygon);
		}
		
	}
	
	@Override
	public void drawPolygonFilled(Polygon p, Float[] col) {
		this.svgGen.setColor(colorFromFloats(col));
		List<java.awt.Polygon> a = jPolyFromPolygon(p);
		for (java.awt.Polygon polygon : a) {
			this.svgGen.fillPolygon(polygon);
		}
	}
	
	@Override
	public void drawShapeFilled(Shape s, Float[] col) {
		super.drawPolygonFilled(s.asPolygon(), col);
	}

	private List<java.awt.Polygon> jPolyFromPolygon(Polygon p) {
		List<java.awt.Polygon> ret = new ArrayList<java.awt.Polygon>();
		
		int[] xpoints = new int[p.nVertices()];
		int[] ypoints = new int[p.nVertices()];
		int npoints = p.nVertices();
		int i = 0;
		for (Point2d p2d : p.getVertices()) {
			xpoints[i] = (int) p2d.getX();
			ypoints[i] = (int) p2d.getY();
			i++;
		}
		java.awt.Polygon pp = new java.awt.Polygon(xpoints, ypoints, npoints);
		ret.add(pp );
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
	public void write(Writer out) throws SVGGraphics2DIOException{
		this.svgGen.stream(out, true);
	}
}
