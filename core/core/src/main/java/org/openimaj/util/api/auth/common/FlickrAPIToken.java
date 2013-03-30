package org.openimaj.util.api.auth.common;

import org.openimaj.util.api.auth.Parameter;
import org.openimaj.util.api.auth.Token;

/**
 * An authentication token for the Flickr API.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Token(name = "Flickr API", url = "http://www.flickr.com/services/api/keys/apply/")
public class FlickrAPIToken {
	/**
	 * The API key
	 */
	@Parameter(name = "Key")
	public String apikey;

	/**
	 * The API Secret
	 */
	@Parameter(name = "Secret")
	public String secret;

	/**
	 * Construct an empty token.
	 */
	public FlickrAPIToken() {

	}

	/**
	 * Construct a token with the given parameters.
	 * 
	 * @param apikey
	 *            the key
	 * @param secret
	 *            the secret
	 */
	public FlickrAPIToken(String apikey, String secret) {
		this.apikey = apikey;
		this.secret = secret;
	}
}
