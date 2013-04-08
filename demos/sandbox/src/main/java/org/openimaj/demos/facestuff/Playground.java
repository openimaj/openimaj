package org.openimaj.demos.facestuff;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.ImageUtilities;

public class Playground {
	public static void main(String[] args) throws IOException {
		ImageUtilities.readMBF(new File("/Users/jon/Desktop/im307026.jpg"));
	}

	// public static void main(String[] args) {
	// final BingAPIToken token =
	// DefaultTokenFactory.getInstance().getToken(BingAPIToken.class);
	//
	// final MapBackedDataset<String, BingImageDataset<FImage>, FImage> dataset
	// = MapBackedDataset.of(
	// BingImageDataset.create(ImageUtilities.FIMAGE_READER, token,
	// "Tom Cruise", "Face:Face", 10),
	// BingImageDataset.create(ImageUtilities.FIMAGE_READER, token,
	// "Nicole Kidman", "Face:Face", 10),
	// BingImageDataset.create(ImageUtilities.FIMAGE_READER, token,
	// "Angelina Jolie", "Face:Face", 10),
	// BingImageDataset.create(ImageUtilities.FIMAGE_READER, token, "Brad Pitt",
	// "Face:Face", 10)
	// );
	//
	// for (final Entry<String, BingImageDataset<FImage>> entry :
	// dataset.entrySet()) {
	// DisplayUtilities.display(entry.getKey(), entry.getValue());
	// }
	//
	// final LocalLBPHistogram.Extractor<CLMDetectedFace> extractor = new
	// LocalLBPHistogram.Extractor<CLMDetectedFace>(new CLMAligner(), 20, 20, 8,
	// 1);
	//
	// final FacialFeatureComparator<LocalLBPHistogram> comparator = new
	// FaceFVComparator<LocalLBPHistogram,
	// FloatFV>(FloatFVComparison.EUCLIDEAN);
	//
	// final CrossValidationBenchmark<String, FImage, CLMDetectedFace> cvd = new
	// CrossValidationBenchmark<String, FImage, CLMDetectedFace>(
	// new StratifiedGroupedKFold<String, CLMDetectedFace>(5),
	// dataset,
	// new CLMFaceDetector(),
	// new FaceRecogniserProvider<CLMDetectedFace, String>() {
	// @Override
	// public FaceRecogniser<CLMDetectedFace, String> create(
	// GroupedDataset<String, ? extends ListDataset<CLMDetectedFace>,
	// CLMDetectedFace> dataset)
	// {
	// final KNNAnnotator<CLMDetectedFace, String, LocalLBPHistogram> knn =
	// KNNAnnotator.create(extractor, comparator, 1, 5f);
	//
	// final AnnotatorFaceRecogniser<CLMDetectedFace, String> recogniser =
	// AnnotatorFaceRecogniser.create(knn);
	//
	// recogniser.train(dataset);
	//
	// return recogniser;
	// }
	// });
	//
	// final ExperimentContext ctx = ExperimentRunner.runExperiment(cvd);
	// System.out.println(ctx);
	//
	// }
}
