package org.openimaj.workinprogress.accel;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.math.geometry.shape.Rectangle;

public class TestConnected {
	public static void main(String[] args) {
		final FImage image = new FImage(100, 100);

		image.drawShape(new Rectangle(50, 50, 80, 10), 1f);

		DisplayUtilities.display(image);

		final GreyscaleConnectedComponentLabeler ccl = new GreyscaleConnectedComponentLabeler();
		for (final ConnectedComponent cc : ccl.findComponents(image)) {
			image.drawPoints(cc, (float) Math.random(), 1);
		}
		DisplayUtilities.display(image);
	}
}
