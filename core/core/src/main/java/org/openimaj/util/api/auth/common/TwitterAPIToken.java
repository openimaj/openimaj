package org.openimaj.util.api.auth.common;

import org.openimaj.util.api.auth.Parameter;
import org.openimaj.util.api.auth.Token;

/**
 * An authentication token for the Twitter API.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Token(name = "Twitter API", url = "https://dev.twitter.com/apps/new")
public class TwitterAPIToken {
	/**
	 * The consumer key
	 */
	@Parameter(name = "Consumer Key")
	public String consumerKey;

	/**
	 * The consumer secret
	 */
	@Parameter(name = "Consumer Secret")
	public String consumerSecret;

	/**
	 * The access token
	 */
	@Parameter(name = "Access Token")
	public String accessToken;

	/**
	 * The access secret
	 */
	@Parameter(name = "Access Secret")
	public String accessSecret;

	/**
	 * Construct an empty token.
	 */
	public TwitterAPIToken() {

	}

	/**
	 * Construct a token with the given parameters.
	 * 
	 * @param consumerKey
	 *            the consumer key
	 * @param consumerSecret
	 *            the consumer secret
	 * @param accessToken
	 *            the access token
	 * @param accessSecret
	 *            the access secret
	 */
	public TwitterAPIToken(String consumerKey, String consumerSecret, String accessToken, String accessSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessSecret = accessSecret;
	}
}
