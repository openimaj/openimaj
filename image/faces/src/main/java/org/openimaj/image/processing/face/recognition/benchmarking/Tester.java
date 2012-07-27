package org.openimaj.image.processing.face.recognition.benchmarking;

import java.io.IOException;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.validation.cross.StratifiedGroupedKFold;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.alignment.NullAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.IdentityFaceDetector;
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.ATandTDataset;

public class Tester {
	public static void main(String[] args) throws IOException {
		CrossValidationBenchmark<Integer, FImage, DetectedFace> benchmark = new CrossValidationBenchmark<Integer, FImage, DetectedFace>();
		
		benchmark.crossValidator = new StratifiedGroupedKFold<Integer, DetectedFace>(10);
		benchmark.dataset = new ATandTDataset();
		benchmark.faceDetector = new IdentityFaceDetector<FImage>();
		benchmark.engine = new FaceRecogniserProvider<DetectedFace, Integer>() {
			@Override
			public FaceRecogniser<DetectedFace, ?, Integer> create(
					GroupedDataset<Integer, ListDataset<DetectedFace>, DetectedFace> dataset)
			{
				EigenFaceRecogniser<DetectedFace, Integer> rec = EigenFaceRecogniser.create(10, new NullAligner<DetectedFace>(), 1);
				
				rec.train(dataset);
				
				return rec;
			}
		};
		
		benchmark.performExperiment();
		
		System.out.println(benchmark.result);
	}
}
