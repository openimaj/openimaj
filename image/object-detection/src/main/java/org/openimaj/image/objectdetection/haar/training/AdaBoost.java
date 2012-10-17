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
			// Create the weak learner and train it
			final ObjectFloatPair<StumpClassifier> h = factory.learn(trainingSet, _weights);

			// Compute the classifications and training error
			final boolean[] hClassification = new boolean[trainingSet.numInstances()];
			double epsilon = 0.0;
			for (int i = 0; i < trainingSet.numInstances(); i++) {
				hClassification[i] = h.first.classify(trainingSet.getInstanceFeature(i));
				epsilon += hClassification[i] != actualClasses[i] ? _weights[i] : 0.0;
			}

			// Check stopping condition
			if (epsilon >= 0.5)
				break;

			// Calculate alpha
			final float alpha = (float) (0.5 * Math.log((1 - epsilon) / epsilon));

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

			// Break if perfectly classifying data
			if (epsilon == 0.0)
				break;
		}

		return _h;
	}
}
