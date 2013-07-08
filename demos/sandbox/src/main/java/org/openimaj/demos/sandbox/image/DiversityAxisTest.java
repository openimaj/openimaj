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
