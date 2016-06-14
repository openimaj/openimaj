/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.workinprogress.featlearn.cifarexps;

import java.io.IOException;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.CIFAR10Dataset;
import org.openimaj.math.statistics.normalisation.ZScore;
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.workinprogress.featlearn.RandomPatchSampler;

import de.bwaldvogel.liblinear.SolverType;

public abstract class CIFARExperimentFramework {
	protected final int patchSize = 6;
	protected final int numPatches = 400000;
	protected final int C = 1;

	protected abstract void learnFeatures(double[][] patches);

	protected abstract double[] extractFeatures(MBFImage image);

	public double run() throws IOException {
		// load training data
		final GroupedDataset<String, ListDataset<MBFImage>, MBFImage> trainingdata = CIFAR10Dataset
				.getTrainingImages(CIFAR10Dataset.MBFIMAGE_READER);

		// create random patches
		final RandomPatchSampler<MBFImage> sampler =
				new RandomPatchSampler<MBFImage>(trainingdata, patchSize, patchSize, numPatches);

		final double[][] patches = new double[numPatches][];
		int i = 0;
		for (final MBFImage p : sampler) {
			patches[i++] = p.getDoublePixelVector();

			if (i % 10000 == 0)
				System.out.format("Extracting patch %d / %d\n", i, numPatches);
		}

		// Perform feature learning
		learnFeatures(patches);

		// extract features
		final MBFImage[] trimages = new MBFImage[trainingdata.numInstances()];
		final String[] trclasses = new String[trainingdata.numInstances()];
		final double[][] trfeatures = new double[trainingdata.numInstances()][];
		i = 0;
		for (final String clz : trainingdata.getGroups()) {
			for (final MBFImage p : trainingdata.get(clz)) {
				// trfeatures[i] = extractFeatures(p);
				trimages[i] = p;
				trclasses[i] = clz;
				i++;
			}
		}
		// for (i = 0; i < trimages.length; i++) {
		// if (i % 100 == 0)
		// System.out.format("Extracting features %d / %d\n", i,
		// trainingdata.numInstances());
		// trfeatures[i] = extractFeatures(trimages[i]);
		// }
		Parallel.forRange(0, trimages.length, 1, new Operation<Parallel.IntRange>() {
			volatile int count = 0;

			@Override
			public void perform(Parallel.IntRange range) {
				for (int ii = range.start; ii < range.stop; ii++) {
					if (count % 100 == 0)
						System.out.format("Extracting features %d / %d\n", count, trainingdata.numInstances());
					trfeatures[ii] = extractFeatures(trimages[ii]);
					count++;
				}
			}
		});

		// feature normalisation
		final ZScore z = new ZScore(0.01);
		z.train(trfeatures);
		final double[][] trfeaturesz = z.normalise(trfeatures);

		// train linear SVM
		final LiblinearAnnotator<double[], String> ann =
				new LiblinearAnnotator<double[], String>(new FeatureExtractor<DoubleFV, double[]>() {
					@Override
					public DoubleFV extractFeature(double[] object) {
						return new DoubleFV(object);
					}
				}, LiblinearAnnotator.Mode.MULTICLASS,
				SolverType.L2R_L2LOSS_SVC_DUAL, C, 0.1, 1 /* bias */, true);

		ann.train(AnnotatedObject.createList(trfeaturesz, trclasses));

		// load test data
		final GroupedDataset<String, ListDataset<MBFImage>, MBFImage> testdata = CIFAR10Dataset
				.getTestImages(CIFAR10Dataset.MBFIMAGE_READER);

		// extract test features
		final String[] teclasses = new String[testdata.numInstances()];
		double[][] tefeatures = new double[testdata.numInstances()][];
		i = 0;
		for (final String clz : testdata.getGroups()) {
			for (final MBFImage p : testdata.get(clz)) {
				tefeatures[i] = extractFeatures(p);
				teclasses[i] = clz;
				i++;
			}
		}

		// feature normalisation (using mean and stddev learned from training
		// data)
		tefeatures = z.normalise(tefeatures);

		// perform classification and calculate accuracy
		double correct = 0, incorrect = 0;
		for (i = 0; i < tefeatures.length; i++) {
			final ClassificationResult<String> res = ann.classify(tefeatures[i]);

			if (res.getPredictedClasses().iterator().next().equals(teclasses[i]))
				correct++;
			else
				incorrect++;
		}
		final double acc = correct / (correct + incorrect);
		System.out.println("Test accuracy " + acc);
		return acc;
	}
}
