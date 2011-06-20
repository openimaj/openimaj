/**
 * 
 */
package org.openimaj.classifier.citylandscape;


import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

/**
 *	Test for the city/landscape detector. 
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 17 Jun 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class CityLandscapeTreeTest
{
	/** The class we're testing */
	private CityLandscapeTree clt = null;
	
	private String CITY_STRING = "City";
	private String LANDSCAPE_STRING = "Landscape";
	
	/**
	 *	@throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		String trainingSet  = "/CityLS10000.2.no-decimal";
		int trainingSetSize = 10000; 
		
		clt = new CityLandscapeTree( CITY_STRING, LANDSCAPE_STRING,
			CityLandscapeUtilities.class.getResourceAsStream( trainingSet ), 
			trainingSetSize );
	}

	@Test
	public void testCityDetection()
	{
		try
		{
			FImage cityImage = ImageUtilities.readF( 
					new File( "src/test/resources/city.jpg" ) );
			String classification = clt.classifyImage( cityImage, 1 );
			System.out.println( classification );
			Assert.assertEquals( CITY_STRING, classification );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	@Test
	public void testLandscapeDetection()
	{
		try
		{
			FImage cityImage = ImageUtilities.readF( 
					new File( "src/test/resources/landscape.jpg" ) );
			String classification = clt.classifyImage( cityImage, 1 );
			System.out.println( classification );
			Assert.assertEquals( LANDSCAPE_STRING, classification );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}		
	}
}
