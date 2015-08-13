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

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;

import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.image.renderer.SVGRenderHints;
import org.openimaj.image.renderer.SVGRenderer;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;

public class SVGImage extends Image<Float[], SVGImage> {
	private SVGRenderer renderer;

	/**
	 * @param hints
	 */
	public SVGImage(SVGRenderHints hints) {
		this.renderer = new SVGRenderer(null, hints);
		this.renderer.setImage(this);
	}

	private SVGImage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Construct an empty SVG-backed image of the given size
	 *
	 * @param w
	 *            the width
	 * @param h
	 *            the height
	 */
	public SVGImage(int w, int h) {
		this(new SVGRenderHints(w, h));
	}

	@Override
	public SVGImage abs() {
		return this;
	}

	@Override
	public SVGImage addInplace(Image<?, ?> im) {
		if (!(im instanceof SVGImage)) {
			this.renderer.drawOIImage(im);
		} else {
			this.renderer.drawImage((SVGImage) im, 0, 0);
		}
		return null;
	}

	@Override
	public SVGImage addInplace(Float[] num) {
		return this;
	}

	@Override
	public SVGImage clip(Float[] min, Float[] max) {
		return this;
	}

	@Override
	public SVGImage clipMax(Float[] thresh) {
		return this;
	}

	@Override
	public SVGImage clipMin(Float[] thresh) {
		return this;
	}

	@Override
	public SVGImage clone() {
		final SVGImage svgImage = new SVGImage();
		svgImage.renderer = new SVGRenderer(svgImage, this.renderer.getGraphics2D().create());
		return svgImage;
	}

	@Override
	public SVGRenderer createRenderer() {
		return this.renderer;
	}

	@Override
	public ImageRenderer<Float[], SVGImage> createRenderer(RenderHints options) {
		return this.renderer;
	}

	@Override
	public SVGImage divideInplace(Image<?, ?> im) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage divideInplace(Float[] val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage extractROI(int x, int y, SVGImage img) {
		img.renderer = new SVGRenderer(img, img.renderer.getRenderHints(), this.renderer.getGraphics2D().create(x, y,
				img.getWidth(), img.getHeight()));
		return img;
	}

	@Override
	public SVGImage extractROI(int x, int y, int w, int h) {
		final SVGImage ret = new SVGImage(w, h);
		return extractROI(x, y, ret);
	}

	@Override
	public SVGImage extractCentreSubPix(float cx, float cy, SVGImage out) {
		return extractCenter((int) cx, (int) cy, out.getWidth(), out.getHeight());
	}

	@Override
	public SVGImage fill(Float[] colour) {
		final SVGRenderHints hint = (SVGRenderHints) this.renderer.getRenderHints();
		this.renderer = new SVGRenderer(hint);
		this.renderer.drawShapeFilled(this.getBounds(), colour);
		return this;
	}

	@Override
	public SVGImage flipX() {
		final AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(this.getWidth(), 0);
		this.renderer.getGraphics2D().transform(tx);
		return this;
	}

	@Override
	public SVGImage flipY() {
		final AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -this.getHeight());
		this.renderer.getGraphics2D().transform(tx);
		return this;
	}

	@Override
	public Rectangle getContentArea() {
		return new Rectangle(0, 0, getWidth(), getHeight());
	}

	@Override
	public SVGImage getField(org.openimaj.image.Image.Field f) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage getFieldCopy(org.openimaj.image.Image.Field f) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage getFieldInterpolate(org.openimaj.image.Image.Field f) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getHeight() {
		return this.renderer.getGraphics2D().getSVGCanvasSize().height;
	}

	@Override
	public Float[] getPixel(int x, int y) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Comparator<? super Float[]> getPixelComparator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Float[] getPixelInterp(double x, double y) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Float[] getPixelInterp(double x, double y, Float[] backgroundColour) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getWidth() {
		return this.renderer.getGraphics2D().getSVGCanvasSize().width;
	}

	@Override
	public SVGImage internalCopy(SVGImage im) {
		this.renderer = im.renderer.clone();
		this.renderer.setImage(this);
		return this;
	}

	@Override
	public SVGImage internalAssign(SVGImage im) {
		this.renderer = im.renderer;
		return this;
	}

	@Override
	public SVGImage internalAssign(int[] pixelData, int width, int height) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage inverse() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Float[] max() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Float[] min() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage multiplyInplace(Image<?, ?> im) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage multiplyInplace(Float[] num) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage newInstance(int width, int height) {
		return new SVGImage(width, height);
	}

	@Override
	public SVGImage normalise() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPixel(int x, int y, Float[] val) {
		this.renderer.drawPoint(new Point2dImpl(x, y), val, 1);
	}

	@Override
	public SVGImage subtractInplace(Image<?, ?> im) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage subtractInplace(Float[] num) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage threshold(Float[] thresh) {
		throw new UnsupportedOperationException();
	}

	private static class BufferedImageTranscoder extends ImageTranscoder {

		private BufferedImage img;

		@Override
		public BufferedImage createImage(int w, int h) {
			final BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			return bi;
		}

		@Override
		public void writeImage(BufferedImage img, TranscoderOutput arg1)
				throws TranscoderException
		{
			this.img = img;
		}

		public BufferedImage getBufferedImage() {
			return this.img;
		}

	}

	@Override
	public byte[] toByteImage() {
		final MBFImage mbf = createMBFImage();
		return mbf.toByteImage();
	}

	public MBFImage createMBFImage() {
		final BufferedImageTranscoder t = new BufferedImageTranscoder();
		t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) getWidth());
		t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) getHeight());
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			this.renderer.write(new OutputStreamWriter(baos));
			baos.flush();
			baos.close();
			final byte[] barr = baos.toByteArray();
			final TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(barr));
			t.transcode(input, null);
		} catch (final SVGGraphics2DIOException e) {
		} catch (final IOException e) {
		} catch (final TranscoderException e) {
		}
		final MBFImage mbf = ImageUtilities.createMBFImage(t.getBufferedImage(), true);
		return mbf;
	}

	@Override
	public int[] toPackedARGBPixels() {
		final MBFImage mbf = createMBFImage();
		return mbf.toPackedARGBPixels();
	}

	@Override
	public SVGImage zero() {
		final SVGRenderHints hint = (SVGRenderHints) this.renderer.getRenderHints();
		this.renderer = new SVGRenderer(hint);
		this.renderer.drawShapeFilled(this.getBounds(), RGBColour.BLACK);
		return this;
	}

	@Override
	public SVGImage overlayInplace(SVGImage image, int x, int y) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SVGImage replace(Float[] target, Float[] replacement) {
		throw new UnsupportedOperationException();
	}
}
