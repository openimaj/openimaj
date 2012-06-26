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
package org.openimaj.image.analysis.algorithm;


import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 8 Aug 2011
 */
public class HoughLinesTest
{
	/**
	 * 	Helper method for debugging when viewing images
	 */
	protected void forceWait()
	{
		synchronized(this){ try	{ wait( 200000 ); } catch( InterruptedException e1 ) {} }
	}
	
	/**
	 * Test Hough line detection
	 */
	@Test
	public void testHoughLines()
	{
		try
        {
			HoughLines hl = new HoughLines();
			
	        FImage i = ImageUtilities.readF( 
	        		HoughLinesTest.class.getResource( "/hough.jpg" ) );
	        i.analyseWith( hl );
	        
	        MBFImage m = new MBFImage( i.getWidth(), i.getHeight(), 3 );
	        MBFImageRenderer renderer = m.createRenderer();
	        renderer.drawImage( i, 0, 0 );
	        
	        List<Line2d> lines = hl.getBestLines( 2 );
	        Assert.assertEquals( 2, lines.size() );
	        
	        for( int j = 0; j < lines.size(); j++ )
	        {
	        	Line2d l = lines.get(j);
	        	
	        	Assert.assertEquals( -2000, l.begin.getX(), 1d );
	        	Assert.assertEquals( 2000, l.end.getX(), 1d );
	        	
	        	l = l.lineWithinSquare( 
	        			new Rectangle( 0, 0, m.getWidth(), m.getHeight() ) );
	        	renderer.drawLine( l, 2, new Float[]{1f,0f,0f} );
	        	System.out.println( l );
	        	
	        	Assert.assertEquals( 0d, l.begin.getX(), 5d );
	        }

	        DisplayUtilities.display( m );	        

        }
        catch( IOException e )
        {
	        e.printStackTrace();
        }
        
        // forceWait();
	}
}
