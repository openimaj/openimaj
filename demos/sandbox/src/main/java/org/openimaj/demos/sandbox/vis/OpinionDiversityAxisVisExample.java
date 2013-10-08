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
package org.openimaj.demos.sandbox.vis;


import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.vis.general.DiversityAxis;
import org.openimaj.vis.general.ImageThumbnailPlotter;

/**
 *	A mock-up of an opinion visualisation using the diveristy axis visualisation.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 6 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class OpinionDiversityAxisVisExample
{
	/**
	 *	@param args
	 * 	@throws FileSystemException
	 */
	public static void main( final String[] args ) throws FileSystemException
	{
		// Use some images from a directory on the disk
		final VFSGroupDataset<MBFImage> i = new VFSGroupDataset<MBFImage>(
				"/data/faces/lfw", ImageUtilities.MBFIMAGE_READER );

		// Set up the diversity axis to plot thumbnail images.
		final ImageThumbnailPlotter itp = new ImageThumbnailPlotter();
		final DiversityAxis<MBFImage> dv = new DiversityAxis<MBFImage>( 1600, 800, itp )
		{
			/** */
			private static final long serialVersionUID = 1L;

			@Override
			public void bandSizeKnown( final int bandSize )
			{
				itp.setThumbnailSize( bandSize );
			}
		};
		dv.setDiversityAxisName( "Opinion" );

		int maxGroups = 10;
		int groupCount = 1;
		for( final String group: i.getGroups() )
		{
			for( final MBFImage img : i.get( group ) )
			{
				// Pretend we've found x number of duplicates for this image
				final int x = (int)(Math.random() * 3)+1;
				for( int j = 0; j < x; j++ )
				{
					// Mock-up an opinion value for each image
					final double opinionValue = (Math.random() - 0.5) * 2;

					// Add it to the diversity axis
					dv.addObject( groupCount, opinionValue, img );
				}

				groupCount++;

				// Let's not show too many groups
				maxGroups--;
				if( maxGroups == 0 ) break;
			}

			if( maxGroups == 0 ) break;
		}

		// Show the vis.
		dv.repaint();
		dv.showWindow( "Opinion VisualisationImpl" );
	}
}
