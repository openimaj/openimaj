/**
 *
 */
package org.openimaj.demos.sandbox.audio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import org.apache.commons.vfs2.FileSystemException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.audio.AudioEventListener;
import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.conversion.BitDepthConverter;
import org.openimaj.audio.conversion.BitDepthConverter.BitDepthConversionAlgorithm;
import org.openimaj.audio.conversion.MultichannelToMonoProcessor;
import org.openimaj.audio.features.MFCCJAudio;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 7 May 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioClassifierSVM extends AbstractAnnotator<SampleBuffer,String>
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
		private final MFCCJAudio mfcc = new MFCCJAudio();

		@Override
		public DoubleFV extractFeature( final SampleBuffer buffer )
		{
			// Calculate the MFCCs
			final double[][] mfccs = this.mfcc.calculateMFCC( buffer );

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
	 *	Returns a set of one second of samples from the input stream.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 7 May 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class OneSecondClipReader implements InputStreamObjectReader<List<SampleBuffer>>
	{
		/**
		 *	{@inheritDoc}
		 * 	@see org.openimaj.io.ObjectReader#read(java.lang.Object)
		 */
		@Override
		public List<SampleBuffer> read( final InputStream stream ) throws IOException
		{
			// Open the stream.
			final XuggleAudio xa = new XuggleAudio( stream );

			// Setup a chunker that will get samples in one second chunks.
			final int nSamplesInOneSecond = (int)(xa.getFormat().getSampleRateKHz() * 1000);
			final FixedSizeSampleAudioProcessor f = new FixedSizeSampleAudioProcessor(
					xa, nSamplesInOneSecond );

			// Setup our output list
			final List<SampleBuffer> buffers = new ArrayList<SampleBuffer>();

			// Now read the audio until we're done
			SampleChunk sc = null;
			while( (sc = f.nextSampleChunk()) != null )
				buffers.add( sc.getSampleBuffer() );

			System.out.println( "Got "+buffers.size()+" one-second sample buffers.");

			return buffers;
		}

		/**
		 *	{@inheritDoc}
		 * 	@see org.openimaj.io.InputStreamObjectReader#canRead(java.io.InputStream, java.lang.String)
		 */
		@Override
		public boolean canRead( final InputStream stream, final String name )
		{
			return true;
		}
	}

	/**
	 * 	Options for the classifier
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 10 May 2013
	 */
	public static class AudioClassifierOptions
	{
		/** Whether to save the model to a file. Null if not */
		@Option(name="-s",aliases="--saveModel",usage="Save the model to the file (default:null)")
		public File saveModel = null;

		/** Whether to load a model from a file. Null if not */
		@Option(name="-l",aliases="--loadModel",usage="Load an existing model (default:null)")
		public File loadModel = null;

		/** Whether to train a model; The directory if yes, null if not */
		@Option(name="-t",aliases="--trainModel",usage="Train the model from a directory (default:null)")
		public File trainModelSource = null;
	}

	/** The feature extractor to use for samples */
	private final SamplesFeatureProvider featureExtractor = new SamplesFeatureProvider();

	/** Default options */
	private AudioClassifierOptions options = new AudioClassifierOptions();

	/** The model that we'll use/train */
	private svm_model model;

	/** The positive class being trained for */
	private String positiveClass;

	/**
	 *	Default constructor
	 */
	public AudioClassifierSVM()
	{
	}

	/**
	 * 	Constructor that takes the non-standard options.
	 *	@param options
	 */
	public AudioClassifierSVM( final AudioClassifierOptions options )
	{
		this.options = options;
	}

	/**
	 * 	Train the classifier on the given corpus
	 *	@param musicSpeechCorpus The corpus to train on
	 * 	@param positiveClass The name of the positive class in the corpus
	 * 	@param negativeClasses The names of the negative classes in the corpus
	 */
	public void train( final GroupedDataset<String, ? extends ListDataset<List<SampleBuffer>>,
			List<SampleBuffer>> musicSpeechCorpus, final String positiveClass,
			final String[] negativeClasses )
	{
		System.out.println( musicSpeechCorpus.getGroups() );

		// Store the name of the positive class.
		this.positiveClass = positiveClass;

		// Setup the SVM problem
		final svm_parameter param = AudioClassifierSVM.getSVMParameters();
		final svm_problem prob = AudioClassifierSVM.getSVMProblem(
						musicSpeechCorpus, param, this.featureExtractor,
						positiveClass, negativeClasses );

		// Train the SVM
		this.model = libsvm.svm.svm_train( prob, param );

		// Save the model if we're going to do that.
		if( this.options.saveModel != null ) try
		{
			svm.svm_save_model( this.options.saveModel.getAbsolutePath(), this.model );
		}
		catch( final IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * 	Returns the default set of SVM parameters.
	 *	@return The default set of SVM parameters
	 */
	static private svm_parameter getSVMParameters()
	{
		// These default values came from:
		// https://github.com/arnaudsj/libsvm/blob/master/java/svm_train.java

		final svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0;	// 1/num_features
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		return param;
	}

	/**
	 * 	Returns an svm_problem for the given dataset. This function will return
	 * 	a new SVM problem and will also side-affect the gamma member of the
	 * 	param argument.
	 *
	 * 	@param trainingCorpus The corpus
	 * 	@param param The SVM parameters
	 * 	@param featureExtractor The feature extractor to use
	 * 	@param positiveClass The name of the positive class in the dataset
	 * 	@param negativeClasses The names of the negative classes in the dataset
	 *	@return A new SVM problem.
	 */
	private static svm_problem getSVMProblem( final GroupedDataset<String,
			? extends ListDataset<List<SampleBuffer>>, List<SampleBuffer>> trainingCorpus,
		final svm_parameter param, final FeatureExtractor<DoubleFV,SampleBuffer> featureExtractor,
		final String positiveClass, final String[] negativeClasses )
	{
		// Get all the nodes for the features
		final svm_node[][] positiveNodes = AudioClassifierSVM.getSamples(
				trainingCorpus, positiveClass, featureExtractor );
		final svm_node[][] negativeNodes = AudioClassifierSVM.getSamples(
				trainingCorpus, negativeClasses[0],  featureExtractor );
		// TODO: Need to allow multiple negative classes

		// Work out how long the problem is
		final int nSamples = positiveNodes.length + negativeNodes.length;

		// The array that determines whether a sample is positive or negative.
		final double[] flagArray = new double[nSamples];
		ArrayUtils.fill( flagArray, +1, 0, positiveNodes.length );
		ArrayUtils.fill( flagArray, -1, positiveNodes.length, negativeNodes.length );

		// Concatenate the samples to a single array
		final svm_node[][] sampleArray = ArrayUtils.concatenate(
				positiveNodes, negativeNodes );

		// Create the svm problem to solve
		final svm_problem prob = new svm_problem();

		// Setup the problem
		prob.l = nSamples;
		prob.x = sampleArray;
		prob.y = flagArray;
		param.gamma = 1.0 / AudioClassifierSVM.getMaxIndex( sampleArray );

		return prob;
	}

	/**
	 * 	Returns the maximum index value from all the svm_nodes in the array.
	 *	@param sampleArray The array of training samples
	 *	@return The max feature index
	 */
	static private int getMaxIndex( final svm_node[][] sampleArray )
	{
		int max = 0;
		for( final svm_node[] x : sampleArray )
			for( int j = 0; j < x.length; j++ )
				max = Math.max( max, x[j].index );
		return max;
	}

	/**
	 * 	Returns a set of svm_node instances that represent the features for the
	 * 	subset of the corpus that's passed in.
	 *	@param corpus The corpus in which the instance group can be found
	 *	@param instanceGroup The group of instances to generate features for
	 * 	@param featureExtractor The feature provider
	 *	@return A set of svm_nodes that represent the instances.
	 */
	static private svm_node[][] getSamples( final GroupedDataset<String,
			? extends ListDataset<List<SampleBuffer>>, List<SampleBuffer>> corpus,
			final String instanceGroup,
			final FeatureExtractor<DoubleFV,SampleBuffer> featureExtractor )
	{
		svm_node[][] retVal = null;
		for( final List<SampleBuffer> samples : corpus.getInstances( instanceGroup ) )
		{
			// Set the size of the problem.
			retVal = new svm_node[samples.size()][];

			for( int sample = 0; sample < samples.size(); sample++ )
			{
				// Get the features for the audio
				final SampleBuffer sampleBuffer = samples.get(sample);
				final DoubleFV fv = featureExtractor.extractFeature( sampleBuffer );

				// Convert the features to an svm_node.
				retVal[sample] = AudioClassifierSVM.featureToNode( fv );
			}
		}

		return retVal;
	}

	/**
	 * 	Takes a {@link DoubleFV} and converts it into an array of {@link svm_node}s
	 * 	for the svm library.
	 *
	 *	@param fv The feature vector to convert
	 *	@return The equivalent svm_node[]
	 */
	static private svm_node[] featureToNode( final DoubleFV fv )
	{
		final int nFeatures = fv.length();
		final svm_node[] nodes = new svm_node[nFeatures];

		for( int i = 0; i < fv.length(); i++ )
		{
			nodes[i] = new svm_node();
			nodes[i].index = i;
			nodes[i].value = fv.get( i );
		}

		return nodes;
	}

	/**
	 * 	Set whether to save the SVM model to disk.
	 *	@param saveModel The file name to save to, or null to disable saving.
	 */
	public void setSaveModel( final File saveModel )
	{
		this.options.saveModel = saveModel;
	}

	/**
	 * 	Load an existing model.
	 *	@param loadModel The model to load from
	 * 	@throws IOException If the loading does not complete
	 */
	private void loadModel( final File loadModel ) throws IOException
	{
		this.model = svm.svm_load_model( loadModel.getAbsolutePath() );
	}

	/**
	 *	Performs cross-validation on the SVM.
	 *
	 * 	@param corpus The corpus
	 * 	@param positiveClass The name of the positive class in the corpus
	 * 	@param negativeClasses The names of classes to use as negative examples in the corpus
	 * 	@param numFold The number of folds
	 * 	@return The accuracy
	 */
	public static double crossValidation( final GroupedDataset<String,
			? extends ListDataset<List<SampleBuffer>>, List<SampleBuffer>> corpus,
			final String positiveClass, final String[] negativeClasses,
			final int numFold )
	{
		// Setup the SVM problem
		final svm_parameter param = AudioClassifierSVM.getSVMParameters();
		final svm_problem prob = AudioClassifierSVM.getSVMProblem( corpus, param,
				new SamplesFeatureProvider(), positiveClass, negativeClasses );

		return AudioClassifierSVM.crossValidation( prob, param, numFold );
	}



	/**
	 *	Performs cross-validation on the SVM.
	 *
	 *	@param prob The problem
	 * 	@param param The parameters
	 * 	@param numFold The number of folds
	 * 	@return The accuracy
	 */
	static public double crossValidation( final svm_problem prob,
			final svm_parameter param, final int numFold )
	{
		// The target array in which the final classifications are put
		final double[] target = new double[prob.l];

		// Perform the cross-validation.
		svm.svm_cross_validation( prob, param, numFold, target );

		// Work out how many classifications were correct.
		int totalCorrect = 0;
		for( int i = 0; i < prob.l; i++ )
			if( target[i] == prob.y[i] )
				totalCorrect++;

		// Calculate the accuracy
		final double accuracy = 100.0 * totalCorrect / prob.l;
		System.out.print("Cross Validation Accuracy = "+accuracy+"%\n");

		return accuracy;
	}

	/**
	 * 	Returns a set which consists of the positive class name and
	 * 	the positive class name prefixed with "not".
	 *
	 *	{@inheritDoc}
	 * 	@see org.openimaj.ml.annotation.Annotator#getAnnotations()
	 */
	@Override
	public Set<String> getAnnotations()
	{
		return new HashSet<String>( Arrays.asList( new String[]
			{ this.positiveClass, "not "+this.positiveClass } ) );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.ml.annotation.Annotator#annotate(java.lang.Object)
	 */
	@Override
	public List<ScoredAnnotation<String>> annotate( final SampleBuffer buffer )
	{
		// Extract the feature and convert to a svm_node[]
		final svm_node[] nodes = AudioClassifierSVM.featureToNode(
					this.featureExtractor.extractFeature( buffer ) );

		// Use the trained SVM model to predict the new buffer's annotation
		final double x = svm.svm_predict( this.model, nodes );

		// Create a singleton list to contain the classified annotation.
		return Collections.singletonList( new ScoredAnnotation<String>(
				x > 0 ? this.positiveClass : ("not" + this.positiveClass), 1.0f ) );
	}

	/**
	 * 	Parse the command line options.
	 *	@param args The args
	 *	@return
	 */
	private static AudioClassifierOptions parseOptions( final String[] args )
	{
		// Create the new options instance
		final AudioClassifierOptions options = new AudioClassifierOptions();

		// Parse the command line
		final CmdLineParser parser = new CmdLineParser( options );
		try
		{
			parser.parseArgument( args );
		}
		catch( final CmdLineException e )
		{
			System.out.println( e.getMessage() );
			System.out.println();
			System.out.println( "AudioClassifierSVM [options]");
			parser.printUsage( System.out );
			System.exit( 1 );
		}

		return options;
	}

	/**
	 * 	Main method.
	 *	@param args Command-line args
	 * 	@throws IOException If the classifier cannot load or save a model
	 */
	public static void main( final String[] args ) throws IOException
	{
		final AudioClassifierOptions options = AudioClassifierSVM.parseOptions( args );

		try
		{
			// Create a new classifier
			final AudioClassifierSVM ac = new AudioClassifierSVM( options );

			// Check whether we need to load a model
			if( options.loadModel != null )
				ac.loadModel( options.loadModel );
			else
			{
				// Whether we need to train the model or not
				if( options.trainModelSource != null )
				{
					// Virtual file system for music speech corpus
					final GroupedDataset<String, ? extends ListDataset<List<SampleBuffer>>, List<SampleBuffer>>
						musicSpeechCorpus = new	VFSGroupDataset<List<SampleBuffer>>(
									options.trainModelSource.getAbsolutePath(),
									new OneSecondClipReader() );

					// Cross validate an SVM using the corpus.
					AudioClassifierSVM.crossValidation( musicSpeechCorpus,
							"speech", new String[]{"music"}, 4 );

					// Set the location to save the model to
					ac.setSaveModel( options.saveModel );

					// Train a classifier that we can use
					ac.train( musicSpeechCorpus, "speech", new String[]{"music"} );
				}
				else
				{
					new CmdLineParser( options ).printUsage( System.out );
					throw new IllegalArgumentException( "You must either load (-l) or train (-t) a classifier" );
				}
			}

			// Create an audio stream which we'll classify every second
			final XuggleAudio xa = new XuggleAudio( new File("CNET1.mp4") );

			// It needs to be mono and each sample chunk needs to be a second.
			// It also needs to be 22KHz, 16bit.
			final MultichannelToMonoProcessor mm = new MultichannelToMonoProcessor( xa );
			final BitDepthConverter bda = new BitDepthConverter( mm,
					BitDepthConversionAlgorithm.NEAREST,
					new AudioFormat( 16, 22.05, 1 ) );
			final FixedSizeSampleAudioProcessor fs = new FixedSizeSampleAudioProcessor( bda,
					(int)(xa.getFormat().getSampleRateKHz() * 1000) );

			while( fs.nextSampleChunk() != null )
				System.out.println( "Sample..." );

			// Create an audio player so we can hear what's going on
			// while the classification is taking place
			final AudioPlayer ap = new AudioPlayer( fs );
			ap.addAudioEventListener( new AudioEventListener()
			{
				@Override
				public void beforePlay( final SampleChunk sc )
				{
				}

				@Override
				public void audioEnded()
				{
				}

				@Override
				public void afterPlay( final AudioPlayer ap, final SampleChunk sc )
				{
					final ClassificationResult<String> cr = ac.classify( sc.getSampleBuffer() );
					System.out.println( cr );
				}
			} );
			ap.run();

		}
		catch( final FileSystemException e )
		{
			e.printStackTrace();
		}
	}
}
