package org.openimaj.image.objectdetection.haar.training;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.pair.ObjectFloatPair;

public class AdaBoost {
	StumpClassifier.WeightedLearner factory = new StumpClassifier.WeightedLearner();

	public List<ObjectFloatPair<StumpClassifier>> learn(HaarTrainingData trainingSet, int _numberOfRounds) {
		// Initialise weights
		final float[] _weights = new float[trainingSet.numInstances()];
		for (int i = 0; i < trainingSet.numInstances(); i++)
			_weights[i] = 1.0f / trainingSet.numInstances();

		final boolean[] actualClasses = trainingSet.getClasses();

		final List<ObjectFloatPair<StumpClassifier>> _h = new ArrayList<ObjectFloatPair<StumpClassifier>>();

		// Perform the learning
		for (int t = 0; t < _numberOfRounds; t++) {
			System.out.println("Iteration: " + t);

			// Create the weak learner and train it
			final ObjectFloatPair<StumpClassifier> h = factory.learn(trainingSet, _weights);

			// Compute the classifications and training error
			final boolean[] hClassification = new boolean[trainingSet.numInstances()];
			final float[] responses = trainingSet.getResponses(h.first.dimension);
			double epsilon = 0.0;
			for (int i = 0; i < trainingSet.numInstances(); i++) {
				hClassification[i] = h.first.classify(responses[i]);
				epsilon += hClassification[i] != actualClasses[i] ? _weights[i] : 0.0;
			}

			System.out.println("epsilon = " + epsilon);

			// Check stopping condition
			if (epsilon >= 0.5)
				break;

			// Calculate alpha
			final float alpha = (float) (0.5 * Math.log((1 - epsilon) / epsilon));

			System.out.println("alpha = " + alpha);

			// Update the weights
			float weightsSum = 0.0f;
			for (int i = 0; i < trainingSet.numInstances(); i++) {
				_weights[i] *= Math.exp(-alpha * (actualClasses[i] ? 1 : -1) * (hClassification[i] ? 1 : -1));
				weightsSum += _weights[i];
			}
			// Normalise
			for (int i = 0; i < trainingSet.numInstances(); i++)
				_weights[i] /= weightsSum;

			// Store the weak learner and alpha value
			_h.add(new ObjectFloatPair<StumpClassifier>(h.first, alpha));

			// if (t % 5 == 0)
			// printClassificationQuality(trainingSet, _h);
			// DisplayUtilities.display(DrawingTest.drawRects(trainingSet.getFeature(h.first.dimension).rects));
			System.out.println("feature = " + h.first.dimension);

			// Break if perfectly classifying data
			if (epsilon == 0.0)
				break;
		}

		return _h;
	}

	public void printClassificationQuality(HaarTrainingData data, List<ObjectFloatPair<StumpClassifier>> ensemble,
			float threshold)
	{
		int tp = 0;
		int fn = 0;
		int tn = 0;
		int fp = 0;

		final int ninstances = data.numInstances();
		final boolean[] classes = data.getClasses();
		for (int i = 0; i < ninstances; i++) {
			final float[] feature = data.getInstanceFeature(i);

			final boolean predicted = AdaBoost.Classify(feature, ensemble, threshold);
			final boolean actual = classes[i];

			if (actual) {
				if (predicted)
					tp++; // TP
				else
					fn++; // FN
			} else {
				if (predicted)
					fp++; // FP
				else
					tn++; // TN
			}
		}

		System.out.format("TP: %d\tFN: %d\tFP: %d\tTN: %d\n", tp, fn, fp, tn);

		final float fpr = (float) fp / (float) (fp + tn);
		final float tpr = (float) tp / (float) (tp + fn);

		System.out.format("FPR: %2.2f\tTPR: %2.2f\n", fpr, tpr);
	}

	public static boolean Classify(float[] data, List<ObjectFloatPair<StumpClassifier>> _h) {
		double classification = 0.0;

		// Call the weak learner classify methods and combine results
		for (int t = 0; t < _h.size(); t++)
			classification += _h.get(t).second * (_h.get(t).first.classify(data) ? 1 : -1);

		// Return the thresholded classification
		return classification > 0.0 ? true : false;
	}

	public static boolean Classify(float[] data, List<ObjectFloatPair<StumpClassifier>> _h, float threshold) {
		double classification = 0.0;

		// Call the weak learner classify methods and combine results
		for (int t = 0; t < _h.size(); t++)
			classification += _h.get(t).second * (_h.get(t).first.classify(data) ? 1 : -1);

		// Return the thresholded classification
		return classification > threshold ? true : false;
	}
}
