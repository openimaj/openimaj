/**
 * 
 */
package org.openimaj.image.processing.algorithm;


import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
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
	
	@Test
	public void testHoughLines()
	{
		try
        {
			HoughLines hl = new HoughLines();
	        FImage i = ImageUtilities.readF( HoughLinesTest.class.getResource( "/hough.jpg" ) );
	        i.process( hl );
	        
	        MBFImage m = new MBFImage( i.getWidth(), i.getHeight(), 3 );
	        m.drawImage( i, 0, 0 );
	        
	        List<Line2d> lines = hl.getBestLines( 2 );
	        Assert.assertEquals( 2, lines.size() );
	        
	        for( int j = 0; j < lines.size(); j++ )
	        {
	        	Line2d l = lines.get(j);
	        	
	        	Assert.assertEquals( -2000, l.begin.getX(), 1d );
	        	Assert.assertEquals( 2000, l.end.getX(), 1d );
	        	
	        	l = l.lineWithinSquare( new Rectangle( 0, 0, m.getWidth(), m.getHeight() ) );
	        	m.drawLine( l, 2, new Float[]{1f,0f,0f} );
	        	System.out.println( l );
	        	
	        	Assert.assertEquals( 0d, l.begin.getX(), 5d );
	        }

	        DisplayUtilities.display( m );	        

        }
        catch( IOException e )
        {
	        e.printStackTrace();
        }
        
        forceWait();
	}
}
