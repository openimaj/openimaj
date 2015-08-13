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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.image.MBFImage;
import org.openimaj.image.SVGImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Circle;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestSVGRenderer {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void before() throws IOException {
		folder.create();
	}

	class BufferedImageTranscoder extends ImageTranscoder {

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

	/**
	 * @throws Exception
	 */
	@Test
	public void testLineRender() throws Exception {
		final int width = 100;
		final int height = 100;
		final SVGRenderer renderer = new SVGRenderer(new SVGImage(new SVGRenderHints(width, height)));
		renderer.drawLine(0, 0, 10, 10, 1, RGBColour.RED);
		renderer.drawShape(new Circle(50, 50, 50), RGBColour.GREEN);
		renderer.drawShapeFilled(new Circle(50, 50, 20), RGBColour.GREEN);

		// SVGGraphics2D svgG = renderer.getGraphics2D();
		// svgG.

		final File f = folder.newFile("out.svg");
		final Writer w = new OutputStreamWriter(new FileOutputStream(f));
		renderer.write(w);
		// Writer w = new OutputStreamWriter(System.out);
		//
		w.flush();
		w.close();
		//
		final BufferedImageTranscoder t = new BufferedImageTranscoder();
		t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
		t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
		final TranscoderInput input = new TranscoderInput(new FileInputStream(f));
		t.transcode(input, null);
		// DisplayUtilities.display(t.getBufferedImage());
	}

	@Test
	public void testRenderMBF() throws Exception {
		final int width = 100;
		final int height = 100;
		final SVGRenderer renderer = new SVGRenderer(null, new SVGRenderHints(width, height));
		final MBFImage out = new MBFImage(width, height, ColourSpace.RGB);
		out.drawLine(0, 0, 10, 10, 1, RGBColour.RED);
		out.drawShape(new Circle(50, 50, 50), RGBColour.GREEN);
		out.drawShapeFilled(new Circle(50, 50, 20), RGBColour.GREEN);

		renderer.drawOIImage(out);
		// SVGGraphics2D svgG = renderer.getGraphics2D();
		// svgG.

		final File f = folder.newFile("out.svg");
		final Writer w = new OutputStreamWriter(new FileOutputStream(f));
		renderer.write(w);
		// Writer w = new OutputStreamWriter(System.out);
		//
		w.flush();
		w.close();
		//
		final BufferedImageTranscoder t = new BufferedImageTranscoder();
		t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
		t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
		final TranscoderInput input = new TranscoderInput(new FileInputStream(f));
		t.transcode(input, null);
		// DisplayUtilities.display(t.getBufferedImage());
	}

	@Test
	public void testRenderSVG() throws Exception {
		final int width = 100;
		final int height = 100;
		final SVGImage renderer = new SVGImage(new SVGRenderHints(width, height));
		renderer.drawLine(0, 0, 10, 10, 1, RGBColour.RED);
		renderer.drawShape(new Circle(50, 50, 50), RGBColour.GREEN);
		renderer.drawShapeFilled(new Circle(50, 50, 20), RGBColour.GREEN);
		// SVGGraphics2D svgG = renderer.getGraphics2D();
		// svgG.
		final SVGRenderer renderer2 = new SVGRenderer(new SVGImage(new SVGRenderHints(width, height)));
		renderer2.drawImage(renderer, 0, 0);
		// File f = folder.newFile("out.svg");
		// Writer w = new OutputStreamWriter(new FileOutputStream(f));
		final Writer w = new OutputStreamWriter(System.out);
		renderer2.write(w);
		//
		w.flush();
		w.close();
		//
		// BufferedImageTranscoder t = new BufferedImageTranscoder();
		// t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float)width);
		// t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float)height);
		// TranscoderInput input = new TranscoderInput(new FileInputStream(f));
		// t.transcode(input, null);
		// DisplayUtilities.display(t.getBufferedImage());
	}

	public static void main(String[] args) throws Exception {
		new TestSVGRenderer().testRenderSVG();
	}
}
