package org.openimaj.image.processing.face.util;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KEDetectedFaceRenderer implements DetectedFaceRenderer<KEDetectedFace>{

	private Float[] boundingBoxColour = RGBColour.RED;
	private Float[] pointColour = RGBColour.BLUE;

	@Override
	public void drawDetectedFace(MBFImage image, int thickness, KEDetectedFace f) {
		Rectangle bounds = f.getBounds();
		image.drawShape(bounds,thickness, boundingBoxColour);
		FacialKeypoint[] kp = f.getKeypoints();
		for (FacialKeypoint facialKeypoint : kp) {
			Point2dImpl position = facialKeypoint.position.clone();
			position.translate(bounds.x, bounds.y);
			image.drawPoint(position,pointColour,thickness);
		}
	}

}
