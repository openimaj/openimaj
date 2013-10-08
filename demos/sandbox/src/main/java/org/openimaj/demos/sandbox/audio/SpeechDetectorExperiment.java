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
/**
 *
 */
package org.openimaj.demos.sandbox.audio;

import java.io.IOException;

import org.openimaj.audio.AudioAnnotator;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.ExperimentContext;
import org.openimaj.experiment.ExperimentRunner;
import org.openimaj.experiment.RunnableExperiment;
import org.openimaj.experiment.agent.ExperimentAgent;
import org.openimaj.experiment.annotations.Experiment;
import org.openimaj.experiment.annotations.IndependentVariable;
import org.openimaj.experiment.annotations.Time;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.AggregatedCMResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAggregator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser.Strategy;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.experiment.validation.ValidationOperation;
import org.openimaj.experiment.validation.ValidationRunner;
import org.openimaj.experiment.validation.cross.StratifiedGroupedKFold;
import org.openimaj.feature.DoubleFV;

/**
 *	An experiment for testing how good the trained speech detector is. It uses a
 *	stratified 10-fold cross-validation and uses a 1-k KNN annotator. The dataset
 *	is created during the {@link #setup()} stage by the detector. The cross-validation
 *	uses the audio annotator delivered by the detector.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 06 Mar 2013
 *	@version $Author$, $Revision$, $Date$
 */
@Experiment(
		author = "David Dupplaw <dpd@ecs.soton.ac.uk>",
		dateCreated = "2013-03-08",
		description = "Speech detection cross validation experiment")
public class SpeechDetectorExperiment implements RunnableExperiment
{
	/** The speech detector we're testing */
	@IndependentVariable
	private final SpeechDetector speechDetector;

	/** The dataset to use for the experiment */
	@IndependentVariable
	private MapBackedDataset<String, ListDataset<DoubleFV>, DoubleFV> dataset = null;

	/** The final result of the experiment */
	@IndependentVariable
	private AggregatedCMResult<String> result;

	/**
	 * 	Takes the speech detector to test
	 * 	@param sd The speech detector to test
	 */
	public SpeechDetectorExperiment( final SpeechDetector sd )
	{
		this.speechDetector = sd;
	}

	/**
	 * 	Creates the datasets from which the validations will take place
	 *
	 *	{@inheritDoc}
	 * 	@see org.openimaj.experiment.RunnableExperiment#setup()
	 */
	@Override
	public void setup()
	{
		this.dataset = this.speechDetector.generateDataset();
	}

	/**
	 * 	Uses a stratified 10-fold grouped cross-validation
	 *
	 *	{@inheritDoc}
	 * 	@see org.openimaj.experiment.RunnableExperiment#perform()
	 */
	@Override
	public void perform()
	{
		// We'll use a stratified K-fold cross-validation with 10 subsets
		final StratifiedGroupedKFold<String,DoubleFV> k =
				new StratifiedGroupedKFold<String, DoubleFV>( 10 );

		// We'll create a confusion matrix for the results
		final CMAggregator<String> confusionMatrixAggregator =
				new CMAggregator<String>();

		this.result = ValidationRunner.run( confusionMatrixAggregator, this.dataset, k,
			new ValidationOperation<GroupedDataset<String,ListDataset<DoubleFV>,DoubleFV>,
				CMResult<String>>()
			{
				/**
				 * 	The validation operation uses a ClassificationEvaluator to evaluate the
				 * 	confusion matrix.
				 *
				 *	@param training Training data
				 *	@param validation Validation data
				 *	@return Confusion Matrix for the classes
				 */
				@Time(identifier = "Train and Evaluate detector")
				@Override
				public CMResult<String> evaluate(
						final GroupedDataset<String, ListDataset<DoubleFV>, DoubleFV> training,
						final GroupedDataset<String, ListDataset<DoubleFV>, DoubleFV> validation )
				{
					for( final String group : training.getGroups() )
					{
						for( int i = 0; i < training.getInstances( group ).size(); i++ )
						{
							final DoubleFV dfv = training.getInstances( group ).getInstance( i );
							if( dfv == null )
								System.out.println( "TraningSet: Null in group "+group+" at "+i );
						}
					}
					for( final String group : validation.getGroups() )
					{
						for( int i = 0; i < validation.getInstances( group ).size(); i++ )
						{
							final DoubleFV dfv = validation.getInstances( group ).getInstance( i );
							if( dfv == null )
								System.out.println( "ValidationSet: Null in group "+group+" at "+i );
						}
					}

					// Get a new annotator and train it with the training set.
					final AudioAnnotator aa = SpeechDetectorExperiment.this.
							speechDetector.getNewAnnotator();
					aa.train( training );

					// Now perform the validation using the validation set
					final ClassificationEvaluator<CMResult<String>, String, DoubleFV>
						eval = new ClassificationEvaluator<CMResult<String>, String, DoubleFV>(
							aa,	validation, new CMAnalyser<DoubleFV,String>( Strategy.SINGLE ) );

					return eval.analyse( eval.evaluate() );
				}
			});

		System.out.println( this.result );
	}

	@Override
	public void finish( final ExperimentContext context )
	{
		System.out.println( "Finished experiment" );
	}

	// ===================================================================================== //


	/**
	 *	Run this experiment.
	 *	@param args The command-line arguments
	 * 	@throws IOException
	 */
	public static void main( final String[] args ) throws IOException
	{
		ExperimentAgent.initialise();

		// This is the detector that we'll test
		final SpeechDetector sd = new SpeechDetector();
		sd.parseArgs( args );

		// Create the experiment
		final SpeechDetectorExperiment experiment = new SpeechDetectorExperiment( sd );

		// Now run it!
		final ExperimentContext ctx = ExperimentRunner.runExperiment( experiment );
		System.out.println( ctx );
	}
}
