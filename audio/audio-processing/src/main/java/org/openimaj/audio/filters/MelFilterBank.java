/**
 * 
 */
package org.openimaj.audio.filters;

import org.openimaj.audio.AudioFormat;

/**
 *	Filter bank of Mel filters for applying to a frequency domain source.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 25 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MelFilterBank
{
	/**
	 * 	Default constructor to create a filter bank with the given number
	 * 	of filters between the two given frequencies.
	 * 
	 *	@param nFilters The number of filters
	 *	@param lowFreq The lowest frequency covered by the bank
	 *	@param highFreq The highest frequency covered by the bank
	 */
	public MelFilterBank( int nFilters, double lowFreq, double highFreq )
	{
		
	}
	
	/**
	 * 
	 *	@param spectrum
	 *	@param format
	 *	@return
	 */
	public double[][] process( double[][] spectrum, AudioFormat format )
	{
		double[][] output = null;
		return output;
	}
}
