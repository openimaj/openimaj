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
 * KLTTracker Example 1
 */
public class Example1 {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String [] args) throws IOException {
		  FImage img1, img2;
		  
		  int nFeatures = 100;
		  TrackingContext tc = new TrackingContext();
		  FeatureList fl = new FeatureList(nFeatures);
		  KLTTracker tracker = new KLTTracker(tc, fl);
		  
		  System.out.println(tc);
		  
		  img1 = ImageUtilities.readF(Example1.class.getResourceAsStream("img0.pgm"));
		  img2 = ImageUtilities.readF(Example1.class.getResourceAsStream("img1.pgm"));

		  tracker.selectGoodFeatures(img1);

		  System.out.println("\nIn first image:\n");
		  for (int i = 0 ; i < fl.features.length ; i++)  {
			  System.out.format("Feature #%d:  (%f,%f) with value of %d\n", i, fl.features[i].x, fl.features[i].y, fl.features[i].val);
		  }

		  DisplayUtilities.display(fl.drawFeatures(img1));
		  fl.writeFeatureList(null, "%3d");

		  tracker.trackFeatures(img1, img2);

		  System.out.println("\nIn second image:\n");
		  for (int i = 0; i < fl.features.length; i++)  {
			  System.out.format("Feature #%d:  (%f,%f) with value of %d\n", i, fl.features[i].x, fl.features[i].y, fl.features[i].val);
		  }

		  DisplayUtilities.display(fl.drawFeatures(img2));
		  fl.writeFeatureList(null, "%5.1f");
	}
}
