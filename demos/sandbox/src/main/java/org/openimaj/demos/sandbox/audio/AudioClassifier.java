/**
 *
 */
package org.openimaj.demos.sandbox.audio;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.features.MFCCJAudio;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.io.ObjectReader;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 7 May 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioClassifier
{
	/** The location of the music speech corpus */
	private static final String MUSIC_SPEECH_CORPUS_LOCATION = "/data/music-speech-corpus/music-speech/";

	/** The location of the music_speech_corpus training data */
	private static final String MUSIC_SPEECH_CORPUS_TRAINING_LOCATION =
			AudioClassifier.MUSIC_SPEECH_CORPUS_LOCATION + "wavfile/train";

	/** The filename for saving the SVM model */
	private static final String SVM_MODEL_FILE_NAME = "svm.model.dat";

	/**
	 *	Returns a set of one second of samples from the input stream.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 7 May 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class OneSecondClipReader implements ObjectReader<List<SampleBuffer>>
	{

		/**
		 *	{@inheritDoc}
		 * 	@see org.openimaj.io.ObjectReader#read(java.io.InputStream)
		 */
		@Override
		public List<SampleBuffer> read( final InputStream stream ) throws IOException
		{
			// Open the stream.
			final XuggleAudio xa = new XuggleAudio( stream );

			// Setup a chunker that will get samples in one second chunks.
			final int nSamplesInOneSecond = (int)(xa.getFormat().getSampleRateKHz() * 1000);
			final FixedSizeSampleAudioProcessor f = new FixedSizeSampleAudioProcessor( xa, nSamplesInOneSecond );

			// Setup our output list
			final List<SampleBuffer> buffers = new ArrayList<SampleBuffer>();

			// Now read the audio until we're done
			SampleChunk sc = null;
			while( (sc = xa.nextSampleChunk()) != null )
				buffers.add( sc.getSampleBuffer() );

			return buffers;
		}

		/**
		 *	{@inheritDoc}
		 * 	@see org.openimaj.io.ObjectReader#canRead(java.io.InputStream, java.lang.String)
		 */
		@Override
		public boolean canRead( final InputStream stream, final String name )
		{
			return true;
		}
	}

	/** The MFCC processor */
	private final MFCCJAudio mfcc = new MFCCJAudio();

	/**
	 *
	 *	@param location
	 */
	public AudioClassifier()
	{
	}

	/**
	 * 	Train the classifier on the given corpus
	 *	@param musicSpeechCorpus The corpus to train on
	 */
	public void train( final GroupedDataset<String, ? extends ListDataset<List<SampleBuffer>>,
			List<SampleBuffer>> musicSpeechCorpus )
	{
		System.out.println( musicSpeechCorpus.getGroups() );

		// Setup the SVM problem
		final svm_problem prob = this.getSVMProblem( musicSpeechCorpus );
		final svm_parameter param = this.getSVMParameters();

		// Train the SVM
		final svm_model model = libsvm.svm.svm_train( prob, param );

		// Save the model if we're going to do that.
		if( AudioClassifier.SVM_MODEL_FILE_NAME != null ) try
		{
			svm.svm_save_model( AudioClassifier.SVM_MODEL_FILE_NAME, model );
		}
		catch( final IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * 	Returns the calculated feature vector for the given sample buffer.
	 *	@param buffer The sample buffer to process
	 *	@return A {@link DoubleFV} containing the feature vector for this buffer
	 */
	public DoubleFV getFeatures( final SampleBuffer buffer )
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

	/**
	 * 	Returns the default set of SVM parameters.
	 *	@return The default set of SVM parameters
	 */
	private svm_parameter getSVMParameters()
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
	 * 	Returns an svm_problem for the given dataset.
	 * 	@param musicSpeechCorpus The corpus
	 *	@return
	 */
	private svm_problem getSVMProblem( final GroupedDataset<String,
			? extends ListDataset<List<SampleBuffer>>, List<SampleBuffer>> musicSpeechCorpus )
	{
		final svm_problem prob = new svm_problem();

		for( final List<SampleBuffer> samples : musicSpeechCorpus.getInstances( "speech" ) )
		{
			for( final SampleBuffer sample : samples )
			{
				final DoubleFV fv = this.getFeatures( sample );
			}
		}

		return prob;
	}

	/**
	 * 	Main method.
	 *	@param args Command-line args
	 */
	public static void main( final String[] args )
	{
		try
		{
			// Virtual file system for music speech corpus
			final GroupedDataset<String, ? extends ListDataset<List<SampleBuffer>>, List<SampleBuffer>>
				musicSpeechCorpus = new	VFSGroupDataset<List<SampleBuffer>>(
							AudioClassifier.MUSIC_SPEECH_CORPUS_TRAINING_LOCATION,
							new OneSecondClipReader() );

			final AudioClassifier ac = new AudioClassifier();
			ac.train( musicSpeechCorpus );
		}
		catch( final FileSystemException e )
		{
			e.printStackTrace();
		}
	}
}
