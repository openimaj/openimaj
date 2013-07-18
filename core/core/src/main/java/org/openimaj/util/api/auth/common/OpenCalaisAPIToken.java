/**
 *
 */
package org.openimaj.util.api.auth.common;

import org.openimaj.util.api.auth.Parameter;
import org.openimaj.util.api.auth.Token;

/**
 * An authentication token for the Bing Search API.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 18 Jul 2013
 */
@Token(name = "OpenCalais REST API", url = "http://www.opencalais.com/APIkey")
public class OpenCalaisAPIToken
{
	/**
	 * 	The OpenCalais API key
	 */
	@Parameter(name = "API Key")
	public String apiKey;

	/**
	 * 	Default constructor
	 */
	public OpenCalaisAPIToken()
	{
	}

	/**
	 * 	Create a token with the given key
	 *	@param key The API key.
	 */
	public OpenCalaisAPIToken( final String key )
	{
		this.apiKey = key;
	}
}
