package org.openimaj.util.auth.web;

import org.openimaj.util.api.auth.Parameter;
import org.openimaj.util.api.auth.Token;

/**
 * API token for tumblr for methods using the api_key parameter
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Token(name = "Tumblr API", url = "http://www.tumblr.com/oauth/apps")
public class TumblrAPIToken {
	/**
	 * The api key
	 */
	@Parameter(name = "API Key")
	public String apikey;
}
