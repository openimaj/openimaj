package org.openimaj.demos.sandbox.image.text;

import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.shape.Rectangle;

public class LetterCandidate {
	ConnectedComponent cc;
	float averageBrightness;
	Pixel centroid;
	float medianStrokeWidth;
	Rectangle regularBoundingBox;

	public LetterCandidate(ConnectedComponent cc, float medianStrokeWidth, FImage image) {
		this.cc = cc;
		this.medianStrokeWidth = medianStrokeWidth;

		regularBoundingBox = cc.calculateRegularBoundingBox();

		centroid = cc.calculateCentroidPixel();

		final DescriptiveStatistics ds = new DescriptiveStatistics(cc.pixels.size());
		for (final Pixel p : cc.pixels) {
			ds.addValue(image.pixels[p.y][p.x]);
		}
		averageBrightness = (float) ds.getMean();
	}

	public static Rectangle computeBounds(List<LetterCandidate> letters) {
		float minx = Float.MAX_VALUE;
		float miny = Float.MAX_VALUE;
		float maxx = 0;
		float maxy = 0;

		for (final LetterCandidate letter : letters) {
			final Rectangle r = letter.cc.calculateRegularBoundingBox();

			if (r.x < minx)
				minx = r.x;
			if (r.y < miny)
				miny = r.y;
			if (r.x + r.width > maxx)
				maxx = r.x + r.width;
			if (r.y + r.height > maxy)
				maxy = r.y + r.height;
		}

		return new Rectangle(minx, miny, maxx - minx, maxy - miny);
	}

	@Override
	public String toString() {
		return regularBoundingBox.toString();
	}
}
