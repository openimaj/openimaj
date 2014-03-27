package org.openimaj.image.renderer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
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
	
	class BufferedImageTranscoder extends ImageTranscoder{

		private BufferedImage img;

		@Override
		public BufferedImage createImage(int w, int h) {
			BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			return bi;
		}

		@Override
		public void writeImage(BufferedImage img, TranscoderOutput arg1)
				throws TranscoderException {
			this.img = img;
		}
		
		public BufferedImage getBufferedImage(){
			return this.img;
		}
		
	}
	/**
	 * @throws Exception
	 */
	@Test
	public void testLineRender() throws Exception {
		int width = 100;
		int height = 100;
		SVGRenderer renderer = new SVGRenderer(null,new SVGRenderHints(width, height));
		renderer.drawLine(0, 0, 10, 10, 1, RGBColour.RED);
		renderer.drawShape(new Circle(50, 50, 50), RGBColour.GREEN);
		renderer.drawShapeFilled(new Circle(50, 50, 20), RGBColour.GREEN);
		
//		SVGGraphics2D svgG = renderer.getGraphics2D();
//		svgG.
		
		File f = folder.newFile("out.svg");
		Writer w = new OutputStreamWriter(new FileOutputStream(f));
		renderer.write(w);
//		Writer w = new OutputStreamWriter(System.out);
//		
		w.flush();
		w.close();
//		
		BufferedImageTranscoder t = new BufferedImageTranscoder();
		t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float)width);
		t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float)height);
		TranscoderInput input = new TranscoderInput(new FileInputStream(f));
		t.transcode(input, null);
//		DisplayUtilities.display(t.getBufferedImage());
	}
	
	@Test
	public void testRenderMBF() throws Exception {
		int width = 100;
		int height = 100;
		SVGRenderer renderer = new SVGRenderer(null,new SVGRenderHints(width, height));
		MBFImage out = new MBFImage(width,height,ColourSpace.RGB);
		out.drawLine(0, 0, 10, 10, 1, RGBColour.RED);
		out.drawShape(new Circle(50, 50, 50), RGBColour.GREEN);
		out.drawShapeFilled(new Circle(50, 50, 20), RGBColour.GREEN);
		
		renderer.drawOIImage(out);
//		SVGGraphics2D svgG = renderer.getGraphics2D();
//		svgG.
		
		File f = folder.newFile("out.svg");
		Writer w = new OutputStreamWriter(new FileOutputStream(f));
		renderer.write(w);
//		Writer w = new OutputStreamWriter(System.out);
//		
		w.flush();
		w.close();
//		
		BufferedImageTranscoder t = new BufferedImageTranscoder();
		t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float)width);
		t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float)height);
		TranscoderInput input = new TranscoderInput(new FileInputStream(f));
		t.transcode(input, null);
//		DisplayUtilities.display(t.getBufferedImage());
	}
	
	@Test
	public void testRenderSVG() throws Exception {
		int width = 100;
		int height = 100;
		SVGImage renderer = new SVGImage(new SVGRenderHints(width, height));
		renderer.drawLine(0, 0, 10, 10, 1, RGBColour.RED);
		renderer.drawShape(new Circle(50, 50, 50), RGBColour.GREEN);
		renderer.drawShapeFilled(new Circle(50, 50, 20), RGBColour.GREEN);
//		SVGGraphics2D svgG = renderer.getGraphics2D();
//		svgG.
		SVGRenderer renderer2 = new SVGRenderer(null,new SVGRenderHints(width, height));
		renderer2.drawImage(renderer, 0, 0);
//		File f = folder.newFile("out.svg");
//		Writer w = new OutputStreamWriter(new FileOutputStream(f));
		Writer w = new OutputStreamWriter(System.out);
		renderer2.write(w);
//		
		w.flush();
		w.close();
//		
//		BufferedImageTranscoder t = new BufferedImageTranscoder();
//		t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float)width);
//		t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float)height);
//		TranscoderInput input = new TranscoderInput(new FileInputStream(f));
//		t.transcode(input, null);
//		DisplayUtilities.display(t.getBufferedImage());
	}
	
	
	public static void main(String[] args) throws Exception {
		new TestSVGRenderer().testRenderSVG();
	}
}
