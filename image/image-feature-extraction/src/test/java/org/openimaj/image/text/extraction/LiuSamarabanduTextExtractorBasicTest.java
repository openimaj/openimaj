package org.openimaj.image.text.extraction;

/**
 * 
 */


import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;

/**
 *	
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 28 Jul 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class LiuSamarabanduTextExtractorBasicTest
{
	@Test
	public void testExtractRegions()
	{
		try
		{
			FImage testImage = ImageUtilities.readF( 
					getClass().getResource("lab-sign.jpg") ).normalise();
			
			LiuSamarabanduTextExtractorBasic te = 
				new LiuSamarabanduTextExtractorBasic();
			// te.setOCRProcessor( new Tess4JOCRProcessor() );
			te.processImage( testImage, (Image<?,?>)null );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
