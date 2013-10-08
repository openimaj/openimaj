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
package org.openimaj.demos.sandbox.image;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.vis.general.DiversityAxis;
import org.openimaj.vis.general.ImageThumbnailPlotter;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jun 2013
 */
public class DiversityAxisTest
{
	/**
	 *	@param args
	 * @throws FileSystemException
	 */
	public static void main( final String[] args ) throws FileSystemException
	{
		final ImageThumbnailPlotter tp = new ImageThumbnailPlotter();
		final DiversityAxis<MBFImage> da = new DiversityAxis<MBFImage>(
				1000, 600, tp )
		{
			/** */
			private static final long serialVersionUID = 1L;

			@Override
			public void bandSizeKnown( final int bandSize )
			{
				tp.setThumbnailSize( bandSize );
			}
		};

		final VFSListDataset<MBFImage> imgs = new VFSListDataset<MBFImage>( "D:/yearbookyourself.com",
				ImageUtilities.MBFIMAGE_READER );

		System.out.println( "Number of imags: "+imgs.size() );

		int count = 0;
		int band = 0;
		for( final MBFImage img : imgs )
		{
			System.out.println( count );

			if( (count % 4) == 0 ) band++;
			count++;

			da.addObject( band, (Math.random()-0.5)*2, img );
		}

		da.updateVis();
		da.showWindow( "Diversity" );
	}
}
