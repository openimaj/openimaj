package org.openimaj.util.api.auth.common;

import org.openimaj.util.api.auth.Parameter;
import org.openimaj.util.api.auth.Token;

/**
 * An authentication token for the Bing Search API.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Token(name = "Bing Search API", url = "http://datamarket.azure.com/dataset/bing/search")
public class BingAPIToken {
	/**
	 * The account key
	 */
	@Parameter(name = "Account Key")
	public String accountKey;

	/**
	 * Construct an empty token.
	 */
	public BingAPIToken() {

	}

	/**
	 * Construct a token with the given account key.
	 * 
	 * @param accountKey
	 *            the account key
	 */
	public BingAPIToken(String accountKey) {
		this.accountKey = accountKey;
	}
}
