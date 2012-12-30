package org.openimaj.image.processing.extraction;

import java.io.IOException;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestOrientedPolygonExtractionProcessor {
	/**
	 * Not a good test yet, just trys to run the processor, no attempt is made to check the processor
	 * @throws IOException
	 */
	@Test
	public void test90Degree() throws IOException{
		FImage img = ImageUtilities.readF(TestOrientedPolygonExtractionProcessor.class.getResourceAsStream("/org/openimaj/image/data/bird.png"));
		Rectangle r = new Rectangle(320,100,60,170);
		Polygon p = r.asPolygon();
		Polygon prot = p.clone();
		Point2d center = Point2dImpl.fromDoubleArray(prot.calculateCentroid());
		prot.rotate(center,Math.PI/3);
		
		OrientedPolygonExtractionProcessor opep = new OrientedPolygonExtractionProcessor(prot, 0.f);
		img.process(opep);
	}
}
