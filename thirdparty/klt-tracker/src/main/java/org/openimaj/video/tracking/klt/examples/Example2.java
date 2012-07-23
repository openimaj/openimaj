/**
 * This source code file is part of a direct port of Stan Birchfield's implementation
 * of a Kanade-Lucas-Tomasi feature tracker. The original implementation can be found
 * here: http://www.ces.clemson.edu/~stb/klt/
 *
 * As per the original code, the source code is in the public domain, available
 * for both commercial and non-commercial use.
 */
package org.openimaj.video.tracking.klt.examples;

import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

/**
 * KLTTracker Example 2
 */
public class Example2 {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String [] args) throws IOException {
		int nFeatures = 100;

		TrackingContext tc = new TrackingContext();
		FeatureList fl = new FeatureList(nFeatures);
		KLTTracker tracker = new KLTTracker(tc, fl);

		FImage img1 = ImageUtilities.readF(Example1.class.getResourceAsStream("img0.pgm"));
		FImage img2 = ImageUtilities.readF(Example1.class.getResourceAsStream("img1.pgm"));

		tracker.selectGoodFeatures(img1);

		DisplayUtilities.display(fl.drawFeatures(img1));
		fl.writeFeatureList(null, "%3d");

		tracker.trackFeatures(img1, img2);
		tracker.replaceLostFeatures(img2);

		DisplayUtilities.display(fl.drawFeatures(img1));
		fl.writeFeatureList(null, "%3d");
	}
}
