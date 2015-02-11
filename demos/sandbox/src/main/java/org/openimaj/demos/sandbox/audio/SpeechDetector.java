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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.audio.AudioAnnotator;
import org.openimaj.audio.AudioAnnotator.AudioAnnotatorType;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.conversion.MultichannelToMonoProcessor;
import org.openimaj.audio.features.MFCC;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Mar 2013
 */
public class SpeechDetector
{
	/**
	 * 	Options for the speech detector
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 6 Dec 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	protected static class SpeechDetectorOptions
	{
		/** Directory containing input files */
		@Argument(required=true,usage="Directory containing positive examples",metaVar="INPUT-DIR")
		public File speechDir;

		/** Output filename for the audioAnnotator */
		@Argument(required=true,usage="Classifier output file",index=1,metaVar="CLASSIFIER-OUTPUT-FILE")
		public File outputFile;

		// ---------------------------------------------------------------- //

		/** Directory containing negative input files */
		@Option(name="--negative",aliases="-n",usage="Directory containing negative examples")
		public File nonSpeechDir;

		/** The filename filter to use */
		@Option(name="--filter",aliases="-f",usage="Filename filter (default:*.wav,*.mp3)")
		public List<String> filePattern = new ArrayList<String>(
				Arrays.asList( new String[]{"*.wav","*.mp3"}) );

		/** Whether to recurse subdirectories */
		@Option(name="--recurse",aliases="-R",usage="Recurse subdirectories" )
		public boolean recurseSubdirectories = false;

		/** The maximum number of files to read from (or -1 for all) */
		@Option(name="--limit",aliases="-l",usage="Limit number of example files")
		public int limitNumber = -1;

		/** The annotator type : default KNN */
		@Option(name="--annotator",aliases="-a",usage="Annotator type (default: KNN)")
		public AudioAnnotatorType audioAnnotatorType = AudioAnnotatorType.KNN;
	}

	/** The options for this detector */
	private SpeechDetectorOptions options;

	/** The number of files processed so far by this class */
	// Note this means this class should only be used to process
	// one directory at a time (don't call process from multiple Threads)
	private int numProcessed;

	/** The classifier/annotator we're going to use */
	private AudioAnnotator audioAnnotator;

	/**
	 * 	Returns a new instance of the audio annotator
	 * 	that we're going to use to extract features.
	 *	@return The audio annotator
	 */
	public AudioAnnotator getNewAnnotator()
	{
		final MFCC a = new MFCC();
//		a.setAnnotator( this.options.audioAnnotatorType );
		return null;
	}

	/**
	 * 	Generates a dataset based on this speech detector's options that
	 * 	contains both positive and negative examples.
	 *
	 *	@return A {@link MapBackedDataset} containing both positive
	 *		and negative classes
	 */
	public MapBackedDataset<String,ListDataset<DoubleFV>,DoubleFV>
		generateDataset()
	{
		try
		{
			// Create a dataset from the input directories.. first the positive
			System.out.println( "----------- POSITIVE EXAMPLES ------------");
			MapBackedDataset<String, ListDataset<DoubleFV>, DoubleFV> ds =
					this.generateDataset( this.options.speechDir,
							true, this.options.limitNumber );

			// Update the dataset with the negative examples
			System.out.println( "----------- NEGATIVE EXAMPLES ------------");
			ds = this.generateDataset( this.options.nonSpeechDir,
					false, this.options.limitNumber, ds );

			return ds;
		}
		catch( final FileNotFoundException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 	Processes the input directory as a set of files whose speech content
	 * 	is given by the boolean parameter and creates a {@link GroupedDataset}.
	 *
	 *	@param inputDir The input directory
	 *	@param speechFiles Whether the files contain speech
	 * 	@param limit The number of files to limit to
	 * 	@throws FileNotFoundException if the input directory cannot be found
	 */
	private MapBackedDataset<String, ListDataset<DoubleFV>, DoubleFV>
		generateDataset( final File inputDir, final boolean speechFiles, final int limit )
			throws FileNotFoundException
	{
		// This will be the dataset we'll return
		final MapBackedDataset<String, ListDataset<DoubleFV>, DoubleFV> newDataset
			 = new MapBackedDataset<String, ListDataset<DoubleFV>, DoubleFV>();

		// This is the recursive call
		this.generateDataset( inputDir, speechFiles, limit, newDataset );

		return newDataset;
	}

	/**
	 * 	Processes the input directory as a set of files whose speech content
	 * 	is given by the boolean parameter and creates a {@link GroupedDataset}.
	 *
	 *	@param inputDir The input directory
	 *	@param speechFiles Whether the files contain speech
	 * 	@param limit The number of files to limit to
	 * 	@param dataset The dataset object to fill with data
	 * 	@throws FileNotFoundException if the input directory cannot be found
	 */
	private MapBackedDataset<String,ListDataset<DoubleFV>, DoubleFV>
		generateDataset( final File inputDir, final boolean speechFiles, final int limit,
				final MapBackedDataset<String,ListDataset<DoubleFV>,DoubleFV> dataset )
						throws FileNotFoundException
	{
		if( !inputDir.exists() )
			throw new FileNotFoundException( inputDir+" does not exist." );

		System.out.println( "Entering directory "+inputDir );

		// Instantiate our filename filter
		final FileFilter fileFilter = new WildcardFileFilter(
				this.options.filePattern );

		// The name of the group  TODO: maybe pass this in?
		final String groupName = (speechFiles? "Speech" : "Non-Speech" );

		// This will be the dataset for this group
		ListBackedDataset<DoubleFV> lbds = null;
		if( (lbds = (ListBackedDataset<DoubleFV>)dataset.getMap().get( groupName ) ) == null )
			dataset.getMap().put( groupName, lbds = new ListBackedDataset<DoubleFV>() );

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
					this.generateDataset( file, speechFiles, limit, dataset );

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
					final DoubleFV mfcc = this.audioAnnotator.extractFeature( sc );

					// We know there's only one channel (we're using a multichannel
					// to mono processor), so we can just take the first array element.
					if( mfcc != null )
							lbds.add( mfcc );
					else	System.out.println( "WARNING: Null MFCC at "+fileChunkCount );

					// Show a little counter thingy
					if( fileChunkCount % 1000 == 0 )
						System.out.print( fileChunkCount+"..." );

					// count how many items we've trained on
					fileChunkCount++;
				}

				System.out.println( fileChunkCount+". " );
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

		System.out.println( "Loaded "+chunkCount+" sample frames.");

		return dataset;
	}

	// ======================================================================
	/**
	 * 	Parses the command line arguments to create an options object.
	 *	@param args The arguments from the command-line
	 * 	@return The options that were parsed from the command-line
	 */
	public SpeechDetectorOptions parseArgs( final String args[] )
	{
		final SpeechDetectorOptions o = new SpeechDetectorOptions();
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

		this.options = o;
		this.audioAnnotator = this.getNewAnnotator();
		return o;
	}
}
