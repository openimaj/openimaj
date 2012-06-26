/**
 * 
 */
package org.openimaj.hardware.serial;


/**
 * 	An interface for objects that can parse serial data.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 12 Jul 2011
 */
public interface SerialDataParser
{
	/**
	 * 	Called to parse the raw data into tokens or sentences.
	 *  
	 *  @param data The raw data.
	 *  @return A list of tokens/sentences
	 */
	public String[] parse( String data );
	
	/**
	 * 	This function must return the data that was left-over
	 * 	from the previous parse, if at all. May return null.
	 * 
	 *  @return The left-over parts of the string after parsing.
	 */
	public String getLeftOverString();
}
