package org.openimaj.util.auth.web;

import org.openimaj.util.api.auth.Parameter;
import org.openimaj.util.api.auth.Token;

@Token(name = "Tumblr API", url = "...")
public class TumblrAPIToken {
	/**
	 * The api key
	 */
	@Parameter(name = "API Key")
	public String apikey;
}
