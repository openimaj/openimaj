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
package org.openimaj.image;

import junit.framework.Assert;

import org.junit.Test;

/**
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
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
	
	/**
	 * Test shifting
	 */
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

	/**
	 * Test shifting 
	 */
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
