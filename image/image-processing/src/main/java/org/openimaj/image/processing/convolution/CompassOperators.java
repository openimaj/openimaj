/**
 * 
 */
package org.openimaj.image.processing.convolution;

import org.openimaj.image.FImage;

/**
 *	Compass operators that are used by the Liu and Samarabandu
 *	text extraction.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 9 Aug 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class CompassOperators
{
	static public class Compass0 extends FConvolution
	{
		public Compass0()
		{
			super( new FImage( new float[][]{
					{-1,-1,-1},
					{ 2, 2, 2},
					{-1,-1,-1}
			}));
		}
	}

	static public class Compass45 extends FConvolution
	{
		public Compass45()
		{
			super( new FImage( new float[][]{
					{-1,-1, 2},
					{-1, 2,-1},
					{ 2,-1,-1}
			}));
		}
	}

	static public class Compass90 extends FConvolution
	{
		public Compass90()
		{
			super( new FImage( new float[][]{
					{-1, 2,-1},
					{-1, 2,-1},
					{-1, 2,-1}
			}));
		}
	}

	static public class Compass135 extends FConvolution
	{
		public Compass135()
		{
			super( new FImage( new float[][]{
					{ 2,-1,-1},
					{-1, 2,-1},
					{-1,-1, 2}
			}));
		}
	}

}
