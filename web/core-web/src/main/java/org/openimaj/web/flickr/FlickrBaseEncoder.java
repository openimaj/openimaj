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