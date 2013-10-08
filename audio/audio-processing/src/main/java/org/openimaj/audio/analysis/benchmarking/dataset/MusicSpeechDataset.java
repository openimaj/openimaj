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
