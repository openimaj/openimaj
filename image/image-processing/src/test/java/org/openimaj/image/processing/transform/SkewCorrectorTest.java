package org.openimaj.image.processing.transform;

/**
 * 
 */


import java.io.IOException;

import org.junit.Test;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.transform.SkewCorrector;

/**
 *	Test for {@link SkewCorrector} processor
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 28 Jul 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class SkewCorrectorTest
{
	/**
	 * 	Helper method for debugging when viewing images
	 */
	protected void forceWait()
	{
		synchronized(this){ try	{ wait( 200000 ); } catch( InterruptedException e1 ) {} }
	}
	

	@Test
	public void testExtractRegions()
	{
		try
		{
			// Read the image
			FImage testImage = ImageUtilities.readF( 
					getClass().getResource("italic--20.png") ).normalise();
//					getClass().getResource("test.jpg") ).normalise();
//					getClass().getResource("lab-sign.jpg") ).normalise();
					
			// Process the image
			SkewCorrector sc = new SkewCorrector();
			sc.processImage( testImage, (Image<?,?>)null );
			DisplayUtilities.display( testImage );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
		// forceWait();
	}
}
