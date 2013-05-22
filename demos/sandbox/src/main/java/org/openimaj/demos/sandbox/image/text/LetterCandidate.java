package org.openimaj.demos.sandbox.image.text;

import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.math.geometry.point.Point2d;

public class LetterCandidate {
	ConnectedComponent cc;
	float[] averageColour;
	Point2d centroid;
	float medianStrokeWidth;
	float width;
	float height;
}
