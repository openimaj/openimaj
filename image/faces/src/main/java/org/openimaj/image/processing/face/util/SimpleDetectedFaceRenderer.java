package org.openimaj.image.processing.face.util;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;

/**
 * Draws the bounding box detected
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SimpleDetectedFaceRenderer implements DetectedFaceRenderer<DetectedFace>{
	private Float[] boundingBoxColour = RGBColour.RED;

	@Override
	public void drawDetectedFace(MBFImage image,int thickness,DetectedFace f) {
		image.drawShape(f.getShape(), thickness,boundingBoxColour);
	}

}
