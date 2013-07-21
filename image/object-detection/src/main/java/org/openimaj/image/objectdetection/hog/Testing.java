package org.openimaj.image.objectdetection.hog;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;

public class Testing {
	public static void main(String[] args) throws IOException {
		final HOGClassifier classifier = IOUtils.readFromFile(new File("final-classifier.dat"));

		final HOGDetector detector = new HOGDetector(classifier);
		final FImage img = ImageUtilities.readF(new File("/Users/jsh2/Data/INRIAPerson/Test/pos/crop_000006.png"));
		final List<Rectangle> dets = detector.detect(img);

		final MBFImage rgb = img.toRGB();
		for (final Rectangle r : dets)
			rgb.drawShape(r, RGBColour.RED);
		DisplayUtilities.display(rgb);

		// classifier.prepare(img);
		//
		// for (int k = 0; k < 1000; k++) {
		// final Timer t1 = Timer.timer();
		// int width = 64;
		// int height = 128;
		// int step = 8;
		// for (int level = 0; level < 10; level++) {
		// final MBFImage rgb = img.toRGB();
		// final Timer t2 = Timer.timer();
		// int nwindows = 0;
		// for (int y = 0; y < img.height - height; y += step) {
		// for (int x = 0; x < img.width - width; x += step) {
		// final Rectangle rectangle = new Rectangle(x, y, width, height);
		// nwindows++;
		// if (classifier.classify(rectangle)) {
		// rgb.drawShape(new Rectangle(x, y, width, height), RGBColour.RED);
		// }
		// }
		// }
		// System.out.format("Image %d x %d (%d windows) took %2.2fs\n",
		// img.width, img.height, nwindows,
		// t2.duration() / 1000.0);
		// DisplayUtilities.displayName(rgb, "name " + level);
		//
		// width = (int) Math.floor(width * 1.2);
		// height = (int) Math.floor(height * 1.2);
		// step = (int) Math.floor(step * 1.2);
		// }
		// System.out.format("Total time: %2.2fs\n", t1.duration() / 1000.0);
		// }
	}
}
