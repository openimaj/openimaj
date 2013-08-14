package org.openimaj.web.flickr;

/**
 * For Flickr's URL shortening
 * 
 * @see "http://dl.dropboxusercontent.com/u/1844215/FlickrBaseEncoder.java"
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 12 Aug 2013
 * @version $Author$, $Revision$, $Date$
 */
public class FlickrBaseEncoder
{
	protected static String alphabetString = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";

	protected static char[] alphabet = alphabetString.toCharArray();

	protected static int base_count = alphabet.length;

	/**
	 * @param num The photo ID number
	 * @return The short URL
	 */
	public static String encode( long num )
	{
		String result = "";
		long div;
		int mod = 0;

		while( num >= base_count )
		{
			div = num / base_count;
			mod = (int) (num - (base_count * (long) div));
			result = alphabet[mod] + result;
			num = (long) div;
		}
		if( num > 0 )
		{
			result = alphabet[(int) num] + result;
		}
		return result;
	}

	/**
	 *	@param link The short URL
	 *	@return The photo ID
	 */
	public static long decode( String link )
	{
		long result = 0;
		long multi = 1;
		while( link.length() > 0 )
		{
			String digit = link.substring( link.length() - 1 );
			result = result + multi * alphabetString.lastIndexOf( digit );
			multi = multi * base_count;
			link = link.substring( 0, link.length() - 1 );
		}
		return result;
	}
}