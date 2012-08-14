package org.openimaj.image.processing.face.recognition.benchmarking;

import java.io.IOException;

import org.openimaj.experiment.ExperimentContext;
import org.openimaj.experiment.ExperimentRunner;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.validation.cross.StratifiedGroupedKFold;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.alignment.IdentityAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.IdentityFaceDetector;
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.ATandTDataset;

public class Tester {
	public static void main(String[] args) throws IOException {
		final CrossValidationBenchmark<Integer, FImage, DetectedFace> benchmark = new CrossValidationBenchmark<Integer, FImage, DetectedFace>(
				new StratifiedGroupedKFold<Integer, DetectedFace>(10),
				new ATandTDataset(),
				new IdentityFaceDetector<FImage>(),
				new FaceRecogniserProvider<DetectedFace, Integer>() {
					@Override
					public FaceRecogniser<DetectedFace, ?, Integer> create(
							GroupedDataset<Integer, ListDataset<DetectedFace>, DetectedFace> dataset)
					{
						final float thresh = DoubleFVComparison.EUCLIDEAN.isDistance() ? Float.MAX_VALUE
								: -Float.MAX_VALUE;

						final EigenFaceRecogniser<DetectedFace, Integer> rec = EigenFaceRecogniser.create(10,
								new IdentityAligner<DetectedFace>(), 1, DoubleFVComparison.EUCLIDEAN, thresh);

						rec.train(dataset);

						return rec;
					}

					@Override
					public String toString() {
						return "EigenFaces Recogniser (identity aligner; 10 E.V's; 1-NN classifier).";
					}
				}
				);

		final ExperimentContext ctx = ExperimentRunner.runExperiment(benchmark);

		System.out.println(ctx);
	}
}
