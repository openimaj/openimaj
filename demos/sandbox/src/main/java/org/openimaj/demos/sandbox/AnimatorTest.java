/**
 * 
 */
package org.openimaj.demos.sandbox;

import org.openimaj.content.animation.animator.LinearTimeBasedFloatValueAnimator;


/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 14 Aug 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class AnimatorTest
{
	/**
	 *	@param args
	 *	@throws InterruptedException
	 */
	public static void main( final String[] args ) throws InterruptedException
	{
		final LinearTimeBasedFloatValueAnimator f = new 
				LinearTimeBasedFloatValueAnimator( 0, 400, 2000 );
		
		for( int i = 0; i < 15; i++ )
		{
			System.out.println( f.nextValue() );
			Thread.sleep( 200 );
		}
	}
}
