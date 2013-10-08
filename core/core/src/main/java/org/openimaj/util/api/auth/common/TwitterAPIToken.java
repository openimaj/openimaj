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
