/**
 * 
 */
package org.openimaj.image;

import junit.framework.Assert;

import org.junit.Test;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 15 Aug 2011
 */
public class ImageTest
{
	private static final boolean DISPLAY = false;

	/**
	 * 	Helper method for debugging when viewing images
	 */
	protected void forceWait()
	{
		synchronized(this){ try	{ wait( 200000 ); } catch( InterruptedException e1 ) {} }
	}
	
	@Test
	public void testShiftLeft()
	{
		FImage img = new FImage( 100, 100 );
		img.createRenderer().drawLine( 0, 0, 100, 100, 1f );
		
		if( DISPLAY )
			DisplayUtilities.display( img, "Original" );
		
		for( int i = 0; i < 10; i++ )
			img = img.shiftLeft();
		
		if( DISPLAY )
			DisplayUtilities.display( img, "Shifted Left 10 times" );
				
		// If we shift left 10 times, the pixel from (99,99) moves to (89,99)
		Assert.assertEquals( 1f, img.getPixel( 89, 99 ) );
		
		// Try it in one go
		img = img.shiftLeft( 10 );

		if( DISPLAY )
			DisplayUtilities.display( img, "Shifted Left 10 times, then 20 pixels" );
				
		// We've effectively shifted left 20 times, the pixel (99,99) moves to (79,99)
		Assert.assertEquals( 1f, img.getPixel( 79, 99 ) );

		// if( DISPLAY ) forceWait();	
	}

	@Test
	public void testShiftRight()
	{
		FImage img = new FImage( 100, 100 );
		img.createRenderer().drawLine( 0, 0, 100, 100, 1f );
		
		if( DISPLAY )
			DisplayUtilities.display( img, "Original" );
		
		for( int i = 0; i < 10; i++ )
			img = img.shiftRight();
		
		if( DISPLAY )
			DisplayUtilities.display( img, "Shifted Right 10 times" );
				
		// If we shift right 10 times, the pixel (99,99) moves to (9,99)
		Assert.assertEquals( 1f, img.getPixel( 9, 99 ) );
		
		// Try it in one go
		img = img.shiftRight( 10 );

		if( DISPLAY )
			DisplayUtilities.display( img, "Shifted Right 10 times, then 20 pixels" );
				
		// We've effectively shifted left 20 times, the pixel (99,99) moves to (19,99)
		Assert.assertEquals( 1f, img.getPixel( 19, 99 ) );

		// if( DISPLAY ) forceWait();
	}
}
