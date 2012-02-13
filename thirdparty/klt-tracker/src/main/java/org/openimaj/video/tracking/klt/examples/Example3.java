package org.openimaj.video.tracking.klt.examples;

import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.FeatureTable;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

/**
 * KLTTracker Example 1
 */
public class Example3 {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String [] args) throws IOException {
		int nFeatures = 150, nFrames = 10;
		boolean replace = false;
		int i;

		TrackingContext tc = new TrackingContext();
		FeatureList fl = new FeatureList(nFeatures);
		FeatureTable ft = new FeatureTable(nFeatures);
		KLTTracker tracker = new KLTTracker(tc, fl);

		tc.setSequentialMode(true);
		tc.setWriteInternalImages(false);
		tc.setAffineConsistencyCheck(-1);  /* set this to 2 to turn on affine consistency check */

		FImage img1 = ImageUtilities.readF(Example1.class.getResourceAsStream("img0.pgm"));

		tracker.selectGoodFeatures(img1);
		ft.storeFeatureList(fl, 0);

		DisplayUtilities.display(fl.drawFeatures(img1));

		for (i = 1 ; i < nFrames ; i++)  {
			String fnamein = String.format("img%d.pgm", i);
			FImage img2 = ImageUtilities.readF(Example1.class.getResourceAsStream(fnamein));
			tracker.trackFeatures(img1, img2);

			if (replace)
				tracker.replaceLostFeatures(img2);

			ft.storeFeatureList(fl, i);
			DisplayUtilities.display(fl.drawFeatures(img2));
		}
		ft.writeFeatureTable(null, "%5.1f");
	}
}
