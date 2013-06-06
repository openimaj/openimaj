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
		final VFSGroupDataset<MBFImage> i = new VFSGroupDataset<MBFImage>( "/data/faces/lfw", ImageUtilities.MBFIMAGE_READER );

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
		dv.showWindow( "Opinion Visualisation" );
	}
}
