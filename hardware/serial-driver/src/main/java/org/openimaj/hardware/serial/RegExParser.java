/**
 * 
 */
package org.openimaj.hardware.serial;

import java.util.Arrays;


/**
 * 	Takes incoming data from a serial parser and splits it into multiple
 * 	strings based on a regular expression.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 12 Jul 2011
 */
public class RegExParser implements SerialDataParser
{
	private String regex = null;
	
	private String leftOvers = null;
	
	/**
	 * 	Construct a new regex parser
	 *  @param regex
	 */
	public RegExParser( String regex )
    {
		this.regex = regex;
    }
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.hardware.serial.SerialDataParser#parse(java.lang.String)
	 */
	@Override
	public String[] parse( String data )
	{
		leftOvers = null;
		
		String[] bits = data.split( regex );
		
		// If the last data item doesn't match our regular expression
		// then it must've been left over bits
		if( !bits[bits.length-1].matches( regex ) )
		{
			leftOvers = bits[bits.length-1];
			return Arrays.copyOfRange( bits, 0, bits.length-1 );
		}
		
		return bits;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.hardware.serial.SerialDataParser#getLeftOverString()
	 */
	@Override
	public String getLeftOverString()
	{
		return leftOvers;
	}

	/**
	 * 	Set the regular expression to use.
	 *  @param regex The regular expression to use
	 */
	public void setRegEx( String regex )
    {
		this.regex = regex;
    }

	
}
