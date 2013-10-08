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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.audio.features.MFCC;
import org.openimaj.audio.reader.OneSecondClipReader;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAggregator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.experiment.validation.cross.StratifiedGroupedKFold;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.annotation.svm.SVMAnnotator;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 14 May 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioClassifierTest
{
	/**
	 * 	A provider for feature vectors for the sample buffers.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 8 May 2013
	 */
	public static class SamplesFeatureProvider implements FeatureExtractor<DoubleFV,SampleBuffer>
	{
		/** The MFCC processor */
		private final MFCC mfcc = new MFCC();

		@Override
		public DoubleFV extractFeature( final SampleBuffer buffer )
		{
			// Calculate the MFCCs
			this.mfcc.process( buffer );
			final double[][] mfccs = this.mfcc.getLastCalculatedFeature();

			// The output vector
			final double[] values = new double[mfccs[0].length];

			if( mfccs.length > 1 )
			{
				// Average across the channels
				for( int i = 0; i < mfccs[0].length; i++ )
				{
					double acc = 0;
					for( int j = 0; j < mfccs.length; j++ )
						acc += mfccs[j][i];
					acc /= mfccs.length;
					values[i] = acc;
				}
			}
			else
				// Copy the mfccs
				System.arraycopy( mfccs[0], 0, values, 0, values.length );

			// Return the new DoubleFV
			return new DoubleFV( values );
		}
	}

	/**
	 * 	Use the OpenIMAJ experiment platform to cross-validate the dataset using the SVM annotator.
	 *	@param data The dataset
	 *	@throws IOException
	 */
	public static void crossValidate( final GroupedDataset<String,
			? extends ListDataset<List<SampleBuffer>>, List<SampleBuffer>> data ) throws IOException
	{
		// Flatten the dataset, and create a random group split operation we can use
		// to get the validation/training data.
		final StratifiedGroupedKFold<String, SampleBuffer> splits =
				new StratifiedGroupedKFold<String, SampleBuffer>( 5 );
//		final GroupedRandomSplits<String, SampleBuffer> splits =
//				new GroupedRandomSplits<String,SampleBuffer>(
//						DatasetAdaptors.flattenListGroupedDataset( data ),
//						data.numInstances()/2, data.numInstances()/2 );

		final CMAggregator<String> cma = new CMAggregator<String>();

		// Loop over the validation data.
		for( final ValidationData<GroupedDataset<String, ListDataset<SampleBuffer>, SampleBuffer>> vd :
				splits.createIterable( DatasetAdaptors.flattenListGroupedDataset( data ) ) )
		{
			// For this validation, create the annotator with the feature extractor and train it.
			final SVMAnnotator<SampleBuffer,String> ann = new SVMAnnotator<SampleBuffer,String>(
					new SamplesFeatureProvider() );

			ann.train( AnnotatedObject.createList( vd.getTrainingDataset() ) );

			// Create a classification evaluator that will do the validation.
			final ClassificationEvaluator<CMResult<String>, String, SampleBuffer> eval =
					new ClassificationEvaluator<CMResult<String>, String, SampleBuffer>(
						ann, vd.getValidationDataset(),
						new CMAnalyser<SampleBuffer, String>(CMAnalyser.Strategy.SINGLE) );

			final Map<SampleBuffer, ClassificationResult<String>> guesses = eval.evaluate();
			final CMResult<String> result = eval.analyse(guesses);
			cma.add( result );

			System.out.println( result.getDetailReport() );
		}

		System.out.println( cma.getAggregatedResult().getDetailReport() );
	}

	/**
	 *
	 *	@param args
	 * @throws IOException
	 */
	public static void main( final String[] args ) throws IOException
	{
		// Virtual file system for music speech corpus
		final GroupedDataset<String, ? extends ListDataset<List<SampleBuffer>>, List<SampleBuffer>>
			musicSpeechCorpus = new	VFSGroupDataset<List<SampleBuffer>>(
						"/data/music-speech-corpus/music-speech/wavfile/train",
						new OneSecondClipReader() );

		System.out.println( "Corpus size: "+musicSpeechCorpus.numInstances() );

		// Cross-validate the audio classifier trained on speech & music.
		final HashMap<String,String[]> regroup = new HashMap<String, String[]>();
		regroup.put( "speech", new String[]{ "speech" } );
		regroup.put( "non-speech", new String[]{ "music", "m+s", "other" } );
		AudioClassifierTest.crossValidate( DatasetAdaptors.getRegroupedDataset(
				musicSpeechCorpus, regroup ) );

//		// Create a new feature extractor for the sample buffer
//		final SamplesFeatureProvider extractor = new SamplesFeatureProvider();
//
//		// Create an SVM annotator
//		final SVMAnnotator<SampleBuffer,String> svm = new SVMAnnotator<SampleBuffer,String>( extractor );
//
//		AudioClassifier<String> ac = new AudioClassifier<String>( svm );
//
//		// Create the training data
//		final List<IndependentPair<AudioStream,String>> trainingData = new ArrayList<IndependentPair<AudioStream,String>>();
//		trainingData.add( new IndependentPair<AudioStream,String>( AudioDatasetHelper.getAudioStream(
//				musicSpeechCorpus.getInstances( "music" ) ), "non-speech" ) );
//		trainingData.add( new IndependentPair<AudioStream,String>( AudioDatasetHelper.getAudioStream(
//				musicSpeechCorpus.getInstances( "speech" ) ), "speech" ) );
//
//		// Train the classifier
//		ac.train( trainingData );
	}
}
