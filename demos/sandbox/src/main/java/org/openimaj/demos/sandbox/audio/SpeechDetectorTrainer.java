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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.audio.AudioAnnotator.AudioAnnotatorType;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.conversion.MultichannelToMonoProcessor;
import org.openimaj.audio.features.MFCC;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.training.IncrementalTrainer;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	A trainer for the speech detector. Uses MFCCs to train a KNN classifier.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 6 Dec 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class SpeechDetectorTrainer
{
	/**
	 * 	Feature extractor for extracting MFCC features from a sample chunk.
	 * 	It assumes the sample chunk is mono, and will only return the MFCC
	 * 	features from the first channel.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 6 Mar 2013
	 */
	protected static class MFCCFeatureExtractor
		implements FeatureExtractor<DoubleFV,SampleChunk>
	{
		private final MFCC mfcc = new MFCC();

		@Override
		public DoubleFV extractFeature( final SampleChunk object )
		{
			final double[] d = this.mfcc.calculateMFCC( object.getSampleBuffer() )[0];
			return new DoubleFV(d);
		}
	}

	/**
	 * 	Options for the tool
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 6 Dec 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	protected static class Options
	{
		/** Directory containing input files */
		@Argument(required=true,usage="Directory containing positive examples",metaVar="INPUT-DIR")
		public File speechDir;

		/** Output filename for the classifier */
		@Argument(required=true,usage="Classifier output file",index=1,metaVar="CLASSIFIER-OUTPUT-FILE")
		public File outputFile;

		/** Directory containing negative input files */
		@Option(name="--negative",aliases="-n",usage="Directory containing negative examples")
		public File nonSpeechDir;

		/** The trainer to use */
		@Option(name="--annotator",aliases="-a",usage="Classifier type (default:KNN)")
		public AudioAnnotatorType trainer = AudioAnnotatorType.KNN;

		/** The filename filter to use */
		@Option(name="--filter",aliases="-f",usage="Filename filter (default:*.wav,*.mp3)")
		public List<String> filePattern = new ArrayList<String>(
				Arrays.asList( new String[]{"*.wav","*.mp3"}) );

		/** Whether to recurse subdirectories */
		@Option(name="--recurse",aliases="-R",usage="Recurse subdirectories" )
		public boolean recurseSubdirectories = false;

		@Option(name="--limit",aliases="-l",usage="Limit number of example files")
		public int limitNumber = -1;
	}

	/** The options being used in this instance of the trainer */
	private final Options options;

	/** The number of files processed so far by this class */
	// Note this means this class should only be used to process
	// one directory at a time (don't call process from multiple Threads)
	private int numProcessed;

	/** MFCC Calculator */
	private final MFCC mfcc = new MFCC();

	/**
	 * 	Constructor for the trainer tool
	 *	@param options the options to use.
	 * @throws FileNotFoundException
	 */
	public SpeechDetectorTrainer( final Options options )
			throws FileNotFoundException
	{
		this.options = options;

		// Create the trainer
		final IncrementalTrainer<Annotated<DoubleFV, String>> t =
				options.trainer.getAnnotator();

		// Add the speech data to the space
		this.processDirectory( options.speechDir, true, t, options.limitNumber );

		// Add the non-speech stuff to the space
		if( options.nonSpeechDir != null )
			this.processDirectory( options.nonSpeechDir, false, t, options.limitNumber );

		// Write the classifier to a file
		try
		{
			IOUtils.write( t, new DataOutputStream( new FileOutputStream( options.outputFile ) ) );
		}
		catch( final IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * 	Processes the input directory as a set of files whose speech content
	 * 	is given by the boolean parameter
	 *	@param inputDir The input directory
	 *	@param speechFiles Whether the files contain speech
	 * 	@param t The trainer to use
	 * 	@param limit The number of files to limit to
	 * 	@throws FileNotFoundException if the input directory cannot be found
	 */
	private void processDirectory( final File inputDir, final boolean speechFiles,
			final IncrementalTrainer<Annotated<DoubleFV, String>> t, final int limit )
			throws FileNotFoundException
	{
		if( !inputDir.exists() )
			throw new FileNotFoundException( inputDir+" does not exist." );

		System.out.println( "Entering directory "+inputDir );

		// Instantiate our filename filter
		final FileFilter fileFilter = new WildcardFileFilter(
				this.options.filePattern );

		// Go through all the files in the directory
		int chunkCount = 0;
		final File[] files = inputDir.listFiles( fileFilter );
		this.numProcessed = 0;
		for( final File file: files )
		{
			if( limit > 0 && this.numProcessed >= limit )
				break;

			if( file.isDirectory() )
			{
				if( this.options.recurseSubdirectories )
					this.processDirectory( file, speechFiles, t, limit );

				continue;
			}

			System.out.println( "Processing "+file );

			try
			{
				int fileChunkCount = 0;

				// Create an audio object for the input file
				final XuggleAudio xa = new XuggleAudio( file );
				final MultichannelToMonoProcessor mtm =
						new MultichannelToMonoProcessor( xa );

				// Loop through all the chunks in the audio file
				SampleChunk sc = null;
				while( (sc = mtm.nextSampleChunk()) != null )
				{
					// Calculate the MFCC for this frame.
					final double[][] calculatedMFCC =
							this.mfcc.calculateMFCC( sc.getSampleBuffer() );

					// Create an annotated object that says that this sample chunk
					// either does or doesn't represent speech
					final AnnotatedObject<DoubleFV,String> o =
							new AnnotatedObject<DoubleFV, String>(
									new DoubleFV(calculatedMFCC[0]),
									speechFiles?"Speech":"Non-Speech" );

					// Now train on that data. Training will involve extracting
					// a feature and inserting it into some feature space.
					t.train( Collections.singleton( o ) );

					// Show a little counter thingy
					if( fileChunkCount % 1000 == 0 )
						System.out.print( fileChunkCount+"..." );

					// count how many items we've trained on
					fileChunkCount++;
				}

				chunkCount += fileChunkCount;
				this.numProcessed++;
			}
			catch( final Exception e )
			{
				e.printStackTrace();
			}
			finally
			{
			}
		}

		System.out.println( "Trained on "+chunkCount+" sample frames.");
	}

	/**
	 * 	Returns the trainer used for this trainer
	 *	@return The trainer
	 */
	public AudioAnnotatorType getTrainer()
	{
		return this.options.trainer;
	}



	// ======================================================================
	/**
	 * 	Parses the command line arguments to create an options object.
	 *	@param args
	 */
	private static Options parseArgs( final String args[] )
	{
		final Options o = new Options();
		final CmdLineParser p = new CmdLineParser( o );
		try
		{
			p.parseArgument( args );
		}
		catch( final CmdLineException e )
		{
	        System.err.println( e.getMessage() );
	        System.err.println( "java SpeechDetectorTrainer INPUT-DIR CLASSIFIER-OUTPUT-FILE");
	        p.printUsage( System.err );
	        System.exit(1);
		}

		return o;
	}

	/**
	 *
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		try
		{
			final Options options = SpeechDetectorTrainer.parseArgs( args );
			new SpeechDetectorTrainer( options );
		}
		catch( final FileNotFoundException e )
		{
			e.printStackTrace();
		}
	}
}
