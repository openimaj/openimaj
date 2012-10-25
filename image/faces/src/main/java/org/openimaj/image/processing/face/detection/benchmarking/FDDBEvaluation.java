package org.openimaj.image.processing.face.detection.benchmarking;

import gnu.trove.set.hash.TDoubleHashSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.EllipticalDetectedFace;
import org.openimaj.image.processing.face.detection.benchmarking.Matcher.Match;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;

public class FDDBEvaluation {
	public interface Detector {
		List<? extends DetectedFace> getDetections(FDDBRecord record);
	}

	public List<Results> performEvaluation(ListDataset<FDDBRecord> dataset, Detector detector) {
		// cumRes stores the cumulative results for all the images
		List<Results> cumRes = new ArrayList<Results>();
		final Matcher matcher = new Matcher();

		// Process each image
		final int numImages = dataset.size();
		for (int i = 0; i < numImages; i++) {
			final FDDBRecord data = dataset.getInstance(i);
			final String imName = data.getImageName();

			final List<? extends DetectedFace> annot = data.getGroundTruth();
			final List<? extends DetectedFace> det = detector.getDetections(data);

			// imageResults holds the results for different thresholds
			// applied to a single image
			final List<Results> imageResults = new ArrayList<Results>();

			if (det.size() == 0) {
				// create the image results for zero detections
				final Results r = new Results(imName, Double.MAX_VALUE, null, annot, det);
				imageResults.add(r);
			} else {
				// find the unique values for detection scores
				final double[] uniqueScores = getUniqueConfidences(det);

				// For each unique score value st,
				// (a) filter the detections with score >= st
				// (b) compute the matching annot-det pairs
				// (c) compute the result statistics
				for (final double scoreThreshold : uniqueScores) {
					final ArrayList<DetectedFace> filteredDet = new ArrayList<DetectedFace>();
					// (a) filter the detections with score >= st
					for (int di = 0; di < det.size(); di++) {
						final DetectedFace rd = det.get(di);
						if (rd.getConfidence() >= scoreThreshold)
							filteredDet.add(rd);
					}

					// (b) match annotations to detections
					final List<Match> mps = matcher.match(annot, filteredDet);

					// (c) compute the result statistics and append to the list
					final Results r = new Results(imName, scoreThreshold, mps, annot, filteredDet);
					imageResults.add(r);
				}
			}

			// merge the list of results for this image (imageResults) with the
			// global list (cumRes)
			cumRes = Results.merge(cumRes, imageResults);
		}

		return cumRes;
	}

	/**
	 * Get a list of unique confidences associated with the given list of faces.
	 * 
	 * @param faces
	 *            the faces
	 * @return the unique confidences, sorted in ascending order.
	 */
	private double[] getUniqueConfidences(List<? extends DetectedFace> faces) {
		final TDoubleHashSet set = new TDoubleHashSet();

		for (final DetectedFace f : faces) {
			set.add(f.getConfidence());
		}

		final double[] ret = set.toArray();

		Arrays.sort(ret);

		return ret;
	}

	public static void main(String[] args) throws IOException {
		final double[][] alldata = {
				{ 67.363819, 44.511485, -1.476417, 105.249970, 87.209036 },
				{ 41.936870, 27.064477, 1.471906, 184.070915, 129.345601 },
				{ 70.993052, 43.355200, 1.370217, 340.894300, 117.498951 }
		};

		final MBFImage img = ImageUtilities.readMBF(new File(
				"/Users/jsh2/Downloads/originalPics/2002/08/26/big/img_265.jpg"));

		for (final double[] data : alldata) {
			final double w = data[0];
			final double h = data[1];
			final double t = data[2];
			final double x = data[3];
			final double y = data[4];

			final Ellipse ell = EllipseUtilities.ellipseFromEquation(x, y, w, h, t);
			final EllipticalDetectedFace face = new EllipticalDetectedFace(ell, img.flatten(), 1);

			DisplayUtilities.displaySimple(ResizeProcessor.doubleSize(face.getFacePatch()));

			img.drawShape(ell, RGBColour.RED);
			img.drawLine((int) x, (int) y, t, (int) h, RGBColour.RED);
		}

		DisplayUtilities.display(img);
	}
}
