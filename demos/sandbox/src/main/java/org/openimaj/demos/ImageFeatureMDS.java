package org.openimaj.demos;

import java.net.URL;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.dataset.FlickrImageDataset;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.similarity.SimilarityMatrix;
import org.openimaj.math.matrix.similarity.processor.MultidimensionalScaling;
import org.openimaj.util.pair.IndependentPair;

public class ImageFeatureMDS {
	public static void main(String[] args) throws Exception {
		final String apikey = "";
		final String secret = "";
		final int numImages = 2;

		final FlickrImageDataset<MBFImage> dataset = FlickrImageDataset.create(ImageUtilities.MBFIMAGE_READER, apikey,
				secret, "colorful", numImages);

		dataset.getPhotos().set(1, dataset.getPhoto(0));

		final DoubleFV[] features = new DoubleFV[numImages];
		for (int i = 0; i < numImages; i++) {
			features[i] = extractFeature(dataset.get(i));
		}

		final SimilarityMatrix matrix = new SimilarityMatrix(numImages);
		for (int i = 0; i < numImages; i++) {
			matrix.setIndexValue(i, dataset.getID(i));
			final DoubleFV fi = features[i];

			for (int j = 0; j < numImages; j++) {
				final DoubleFV fj = features[j];

				matrix.set(i, j, fi.compare(fj, DoubleFVComparison.COSINE_SIM));
			}
		}

		System.out.println(matrix);

		final MultidimensionalScaling mds = new MultidimensionalScaling();
		mds.process(matrix);
		System.out.println(mds.getPoints());

		final MBFImage img = new MBFImage(1000, 1000, ColourSpace.RGB);
		for (final IndependentPair<String, Point2d> pt : mds.getPoints()) {
			// img.drawPoint(pt.getSecondObject(), RGBColour.RED, 3);

			final int idx = dataset.indexOfID(pt.firstObject());
			final MBFImage thumb = ImageUtilities.readMBF(new URL(dataset.getPhoto(idx).getThumbnailUrl()));
			img.drawImage(thumb, pt.getSecondObject().transform(TransformUtilities.scaleMatrix(1000, 1000)));
		}
		DisplayUtilities.display(img);
	}

	static DoubleFV extractFeature(MBFImage image) {
		final HistogramModel model = new HistogramModel(4, 4, 4);

		model.estimateModel(image);

		return model.histogram.normaliseFV();
	}
}
