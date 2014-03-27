package org.openimaj.image.renderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
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
	private void before() throws IOException {
		folder.create();
	}
	/**
	 * @throws Exception
	 */
	@Test
	public void testLineRender() throws Exception {
		SVGRenderer renderer = new SVGRenderer(null,new SVGRenderHints(100, 100));
		renderer.drawLine(0, 0, 10, 10, 1, RGBColour.RED);
		renderer.drawShape(new Circle(50, 50, 50), RGBColour.GREEN);
		renderer.drawShapeFilled(new Circle(50, 50, 20), RGBColour.GREEN);
		
		File f = folder.newFile("out.svg");
		Writer w = new OutputStreamWriter(System.out);
		renderer.write(w);
//		w.flush();
//		w.close();
//		
//		JPEGTranscoder t = new JPEGTranscoder();
//		t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
//                new Float(.8));
//		TranscoderInput input = new TranscoderInput(new FileInputStream(f));
//		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		t.transcode(input, new TranscoderOutput(baos));
//		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//		DisplayUtilities.display(ImageUtilities.readMBF(bais));
	}
	
	public static void main(String[] args) throws Exception {
		new TestSVGRenderer().testLineRender();
	}
}
