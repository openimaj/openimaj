package org.openimaj.audio.analysis.benchmarking.dataset;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.annotations.DatasetDescription;

import cern.colt.Arrays;

/**
 *	OpenIMAJ Dataset for the MusicSpeech Database
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Mar 2013
 */
@DatasetDescription(
		name = "Music-Speech Dataset",
		description = "The 'music-speech' corpus is a small collection of some 240 " +
				"15-second extracts collected 'at random' from the radio by Eric Scheirer " +
				"during his internship at Interval Research Corporation in the summer of 1996 " +
				"under the supervision of Malcolm Slaney",
		url = "http://labrosa.ee.columbia.edu/sounds/musp/scheislan.html")
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Scheirer E.", "Slaney, M." },
		title = "Construction And Evaluation Of A Robust Multifeature Speech/music Discriminator",
		year = "1997",
		booktitle = "Proc. ICASSP-97, Munich.")
public class MusicSpeechDataset extends MapBackedDataset<String, Dataset<File>, File>
{
	/** Directory of the sounds */
	private File soundsDir;

	/**
	 * 	Create the Music-Speech Dataset from the given directory.
	 *
	 *	@param baseDir The base directory of the music-speech dataset.
	 * 	@param testOrTrain TRUE for testing set, FALSE for training set
	 */
	public MusicSpeechDataset( final File baseDir, final boolean testOrTrain )
	{
		// construct the directory of which set to get
		String soundsDir = "wavfile" + File.separator;
		soundsDir += testOrTrain? "test" : "train";

		this.processDir( this.soundsDir = new File( baseDir, soundsDir ) );

		System.out.println( this );
	}

	private void processDir( final File dir )
	{
		final File[] groups = dir.listFiles( new FileFilter()
		{
			@Override
			public boolean accept( final File pathname )
			{
				return 	pathname.isDirectory() &&
						!pathname.getName().equals( "." ) &&
						!pathname.getName().equals("..");
			}
		} );

		System.out.println( Arrays.toString( groups ));

		if( groups.length == 0 )
		{
			System.out.println( "Processing "+dir );

			final File[] files = dir.listFiles( new FilenameFilter()
			{
				@Override
				public boolean accept( final File dir, final String name )
				{
					return name.endsWith( ".wav" );
				}
			} );

			final ListBackedDataset<File> list = new ListBackedDataset<File>();
			this.map.put( dir.getAbsolutePath().substring( this.soundsDir.getAbsolutePath().length()+1 ), list );

			for( final File file : files )
			{
				list.add( file );
			}
		}
		else
		{
			for( final File group: groups )
				this.processDir( group );
		}
	}
}
