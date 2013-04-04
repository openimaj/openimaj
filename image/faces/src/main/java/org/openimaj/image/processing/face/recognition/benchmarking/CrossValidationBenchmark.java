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
package org.openimaj.image.processing.face.recognition.benchmarking;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.ExperimentContext;
import org.openimaj.experiment.RunnableExperiment;
import org.openimaj.experiment.annotations.DependentVariable;
import org.openimaj.experiment.annotations.Experiment;
import org.openimaj.experiment.annotations.IndependentVariable;
import org.openimaj.experiment.annotations.Time;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.AggregatedCMResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAggregator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.experiment.validation.ValidationOperation;
import org.openimaj.experiment.validation.ValidationRunner;
import org.openimaj.experiment.validation.cross.CrossValidator;
import org.openimaj.image.Image;
import org.openimaj.image.processing.face.detection.DatasetFaceDetector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;

/**
 * An {@link RunnableExperiment} for performing cross-validation experiments on
 * face recognisers & classifiers.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 * @param <IMAGE>
 * @param <FACE>
 */
@Experiment(
		author = "Jonathon Hare",
		dateCreated = "2012-07-26",
		description = "Face recognition cross validation experiment")
public class CrossValidationBenchmark<KEY, IMAGE extends Image<?, IMAGE>, FACE extends DetectedFace>
		implements
		RunnableExperiment
{
	@IndependentVariable
	protected CrossValidator<GroupedDataset<KEY, ListDataset<FACE>, FACE>> crossValidator;

	@IndependentVariable
	protected GroupedDataset<KEY, ListDataset<IMAGE>, IMAGE> dataset;

	@IndependentVariable
	protected FaceDetector<FACE, IMAGE> faceDetector;

	@IndependentVariable
	protected FaceRecogniserProvider<FACE, KEY> engine;

	@DependentVariable
	protected AggregatedCMResult<KEY> result;

	/**
	 * Construct the {@link CrossValidationBenchmark} experiment with the given
	 * dependent variables.
	 * 
	 * @param dataset
	 *            the dataset
	 * @param crossValidator
	 *            the cross-validator
	 * @param faceDetector
	 *            the face detector
	 * @param engine
	 *            the recogniser
	 */
	public CrossValidationBenchmark(
			CrossValidator<GroupedDataset<KEY, ListDataset<FACE>, FACE>> crossValidator,
			GroupedDataset<KEY, ListDataset<IMAGE>, IMAGE> dataset,
			FaceDetector<FACE, IMAGE> faceDetector,
			FaceRecogniserProvider<FACE, KEY> engine)
	{
		this.dataset = dataset;
		this.crossValidator = crossValidator;
		this.faceDetector = faceDetector;
		this.engine = engine;
	}

	@Override
	public void perform() {
		final CMAggregator<KEY> aggregator = new CMAggregator<KEY>();

		final GroupedDataset<KEY, ListDataset<FACE>, FACE> faceDataset = DatasetFaceDetector.process(dataset,
				faceDetector);

		result = ValidationRunner.run(
				aggregator,
				faceDataset,
				crossValidator,
				new ValidationOperation<GroupedDataset<KEY, ListDataset<FACE>, FACE>, CMResult<KEY>>()
				{
					@Time(identifier = "Train and Evaluate recogniser")
					@Override
					public CMResult<KEY> evaluate(
							GroupedDataset<KEY, ListDataset<FACE>, FACE> training,
							GroupedDataset<KEY, ListDataset<FACE>, FACE> validation)
					{
						final FaceRecogniser<FACE, KEY> rec = engine.create(training);

						final ClassificationEvaluator<CMResult<KEY>, KEY, FACE> eval =
								new ClassificationEvaluator<CMResult<KEY>, KEY, FACE>(
										rec, validation, new CMAnalyser<FACE, KEY>(CMAnalyser.Strategy.SINGLE)
								);

						return eval.analyse(eval.evaluate());
					}
				});
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish(ExperimentContext context) {
		// TODO Auto-generated method stub

	}
}
