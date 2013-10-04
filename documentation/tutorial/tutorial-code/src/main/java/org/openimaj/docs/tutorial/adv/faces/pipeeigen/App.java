package org.openimaj.docs.tutorial.adv.faces.pipeeigen;

import java.io.IOException;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.IdentityAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.IdentityFaceDetector;
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		/*
		 * Load the data, and create some training and test data
		 */
		final VFSGroupDataset<FImage> dataset =
				new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip",
						ImageUtilities.FIMAGE_READER);

		final GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(dataset, 5, 0, 5);
		final GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
		final GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();

		/*
		 * Configure recogniser
		 */
		final FaceAligner<DetectedFace> aligner = new IdentityAligner<DetectedFace>();
		final FaceDetector<DetectedFace, FImage> detector = new IdentityFaceDetector<FImage>();

		final EigenFaceRecogniser<DetectedFace, String> recogniser =
				EigenFaceRecogniser.create(100, aligner, 1, DoubleFVComparison.EUCLIDEAN);

		final FaceRecognitionEngine<DetectedFace, String> engine =
				new FaceRecognitionEngine<DetectedFace, String>(detector, recogniser);

		/*
		 * Train
		 */
		engine.train(training);

		/*
		 * Now we can test our performance on the test set
		 */
		double correct = 0, incorrect = 0;
		for (final String truePerson : testing.getGroups()) {
			for (final FImage face : testing.get(truePerson)) {
				System.out.println(engine.recogniseBest(face));
				final String bestPerson = engine.recogniseBest(face).get(0).secondObject().annotation;

				System.out.println("Actual: " + truePerson + "\tguess: " + bestPerson);

				if (truePerson.equals(bestPerson))
					correct++;
				else
					incorrect++;
			}
		}

		System.out.println("Accuracy: " + (correct / (correct + incorrect)));
	}
}
